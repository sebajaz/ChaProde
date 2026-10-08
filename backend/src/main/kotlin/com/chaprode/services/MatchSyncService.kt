package com.chaprode.services

import com.chaprode.dto.MatchResultResponse
import com.chaprode.dto.SimulateMatchRequest
import com.chaprode.dto.SyncSummaryDto
import com.chaprode.providers.ExternalMatchScore
import com.chaprode.providers.FootballDataApiClient
import com.chaprode.providers.FootballDataSimulator
import com.chaprode.providers.SportsDataProvider
import com.chaprode.repositories.MatchRepository
import org.slf4j.LoggerFactory
import java.util.*

class MatchSyncService(
    private val matchRepository: MatchRepository = MatchRepository(),
    private val apiClient: SportsDataProvider = FootballDataApiClient(),
    val simulator: FootballDataSimulator = FootballDataSimulator()
) {

    private val log = LoggerFactory.getLogger(MatchSyncService::class.java)

    suspend fun syncMatches(
        torneoId: UUID? = null,
        forceSimulator: Boolean = false
    ): SyncSummaryDto {
        val pendingOrLiveMatches = matchRepository.getPendingOrLiveMatches(torneoId)

        if (pendingOrLiveMatches.isEmpty()) {
            return SyncSummaryDto(
                partidosProcesados = 0,
                partidosFinalizados = 0,
                pronosticosLiquidados = 0,
                puntosOtorgados = 0,
                mensaje = "Todos los partidos ya se encuentran finalizados o no hay partidos programados.",
                detalles = emptyList()
            )
        }

        // Obtener resultados externos (API o simulador)
        val externalScores: List<ExternalMatchScore> = if (forceSimulator) {
            log.info("Sincronizando en MODO SIMULACIÓN...")
            simulator.fetchMatchResults()
        } else {
            val apiResults = apiClient.fetchMatchResults()
            if (apiResults.isEmpty()) {
                log.info("API externa no devolvió resultados (sin token o sin partidos activos). Usando simulador de respaldo...")
                simulator.fetchMatchResults()
            } else {
                apiResults
            }
        }

        var procesados = 0
        var finalizados = 0
        var totalPronosticosLiquidados = 0
        var totalPuntosOtorgados = 0
        val detalles = mutableListOf<String>()

        for (match in pendingOrLiveMatches) {
            val matchUuid = UUID.fromString(match.id)

            // Buscar coincidencia por código externo del partido o por códigos de los equipos
            val externalMatch = externalScores.firstOrNull { ext ->
                (match.codigoExterno != null && ext.codigoExterno.equals(match.codigoExterno, ignoreCase = true)) ||
                (ext.localCodigo.equals(match.equipoLocal.codigoExterno, ignoreCase = true) &&
                 ext.visitanteCodigo.equals(match.equipoVisitante.codigoExterno, ignoreCase = true))
            }

            if (externalMatch != null) {
                procesados++
                val gLocal = externalMatch.golesLocal ?: 0
                val gVisitante = externalMatch.golesVisitante ?: 0

                when (externalMatch.estado.uppercase()) {
                    "FINALIZADO" -> {
                        val result = matchRepository.settleMatchResult(
                            partidoId = matchUuid,
                            golesLocal = gLocal,
                            golesVisitante = gVisitante,
                            estado = "FINALIZADO"
                        )
                        finalizados++
                        totalPronosticosLiquidados += result.totalPronosticosLiquidados
                        totalPuntosOtorgados += result.totalPuntosOtorgados
                        detalles.add(
                            "✅ ${match.equipoLocal.nombre} $gLocal - $gVisitante ${match.equipoVisitante.nombre}: FINALIZADO (${result.totalPronosticosLiquidados} pronósticos calculados, +${result.totalPuntosOtorgados} pts asignados)."
                        )
                        log.info("Partido {} liquidado con éxito por sincronización: {} vs {}", match.id, gLocal, gVisitante)
                    }
                    "EN_JUEGO" -> {
                        matchRepository.updateMatchLiveScore(
                            partidoId = matchUuid,
                            golesLocal = gLocal,
                            golesVisitante = gVisitante,
                            estado = "EN_JUEGO"
                        )
                        detalles.add(
                            "⏱️ ${match.equipoLocal.nombre} $gLocal - $gVisitante ${match.equipoVisitante.nombre}: EN JUEGO ($gLocal - $gVisitante parcial)."
                        )
                        log.info("Partido {} actualizado a EN_JUEGO: {} - {}", match.id, gLocal, gVisitante)
                    }
                }
            }
        }

        val resumenMensaje = if (finalizados > 0 || procesados > 0) {
            "Sincronización exitosa: $finalizados partidos finalizados, $totalPronosticosLiquidados pronósticos liquidados ($totalPuntosOtorgados puntos otorgados)."
        } else {
            "No se encontraron nuevos marcadores para actualizar."
        }

        return SyncSummaryDto(
            partidosProcesados = procesados,
            partidosFinalizados = finalizados,
            pronosticosLiquidados = totalPronosticosLiquidados,
            puntosOtorgados = totalPuntosOtorgados,
            mensaje = resumenMensaje,
            detalles = detalles
        )
    }

    suspend fun simulateSingleMatch(request: SimulateMatchRequest): MatchResultResponse {
        val matchUuid = when {
            !request.partidoId.isNullOrBlank() -> UUID.fromString(request.partidoId)
            !request.codigoExterno.isNullOrBlank() -> {
                val match = matchRepository.getMatchByExternalCode(request.codigoExterno)
                    ?: throw IllegalArgumentException("Partido con código externo '${request.codigoExterno}' no encontrado.")
                UUID.fromString(match.id)
            }
            else -> throw IllegalArgumentException("Se requiere 'partidoId' o 'codigoExterno' para simular el partido.")
        }

        val gLocal = request.golesLocal ?: 0
        val gVisitante = request.golesVisitante ?: 0

        return matchRepository.settleMatchResult(
            partidoId = matchUuid,
            golesLocal = gLocal,
            golesVisitante = gVisitante,
            estado = request.estado
        )
    }
}

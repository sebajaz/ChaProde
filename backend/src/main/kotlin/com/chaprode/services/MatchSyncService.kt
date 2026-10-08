package com.chaprode.services

import com.chaprode.dto.MatchResultResponse
import com.chaprode.dto.SimulateMatchRequest
import com.chaprode.dto.SyncSummaryDto
import com.chaprode.providers.ExternalMatchScore
import com.chaprode.providers.FootballDataApiClient
import com.chaprode.providers.FootballDataSimulator
import com.chaprode.providers.SportsDataProvider
import com.chaprode.repositories.MatchRepository
import com.chaprode.repositories.TournamentRepository
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import org.slf4j.LoggerFactory
import java.util.*
import kotlin.time.Duration.Companion.days

class MatchSyncService(
    private val matchRepository: MatchRepository = MatchRepository(),
    private val tournamentRepository: TournamentRepository = TournamentRepository(),
    private val apiClient: SportsDataProvider = FootballDataApiClient(),
    val simulator: FootballDataSimulator = FootballDataSimulator()
) {

    private val log = LoggerFactory.getLogger(MatchSyncService::class.java)

    suspend fun syncMatches(
        torneoId: UUID? = null,
        forceSimulator: Boolean = false,
        dateFrom: String? = null,
        dateTo: String? = null
    ): SyncSummaryDto {
        // 1. Obtener partidos mundiales (desde API de deportes en vivo o simulador)
        val externalScores: List<ExternalMatchScore> = if (forceSimulator) {
            log.info("Sincronizando partidos mundiales en MODO SIMULACIÓN...")
            simulator.fetchWorldwideMatches(dateFrom, dateTo)
        } else {
            val apiResults = apiClient.fetchWorldwideMatches(dateFrom, dateTo)
            if (apiResults.isEmpty()) {
                log.info("API externa no devolvió partidos (sin token configurado o fuera de rango). Usando simulador mundial de respaldo...")
                simulator.fetchWorldwideMatches(dateFrom, dateTo)
            } else {
                apiResults
            }
        }

        if (externalScores.isEmpty()) {
            return SyncSummaryDto(
                partidosProcesados = 0,
                partidosFinalizados = 0,
                pronosticosLiquidados = 0,
                puntosOtorgados = 0,
                mensaje = "No se encontraron partidos para sincronizar en el período consultado.",
                detalles = emptyList()
            )
        }

        var procesados = 0
        var importadosNuevos = 0
        var finalizados = 0
        var totalPronosticosLiquidados = 0
        var totalPuntosOtorgados = 0
        val detalles = mutableListOf<String>()

        // 2. Procesar e importar partidos externos a la base de datos
        for (ext in externalScores) {
            try {
                // A. Identificar o crear el Torneo correspondiente
                val compCode = ext.competitionCode ?: "MUNDO"
                val compName = ext.competitionName ?: "Competición Internacional"
                var torneo = tournamentRepository.findTournamentByCodigoOrNombre(compCode, compName)
                if (torneo == null) {
                    torneo = tournamentRepository.createTournament(
                        nombre = compName,
                        codigoExterno = compCode,
                        logoUrl = ext.competitionEmblem
                    )
                    log.info("Nuevo torneo creado automáticamente desde API: {} ({})", compName, compCode)
                }
                val currentTorneoUuid = UUID.fromString(torneo.id)

                // Si se solicitó filtrar por un torneo específico y no coincide, omitir
                if (torneoId != null && torneoId != currentTorneoUuid) {
                    continue
                }

                // B. Identificar o crear equipos (Local y Visitante)
                val localNombre = ext.localNombre ?: ext.localCodigo
                var localTeam = tournamentRepository.findTeamByCodigoOrNombre(ext.localCodigo, localNombre)
                if (localTeam == null) {
                    localTeam = tournamentRepository.createTeam(
                        nombre = localNombre,
                        codigoExterno = ext.localCodigo,
                        urlBandera = ext.localBandera,
                        codigoIso = ext.localCodigo.take(2)
                    )
                }
                val localTeamUuid = UUID.fromString(localTeam.id)
                tournamentRepository.addTeamToTournament(currentTorneoUuid, localTeamUuid)

                val visitNombre = ext.visitanteNombre ?: ext.visitanteCodigo
                var visitTeam = tournamentRepository.findTeamByCodigoOrNombre(ext.visitanteCodigo, visitNombre)
                if (visitTeam == null) {
                    visitTeam = tournamentRepository.createTeam(
                        nombre = visitNombre,
                        codigoExterno = ext.visitanteCodigo,
                        urlBandera = ext.visitanteBandera,
                        codigoIso = ext.visitanteCodigo.take(2)
                    )
                }
                val visitTeamUuid = UUID.fromString(visitTeam.id)
                tournamentRepository.addTeamToTournament(currentTorneoUuid, visitTeamUuid)

                // C. Buscar si el partido ya existe en la base de datos
                var existingMatch = if (!ext.codigoExterno.isNullOrBlank()) {
                    matchRepository.getMatchByExternalCode(ext.codigoExterno)
                } else null

                val gLocal = ext.golesLocal ?: 0
                val gVisitante = ext.golesVisitante ?: 0

                val matchUuid: UUID
                if (existingMatch == null) {
                    // Partido nuevo: crearlo en la base de datos
                    val fPartido = ext.fechaPartido?.let {
                        try { Instant.parse(it) } catch (e: Exception) { Clock.System.now().plus(2.days) }
                    } ?: Clock.System.now().plus(2.days)

                    matchUuid = matchRepository.createMatch(
                        torneoId = currentTorneoUuid,
                        equipoLocalId = localTeamUuid,
                        equipoVisitanteId = visitTeamUuid,
                        fechaPartido = fPartido,
                        codigoExterno = ext.codigoExterno
                    )
                    importadosNuevos++
                    procesados++
                    log.info("Nuevo partido importado desde API: {} vs {} en {}", localNombre, visitNombre, compName)
                } else {
                    matchUuid = UUID.fromString(existingMatch.id)
                    procesados++
                }

                // D. Actualizar marcador o liquidar pronósticos si finalizó
                when (ext.estado.uppercase()) {
                    "FINALIZADO" -> {
                        if (existingMatch?.estado != "FINALIZADO") {
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
                                "✅ [$compName] $localNombre $gLocal - $gVisitante $visitNombre: FINALIZADO (${result.totalPronosticosLiquidados} pronósticos calculados, +${result.totalPuntosOtorgados} pts asignados)."
                            )
                            log.info("Partido {} liquidado con éxito: {} {} - {} {}", matchUuid, localNombre, gLocal, gVisitante, visitNombre)
                        }
                    }
                    "EN_JUEGO" -> {
                        matchRepository.updateMatchLiveScore(
                            partidoId = matchUuid,
                            golesLocal = gLocal,
                            golesVisitante = gVisitante,
                            estado = "EN_JUEGO"
                        )
                        detalles.add(
                            "⏱️ [$compName] $localNombre $gLocal - $gVisitante $visitNombre: EN JUEGO ($gLocal - $gVisitante parcial)."
                        )
                    }
                }
            } catch (e: Exception) {
                log.error("Error al procesar partido externo {}: {}", ext.codigoExterno, e.message)
            }
        }

        val resumenMensaje = if (importadosNuevos > 0 || finalizados > 0) {
            "Sincronización mundial exitosa: $procesados partidos sincronizados ($importadosNuevos nuevos importados, $finalizados finalizados, $totalPronosticosLiquidados pronósticos liquidados)."
        } else {
            "Sincronización al día: $procesados partidos verificados en todo el mundo. No hay nuevos cambios pendientes."
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

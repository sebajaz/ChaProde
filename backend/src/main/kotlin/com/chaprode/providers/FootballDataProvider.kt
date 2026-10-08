package com.chaprode.providers

import com.chaprode.repositories.MatchRepository
import com.chaprode.repositories.TournamentRepository
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory
import java.util.*

@Serializable
data class SeedTorneo(
    val nombre: String,
    val codigoExterno: String,
    val logoUrl: String? = null,
    val fechaInicio: String? = null,
    val fechaFin: String? = null
)

@Serializable
data class SeedEquipo(
    val nombre: String,
    val codigoExterno: String,
    val urlBandera: String? = null,
    val codigoIso: String? = null
)

@Serializable
data class SeedPartido(
    val codigoExterno: String,
    val equipoLocalCodigo: String,
    val equipoVisitanteCodigo: String,
    val fechaPartido: String,
    val estado: String = "PENDIENTE"
)

@Serializable
data class SeedData(
    val torneo: SeedTorneo,
    val equipos: List<SeedEquipo>,
    val partidos: List<SeedPartido>
)

@Serializable
data class SeedTorneoConDetalles(
    val torneo: SeedTorneo,
    val equipos: List<SeedEquipo>,
    val partidos: List<SeedPartido>
)

interface FootballDataProvider {
    suspend fun seedInitialData(): Boolean
}

class JsonSeedFootballDataProvider(
    private val tournamentRepository: TournamentRepository = TournamentRepository(),
    private val matchRepository: MatchRepository = MatchRepository()
) : FootballDataProvider {

    private val log = LoggerFactory.getLogger(JsonSeedFootballDataProvider::class.java)

    override suspend fun seedInitialData(): Boolean {
        return try {
            val worldStream = javaClass.classLoader.getResourceAsStream("data/seeds_world_leagues.json")
            val tournamentBatches: List<SeedTorneoConDetalles> = if (worldStream != null) {
                val jsonContent = worldStream.bufferedReader().use { it.readText() }
                Json { ignoreUnknownKeys = true }.decodeFromString<List<SeedTorneoConDetalles>>(jsonContent)
            } else {
                val singleStream = javaClass.classLoader.getResourceAsStream("data/seeds_mundial2026.json")
                    ?: throw IllegalStateException("Archivos de semilla no encontrados en classpath")
                val jsonContent = singleStream.bufferedReader().use { it.readText() }
                val single = Json { ignoreUnknownKeys = true }.decodeFromString<SeedData>(jsonContent)
                listOf(SeedTorneoConDetalles(single.torneo, single.equipos, single.partidos))
            }

            var totalTorneosCreados = 0
            var totalPartidosCreados = 0

            for (batch in tournamentBatches) {
                // 1. Crear o buscar Torneo
                var torneo = tournamentRepository.findTournamentByCodigoExterno(batch.torneo.codigoExterno)
                if (torneo == null) {
                    val fInicio = batch.torneo.fechaInicio?.let { Instant.parse(it) }
                    val fFin = batch.torneo.fechaFin?.let { Instant.parse(it) }
                    torneo = tournamentRepository.createTournament(
                        nombre = batch.torneo.nombre,
                        codigoExterno = batch.torneo.codigoExterno,
                        logoUrl = batch.torneo.logoUrl,
                        fechaInicio = fInicio,
                        fechaFin = fFin
                    )
                    totalTorneosCreados++
                    log.info("Torneo creado desde seed: {} ({})", torneo.nombre, torneo.codigoExterno)
                }
                val torneoId = UUID.fromString(torneo.id)

                // 2. Crear Equipos y vincular al Torneo
                val teamMap = mutableMapOf<String, UUID>()
                for (eq in batch.equipos) {
                    var equipo = tournamentRepository.findTeamByCodigo(eq.codigoExterno)
                    if (equipo == null) {
                        equipo = tournamentRepository.createTeam(
                            nombre = eq.nombre,
                            codigoExterno = eq.codigoExterno,
                            urlBandera = eq.urlBandera,
                            codigoIso = eq.codigoIso
                        )
                        log.info("Equipo creado desde seed: {}", equipo.nombre)
                    }
                    val eqId = UUID.fromString(equipo.id)
                    teamMap[eq.codigoExterno] = eqId
                    tournamentRepository.addTeamToTournament(torneoId, eqId)
                }

                // 3. Crear Partidos
                val existingMatches = matchRepository.getMatchesByTournament(torneoId)
                val existingCodes = existingMatches.mapNotNull { it.codigoExterno }.toSet()

                for (p in batch.partidos) {
                    if (!existingCodes.contains(p.codigoExterno)) {
                        val localId = teamMap[p.equipoLocalCodigo]
                        val visitanteId = teamMap[p.equipoVisitanteCodigo]
                        if (localId != null && visitanteId != null) {
                            val fecha = Instant.parse(p.fechaPartido)
                            matchRepository.createMatch(
                                torneoId = torneoId,
                                equipoLocalId = localId,
                                equipoVisitanteId = visitanteId,
                                fechaPartido = fecha,
                                codigoExterno = p.codigoExterno
                            )
                            totalPartidosCreados++
                            log.info("Partido creado desde seed: {} vs {} ({})", p.equipoLocalCodigo, p.equipoVisitanteCodigo, batch.torneo.nombre)
                        }
                    }
                }
            }

            log.info("Semilla mundial cargada con éxito: {} torneos procesados, {} nuevos partidos creados.", tournamentBatches.size, totalPartidosCreados)
            true
        } catch (e: Exception) {
            log.error("Error al cargar datos semilla mundial", e)
            false
        }
    }
}

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
            val stream = javaClass.classLoader.getResourceAsStream("data/seeds_mundial2026.json")
                ?: throw IllegalStateException("Archivo seeds_mundial2026.json no encontrado en classpath")

            val jsonContent = stream.bufferedReader().use { it.readText() }
            val data = Json { ignoreUnknownKeys = true }.decodeFromString<SeedData>(jsonContent)

            // 1. Crear o buscar Torneo
            var torneo = tournamentRepository.findTournamentByCodigoExterno(data.torneo.codigoExterno)
            if (torneo == null) {
                val fInicio = data.torneo.fechaInicio?.let { Instant.parse(it) }
                val fFin = data.torneo.fechaFin?.let { Instant.parse(it) }
                torneo = tournamentRepository.createTournament(
                    nombre = data.torneo.nombre,
                    codigoExterno = data.torneo.codigoExterno,
                    logoUrl = data.torneo.logoUrl,
                    fechaInicio = fInicio,
                    fechaFin = fFin
                )
                log.info("Torneo creado desde seed: {}", torneo.nombre)
            }
            val torneoId = UUID.fromString(torneo.id)

            // 2. Crear Equipos y vincular al Torneo
            val teamMap = mutableMapOf<String, UUID>()
            for (eq in data.equipos) {
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

            for (p in data.partidos) {
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
                        log.info("Partido creado desde seed: {} vs {}", p.equipoLocalCodigo, p.equipoVisitanteCodigo)
                    }
                }
            }

            log.info("Semilla de torneo, equipos y partidos cargada exitosamente.")
            true
        } catch (e: Exception) {
            log.error("Error al cargar datos semilla", e)
            false
        }
    }
}

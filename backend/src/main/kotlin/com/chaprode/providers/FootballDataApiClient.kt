package com.chaprode.providers

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory

import kotlinx.datetime.Clock
import kotlin.time.Duration.Companion.days

@Serializable
data class FootballDataMatchesResponse(
    val matches: List<FootballDataMatch> = emptyList()
)

@Serializable
data class FootballDataMatch(
    val id: Long,
    val utcDate: String? = null,
    val status: String,
    val competition: FootballDataCompetition? = null,
    val homeTeam: FootballDataTeam,
    val awayTeam: FootballDataTeam,
    val score: FootballDataScore? = null
)

@Serializable
data class FootballDataCompetition(
    val id: Long? = null,
    val name: String? = null,
    val code: String? = null,
    val type: String? = null,
    val emblem: String? = null
)

@Serializable
data class FootballDataTeam(
    val id: Long? = null,
    val name: String? = null,
    val shortName: String? = null,
    val tla: String? = null,
    val crest: String? = null
)

@Serializable
data class FootballDataScore(
    val winner: String? = null,
    val fullTime: FootballDataScoreDetail? = null,
    val regularTime: FootballDataScoreDetail? = null
)

@Serializable
data class FootballDataScoreDetail(
    val home: Int? = null,
    val away: Int? = null
)

class FootballDataApiClient(
    private val baseUrl: String = "https://api.football-data.org/v4",
    private val apiToken: String = ""
) : SportsDataProvider {

    private val log = LoggerFactory.getLogger(FootballDataApiClient::class.java)

    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
    }

    override suspend fun fetchWorldwideMatches(dateFrom: String?, dateTo: String?): List<ExternalMatchScore> {
        if (apiToken.isBlank()) {
            log.warn("FootballDataApiClient: FOOTBALL_API_TOKEN está vacío. Se requiere token para consultar football-data.org.")
            return emptyList()
        }

        val now = Clock.System.now()
        val f = dateFrom ?: now.minus(2.days).toString().take(10)
        val t = dateTo ?: now.plus(8.days).toString().take(10)

        // football-data.org /v4/matches permite dateFrom y dateTo dentro de un rango de 10 días
        val endpoint = "$baseUrl/matches?dateFrom=$f&dateTo=$t"

        log.info("Consultando partidos mundiales en API externa: {}", endpoint)

        return try {
            val response = client.get(endpoint) {
                header("X-Auth-Token", apiToken)
                contentType(ContentType.Application.Json)
            }

            if (response.status != HttpStatusCode.OK) {
                log.error("API externa respondió con status HTTP {} ({})", response.status.value, response.status.description)
                return emptyList()
            }

            val body = response.body<FootballDataMatchesResponse>()
            log.info("API externa devolvió {} partidos a nivel mundial (ventana {} a {}).", body.matches.size, f, t)

            body.matches.map { match ->
                mapMatchToExternalScore(match)
            }
        } catch (e: Exception) {
            log.error("Error al consultar partidos mundiales de football-data.org: {}", e.message)
            emptyList()
        }
    }

    override suspend fun fetchMatchResults(competitionCode: String?): List<ExternalMatchScore> {
        if (apiToken.isBlank()) {
            log.warn("FootballDataApiClient: Sin token de API configurado. Omitiendo llamada HTTP.")
            return emptyList()
        }

        val now = Clock.System.now()
        val f = now.minus(2.days).toString().take(10)
        val t = now.plus(8.days).toString().take(10)

        return try {
            val endpoint = if (!competitionCode.isNullOrBlank()) {
                "$baseUrl/competitions/$competitionCode/matches?dateFrom=$f&dateTo=$t"
            } else {
                "$baseUrl/matches?dateFrom=$f&dateTo=$t"
            }

            log.info("Consultando API externa en {}", endpoint)

            val response = client.get(endpoint) {
                header("X-Auth-Token", apiToken)
                contentType(ContentType.Application.Json)
            }

            if (response.status != HttpStatusCode.OK) {
                log.error("API externa respondió con status HTTP {}", response.status)
                return emptyList()
            }

            val body = response.body<FootballDataMatchesResponse>()
            log.info("Se recibieron {} partidos desde la API externa.", body.matches.size)

            body.matches.map { match ->
                mapMatchToExternalScore(match)
            }
        } catch (e: Exception) {
            log.error("Error al comunicarse con la API de deportes: {}", e.message)
            emptyList()
        }
    }

    private fun mapMatchToExternalScore(match: FootballDataMatch): ExternalMatchScore {
        val estadoMapeado = when (match.status.uppercase()) {
            "FINISHED" -> "FINALIZADO"
            "IN_PLAY", "PAUSED", "LIVE" -> "EN_JUEGO"
            else -> "PENDIENTE"
        }

        val gLocal = match.score?.fullTime?.home ?: match.score?.regularTime?.home
        val gVisitante = match.score?.fullTime?.away ?: match.score?.regularTime?.away

        val localCode = match.homeTeam.tla ?: match.homeTeam.shortName?.take(3)?.uppercase() ?: match.homeTeam.name?.take(3)?.uppercase() ?: "LOC"
        val visitCode = match.awayTeam.tla ?: match.awayTeam.shortName?.take(3)?.uppercase() ?: match.awayTeam.name?.take(3)?.uppercase() ?: "VIS"

        val localNombre = match.homeTeam.shortName ?: match.homeTeam.name ?: localCode
        val visitNombre = match.awayTeam.shortName ?: match.awayTeam.name ?: visitCode

        return ExternalMatchScore(
            codigoExterno = "MATCH-${match.id}",
            localCodigo = localCode,
            visitanteCodigo = visitCode,
            golesLocal = gLocal,
            golesVisitante = gVisitante,
            estado = estadoMapeado,
            competitionCode = match.competition?.code,
            competitionName = match.competition?.name,
            competitionEmblem = match.competition?.emblem,
            localNombre = localNombre,
            visitanteNombre = visitNombre,
            localBandera = match.homeTeam.crest,
            visitanteBandera = match.awayTeam.crest,
            fechaPartido = match.utcDate
        )
    }
}

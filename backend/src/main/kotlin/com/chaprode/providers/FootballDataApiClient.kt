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

@Serializable
data class FootballDataMatchesResponse(
    val matches: List<FootballDataMatch> = emptyList()
)

@Serializable
data class FootballDataMatch(
    val id: Long,
    val utcDate: String? = null,
    val status: String,
    val homeTeam: FootballDataTeam,
    val awayTeam: FootballDataTeam,
    val score: FootballDataScore? = null
)

@Serializable
data class FootballDataTeam(
    val id: Long? = null,
    val name: String? = null,
    val tla: String? = null
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

    override suspend fun fetchMatchResults(competitionCode: String?): List<ExternalMatchScore> {
        if (apiToken.isBlank()) {
            log.warn("FootballDataApiClient: No se proporcionó un token de API (FOOTBALL_API_TOKEN está vacío). Omitiendo llamada HTTP.")
            return emptyList()
        }

        return try {
            val endpoint = if (!competitionCode.isNullOrBlank()) {
                "$baseUrl/competitions/$competitionCode/matches"
            } else {
                "$baseUrl/matches"
            }

            log.info("Consultando API externa de deportes en {}", endpoint)

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
                val estadoMapeado = when (match.status.uppercase()) {
                    "FINISHED" -> "FINALIZADO"
                    "IN_PLAY", "PAUSED", "LIVE" -> "EN_JUEGO"
                    else -> "PENDIENTE"
                }

                val gLocal = match.score?.fullTime?.home ?: match.score?.regularTime?.home
                val gVisitante = match.score?.fullTime?.away ?: match.score?.regularTime?.away

                val localCode = match.homeTeam.tla ?: match.homeTeam.name?.take(3)?.uppercase() ?: "LOC"
                val visitCode = match.awayTeam.tla ?: match.awayTeam.name?.take(3)?.uppercase() ?: "VIS"

                ExternalMatchScore(
                    codigoExterno = "MATCH-${match.id}",
                    localCodigo = localCode,
                    visitanteCodigo = visitCode,
                    golesLocal = gLocal,
                    golesVisitante = gVisitante,
                    estado = estadoMapeado
                )
            }
        } catch (e: Exception) {
            log.error("Error al comunicarse con la API de deportes: {}", e.message)
            emptyList()
        }
    }
}

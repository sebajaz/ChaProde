package com.chaprode.services

import com.chaprode.dto.MatchWithPredictionDto
import com.chaprode.dto.PredictionDto
import com.chaprode.dto.SubmitPredictionRequest
import com.chaprode.repositories.MatchRepository
import com.chaprode.repositories.PredictionRepository
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import java.util.*
import kotlin.time.Duration.Companion.minutes

class PredictionService(
    private val predictionRepository: PredictionRepository = PredictionRepository(),
    private val matchRepository: MatchRepository = MatchRepository()
) {

    suspend fun submitPrediction(userId: String, request: SubmitPredictionRequest): PredictionDto {
        if (request.golesLocal < 0 || request.golesLocal > 99 ||
            request.golesVisitante < 0 || request.golesVisitante > 99) {
            throw IllegalArgumentException("La cantidad de goles debe ser un número válido entre 0 y 99.")
        }

        val userUuid = try {
            UUID.fromString(userId)
        } catch (e: Exception) {
            throw IllegalArgumentException("Identificador de usuario inválido.")
        }

        val matchUuid = try {
            UUID.fromString(request.partidoId)
        } catch (e: Exception) {
            throw IllegalArgumentException("Identificador de partido inválido.")
        }

        val match = matchRepository.getMatchById(matchUuid)
            ?: throw IllegalArgumentException("El partido seleccionado no existe.")

        if (match.estado != "PENDIENTE") {
            throw IllegalArgumentException("No se pueden cargar pronósticos en partidos que ya iniciaron o finalizaron.")
        }

        // Regla de los 5 minutos en hora del servidor
        val matchTime = Instant.parse(match.fechaPartido)
        val serverNow = Clock.System.now()
        val cutoffTime = matchTime.minus(5.minutes)

        if (serverNow >= cutoffTime) {
            throw IllegalArgumentException("Los pronósticos para este partido cerraron 5 minutos antes de su inicio.")
        }

        return predictionRepository.upsertPrediction(
            usuarioId = userUuid,
            partidoId = matchUuid,
            golesLocal = request.golesLocal,
            golesVisitante = request.golesVisitante
        )
    }

    suspend fun getMyPredictions(userId: String, torneoId: String): List<PredictionDto> {
        val userUuid = UUID.fromString(userId)
        val torneoUuid = UUID.fromString(torneoId)
        return predictionRepository.getPredictionsByUserAndTournament(userUuid, torneoUuid)
    }

    suspend fun getMatchesWithMyPredictions(userId: String, torneoId: String): List<MatchWithPredictionDto> {
        val userUuid = UUID.fromString(userId)
        val torneoUuid = UUID.fromString(torneoId)

        val matches = matchRepository.getMatchesByTournament(torneoUuid)
        val myPredictions = predictionRepository.getPredictionsByUserAndTournament(userUuid, torneoUuid)
        val predictionsMap = myPredictions.associateBy { it.partidoId }

        val serverNow = Clock.System.now()

        return matches.map { match ->
            val matchTime = Instant.parse(match.fechaPartido)
            val cutoffTime = matchTime.minus(5.minutes)
            val isClosed = serverNow >= cutoffTime || match.estado != "PENDIENTE"
            val remainingSeconds = cutoffTime.epochSeconds - serverNow.epochSeconds
            val remainingMinutes = if (remainingSeconds > 0) remainingSeconds / 60 else 0

            val pred = predictionsMap[match.id]?.copy(bloqueado = isClosed)

            MatchWithPredictionDto(
                match = match,
                myPrediction = pred,
                cerrado = isClosed,
                minutosRestantesParaCierre = remainingMinutes
            )
        }
    }
}

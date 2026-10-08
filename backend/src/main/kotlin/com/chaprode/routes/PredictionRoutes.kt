package com.chaprode.routes

import com.chaprode.dto.ApiResponse
import com.chaprode.dto.SubmitPredictionRequest
import com.chaprode.services.PredictionService
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.predictionRoutes(predictionService: PredictionService = PredictionService()) {
    route("/api") {
        authenticate("auth-jwt") {

            post("/pronosticos") {
                val principal = call.principal<JWTPrincipal>()
                val userId = principal?.payload?.getClaim("userId")?.asString()
                    ?: throw IllegalArgumentException("Token inválido.")

                val request = call.receive<SubmitPredictionRequest>()
                val prediction = predictionService.submitPrediction(userId, request)

                call.respond(
                    HttpStatusCode.OK,
                    ApiResponse.ok(prediction, "Pronóstico guardado exitosamente")
                )
            }

            get("/pronosticos/mis-pronosticos") {
                val principal = call.principal<JWTPrincipal>()
                val userId = principal?.payload?.getClaim("userId")?.asString()
                    ?: throw IllegalArgumentException("Token inválido.")

                val torneoId = call.request.queryParameters["torneoId"]
                    ?: throw IllegalArgumentException("Parámetro 'torneoId' requerido.")

                val predictions = predictionService.getMyPredictions(userId, torneoId)
                call.respond(
                    HttpStatusCode.OK,
                    ApiResponse.ok(predictions, "Pronósticos del usuario obtenidos")
                )
            }
        }

        // Permite ver la cartelera de partidos tanto a invitados como a usuarios autenticados
        authenticate("auth-jwt", optional = true) {
            get("/torneos/{id}/partidos-con-pronosticos") {
                val principal = call.principal<JWTPrincipal>()
                val userId = principal?.payload?.getClaim("userId")?.asString()

                val torneoId = call.parameters["id"]
                    ?: throw IllegalArgumentException("Parámetro 'id' del torneo requerido.")

                val matchesWithPredictions = predictionService.getMatchesWithMyPredictions(userId, torneoId)
                call.respond(
                    HttpStatusCode.OK,
                    ApiResponse.ok(matchesWithPredictions, "Fixture con pronósticos obtenido")
                )
            }
        }
    }
}

package com.chaprode.routes

import com.chaprode.dto.ApiResponse
import com.chaprode.services.LeaderboardService
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.leaderboardRoutes(leaderboardService: LeaderboardService = LeaderboardService()) {
    route("/api") {
        get("/torneos/{id}/ranking") {
            val torneoId = call.parameters["id"]
                ?: throw IllegalArgumentException("Parámetro 'id' del torneo faltante.")

            val response = leaderboardService.getTournamentLeaderboard(torneoId)
            call.respond(HttpStatusCode.OK, ApiResponse.ok(response, "Ranking del torneo obtenido"))
        }

        get("/ranking/global") {
            val response = leaderboardService.getGlobalLeaderboard()
            call.respond(HttpStatusCode.OK, ApiResponse.ok(response, "Ranking global obtenido"))
        }
    }
}

package com.chaprode.routes

import com.chaprode.dto.ApiResponse
import com.chaprode.dto.CreateLeagueRequest
import com.chaprode.dto.JoinLeagueRequest
import com.chaprode.services.LeagueService
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.leagueRoutes(leagueService: LeagueService = LeagueService()) {
    route("/api/ligas") {
        authenticate("auth-jwt") {

            post {
                val principal = call.principal<JWTPrincipal>()
                val userId = principal?.payload?.getClaim("userId")?.asString()
                    ?: throw IllegalArgumentException("Token inválido.")

                val request = call.receive<CreateLeagueRequest>()
                val league = leagueService.createLeague(userId, request)

                call.respond(HttpStatusCode.Created, ApiResponse.ok(league, "Liga privada creada exitosamente"))
            }

            post("/unirse") {
                val principal = call.principal<JWTPrincipal>()
                val userId = principal?.payload?.getClaim("userId")?.asString()
                    ?: throw IllegalArgumentException("Token inválido.")

                val request = call.receive<JoinLeagueRequest>()
                val league = leagueService.joinLeague(userId, request)

                call.respond(HttpStatusCode.OK, ApiResponse.ok(league, "Te has unido a la liga exitosamente"))
            }

            get("/mis-ligas") {
                val principal = call.principal<JWTPrincipal>()
                val userId = principal?.payload?.getClaim("userId")?.asString()
                    ?: throw IllegalArgumentException("Token inválido.")

                val leagues = leagueService.getMyLeagues(userId)
                call.respond(HttpStatusCode.OK, ApiResponse.ok(leagues, "Ligas privadas obtenidas"))
            }

            get("/{id}") {
                val id = call.parameters["id"]
                    ?: throw IllegalArgumentException("Parámetro 'id' faltante.")

                val detail = leagueService.getLeagueDetail(id)
                call.respond(HttpStatusCode.OK, ApiResponse.ok(detail, "Detalle de la liga obtenido"))
            }
        }
    }
}

package com.chaprode.routes

import com.chaprode.dto.*
import com.chaprode.services.TournamentService
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.tournamentRoutes(tournamentService: TournamentService = TournamentService()) {
    route("/api") {

        // Rutas Públicas / Usuario
        get("/torneos") {
            val torneos = tournamentService.getActiveTournaments()
            call.respond(HttpStatusCode.OK, ApiResponse.ok(torneos, "Torneos activos obtenidos"))
        }

        get("/torneos/{id}") {
            val id = call.parameters["id"] ?: throw IllegalArgumentException("Parámetro id faltante")
            val torneo = tournamentService.getTournamentById(id)
            call.respond(HttpStatusCode.OK, ApiResponse.ok(torneo, "Detalle del torneo obtenido"))
        }

        get("/torneos/{id}/partidos") {
            val id = call.parameters["id"] ?: throw IllegalArgumentException("Parámetro id faltante")
            val partidos = tournamentService.getMatchesByTournament(id)
            call.respond(HttpStatusCode.OK, ApiResponse.ok(partidos, "Fixture de partidos obtenido"))
        }

        get("/equipos") {
            val equipos = tournamentService.getAllTeams()
            call.respond(HttpStatusCode.OK, ApiResponse.ok(equipos, "Equipos obtenidos"))
        }

        // Rutas de Administración
        route("/admin") {
            post("/torneos/seed") {
                val success = tournamentService.seedData()
                if (success) {
                    val torneos = tournamentService.getActiveTournaments()
                    call.respond(HttpStatusCode.OK, ApiResponse.ok(torneos, "Datos semilla importados correctamente"))
                } else {
                    call.respond(HttpStatusCode.InternalServerError, ApiResponse.error<Unit>("Error al importar datos semilla"))
                }
            }

            post("/torneos") {
                val request = call.receive<CreateTournamentRequest>()
                val torneo = tournamentService.createTournament(request)
                call.respond(HttpStatusCode.Created, ApiResponse.ok(torneo, "Torneo creado exitosamente"))
            }

            post("/equipos") {
                val request = call.receive<CreateTeamRequest>()
                val equipo = tournamentService.createTeam(request)
                call.respond(HttpStatusCode.Created, ApiResponse.ok(equipo, "Equipo creado exitosamente"))
            }

            post("/partidos") {
                val request = call.receive<CreateMatchRequest>()
                val matchId = tournamentService.createMatch(request)
                call.respond(HttpStatusCode.Created, ApiResponse.ok(mapOf("id" to matchId), "Partido creado exitosamente"))
            }
        }
    }
}

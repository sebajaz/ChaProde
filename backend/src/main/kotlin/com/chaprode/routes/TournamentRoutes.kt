package com.chaprode.routes

import com.chaprode.dto.*
import com.chaprode.services.TournamentService
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.tournamentRoutes(
    tournamentService: TournamentService = TournamentService(),
    matchSyncService: com.chaprode.services.MatchSyncService = com.chaprode.services.MatchSyncService()
) {
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

        get("/partidos") {
            val torneoIdParam = call.request.queryParameters["torneoId"]
            val estadoParam = call.request.queryParameters["estado"]
            val partidos = if (torneoIdParam.isNullOrBlank() || torneoIdParam.equals("todos", ignoreCase = true) || torneoIdParam.equals("all", ignoreCase = true)) {
                tournamentService.getAllMatches()
            } else {
                tournamentService.getMatchesByTournament(torneoIdParam)
            }
            val filtrados = if (!estadoParam.isNullOrBlank()) {
                partidos.filter { it.estado.equals(estadoParam, ignoreCase = true) }
            } else {
                partidos
            }
            call.respond(HttpStatusCode.OK, ApiResponse.ok(filtrados, "Partidos obtenidos exitosamente"))
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

            post("/partidos/{id}/resultado") {
                val matchId = call.parameters["id"]
                    ?: return@post call.respond(HttpStatusCode.BadRequest, ApiResponse.error<Unit>("Parámetro 'id' del partido faltante."))
                val matchUuid = try {
                    java.util.UUID.fromString(matchId)
                } catch (e: Exception) {
                    return@post call.respond(HttpStatusCode.BadRequest, ApiResponse.error<Unit>("ID de partido inválido."))
                }

                val request = call.receive<SetMatchResultRequest>()
                val matchRepo = com.chaprode.repositories.MatchRepository()
                val match = matchRepo.getMatchById(matchUuid)
                    ?: return@post call.respond(HttpStatusCode.NotFound, ApiResponse.error<Unit>("Partido no encontrado."))

                // Regla 1: No se pueden modificar los resultados de partidos finalizados traídos por la API
                if (match.estado == "FINALIZADO" && !match.codigoExterno.isNullOrBlank()) {
                    return@post call.respond(
                        HttpStatusCode.BadRequest,
                        ApiResponse.error<Unit>("No se pueden modificar los resultados de partidos finalizados provistos por la API externa.")
                    )
                }

                // Regla 2: No se pueden cargar resultados en partidos pendientes
                if (match.estado == "PENDIENTE") {
                    return@post call.respond(
                        HttpStatusCode.BadRequest,
                        ApiResponse.error<Unit>("No se pueden cargar resultados a partidos en estado PENDIENTE. El partido debe iniciar o estar EN_JUEGO primero.")
                    )
                }

                val result = matchRepo.settleMatchResult(
                    partidoId = matchUuid,
                    golesLocal = request.golesLocal,
                    golesVisitante = request.golesVisitante,
                    estado = request.estado
                )
                call.respond(
                    HttpStatusCode.OK,
                    ApiResponse.ok(result, "Resultado registrado y pronósticos liquidados exitosamente")
                )
            }

            post("/partidos/{id}/iniciar") {
                val matchId = call.parameters["id"]
                    ?: return@post call.respond(HttpStatusCode.BadRequest, ApiResponse.error<Unit>("Parámetro 'id' del partido faltante."))
                val matchUuid = try {
                    java.util.UUID.fromString(matchId)
                } catch (e: Exception) {
                    return@post call.respond(HttpStatusCode.BadRequest, ApiResponse.error<Unit>("ID de partido inválido."))
                }

                val matchRepo = com.chaprode.repositories.MatchRepository()
                val match = matchRepo.getMatchById(matchUuid)
                    ?: return@post call.respond(HttpStatusCode.NotFound, ApiResponse.error<Unit>("Partido no encontrado."))

                if (match.estado != "PENDIENTE") {
                    return@post call.respond(
                        HttpStatusCode.BadRequest,
                        ApiResponse.error<Unit>("Solo los partidos en estado PENDIENTE pueden ser iniciados.")
                    )
                }

                matchRepo.updateMatchStatus(matchUuid, "EN_JUEGO")
                call.respond(
                    HttpStatusCode.OK,
                    ApiResponse.ok(mapOf("id" to matchId, "estado" to "EN_JUEGO"), "Partido iniciado exitosamente (estado EN_JUEGO).")
                )
            }

            // Sincronización Automática / On-Demand con API Deportiva Oficial
            post("/partidos/sincronizar") {
                val torneoId = call.request.queryParameters["torneoId"]
                val dateFrom = call.request.queryParameters["dateFrom"]
                val dateTo = call.request.queryParameters["dateTo"]

                val torneoUuid = if (!torneoId.isNullOrBlank() && !torneoId.equals("todos", ignoreCase = true) && !torneoId.equals("all", ignoreCase = true)) {
                    tournamentService.resolveTournamentUuid(torneoId)
                } else null

                val summary = matchSyncService.syncMatches(
                    torneoId = torneoUuid,
                    dateFrom = dateFrom,
                    dateTo = dateTo
                )
                call.respond(HttpStatusCode.OK, ApiResponse.ok(summary, summary.mensaje))
            }
        }
    }
}

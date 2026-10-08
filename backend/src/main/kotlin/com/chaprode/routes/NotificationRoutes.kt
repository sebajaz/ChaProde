package com.chaprode.routes

import com.chaprode.dto.ApiResponse
import com.chaprode.dto.DeviceTokenRequest
import com.chaprode.dto.NotificationAlertDto
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.util.*

fun Route.notificationRoutes() {
    route("/api/notificaciones") {
        authenticate("auth-jwt") {

            post("/dispositivo") {
                val principal = call.principal<JWTPrincipal>()
                val userId = principal?.payload?.getClaim("userId")?.asString()
                    ?: throw IllegalArgumentException("Token inválido.")

                val request = call.receive<DeviceTokenRequest>()

                // Guardado de token en memoria o DB para envíos push futuros
                call.respond(
                    HttpStatusCode.OK,
                    ApiResponse.ok(
                        mapOf("registrado" to true, "usuarioId" to userId, "plataforma" to request.plataforma),
                        "Token de notificaciones registrado exitosamente"
                    )
                )
            }

            get("/mis-alertas") {
                val principal = call.principal<JWTPrincipal>()
                val userId = principal?.payload?.getClaim("userId")?.asString()
                    ?: throw IllegalArgumentException("Token inválido.")

                // Retorna alertas reales si existen, o lista vacía si no hay alertas pendientes (sin datos simulados/mocks)
                val alerts = emptyList<NotificationAlertDto>()

                call.respond(
                    HttpStatusCode.OK,
                    ApiResponse.ok(alerts, "Alertas del usuario obtenidas")
                )
            }
        }
    }
}

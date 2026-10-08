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

                val now = kotlinx.datetime.Clock.System.now().toString()
                val mockAlertas = listOf(
                    NotificationAlertDto(
                        id = UUID.randomUUID().toString(),
                        titulo = "⏱️ ¡Pronóstico por cerrar! (-5 min)",
                        mensaje = "El partido Argentina vs México cierra sus pronósticos en 5 minutos.",
                        tipo = "REMINDER",
                        leido = false,
                        createdAt = now
                    ),
                    NotificationAlertDto(
                        id = UUID.randomUUID().toString(),
                        titulo = "🎉 ¡PLENO EXACTO! (+3 Pts)",
                        mensaje = "Acertaste el resultado exacto en Argentina 2 - México 1.",
                        tipo = "POINTS",
                        leido = true,
                        createdAt = now
                    )
                )

                call.respond(
                    HttpStatusCode.OK,
                    ApiResponse.ok(mockAlertas, "Alertas del usuario obtenidas")
                )
            }
        }
    }
}

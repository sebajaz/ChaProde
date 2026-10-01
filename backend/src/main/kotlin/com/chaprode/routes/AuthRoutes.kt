package com.chaprode.routes

import com.chaprode.dto.ApiResponse
import com.chaprode.dto.LoginRequest
import com.chaprode.dto.RegisterRequest
import com.chaprode.services.AuthService
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.authRoutes(authService: AuthService = AuthService()) {
    route("/api/auth") {

        post("/register") {
            val request = call.receive<RegisterRequest>()
            val authResponse = authService.register(request)
            call.respond(
                HttpStatusCode.Created,
                ApiResponse.ok(authResponse, "Usuario registrado exitosamente")
            )
        }

        post("/login") {
            val request = call.receive<LoginRequest>()
            val authResponse = authService.login(request)
            call.respond(
                HttpStatusCode.OK,
                ApiResponse.ok(authResponse, "Inicio de sesión exitoso")
            )
        }

        authenticate("auth-jwt") {
            get("/me") {
                val principal = call.principal<JWTPrincipal>()
                val userId = principal?.payload?.getClaim("userId")?.asString()
                    ?: throw IllegalArgumentException("Token inválido o expirado.")

                val user = authService.getMe(userId)
                call.respond(
                    HttpStatusCode.OK,
                    ApiResponse.ok(user, "Perfil de usuario obtenido")
                )
            }
        }
    }
}

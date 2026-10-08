package com.chaprode

import com.chaprode.config.DatabaseFactory
import com.chaprode.dto.ApiResponse
import com.chaprode.routes.authRoutes
import com.chaprode.routes.healthRoutes
import com.chaprode.routes.leaderboardRoutes
import com.chaprode.routes.leagueRoutes
import com.chaprode.routes.predictionRoutes
import com.chaprode.routes.tournamentRoutes
import com.chaprode.security.JwtConfig
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory

fun main(args: Array<String>) {
    EngineMain.main(args)
}

fun Application.module() {
    val log = LoggerFactory.getLogger("ChaProdeApplication")
    log.info("Inicializando ChaProde Backend...")

    // 1. Inicializar Base de Datos PostgreSQL
    try {
        DatabaseFactory.init(environment.config)
        log.info("Base de datos inicializada y conectada exitosamente.")
    } catch (e: Exception) {
        log.error("Fallo al inicializar la base de datos: {}", e.message)
    }

    // 2. Configurar JWT
    JwtConfig.init(environment.config)

    // 3. Serialización JSON
    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            isLenient = true
            ignoreUnknownKeys = true
            encodeDefaults = true
        })
    }

    // 4. CORS (Para clientes Web y Mobile)
    install(CORS) {
        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        allowMethod(HttpMethod.Patch)
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Post)
        allowHeader(HttpHeaders.Authorization)
        allowHeader(HttpHeaders.ContentType)
        anyHost()
    }

    // 5. Manejo Global de Excepciones (StatusPages)
    install(StatusPages) {
        exception<IllegalArgumentException> { call, cause ->
            call.respond(
                HttpStatusCode.BadRequest,
                ApiResponse.error<Unit>(cause.message ?: "Solicitud inválida")
            )
        }
        exception<Throwable> { call, cause ->
            log.error("Error no controlado en la aplicación", cause)
            call.respond(
                HttpStatusCode.InternalServerError,
                ApiResponse.error<Unit>(cause.message ?: "Error interno del servidor")
            )
        }
    }

    // 6. Autenticación JWT
    install(Authentication) {
        jwt("auth-jwt") {
            realm = JwtConfig.getRealm()
            verifier(JwtConfig.getVerifier())
            validate { credential ->
                val userId = credential.payload.getClaim("userId").asString()
                if (!userId.isNullOrBlank()) {
                    JWTPrincipal(credential.payload)
                } else {
                    null
                }
            }
            challenge { defaultScheme, realm ->
                call.respond(
                    HttpStatusCode.Unauthorized,
                    ApiResponse.error<Unit>("Acceso no autorizado: Token no proporcionado o inválido.")
                )
            }
        }
    }

    // 7. Enrutamiento
    routing {
        get("/") {
            call.respond(ApiResponse.ok(mapOf("app" to "ChaProde API", "version" to "1.0.0"), "Bienvenido a ChaProde API"))
        }
        healthRoutes()
        authRoutes()
        tournamentRoutes()
        predictionRoutes()
        leaderboardRoutes()
        leagueRoutes()
    }
}

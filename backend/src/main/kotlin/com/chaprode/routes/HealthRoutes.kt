package com.chaprode.routes

import com.chaprode.config.DatabaseFactory
import com.chaprode.dto.ApiResponse
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable

@Serializable
data class HealthStatus(
    val status: String,
    val database: String,
    val version: String = "1.0.0",
    val uptimeMillis: Long
)

private val startTime = System.currentTimeMillis()

fun Route.healthRoutes() {
    route("/api/health") {
        get {
            val isDbHealthy = DatabaseFactory.checkHealth()
            val status = HealthStatus(
                status = if (isDbHealthy) "UP" else "DEGRADED",
                database = if (isDbHealthy) "CONNECTED" else "DISCONNECTED",
                uptimeMillis = System.currentTimeMillis() - startTime
            )

            val httpStatusCode = if (isDbHealthy) HttpStatusCode.OK else HttpStatusCode.ServiceUnavailable
            call.respond(httpStatusCode, ApiResponse.ok(status, "Estado del servidor ChaProde"))
        }
    }
}

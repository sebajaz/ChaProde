package com.chaprode.security

import at.favre.lib.crypto.bcrypt.BCrypt
import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.server.config.*
import java.util.*

/**
 * Utilidades criptográficas para contraseñas de usuarios.
 * Utiliza el algoritmo BCrypt con factor de costo 12 (resistente a fuerza bruta).
 */
object SecurityUtils {
    /** Genera el hash salado de una contraseña en texto claro. */
    fun hashPassword(password: String): String {
        return BCrypt.withDefaults().hashToString(12, password.toCharArray())
    }

    /** Valida si una contraseña ingresada coincide con el hash almacenado en PostgreSQL. */
    fun verifyPassword(password: String, hashed: String): Boolean {
        val result = BCrypt.verifyer().verify(password.toCharArray(), hashed.toCharArray())
        return result.verified
    }
}

/**
 * Gestor y emisor de tokens de sesión JSON Web Token (JWT).
 * Configurado con algoritmo simétrico HMAC-256 y expiración automática de 72 horas.
 */
object JwtConfig {
    private var secret: String = "chaprode_super_secret_jwt_key_2026_very_long_and_safe_token"
    private var issuer: String = "http://0.0.0.0:8080/"
    private var audience: String = "chaprode_users"
    private var realm: String = "ChaProde App"
    private var validityInMs: Long = 72 * 3600 * 1000L // 72 horas

    fun init(config: ApplicationConfig) {
        secret = config.propertyOrNull("jwt.secret")?.getString() ?: secret
        issuer = config.propertyOrNull("jwt.issuer")?.getString() ?: issuer
        audience = config.propertyOrNull("jwt.audience")?.getString() ?: audience
        realm = config.propertyOrNull("jwt.realm")?.getString() ?: realm
        val hours = config.propertyOrNull("jwt.expirationHours")?.getString()?.toLongOrNull() ?: 72
        validityInMs = hours * 3600 * 1000L
    }

    fun getRealm(): String = realm
    fun getAudience(): String = audience
    fun getIssuer(): String = issuer

    fun getVerifier(): JWTVerifier = JWT
        .require(Algorithm.HMAC256(secret))
        .withAudience(audience)
        .withIssuer(issuer)
        .build()

    fun generateToken(userId: String, username: String, rol: String): String {
        return JWT.create()
            .withAudience(audience)
            .withIssuer(issuer)
            .withClaim("userId", userId)
            .withClaim("username", username)
            .withClaim("rol", rol)
            .withExpiresAt(Date(System.currentTimeMillis() + validityInMs))
            .sign(Algorithm.HMAC256(secret))
    }
}

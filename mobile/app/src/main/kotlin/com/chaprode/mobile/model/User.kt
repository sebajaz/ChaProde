package com.chaprode.mobile.model

/**
 * Modelo de dominio que representa a un usuario registrado en ChaProde.
 *
 * @param id Identificador único UUID del usuario.
 * @param username Nombre público de usuario (usado en el ranking y ligas).
 * @param email Correo electrónico de contacto y login.
 * @param rol Rol del usuario en el sistema ("USER" o "ADMIN").
 * @param createdAt Fecha y hora ISO-8601 del registro de la cuenta.
 */
data class User(
    val id: String,
    val username: String,
    val email: String,
    val rol: String,
    val createdAt: String? = null
)

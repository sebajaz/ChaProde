package com.chaprode.mobile.data.local.entity

import com.chaprode.mobile.model.User

/**
 * Entidad de persistencia local para la tabla 'users'.
 * Diseñada para integrarse directamente con Room en Android.
 */
data class UserEntity(
    val id: String,
    val username: String,
    val email: String,
    val rol: String,
    val token: String? = null,
    val createdAt: String? = null
) {
    fun toDomain(): User = User(
        id = id,
        username = username,
        email = email,
        rol = rol,
        createdAt = createdAt
    )

    companion object {
        fun fromDomain(user: User, token: String? = null): UserEntity = UserEntity(
            id = user.id,
            username = user.username,
            email = user.email,
            rol = user.rol,
            token = token,
            createdAt = user.createdAt
        )
    }
}

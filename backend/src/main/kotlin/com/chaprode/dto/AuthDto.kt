package com.chaprode.dto

import kotlinx.serialization.Serializable

@Serializable
data class RegisterRequest(
    val username: String,
    val email: String,
    val password: String,
    val rol: String = "USER"
)

@Serializable
data class LoginRequest(
    val usernameOrEmail: String,
    val password: String
)

@Serializable
data class UserDto(
    val id: String,
    val username: String,
    val email: String,
    val rol: String,
    val createdAt: String
)

@Serializable
data class AuthResponse(
    val token: String,
    val user: UserDto
)

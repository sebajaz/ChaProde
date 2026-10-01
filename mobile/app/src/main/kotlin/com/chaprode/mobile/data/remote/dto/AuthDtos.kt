package com.chaprode.mobile.data.remote.dto

data class LoginRequestDto(
    val usernameOrEmail: String,
    val password: String
)

data class RegisterRequestDto(
    val username: String,
    val email: String,
    val password: String,
    val rol: String = "USER"
)

data class UserDto(
    val id: String,
    val username: String,
    val email: String,
    val rol: String,
    val createdAt: String? = null
)

data class AuthResponseDto(
    val token: String,
    val user: UserDto
)

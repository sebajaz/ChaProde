package com.chaprode.mobile.model

data class User(
    val id: String,
    val username: String,
    val email: String,
    val rol: String
)

sealed class AuthState {
    object Unauthenticated : AuthState()
    object Loading : AuthState()
    data class Authenticated(val user: User, val token: String) : AuthState()
    data class Error(val message: String) : AuthState()
}

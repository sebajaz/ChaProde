package com.chaprode.mobile.data.repository

import com.chaprode.mobile.data.local.preferences.SessionManager
import com.chaprode.mobile.data.remote.api.AuthApiService
import com.chaprode.mobile.data.remote.dto.LoginRequestDto
import com.chaprode.mobile.data.remote.dto.RegisterRequestDto
import com.chaprode.mobile.model.Resource
import com.chaprode.mobile.model.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Repositorio de Autenticación (Single Source of Truth).
 * Coordina la comunicación con el Backend REST y la persistencia local (SessionManager / Room).
 */
class AuthRepository(
    private val apiService: AuthApiService = AuthApiService(),
    private val sessionManager: SessionManager = SessionManager()
) {

    fun login(usernameOrEmail: String, password: String): Flow<Resource<User>> = flow {
        emit(Resource.Loading)
        val result = apiService.login(LoginRequestDto(usernameOrEmail.trim(), password))
        result.fold(
            onSuccess = { response ->
                val user = User(
                    id = response.user.id,
                    username = response.user.username,
                    email = response.user.email,
                    rol = response.user.rol,
                    createdAt = response.user.createdAt
                )
                // Persistencia local (preparado para Room y SessionManager)
                sessionManager.saveSession(response.token, user)
                emit(Resource.Success(user))
            },
            onFailure = { error ->
                emit(Resource.Error(error.localizedMessage ?: "Error desconocido al iniciar sesión", error))
            }
        )
    }

    fun register(username: String, email: String, password: String): Flow<Resource<User>> = flow {
        emit(Resource.Loading)
        val result = apiService.register(RegisterRequestDto(username.trim(), email.trim(), password))
        result.fold(
            onSuccess = { response ->
                val user = User(
                    id = response.user.id,
                    username = response.user.username,
                    email = response.user.email,
                    rol = response.user.rol,
                    createdAt = response.user.createdAt
                )
                sessionManager.saveSession(response.token, user)
                emit(Resource.Success(user))
            },
            onFailure = { error ->
                emit(Resource.Error(error.localizedMessage ?: "Error desconocido al registrarse", error))
            }
        )
    }

    fun logout() {
        sessionManager.clearSession()
    }

    fun getCurrentUser(): User? = sessionManager.getUser()

    fun isLoggedIn(): Boolean = sessionManager.isLoggedIn()
}

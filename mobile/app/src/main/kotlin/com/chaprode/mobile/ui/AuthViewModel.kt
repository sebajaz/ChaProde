package com.chaprode.mobile.ui

import com.chaprode.mobile.data.AuthRepository
import com.chaprode.mobile.model.AuthState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel(
    private val authRepository: AuthRepository = AuthRepository(),
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Main)
) {
    val authState: StateFlow<AuthState> = authRepository.authState

    fun login(usernameOrEmail: String, password: String, onComplete: (Boolean) -> Unit = {}) {
        scope.launch {
            val result = authRepository.login(usernameOrEmail, password)
            onComplete(result.isSuccess)
        }
    }

    fun register(username: String, email: String, password: String, onComplete: (Boolean) -> Unit = {}) {
        scope.launch {
            val result = authRepository.register(username, email, password)
            onComplete(result.isSuccess)
        }
    }

    fun logout() {
        authRepository.logout()
    }
}

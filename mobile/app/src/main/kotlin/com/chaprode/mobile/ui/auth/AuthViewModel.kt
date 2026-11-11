package com.chaprode.mobile.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chaprode.mobile.data.repository.AuthRepository
import com.chaprode.mobile.model.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel de Autenticación:
 * Administra la máquina de estados reactiva de login y logout.
 * Coordina llamadas a `AuthRepository`, actualiza el flujo `StateFlow` y maneja
 * la persistencia automática de credenciales JWT en Android.
 */
class AuthViewModel(
    private val authRepository: AuthRepository = AuthRepository()
) : ViewModel() {

    // Flujo mutable privado que almacena el estado reactivo actual
    private val _uiState = MutableStateFlow(AuthUiState())
    // Exposición pública de solo lectura para ser consumida por Jetpack Compose
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        // ----------------------------------------------------------------------------------------
        // VERIFICACIÓN INICIAL DE SESIÓN ACTIVA
        // ----------------------------------------------------------------------------------------
        // Al instanciar el ViewModel, revisa si hay un usuario logueado en SessionManager.
        val currentUser = authRepository.getCurrentUser()
        if (currentUser != null && authRepository.isLoggedIn()) {
            _uiState.update { it.copy(user = currentUser, isSuccess = true) }
        }
    }

    /**
     * Procesador central de intenciones de la UI (MVI).
     * Cada acción del usuario es procesada de forma atómica actualizando el estado reactivo.
     */
    fun onEvent(event: AuthUiEvent) {
        when (event) {
            is AuthUiEvent.OnUsernameOrEmailChanged -> {
                _uiState.update { it.copy(usernameOrEmail = event.value, errorMessage = null) }
            }
            is AuthUiEvent.OnPasswordChanged -> {
                _uiState.update { it.copy(password = event.value, errorMessage = null) }
            }
            is AuthUiEvent.OnTogglePasswordVisibility -> {
                _uiState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
            }
            is AuthUiEvent.OnDismissError -> {
                _uiState.update { it.copy(errorMessage = null) }
            }
            is AuthUiEvent.OnLoginClicked -> {
                login()
            }
        }
    }

    private fun login() {
        val currentState = _uiState.value
        val identifier = currentState.usernameOrEmail.trim()
        val password = currentState.password

        if (identifier.isBlank() || password.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Por favor completa todos los campos.") }
            return
        }

        viewModelScope.launch {
            authRepository.login(identifier, password).collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
                    }
                    is Resource.Success -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                isSuccess = true,
                                user = resource.data,
                                errorMessage = null
                            )
                        }
                    }
                    is Resource.Error -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                isSuccess = false,
                                errorMessage = resource.message
                            )
                        }
                    }
                }
            }
        }
    }

    fun logout() {
        authRepository.logout()
        _uiState.value = AuthUiState()
    }
}

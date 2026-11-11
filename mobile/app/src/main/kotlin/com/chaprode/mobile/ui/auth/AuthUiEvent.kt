package com.chaprode.mobile.ui.auth

/**
 * Contrato de eventos de usuario (MVI Intentions) para el flujo de autenticación.
 * Modela todas las acciones posibles disparadas desde la UI hacia el AuthViewModel.
 */
sealed interface AuthUiEvent {
    /** Actualización del texto en el campo de usuario o correo electrónico. */
    data class OnUsernameOrEmailChanged(val value: String) : AuthUiEvent
    
    /** Actualización del texto en el campo de contraseña secreta. */
    data class OnPasswordChanged(val value: String) : AuthUiEvent
    
    /** Alterna la máscara de visualización de la contraseña (oculta/visible). */
    object OnTogglePasswordVisibility : AuthUiEvent
    
    /** Intención de enviar las credenciales para iniciar sesión. */
    object OnLoginClicked : AuthUiEvent
    
    /** Descarta el cartel de error activo en la interfaz. */
    object OnDismissError : AuthUiEvent
}

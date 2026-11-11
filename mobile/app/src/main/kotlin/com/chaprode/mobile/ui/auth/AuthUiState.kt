package com.chaprode.mobile.ui.auth

import com.chaprode.mobile.model.User

/**
 * Estado inmutable de la pantalla de autenticación.
 *
 * @param usernameOrEmail Valor digitado en el campo de usuario o email.
 * @param password Valor digitado en el campo de contraseña.
 * @param isPasswordVisible Indica si la contraseña se muestra en texto plano o con asteriscos.
 * @param isLoading Indica si una petición de red con la API REST está en progreso.
 * @param errorMessage Mensaje de error a presentar al usuario si falla la autenticación.
 * @param isSuccess Bandera que indica si el inicio de sesión se completó satisfactoriamente.
 * @param user Información del usuario autenticado devuelta por el servidor.
 */
data class AuthUiState(
    val usernameOrEmail: String = "",
    val password: String = "",
    val isPasswordVisible: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isSuccess: Boolean = false,
    val user: User? = null
) {
    /**
     * Propiedad computada que valida si el botón de inicio de sesión debe estar habilitado.
     * Requiere que ambos campos contengan texto y que no haya una solicitud activa en curso.
     */
    val isLoginButtonEnabled: Boolean
        get() = usernameOrEmail.isNotBlank() && password.isNotBlank() && !isLoading
}

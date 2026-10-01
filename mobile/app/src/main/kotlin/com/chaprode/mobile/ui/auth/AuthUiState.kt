package com.chaprode.mobile.ui.auth

import com.chaprode.mobile.model.User

data class AuthUiState(
    val usernameOrEmail: String = "",
    val password: String = "",
    val isPasswordVisible: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isSuccess: Boolean = false,
    val user: User? = null
) {
    val isLoginButtonEnabled: Boolean
        get() = usernameOrEmail.isNotBlank() && password.isNotBlank() && !isLoading
}

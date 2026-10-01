package com.chaprode.mobile.ui.auth

sealed interface AuthUiEvent {
    data class OnUsernameOrEmailChanged(val value: String) : AuthUiEvent
    data class OnPasswordChanged(val value: String) : AuthUiEvent
    object OnTogglePasswordVisibility : AuthUiEvent
    object OnLoginClicked : AuthUiEvent
    object OnDismissError : AuthUiEvent
}

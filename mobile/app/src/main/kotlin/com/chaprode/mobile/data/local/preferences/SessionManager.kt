package com.chaprode.mobile.data.local.preferences

import android.content.Context
import android.content.SharedPreferences
import com.chaprode.mobile.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SessionManager(context: Context? = null) {

    private val prefs: SharedPreferences? = context?.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    private val _currentToken = MutableStateFlow<String?>(prefs?.getString(KEY_TOKEN, null))
    val currentToken: StateFlow<String?> = _currentToken.asStateFlow()

    fun saveSession(token: String, user: User) {
        prefs?.edit()?.apply {
            putString(KEY_TOKEN, token)
            putString(KEY_USER_ID, user.id)
            putString(KEY_USERNAME, user.username)
            putString(KEY_EMAIL, user.email)
            putString(KEY_ROLE, user.rol)
            apply()
        }
        _currentToken.value = token
    }

    fun getUser(): User? {
        val id = prefs?.getString(KEY_USER_ID, null) ?: return null
        val username = prefs.getString(KEY_USERNAME, "") ?: ""
        val email = prefs.getString(KEY_EMAIL, "") ?: ""
        val role = prefs.getString(KEY_ROLE, "USER") ?: "USER"
        return User(id = id, username = username, email = email, rol = role)
    }

    fun clearSession() {
        prefs?.edit()?.clear()?.apply()
        _currentToken.value = null
    }

    fun isLoggedIn(): Boolean = !prefs?.getString(KEY_TOKEN, null).isNullOrBlank()

    companion object {
        private const val PREF_NAME = "chaprode_session_prefs"
        private const val KEY_TOKEN = "jwt_token"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USERNAME = "username"
        private const val KEY_EMAIL = "email"
        private const val KEY_ROLE = "role"
    }
}

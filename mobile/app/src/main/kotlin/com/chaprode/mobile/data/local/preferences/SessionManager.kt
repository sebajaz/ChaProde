package com.chaprode.mobile.data.local.preferences

import android.content.Context
import android.content.SharedPreferences
import com.chaprode.mobile.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SessionManager(context: Context? = null) {

    init {
        context?.let { init(it) }
    }

    val currentToken: StateFlow<String?> = Companion.currentToken
    val currentUser: StateFlow<User?> = Companion.currentUser

    fun saveSession(token: String, user: User) {
        Companion.saveSession(token, user)
    }

    fun getUser(): User? {
        return Companion.getUser()
    }

    fun clearSession() {
        Companion.clearSession()
    }

    fun isLoggedIn(): Boolean = Companion.isLoggedIn()

    companion object {
        private const val PREF_NAME = "chaprode_session_prefs"
        private const val KEY_TOKEN = "jwt_token"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USERNAME = "username"
        private const val KEY_EMAIL = "email"
        private const val KEY_ROLE = "role"

        private var sharedPrefs: SharedPreferences? = null

        private val _currentToken = MutableStateFlow<String?>(null)
        val currentToken: StateFlow<String?> = _currentToken.asStateFlow()

        private val _currentUser = MutableStateFlow<User?>(null)
        val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

        fun init(context: Context) {
            if (sharedPrefs == null) {
                sharedPrefs = context.applicationContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                val savedToken = sharedPrefs?.getString(KEY_TOKEN, null)
                val savedId = sharedPrefs?.getString(KEY_USER_ID, null)
                if (!savedToken.isNullOrBlank() && !savedId.isNullOrBlank()) {
                    val user = User(
                        id = savedId,
                        username = sharedPrefs?.getString(KEY_USERNAME, "") ?: "",
                        email = sharedPrefs?.getString(KEY_EMAIL, "") ?: "",
                        rol = sharedPrefs?.getString(KEY_ROLE, "USER") ?: "USER"
                    )
                    _currentToken.value = savedToken
                    _currentUser.value = user
                }
            }
        }

        fun saveSession(token: String, user: User) {
            sharedPrefs?.edit()?.apply {
                putString(KEY_TOKEN, token)
                putString(KEY_USER_ID, user.id)
                putString(KEY_USERNAME, user.username)
                putString(KEY_EMAIL, user.email)
                putString(KEY_ROLE, user.rol)
                apply()
            }
            _currentToken.value = token
            _currentUser.value = user
        }

        fun getUser(): User? {
            if (_currentUser.value != null) return _currentUser.value
            val id = sharedPrefs?.getString(KEY_USER_ID, null) ?: return null
            val username = sharedPrefs?.getString(KEY_USERNAME, "") ?: ""
            val email = sharedPrefs?.getString(KEY_EMAIL, "") ?: ""
            val role = sharedPrefs?.getString(KEY_ROLE, "USER") ?: "USER"
            val user = User(id = id, username = username, email = email, rol = role)
            _currentUser.value = user
            return user
        }

        fun clearSession() {
            sharedPrefs?.edit()?.clear()?.apply()
            _currentToken.value = null
            _currentUser.value = null
        }

        fun isLoggedIn(): Boolean {
            return !_currentToken.value.isNullOrBlank() || !sharedPrefs?.getString(KEY_TOKEN, null).isNullOrBlank()
        }
    }
}

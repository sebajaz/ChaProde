package com.chaprode.mobile.data.local.preferences

import android.content.Context
import android.content.SharedPreferences
import com.chaprode.mobile.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * =========================================================================================
 * SessionManager: Gestor de Persistencia y Estado de Autenticación de Usuario.
 * =========================================================================================
 * Propósito:
 * Administra el ciclo de vida de la sesión activa del usuario en el dispositivo.
 * 
 * Funcionalidades clave:
 * 1. Persistencia permanente: Almacena en [SharedPreferences] el token JWT y el perfil básico del usuario
 *    (ID, nombre, correo y rol) para que no sea necesario iniciar sesión cada vez que se abre la app.
 * 2. Estado reactivo en memoria: Expone StateFlows ([currentToken], [currentUser]) para que las vistas
 *    Compose y los ViewModels reaccionen inmediatamente al iniciar o cerrar sesión.
 * 3. Doble interfaz: Puede usarse como instancia (`SessionManager()`) o de forma estática (`SessionManager.Companion`).
 */
class SessionManager(context: Context? = null) {

    init {
        context?.let { init(it) }
    }

    /** Flujo reactivo con el token JWT de la sesión activa */
    val currentToken: StateFlow<String?> = Companion.currentToken

    /** Flujo reactivo con el objeto de usuario [User] autenticado */
    val currentUser: StateFlow<User?> = Companion.currentUser

    /**
     * Guarda el token JWT y los datos del usuario en la sesión local.
     */
    fun saveSession(token: String, user: User) {
        Companion.saveSession(token, user)
    }

    /**
     * Recupera el usuario actualmente autenticado o null si no hay sesión activa.
     */
    fun getUser(): User? {
        return Companion.getUser()
    }

    /**
     * Cierra la sesión activa borrando los datos persistidos y limpiando el estado reactivo en memoria.
     */
    fun clearSession() {
        Companion.clearSession()
    }

    /**
     * Indica si el usuario está autenticado en este momento.
     */
    fun isLoggedIn(): Boolean = Companion.isLoggedIn()

    companion object {
        private const val PREF_NAME = "chaprode_session_prefs"
        private const val KEY_TOKEN = "jwt_token"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USERNAME = "username"
        private const val KEY_EMAIL = "email"
        private const val KEY_ROLE = "role"

        private var sharedPrefs: SharedPreferences? = null

        // Estado reactivo mutable interno para el token
        private val _currentToken = MutableStateFlow<String?>(null)
        /** Flujo inmutable expuesto públicamente para observar cambios en el token JWT */
        val currentToken: StateFlow<String?> = _currentToken.asStateFlow()

        // Estado reactivo mutable interno para el usuario
        private val _currentUser = MutableStateFlow<User?>(null)
        /** Flujo inmutable expuesto públicamente para observar cambios en el usuario autenticado */
        val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

        /**
         * Inicializa las preferencias compartidas y restaura la sesión previa si existía en disco.
         * Se llama una sola vez al inicio de la aplicación en [com.chaprode.mobile.MainActivity.onCreate].
         */
        fun init(context: Context) {
            if (sharedPrefs == null) {
                sharedPrefs = context.applicationContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                val savedToken = sharedPrefs?.getString(KEY_TOKEN, null)
                val savedId = sharedPrefs?.getString(KEY_USER_ID, null)
                // Si encontramos un token y un ID válidos en disco, restauramos la sesión automáticamente
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

        /**
         * Guarda de forma atómica en disco el token JWT y los atributos del usuario,
         * y actualiza de inmediato los StateFlows en memoria.
         */
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

        /**
         * Obtiene el usuario autenticado. Si está cargado en memoria lo retorna directamente;
         * si no, intenta leerlo desde las preferencias guardadas en disco.
         */
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

        /**
         * Elimina todas las claves de sesión en disco y reinicia los estados en memoria a null.
         */
        fun clearSession() {
            sharedPrefs?.edit()?.clear()?.apply()
            _currentToken.value = null
            _currentUser.value = null
        }

        /**
         * Verifica si hay una sesión activa analizando la memoria o las preferencias en disco.
         */
        fun isLoggedIn(): Boolean {
            return !_currentToken.value.isNullOrBlank() || !sharedPrefs?.getString(KEY_TOKEN, null).isNullOrBlank()
        }
    }
}

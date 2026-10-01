package com.chaprode.mobile.data

import com.chaprode.mobile.model.AuthState
import com.chaprode.mobile.model.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

class AuthRepository(
    private val baseUrl: String = "http://10.0.2.2:8080" // 10.0.2.2 para emulador Android, 127.0.0.1 para local
) {
    private val _authState = MutableStateFlow<AuthState>(AuthState.Unauthenticated)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    var currentToken: String? = null
        private set

    suspend fun register(username: String, email: String, password: String): Result<User> = withContext(Dispatchers.IO) {
        _authState.value = AuthState.Loading
        try {
            val url = URL("$baseUrl/api/auth/register")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                setRequestProperty("Content-Type", "application/json")
                doOutput = true
                doInput = true
            }

            val payload = JSONObject().apply {
                put("username", username)
                put("email", email)
                put("password", password)
                put("rol", "USER")
            }

            OutputStreamWriter(conn.outputStream).use { it.write(payload.toString()) }

            val responseCode = conn.responseCode
            val stream = if (responseCode in 200..299) conn.inputStream else conn.errorStream
            val responseText = BufferedReader(InputStreamReader(stream)).use { it.readText() }
            val json = JSONObject(responseText)

            if (responseCode in 200..299 && json.getBoolean("success")) {
                val data = json.getJSONObject("data")
                val token = data.getString("token")
                val userJson = data.getJSONObject("user")
                val user = User(
                    id = userJson.getString("id"),
                    username = userJson.getString("username"),
                    email = userJson.getString("email"),
                    rol = userJson.getString("rol")
                )
                currentToken = token
                _authState.value = AuthState.Authenticated(user, token)
                Result.success(user)
            } else {
                val errorMsg = json.optString("error", "Error al registrar usuario")
                _authState.value = AuthState.Error(errorMsg)
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            val errorMsg = e.localizedMessage ?: "Error de conexión"
            _authState.value = AuthState.Error(errorMsg)
            Result.failure(e)
        }
    }

    suspend fun login(usernameOrEmail: String, password: String): Result<User> = withContext(Dispatchers.IO) {
        _authState.value = AuthState.Loading
        try {
            val url = URL("$baseUrl/api/auth/login")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                setRequestProperty("Content-Type", "application/json")
                doOutput = true
                doInput = true
            }

            val payload = JSONObject().apply {
                put("usernameOrEmail", usernameOrEmail)
                put("password", password)
            }

            OutputStreamWriter(conn.outputStream).use { it.write(payload.toString()) }

            val responseCode = conn.responseCode
            val stream = if (responseCode in 200..299) conn.inputStream else conn.errorStream
            val responseText = BufferedReader(InputStreamReader(stream)).use { it.readText() }
            val json = JSONObject(responseText)

            if (responseCode in 200..299 && json.getBoolean("success")) {
                val data = json.getJSONObject("data")
                val token = data.getString("token")
                val userJson = data.getJSONObject("user")
                val user = User(
                    id = userJson.getString("id"),
                    username = userJson.getString("username"),
                    email = userJson.getString("email"),
                    rol = userJson.getString("rol")
                )
                currentToken = token
                _authState.value = AuthState.Authenticated(user, token)
                Result.success(user)
            } else {
                val errorMsg = json.optString("error", "Credenciales incorrectas")
                _authState.value = AuthState.Error(errorMsg)
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            val errorMsg = e.localizedMessage ?: "Error de conexión"
            _authState.value = AuthState.Error(errorMsg)
            Result.failure(e)
        }
    }

    fun logout() {
        currentToken = null
        _authState.value = AuthState.Unauthenticated
    }
}

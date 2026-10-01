package com.chaprode.mobile.data.remote.api

import com.chaprode.mobile.data.remote.dto.AuthResponseDto
import com.chaprode.mobile.data.remote.dto.LoginRequestDto
import com.chaprode.mobile.data.remote.dto.RegisterRequestDto
import com.chaprode.mobile.data.remote.dto.UserDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

class AuthApiService(
    private val baseUrl: String = "http://10.0.2.2:8080"
) {

    suspend fun login(request: LoginRequestDto): Result<AuthResponseDto> = withContext(Dispatchers.IO) {
        try {
            val url = URL("$baseUrl/api/auth/login")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                setRequestProperty("Content-Type", "application/json")
                connectTimeout = 8000
                readTimeout = 8000
                doOutput = true
                doInput = true
            }

            val payload = JSONObject().apply {
                put("usernameOrEmail", request.usernameOrEmail)
                put("password", request.password)
            }

            OutputStreamWriter(conn.outputStream).use { it.write(payload.toString()) }

            val responseCode = conn.responseCode
            val stream = if (responseCode in 200..299) conn.inputStream else conn.errorStream
            val responseText = BufferedReader(InputStreamReader(stream)).use { it.readText() }
            val json = JSONObject(responseText)

            if (responseCode in 200..299 && json.getBoolean("success")) {
                val data = json.getJSONObject("data")
                val token = data.getString("token")
                val uJson = data.getJSONObject("user")
                val userDto = UserDto(
                    id = uJson.getString("id"),
                    username = uJson.getString("username"),
                    email = uJson.getString("email"),
                    rol = uJson.getString("rol"),
                    createdAt = if (uJson.has("createdAt") && !uJson.isNull("createdAt")) uJson.getString("createdAt") else null
                )
                Result.success(AuthResponseDto(token = token, user = userDto))
            } else {
                val errorMsg = json.optString("error", "Error al iniciar sesión")
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun register(request: RegisterRequestDto): Result<AuthResponseDto> = withContext(Dispatchers.IO) {
        try {
            val url = URL("$baseUrl/api/auth/register")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                setRequestProperty("Content-Type", "application/json")
                connectTimeout = 8000
                readTimeout = 8000
                doOutput = true
                doInput = true
            }

            val payload = JSONObject().apply {
                put("username", request.username)
                put("email", request.email)
                put("password", request.password)
                put("rol", request.rol)
            }

            OutputStreamWriter(conn.outputStream).use { it.write(payload.toString()) }

            val responseCode = conn.responseCode
            val stream = if (responseCode in 200..299) conn.inputStream else conn.errorStream
            val responseText = BufferedReader(InputStreamReader(stream)).use { it.readText() }
            val json = JSONObject(responseText)

            if (responseCode in 200..299 && json.getBoolean("success")) {
                val data = json.getJSONObject("data")
                val token = data.getString("token")
                val uJson = data.getJSONObject("user")
                val userDto = UserDto(
                    id = uJson.getString("id"),
                    username = uJson.getString("username"),
                    email = uJson.getString("email"),
                    rol = uJson.getString("rol"),
                    createdAt = if (uJson.has("createdAt") && !uJson.isNull("createdAt")) uJson.getString("createdAt") else null
                )
                Result.success(AuthResponseDto(token = token, user = userDto))
            } else {
                val errorMsg = json.optString("error", "Error al registrarse")
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

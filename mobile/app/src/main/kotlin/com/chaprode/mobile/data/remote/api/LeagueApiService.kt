package com.chaprode.mobile.data.remote.api

import com.chaprode.mobile.model.LeagueDetailItem
import com.chaprode.mobile.model.LeagueItem
import com.chaprode.mobile.model.RankingUserItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import com.chaprode.mobile.data.remote.ApiConfig
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

class LeagueApiService(
    private val baseUrl: String = ApiConfig.BASE_URL
) {

    suspend fun getMyLeagues(token: String): Result<List<LeagueItem>> = withContext(Dispatchers.IO) {
        try {
            val url = URL("$baseUrl/api/ligas/mis-ligas")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                setRequestProperty("Content-Type", "application/json")
                setRequestProperty("Authorization", "Bearer $token")
                connectTimeout = 8000
                readTimeout = 8000
            }

            val responseCode = conn.responseCode
            val stream = if (responseCode in 200..299) conn.inputStream else conn.errorStream
            val responseText = BufferedReader(InputStreamReader(stream)).use { it.readText() }
            val json = JSONObject(responseText)

            if (responseCode in 200..299 && json.getBoolean("success")) {
                val array = json.getJSONArray("data")
                val list = mutableListOf<LeagueItem>()
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    list.add(parseLeagueItem(obj))
                }
                Result.success(list)
            } else {
                val errorMsg = json.optString("error", "Error al obtener ligas")
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createLeague(nombre: String, torneoId: String, token: String): Result<LeagueItem> = withContext(Dispatchers.IO) {
        try {
            val url = URL("$baseUrl/api/ligas")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                setRequestProperty("Content-Type", "application/json")
                setRequestProperty("Authorization", "Bearer $token")
                connectTimeout = 8000
                readTimeout = 8000
                doOutput = true
                doInput = true
            }

            val payload = JSONObject().apply {
                put("nombre", nombre)
                put("torneoId", torneoId)
            }

            OutputStreamWriter(conn.outputStream).use { it.write(payload.toString()) }

            val responseCode = conn.responseCode
            val stream = if (responseCode in 200..299) conn.inputStream else conn.errorStream
            val responseText = BufferedReader(InputStreamReader(stream)).use { it.readText() }
            val json = JSONObject(responseText)

            if (responseCode in 200..299 && json.getBoolean("success")) {
                val obj = json.getJSONObject("data")
                Result.success(parseLeagueItem(obj))
            } else {
                val errorMsg = json.optString("error", "Error al crear liga")
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun joinLeague(codigoAcceso: String, token: String): Result<LeagueItem> = withContext(Dispatchers.IO) {
        try {
            val url = URL("$baseUrl/api/ligas/unirse")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                setRequestProperty("Content-Type", "application/json")
                setRequestProperty("Authorization", "Bearer $token")
                connectTimeout = 8000
                readTimeout = 8000
                doOutput = true
                doInput = true
            }

            val payload = JSONObject().apply {
                put("codigoAcceso", codigoAcceso)
            }

            OutputStreamWriter(conn.outputStream).use { it.write(payload.toString()) }

            val responseCode = conn.responseCode
            val stream = if (responseCode in 200..299) conn.inputStream else conn.errorStream
            val responseText = BufferedReader(InputStreamReader(stream)).use { it.readText() }
            val json = JSONObject(responseText)

            if (responseCode in 200..299 && json.getBoolean("success")) {
                val obj = json.getJSONObject("data")
                Result.success(parseLeagueItem(obj))
            } else {
                val errorMsg = json.optString("error", "Error al unirse a la liga")
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getLeagueDetail(leagueId: String, token: String, currentUserId: String? = null): Result<LeagueDetailItem> = withContext(Dispatchers.IO) {
        try {
            val url = URL("$baseUrl/api/ligas/$leagueId")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                setRequestProperty("Content-Type", "application/json")
                setRequestProperty("Authorization", "Bearer $token")
                connectTimeout = 8000
                readTimeout = 8000
            }

            val responseCode = conn.responseCode
            val stream = if (responseCode in 200..299) conn.inputStream else conn.errorStream
            val responseText = BufferedReader(InputStreamReader(stream)).use { it.readText() }
            val json = JSONObject(responseText)

            if (responseCode in 200..299 && json.getBoolean("success")) {
                val data = json.getJSONObject("data")
                val leagueObj = data.getJSONObject("liga")
                val rankingArray = data.getJSONArray("ranking")

                val leagueItem = parseLeagueItem(leagueObj)
                val rankingList = mutableListOf<RankingUserItem>()

                for (i in 0 until rankingArray.length()) {
                    val item = rankingArray.getJSONObject(i)
                    val uid = item.getString("usuarioId")
                    rankingList.add(
                        RankingUserItem(
                            posicion = item.getInt("posicion"),
                            usuarioId = uid,
                            username = item.getString("username"),
                            puntosTotales = item.getInt("puntosTotales"),
                            plenosExactos = item.getInt("plenosExactos"),
                            aciertosTendencia = item.getInt("aciertosTendencia"),
                            pronosticosTotales = item.getInt("pronosticosTotales"),
                            isCurrentUser = (uid == currentUserId)
                        )
                    )
                }

                Result.success(LeagueDetailItem(liga = leagueItem, ranking = rankingList))
            } else {
                val errorMsg = json.optString("error", "Error al obtener detalle de la liga")
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun parseLeagueItem(obj: JSONObject): LeagueItem = LeagueItem(
        id = obj.getString("id"),
        nombre = obj.getString("nombre"),
        codigoAcceso = obj.getString("codigoAcceso"),
        creadorId = obj.getString("creadorId"),
        creadorUsername = obj.getString("creadorUsername"),
        torneoId = obj.getString("torneoId"),
        torneoNombre = obj.getString("torneoNombre"),
        totalMiembros = obj.getInt("totalMiembros"),
        createdAt = obj.getString("createdAt")
    )
}

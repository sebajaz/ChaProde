package com.chaprode.mobile.data.remote.api

import com.chaprode.mobile.model.RankingUserItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class RankingApiService(
    private val baseUrl: String = "http://10.0.2.2:8080"
) {

    suspend fun getTournamentRanking(torneoId: String, currentUserId: String? = null): Result<List<RankingUserItem>> = withContext(Dispatchers.IO) {
        try {
            val url = URL("$baseUrl/api/torneos/$torneoId/ranking")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                setRequestProperty("Content-Type", "application/json")
                connectTimeout = 8000
                readTimeout = 8000
            }

            val responseCode = conn.responseCode
            val stream = if (responseCode in 200..299) conn.inputStream else conn.errorStream
            val responseText = BufferedReader(InputStreamReader(stream)).use { it.readText() }
            val json = JSONObject(responseText)

            if (responseCode in 200..299 && json.getBoolean("success")) {
                val dataObj = json.getJSONObject("data")
                val rankingArray = dataObj.getJSONArray("ranking")
                val list = mutableListOf<RankingUserItem>()

                for (i in 0 until rankingArray.length()) {
                    val item = rankingArray.getJSONObject(i)
                    val uid = item.getString("usuarioId")
                    list.add(
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
                Result.success(list)
            } else {
                val errorMsg = json.optString("error", "Error al obtener ranking")
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

package com.chaprode.mobile.data.remote.api

import com.chaprode.mobile.model.MatchWithPredictionItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

class PredictionApiService(
    private val baseUrl: String = "http://10.0.2.2:8080"
) {

    suspend fun getMatchesWithPredictions(torneoId: String, token: String?): Result<List<MatchWithPredictionItem>> = withContext(Dispatchers.IO) {
        try {
            val url = URL("$baseUrl/api/torneos/$torneoId/partidos-con-pronosticos")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                setRequestProperty("Content-Type", "application/json")
                if (!token.isNullOrBlank()) {
                    setRequestProperty("Authorization", "Bearer $token")
                }
                connectTimeout = 8000
                readTimeout = 8000
            }

            val responseCode = conn.responseCode
            val stream = if (responseCode in 200..299) conn.inputStream else conn.errorStream
            val responseText = BufferedReader(InputStreamReader(stream)).use { it.readText() }
            val json = JSONObject(responseText)

            if (responseCode in 200..299 && json.getBoolean("success")) {
                val dataArray = json.getJSONArray("data")
                val list = mutableListOf<MatchWithPredictionItem>()

                for (i in 0 until dataArray.length()) {
                    val itemJson = dataArray.getJSONObject(i)
                    val matchJson = itemJson.getJSONObject("match")
                    val local = matchJson.getJSONObject("equipoLocal")
                    val visitante = matchJson.getJSONObject("equipoVisitante")

                    val predJson = if (itemJson.isNull("myPrediction")) null else itemJson.getJSONObject("myPrediction")
                    val predLocal = predJson?.optInt("golesLocalPredicho", -1)?.takeIf { it >= 0 }
                    val predVisitante = predJson?.optInt("golesVisitantePredicho", -1)?.takeIf { it >= 0 }
                    val puntos = predJson?.optInt("puntosGanados", 0) ?: 0

                    val item = MatchWithPredictionItem(
                        id = matchJson.getString("id"),
                        localNombre = local.getString("nombre"),
                        localBandera = local.optString("urlBandera", ""),
                        visitanteNombre = visitante.getString("nombre"),
                        visitanteBandera = visitante.optString("urlBandera", ""),
                        fechaHora = matchJson.getString("fechaPartido"),
                        estado = matchJson.getString("estado"),
                        golesLocalReal = if (matchJson.isNull("golesLocal")) null else matchJson.getInt("golesLocal"),
                        golesVisitanteReal = if (matchJson.isNull("golesVisitante")) null else matchJson.getInt("golesVisitante"),
                        miPronosticoLocal = predLocal,
                        miPronosticoVisitante = predVisitante,
                        puntosGanados = puntos,
                        cerrado = itemJson.optBoolean("cerrado", false),
                        minutosRestantesParaCierre = itemJson.optLong("minutosRestantesParaCierre", 0),
                        torneoNombre = matchJson.optString("torneoNombre", "").takeIf { it.isNotBlank() },
                        torneoCodigo = matchJson.optString("torneoCodigo", "").takeIf { it.isNotBlank() },
                        torneoLogoUrl = matchJson.optString("torneoLogoUrl", "").takeIf { it.isNotBlank() },
                        editedGolesLocal = predLocal ?: 0,
                        editedGolesVisitante = predVisitante ?: 0,
                        isEdited = false
                    )
                    list.add(item)
                }
                Result.success(list)
            } else {
                val errorMsg = json.optString("error", "Error al obtener partidos con pronósticos")
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun submitPrediction(
        partidoId: String,
        golesLocal: Int,
        golesVisitante: Int,
        token: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val url = URL("$baseUrl/api/pronosticos")
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
                put("partidoId", partidoId)
                put("golesLocal", golesLocal)
                put("golesVisitante", golesVisitante)
            }

            OutputStreamWriter(conn.outputStream).use { it.write(payload.toString()) }

            val responseCode = conn.responseCode
            val stream = if (responseCode in 200..299) conn.inputStream else conn.errorStream
            val responseText = BufferedReader(InputStreamReader(stream)).use { it.readText() }
            val json = JSONObject(responseText)

            if (responseCode in 200..299 && json.getBoolean("success")) {
                Result.success(Unit)
            } else {
                val errorMsg = json.optString("error", "Error al guardar pronóstico")
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

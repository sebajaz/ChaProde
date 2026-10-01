package com.chaprode.mobile.data

import com.chaprode.mobile.model.PartidoItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class FixtureRepository(
    private val baseUrl: String = "http://10.0.2.2:8080"
) {

    suspend fun getMatches(torneoId: String): Result<List<PartidoItem>> = withContext(Dispatchers.IO) {
        try {
            val url = URL("$baseUrl/api/torneos/$torneoId/partidos")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                setRequestProperty("Content-Type", "application/json")
            }

            val responseCode = conn.responseCode
            val stream = if (responseCode in 200..299) conn.inputStream else conn.errorStream
            val responseText = BufferedReader(InputStreamReader(stream)).use { it.readText() }
            val json = JSONObject(responseText)

            if (responseCode in 200..299 && json.getBoolean("success")) {
                val dataArray = json.getJSONArray("data")
                val list = mutableListOf<PartidoItem>()

                for (i in 0 until dataArray.length()) {
                    val m = dataArray.getJSONObject(i)
                    val local = m.getJSONObject("equipoLocal")
                    val visitante = m.getJSONObject("equipoVisitante")

                    list.add(
                        PartidoItem(
                            id = m.getString("id"),
                            localNombre = local.getString("nombre"),
                            localBandera = local.optString("urlBandera", ""),
                            visitanteNombre = visitante.getString("nombre"),
                            visitanteBandera = visitante.optString("urlBandera", ""),
                            fechaHora = m.getString("fechaPartido"),
                            estado = m.getString("estado"),
                            golesLocal = if (m.isNull("golesLocal")) null else m.getInt("golesLocal"),
                            golesVisitante = if (m.isNull("golesVisitante")) null else m.getInt("golesVisitante")
                        )
                    )
                }
                Result.success(list)
            } else {
                Result.failure(Exception(json.optString("error", "Error al obtener fixture")))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

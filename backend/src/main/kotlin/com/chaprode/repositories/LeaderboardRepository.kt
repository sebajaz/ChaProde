package com.chaprode.repositories

import com.chaprode.config.DatabaseFactory.dbQuery
import com.chaprode.dto.LeaderboardEntryDto
import org.jetbrains.exposed.sql.transactions.TransactionManager
import java.util.*

class LeaderboardRepository {

    suspend fun getLeaderboardByTournament(torneoId: UUID): List<LeaderboardEntryDto> = dbQuery {
        val sql = """
            SELECT 
                u.id AS usuario_id,
                u.username AS username,
                COALESCE(SUM(p.puntos_ganados), 0) AS puntos_totales,
                COUNT(CASE WHEN p.puntos_ganados = 3 THEN 1 END) AS plenos_exactos,
                COUNT(CASE WHEN p.puntos_ganados = 1 THEN 1 END) AS aciertos_tendencia,
                COUNT(p.id) AS pronosticos_totales
            FROM usuarios u
            INNER JOIN pronosticos p ON p.usuario_id = u.id
            INNER JOIN partidos m ON m.id = p.partido_id
            WHERE m.torneo_id = '$torneoId'
            GROUP BY u.id, u.username
            ORDER BY puntos_totales DESC, plenos_exactos DESC, username ASC
        """.trimIndent()

        val results = mutableListOf<LeaderboardEntryDto>()
        TransactionManager.current().exec(sql) { rs ->
            var pos = 1
            while (rs.next()) {
                results.add(
                    LeaderboardEntryDto(
                        posicion = pos++,
                        usuarioId = rs.getString("usuario_id"),
                        username = rs.getString("username"),
                        puntosTotales = rs.getInt("puntos_totales"),
                        plenosExactos = rs.getInt("plenos_exactos"),
                        aciertosTendencia = rs.getInt("aciertos_tendencia"),
                        pronosticosTotales = rs.getInt("pronosticos_totales")
                    )
                )
            }
        }
        results
    }

    suspend fun getGlobalLeaderboard(): List<LeaderboardEntryDto> = dbQuery {
        val sql = """
            SELECT 
                u.id AS usuario_id,
                u.username AS username,
                COALESCE(SUM(p.puntos_ganados), 0) AS puntos_totales,
                COUNT(CASE WHEN p.puntos_ganados = 3 THEN 1 END) AS plenos_exactos,
                COUNT(CASE WHEN p.puntos_ganados = 1 THEN 1 END) AS aciertos_tendencia,
                COUNT(p.id) AS pronosticos_totales
            FROM usuarios u
            INNER JOIN pronosticos p ON p.usuario_id = u.id
            GROUP BY u.id, u.username
            ORDER BY puntos_totales DESC, plenos_exactos DESC, username ASC
        """.trimIndent()

        val results = mutableListOf<LeaderboardEntryDto>()
        TransactionManager.current().exec(sql) { rs ->
            var pos = 1
            while (rs.next()) {
                results.add(
                    LeaderboardEntryDto(
                        posicion = pos++,
                        usuarioId = rs.getString("usuario_id"),
                        username = rs.getString("username"),
                        puntosTotales = rs.getInt("puntos_totales"),
                        plenosExactos = rs.getInt("plenos_exactos"),
                        aciertosTendencia = rs.getInt("aciertos_tendencia"),
                        pronosticosTotales = rs.getInt("pronosticos_totales")
                    )
                )
            }
        }
        results
    }
}

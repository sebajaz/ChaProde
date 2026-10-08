package com.chaprode.repositories

import com.chaprode.config.DatabaseFactory.dbQuery
import com.chaprode.dto.LeaderboardEntryDto
import com.chaprode.dto.LeagueDto
import com.chaprode.models.LigasPrivadasTable
import com.chaprode.models.MiembrosLigaTable
import com.chaprode.models.TorneosTable
import com.chaprode.models.UsuariosTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.TransactionManager
import java.util.*

class LeagueRepository {

    suspend fun createLeague(
        nombre: String,
        creadorId: UUID,
        torneoId: UUID,
        codigoAcceso: String
    ): LeagueDto = dbQuery {
        val ligaId = LigasPrivadasTable.insertAndGetId {
            it[LigasPrivadasTable.nombre] = nombre
            it[LigasPrivadasTable.creadorId] = creadorId
            it[LigasPrivadasTable.torneoId] = torneoId
            it[LigasPrivadasTable.codigoAcceso] = codigoAcceso
            it[createdAt] = kotlinx.datetime.Clock.System.now()
        }.value

        // Agregar al creador automáticamente como miembro fundador
        MiembrosLigaTable.insert {
            it[MiembrosLigaTable.ligaId] = ligaId
            it[MiembrosLigaTable.usuarioId] = creadorId
            it[fechaIngreso] = kotlinx.datetime.Clock.System.now()
        }

        getLeagueByIdInternal(ligaId)!!
    }

    suspend fun joinLeague(usuarioId: UUID, codigoAcceso: String): LeagueDto = dbQuery {
        val leagueRow = LigasPrivadasTable
            .selectAll()
            .where { LigasPrivadasTable.codigoAcceso eq codigoAcceso }
            .singleOrNull() ?: throw IllegalArgumentException("No se encontró ninguna liga con el código proporcionado.")

        val ligaId = leagueRow[LigasPrivadasTable.id].value

        val alreadyMember = MiembrosLigaTable
            .selectAll()
            .where { (MiembrosLigaTable.ligaId eq ligaId) and (MiembrosLigaTable.usuarioId eq usuarioId) }
            .count() > 0

        if (alreadyMember) {
            throw IllegalArgumentException("Ya eres miembro de esta liga privada.")
        }

        MiembrosLigaTable.insert {
            it[MiembrosLigaTable.ligaId] = ligaId
            it[MiembrosLigaTable.usuarioId] = usuarioId
            it[fechaIngreso] = kotlinx.datetime.Clock.System.now()
        }

        getLeagueByIdInternal(ligaId)!!
    }

    suspend fun getLeagueById(ligaId: UUID): LeagueDto? = dbQuery {
        getLeagueByIdInternal(ligaId)
    }

    suspend fun getLeaguesByUser(usuarioId: UUID): List<LeagueDto> = dbQuery {
        val leagues = (MiembrosLigaTable innerJoin LigasPrivadasTable)
            .selectAll()
            .where { MiembrosLigaTable.usuarioId eq usuarioId }
            .map { it[LigasPrivadasTable.id].value }

        leagues.mapNotNull { getLeagueByIdInternal(it) }
    }

    suspend fun isCodeAvailable(codigo: String): Boolean = dbQuery {
        LigasPrivadasTable
            .selectAll()
            .where { LigasPrivadasTable.codigoAcceso eq codigo }
            .count() == 0L
    }

    suspend fun getLeagueLeaderboard(ligaId: UUID): List<LeaderboardEntryDto> = dbQuery {
        val sql = """
            SELECT 
                u.id AS usuario_id,
                u.username AS username,
                COALESCE(SUM(p.puntos_ganados), 0) AS puntos_totales,
                COUNT(CASE WHEN p.puntos_ganados = 3 THEN 1 END) AS plenos_exactos,
                COUNT(CASE WHEN p.puntos_ganados = 1 THEN 1 END) AS aciertos_tendencia,
                COUNT(p.id) AS pronosticos_totales
            FROM usuarios u
            INNER JOIN miembros_liga ml ON ml.usuario_id = u.id AND ml.liga_id = '$ligaId'
            INNER JOIN ligas_privadas lp ON lp.id = ml.liga_id
            LEFT JOIN pronosticos p ON p.usuario_id = u.id
            LEFT JOIN partidos m ON m.id = p.partido_id AND m.torneo_id = lp.torneo_id
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

    private fun getLeagueByIdInternal(ligaId: UUID): LeagueDto? {
        val row = (LigasPrivadasTable innerJoin UsuariosTable innerJoin TorneosTable)
            .selectAll()
            .where { LigasPrivadasTable.id eq ligaId }
            .singleOrNull() ?: return null

        val membersCount = MiembrosLigaTable
            .selectAll()
            .where { MiembrosLigaTable.ligaId eq ligaId }
            .count()
            .toInt()

        return LeagueDto(
            id = row[LigasPrivadasTable.id].value.toString(),
            nombre = row[LigasPrivadasTable.nombre],
            codigoAcceso = row[LigasPrivadasTable.codigoAcceso],
            creadorId = row[LigasPrivadasTable.creadorId].value.toString(),
            creadorUsername = row[UsuariosTable.username],
            torneoId = row[LigasPrivadasTable.torneoId].value.toString(),
            torneoNombre = row[TorneosTable.nombre],
            totalMiembros = membersCount,
            createdAt = row[LigasPrivadasTable.createdAt].toString()
        )
    }
}

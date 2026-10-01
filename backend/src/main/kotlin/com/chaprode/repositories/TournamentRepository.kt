package com.chaprode.repositories

import com.chaprode.config.DatabaseFactory.dbQuery
import com.chaprode.dto.TeamDto
import com.chaprode.dto.TournamentDto
import com.chaprode.models.EquiposTable
import com.chaprode.models.PartidosTable
import com.chaprode.models.TorneoEquiposTable
import com.chaprode.models.TorneosTable
import kotlinx.datetime.Instant
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import java.util.*

class TournamentRepository {

    private fun ResultRow.toTeamDto(): TeamDto = TeamDto(
        id = this[EquiposTable.id].value.toString(),
        nombre = this[EquiposTable.nombre],
        codigoExterno = this[EquiposTable.codigoExterno],
        urlBandera = this[EquiposTable.urlBandera],
        codigoIso = this[EquiposTable.codigoIso]
    )

    private fun ResultRow.toTournamentDto(totalEquipos: Int = 0, totalPartidos: Int = 0): TournamentDto = TournamentDto(
        id = this[TorneosTable.id].value.toString(),
        nombre = this[TorneosTable.nombre],
        codigoExterno = this[TorneosTable.codigoExterno],
        logoUrl = this[TorneosTable.logoUrl],
        activo = this[TorneosTable.activo],
        fechaInicio = this[TorneosTable.fechaInicio]?.toString(),
        fechaFin = this[TorneosTable.fechaFin]?.toString(),
        totalEquipos = totalEquipos,
        totalPartidos = totalPartidos
    )

    suspend fun getAllActiveTournaments(): List<TournamentDto> = dbQuery {
        TorneosTable
            .selectAll()
            .where { TorneosTable.activo eq true }
            .orderBy(TorneosTable.fechaInicio, SortOrder.ASC)
            .map { row ->
                val torneoId = row[TorneosTable.id]
                val eqCount = TorneoEquiposTable.selectAll().where { TorneoEquiposTable.torneoId eq torneoId }.count().toInt()
                val partCount = PartidosTable.selectAll().where { PartidosTable.torneoId eq torneoId }.count().toInt()
                row.toTournamentDto(eqCount, partCount)
            }
    }

    suspend fun getTournamentById(id: UUID): TournamentDto? = dbQuery {
        val row = TorneosTable.selectAll().where { TorneosTable.id eq id }.singleOrNull() ?: return@dbQuery null
        val eqCount = TorneoEquiposTable.selectAll().where { TorneoEquiposTable.torneoId eq id }.count().toInt()
        val partCount = PartidosTable.selectAll().where { PartidosTable.torneoId eq id }.count().toInt()
        row.toTournamentDto(eqCount, partCount)
    }

    suspend fun findTournamentByCodigoExterno(codigo: String): TournamentDto? = dbQuery {
        TorneosTable
            .selectAll()
            .where { TorneosTable.codigoExterno eq codigo }
            .map { it.toTournamentDto() }
            .singleOrNull()
    }

    suspend fun createTournament(
        nombre: String,
        codigoExterno: String? = null,
        logoUrl: String? = null,
        fechaInicio: Instant? = null,
        fechaFin: Instant? = null
    ): TournamentDto = dbQuery {
        val id = TorneosTable.insertAndGetId {
            it[TorneosTable.nombre] = nombre.trim()
            it[TorneosTable.codigoExterno] = codigoExterno?.trim()
            it[TorneosTable.logoUrl] = logoUrl?.trim()
            it[TorneosTable.activo] = true
            it[TorneosTable.fechaInicio] = fechaInicio
            it[TorneosTable.fechaFin] = fechaFin
        }
        TorneosTable.selectAll().where { TorneosTable.id eq id }.single().toTournamentDto()
    }

    suspend fun getAllTeams(): List<TeamDto> = dbQuery {
        EquiposTable
            .selectAll()
            .orderBy(EquiposTable.nombre, SortOrder.ASC)
            .map { it.toTeamDto() }
    }

    suspend fun getTeamById(id: UUID): TeamDto? = dbQuery {
        EquiposTable
            .selectAll()
            .where { EquiposTable.id eq id }
            .map { it.toTeamDto() }
            .singleOrNull()
    }

    suspend fun findTeamByCodigo(codigo: String): TeamDto? = dbQuery {
        EquiposTable
            .selectAll()
            .where { EquiposTable.codigoExterno eq codigo }
            .map { it.toTeamDto() }
            .singleOrNull()
    }

    suspend fun createTeam(
        nombre: String,
        codigoExterno: String? = null,
        urlBandera: String? = null,
        codigoIso: String? = null
    ): TeamDto = dbQuery {
        val id = EquiposTable.insertAndGetId {
            it[EquiposTable.nombre] = nombre.trim()
            it[EquiposTable.codigoExterno] = codigoExterno?.trim()
            it[EquiposTable.urlBandera] = urlBandera?.trim()
            it[EquiposTable.codigoIso] = codigoIso?.trim()
        }
        EquiposTable.selectAll().where { EquiposTable.id eq id }.single().toTeamDto()
    }

    suspend fun addTeamToTournament(torneoId: UUID, equipoId: UUID) = dbQuery {
        val exists = TorneoEquiposTable
            .selectAll()
            .where { (TorneoEquiposTable.torneoId eq torneoId) and (TorneoEquiposTable.equipoId eq equipoId) }
            .count() > 0

        if (!exists) {
            TorneoEquiposTable.insert {
                it[TorneoEquiposTable.torneoId] = torneoId
                it[TorneoEquiposTable.equipoId] = equipoId
            }
        }
    }
}

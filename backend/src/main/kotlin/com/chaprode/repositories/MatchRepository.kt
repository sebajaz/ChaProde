package com.chaprode.repositories

import com.chaprode.config.DatabaseFactory.dbQuery
import com.chaprode.dto.MatchDto
import com.chaprode.dto.TeamDto
import com.chaprode.models.EquiposTable
import com.chaprode.models.PartidosTable
import kotlinx.datetime.Instant
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import java.util.*

class MatchRepository {

    suspend fun getMatchesByTournament(torneoId: UUID): List<MatchDto> = dbQuery {
        val localEquipos = EquiposTable.alias("local_equipos")
        val visitanteEquipos = EquiposTable.alias("visitante_equipos")

        PartidosTable
            .join(localEquipos, JoinType.INNER, additionalConstraint = { PartidosTable.equipoLocalId eq localEquipos[EquiposTable.id] })
            .join(visitanteEquipos, JoinType.INNER, additionalConstraint = { PartidosTable.equipoVisitanteId eq visitanteEquipos[EquiposTable.id] })
            .selectAll()
            .where { PartidosTable.torneoId eq torneoId }
            .orderBy(PartidosTable.fechaPartido, SortOrder.ASC)
            .map { row ->
                val localDto = TeamDto(
                    id = row[localEquipos[EquiposTable.id]].value.toString(),
                    nombre = row[localEquipos[EquiposTable.nombre]],
                    codigoExterno = row[localEquipos[EquiposTable.codigoExterno]],
                    urlBandera = row[localEquipos[EquiposTable.urlBandera]],
                    codigoIso = row[localEquipos[EquiposTable.codigoIso]]
                )
                val visitanteDto = TeamDto(
                    id = row[visitanteEquipos[EquiposTable.id]].value.toString(),
                    nombre = row[visitanteEquipos[EquiposTable.nombre]],
                    codigoExterno = row[visitanteEquipos[EquiposTable.codigoExterno]],
                    urlBandera = row[visitanteEquipos[EquiposTable.urlBandera]],
                    codigoIso = row[visitanteEquipos[EquiposTable.codigoIso]]
                )

                MatchDto(
                    id = row[PartidosTable.id].value.toString(),
                    torneoId = row[PartidosTable.torneoId].value.toString(),
                    equipoLocal = localDto,
                    equipoVisitante = visitanteDto,
                    fechaPartido = row[PartidosTable.fechaPartido].toString(),
                    golesLocal = row[PartidosTable.golesLocal],
                    golesVisitante = row[PartidosTable.golesVisitante],
                    estado = row[PartidosTable.estado],
                    codigoExterno = row[PartidosTable.codigoExterno]
                )
            }
    }

    suspend fun getMatchById(id: UUID): MatchDto? = dbQuery {
        val localEquipos = EquiposTable.alias("local_equipos")
        val visitanteEquipos = EquiposTable.alias("visitante_equipos")

        PartidosTable
            .join(localEquipos, JoinType.INNER, additionalConstraint = { PartidosTable.equipoLocalId eq localEquipos[EquiposTable.id] })
            .join(visitanteEquipos, JoinType.INNER, additionalConstraint = { PartidosTable.equipoVisitanteId eq visitanteEquipos[EquiposTable.id] })
            .selectAll()
            .where { PartidosTable.id eq id }
            .map { row ->
                val localDto = TeamDto(
                    id = row[localEquipos[EquiposTable.id]].value.toString(),
                    nombre = row[localEquipos[EquiposTable.nombre]],
                    codigoExterno = row[localEquipos[EquiposTable.codigoExterno]],
                    urlBandera = row[localEquipos[EquiposTable.urlBandera]],
                    codigoIso = row[localEquipos[EquiposTable.codigoIso]]
                )
                val visitanteDto = TeamDto(
                    id = row[visitanteEquipos[EquiposTable.id]].value.toString(),
                    nombre = row[visitanteEquipos[EquiposTable.nombre]],
                    codigoExterno = row[visitanteEquipos[EquiposTable.codigoExterno]],
                    urlBandera = row[visitanteEquipos[EquiposTable.urlBandera]],
                    codigoIso = row[visitanteEquipos[EquiposTable.codigoIso]]
                )

                MatchDto(
                    id = row[PartidosTable.id].value.toString(),
                    torneoId = row[PartidosTable.torneoId].value.toString(),
                    equipoLocal = localDto,
                    equipoVisitante = visitanteDto,
                    fechaPartido = row[PartidosTable.fechaPartido].toString(),
                    golesLocal = row[PartidosTable.golesLocal],
                    golesVisitante = row[PartidosTable.golesVisitante],
                    estado = row[PartidosTable.estado],
                    codigoExterno = row[PartidosTable.codigoExterno]
                )
            }
            .singleOrNull()
    }

    suspend fun createMatch(
        torneoId: UUID,
        equipoLocalId: UUID,
        equipoVisitanteId: UUID,
        fechaPartido: Instant,
        codigoExterno: String? = null
    ): UUID = dbQuery {
        PartidosTable.insertAndGetId {
            it[PartidosTable.torneoId] = torneoId
            it[PartidosTable.equipoLocalId] = equipoLocalId
            it[PartidosTable.equipoVisitanteId] = equipoVisitanteId
            it[PartidosTable.fechaPartido] = fechaPartido
            it[PartidosTable.estado] = "PENDIENTE"
            it[PartidosTable.codigoExterno] = codigoExterno?.trim()
        }.value
    }
}

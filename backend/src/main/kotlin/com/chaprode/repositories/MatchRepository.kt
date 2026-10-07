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

    suspend fun settleMatchResult(
        partidoId: UUID,
        golesLocal: Int,
        golesVisitante: Int,
        estado: String = "FINALIZADO"
    ): com.chaprode.dto.MatchResultResponse = dbQuery {
        // 1. Actualizar resultado del partido
        PartidosTable.update({ PartidosTable.id eq partidoId }) {
            it[PartidosTable.golesLocal] = golesLocal
            it[PartidosTable.golesVisitante] = golesVisitante
            it[PartidosTable.estado] = estado
        }

        // 2. Obtener todos los pronósticos registrados para este partido
        val predictions = com.chaprode.models.PronosticosTable
            .selectAll()
            .where { com.chaprode.models.PronosticosTable.partidoId eq partidoId }
            .toList()

        var totalPuntos = 0
        val now = kotlinx.datetime.Clock.System.now()

        // 3. Liquidar y asignar puntos a cada pronóstico
        for (row in predictions) {
            val predId = row[com.chaprode.models.PronosticosTable.id]
            val predLocal = row[com.chaprode.models.PronosticosTable.golesLocalPredicho]
            val predVisitante = row[com.chaprode.models.PronosticosTable.golesVisitantePredicho]

            val puntos = com.chaprode.services.ScoringEngine.calculatePoints(
                predLocal = predLocal,
                predVisitante = predVisitante,
                realLocal = golesLocal,
                realVisitante = golesVisitante
            )

            totalPuntos += puntos

            com.chaprode.models.PronosticosTable.update({ com.chaprode.models.PronosticosTable.id eq predId }) {
                it[puntosGanados] = puntos
                it[updatedAt] = now
            }
        }

        com.chaprode.dto.MatchResultResponse(
            partidoId = partidoId.toString(),
            golesLocal = golesLocal,
            golesVisitante = golesVisitante,
            estado = estado,
            totalPronosticosLiquidados = predictions.size,
            totalPuntosOtorgados = totalPuntos
        )
    }
}

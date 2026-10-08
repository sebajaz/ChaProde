package com.chaprode.repositories

import com.chaprode.config.DatabaseFactory.dbQuery
import com.chaprode.dto.PredictionDto
import com.chaprode.models.PartidosTable
import com.chaprode.models.PronosticosTable
import kotlinx.datetime.Clock
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import java.util.*

class PredictionRepository {

    private fun ResultRow.toPredictionDto(): PredictionDto = PredictionDto(
        id = this[PronosticosTable.id].value.toString(),
        usuarioId = this[PronosticosTable.usuarioId].value.toString(),
        partidoId = this[PronosticosTable.partidoId].value.toString(),
        golesLocalPredicho = this[PronosticosTable.golesLocalPredicho],
        golesVisitantePredicho = this[PronosticosTable.golesVisitantePredicho],
        puntosGanados = this[PronosticosTable.puntosGanados],
        updatedAt = this[PronosticosTable.updatedAt].toString()
    )

    suspend fun getPredictionByUserAndMatch(usuarioId: UUID, partidoId: UUID): PredictionDto? = dbQuery {
        PronosticosTable
            .selectAll()
            .where { (PronosticosTable.usuarioId eq usuarioId) and (PronosticosTable.partidoId eq partidoId) }
            .map { it.toPredictionDto() }
            .singleOrNull()
    }

    suspend fun getPredictionsByUserAndTournament(usuarioId: UUID, torneoId: UUID): List<PredictionDto> = dbQuery {
        (PronosticosTable innerJoin PartidosTable)
            .selectAll()
            .where { (PronosticosTable.usuarioId eq usuarioId) and (PartidosTable.torneoId eq torneoId) }
            .map { it.toPredictionDto() }
    }

    suspend fun getAllPredictionsByUser(usuarioId: UUID): List<PredictionDto> = dbQuery {
        PronosticosTable
            .selectAll()
            .where { PronosticosTable.usuarioId eq usuarioId }
            .map { it.toPredictionDto() }
    }

    suspend fun upsertPrediction(
        usuarioId: UUID,
        partidoId: UUID,
        golesLocal: Int,
        golesVisitante: Int
    ): PredictionDto = dbQuery {
        val existing = PronosticosTable
            .selectAll()
            .where { (PronosticosTable.usuarioId eq usuarioId) and (PronosticosTable.partidoId eq partidoId) }
            .singleOrNull()

        val now = Clock.System.now()

        if (existing != null) {
            val id = existing[PronosticosTable.id]
            PronosticosTable.update({ PronosticosTable.id eq id }) {
                it[golesLocalPredicho] = golesLocal
                it[golesVisitantePredicho] = golesVisitante
                it[updatedAt] = now
            }
            PronosticosTable.selectAll().where { PronosticosTable.id eq id }.single().toPredictionDto()
        } else {
            val id = PronosticosTable.insertAndGetId {
                it[PronosticosTable.usuarioId] = usuarioId
                it[PronosticosTable.partidoId] = partidoId
                it[golesLocalPredicho] = golesLocal
                it[golesVisitantePredicho] = golesVisitante
                it[puntosGanados] = 0
                it[createdAt] = now
                it[updatedAt] = now
            }
            PronosticosTable.selectAll().where { PronosticosTable.id eq id }.single().toPredictionDto()
        }
    }
}

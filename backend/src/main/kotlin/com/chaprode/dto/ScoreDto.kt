package com.chaprode.dto

import kotlinx.serialization.Serializable

@Serializable
data class SetMatchResultRequest(
    val golesLocal: Int,
    val golesVisitante: Int,
    val estado: String = "FINALIZADO"
)

@Serializable
data class MatchResultResponse(
    val partidoId: String,
    val golesLocal: Int,
    val golesVisitante: Int,
    val estado: String,
    val totalPronosticosLiquidados: Int,
    val totalPuntosOtorgados: Int
)

@Serializable
data class SyncSummaryDto(
    val partidosProcesados: Int,
    val partidosFinalizados: Int,
    val pronosticosLiquidados: Int,
    val puntosOtorgados: Int,
    val mensaje: String,
    val detalles: List<String> = emptyList()
)

@Serializable
data class SimulateMatchRequest(
    val partidoId: String? = null,
    val codigoExterno: String? = null,
    val golesLocal: Int? = null,
    val golesVisitante: Int? = null,
    val estado: String = "FINALIZADO"
)

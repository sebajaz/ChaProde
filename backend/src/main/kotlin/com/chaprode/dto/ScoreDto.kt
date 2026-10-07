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

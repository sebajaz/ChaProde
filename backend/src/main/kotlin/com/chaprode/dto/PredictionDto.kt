package com.chaprode.dto

import kotlinx.serialization.Serializable

@Serializable
data class SubmitPredictionRequest(
    val partidoId: String,
    val golesLocal: Int,
    val golesVisitante: Int
)

@Serializable
data class PredictionDto(
    val id: String,
    val usuarioId: String,
    val partidoId: String,
    val golesLocalPredicho: Int,
    val golesVisitantePredicho: Int,
    val puntosGanados: Int = 0,
    val bloqueado: Boolean = false,
    val updatedAt: String
)

@Serializable
data class MatchWithPredictionDto(
    val match: MatchDto,
    val myPrediction: PredictionDto? = null,
    val cerrado: Boolean = false,
    val minutosRestantesParaCierre: Long = 0
)

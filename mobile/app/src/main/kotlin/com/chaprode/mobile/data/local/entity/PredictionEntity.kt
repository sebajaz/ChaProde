package com.chaprode.mobile.data.local.entity

/**
 * Entidad de persistencia local para la tabla 'predictions'.
 * Diseñada para integrarse directamente con Room en Android.
 */
data class PredictionEntity(
    val id: String,
    val partidoId: String,
    val golesLocalPredicho: Int,
    val golesVisitantePredicho: Int,
    val puntosGanados: Int = 0,
    val updatedAt: String
)

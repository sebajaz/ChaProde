package com.chaprode.mobile.data.local.entity

/**
 * Entidad de persistencia local para cachear la tabla de posiciones con Room.
 */
data class RankingEntity(
    val posicion: Int,
    val usuarioId: String,
    val username: String,
    val puntosTotales: Int,
    val plenosExactos: Int,
    val aciertosTendencia: Int,
    val pronosticosTotales: Int,
    val torneoId: String
)

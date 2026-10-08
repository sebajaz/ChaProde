package com.chaprode.mobile.data.local.entity

/**
 * Entidad de persistencia local para cachear ligas privadas con Room.
 */
data class LeagueEntity(
    val id: String,
    val nombre: String,
    val codigoAcceso: String,
    val creadorId: String,
    val creadorUsername: String,
    val torneoId: String,
    val torneoNombre: String,
    val totalMiembros: Int,
    val createdAt: String
)

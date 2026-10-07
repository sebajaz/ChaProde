package com.chaprode.dto

import kotlinx.serialization.Serializable

@Serializable
data class LeaderboardEntryDto(
    val posicion: Int,
    val usuarioId: String,
    val username: String,
    val puntosTotales: Int,
    val plenosExactos: Int,
    val aciertosTendencia: Int,
    val pronosticosTotales: Int
)

@Serializable
data class TournamentLeaderboardResponse(
    val torneoId: String,
    val torneoNombre: String,
    val ranking: List<LeaderboardEntryDto>
)

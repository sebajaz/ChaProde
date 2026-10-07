package com.chaprode.mobile.model

data class RankingUserItem(
    val posicion: Int,
    val usuarioId: String,
    val username: String,
    val puntosTotales: Int,
    val plenosExactos: Int,
    val aciertosTendencia: Int,
    val pronosticosTotales: Int,
    val isCurrentUser: Boolean = false
)

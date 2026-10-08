package com.chaprode.mobile.model

data class LeagueItem(
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

data class LeagueDetailItem(
    val liga: LeagueItem,
    val ranking: List<RankingUserItem>
)

package com.chaprode.dto

import kotlinx.serialization.Serializable

@Serializable
data class CreateLeagueRequest(
    val nombre: String,
    val torneoId: String
)

@Serializable
data class JoinLeagueRequest(
    val codigoAcceso: String
)

@Serializable
data class LeagueDto(
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

@Serializable
data class LeagueDetailDto(
    val liga: LeagueDto,
    val ranking: List<LeaderboardEntryDto>
)

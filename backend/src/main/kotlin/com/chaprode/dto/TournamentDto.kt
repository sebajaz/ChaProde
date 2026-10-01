package com.chaprode.dto

import kotlinx.serialization.Serializable

@Serializable
data class TeamDto(
    val id: String,
    val nombre: String,
    val codigoExterno: String? = null,
    val urlBandera: String? = null,
    val codigoIso: String? = null
)

@Serializable
data class TournamentDto(
    val id: String,
    val nombre: String,
    val codigoExterno: String? = null,
    val logoUrl: String? = null,
    val activo: Boolean = true,
    val fechaInicio: String? = null,
    val fechaFin: String? = null,
    val totalEquipos: Int = 0,
    val totalPartidos: Int = 0
)

@Serializable
data class MatchDto(
    val id: String,
    val torneoId: String,
    val equipoLocal: TeamDto,
    val equipoVisitante: TeamDto,
    val fechaPartido: String,
    val golesLocal: Int? = null,
    val golesVisitante: Int? = null,
    val estado: String = "PENDIENTE",
    val codigoExterno: String? = null
)

@Serializable
data class CreateTournamentRequest(
    val nombre: String,
    val codigoExterno: String? = null,
    val logoUrl: String? = null,
    val fechaInicio: String? = null,
    val fechaFin: String? = null
)

@Serializable
data class CreateTeamRequest(
    val nombre: String,
    val codigoExterno: String? = null,
    val urlBandera: String? = null,
    val codigoIso: String? = null
)

@Serializable
data class CreateMatchRequest(
    val torneoId: String,
    val equipoLocalId: String,
    val equipoVisitanteId: String,
    val fechaPartido: String,
    val codigoExterno: String? = null
)

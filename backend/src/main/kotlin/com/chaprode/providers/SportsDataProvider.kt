package com.chaprode.providers

import kotlinx.serialization.Serializable

@Serializable
data class ExternalMatchScore(
    val codigoExterno: String? = null,
    val localCodigo: String,
    val visitanteCodigo: String,
    val golesLocal: Int? = null,
    val golesVisitante: Int? = null,
    val estado: String // "PENDIENTE", "EN_JUEGO", "FINALIZADO"
)

interface SportsDataProvider {
    suspend fun fetchMatchResults(competitionCode: String? = null): List<ExternalMatchScore>
}

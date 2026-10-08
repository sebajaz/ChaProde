package com.chaprode.providers

import kotlinx.serialization.Serializable

@Serializable
data class ExternalMatchScore(
    val codigoExterno: String? = null,
    val localCodigo: String,
    val visitanteCodigo: String,
    val golesLocal: Int? = null,
    val golesVisitante: Int? = null,
    val estado: String, // "PENDIENTE", "EN_JUEGO", "FINALIZADO"
    val competitionCode: String? = null,
    val competitionName: String? = null,
    val competitionEmblem: String? = null,
    val localNombre: String? = null,
    val visitanteNombre: String? = null,
    val localBandera: String? = null,
    val visitanteBandera: String? = null,
    val fechaPartido: String? = null
)

interface SportsDataProvider {
    suspend fun fetchMatchResults(competitionCode: String? = null): List<ExternalMatchScore>
    suspend fun fetchWorldwideMatches(dateFrom: String? = null, dateTo: String? = null): List<ExternalMatchScore> = fetchMatchResults(null)
}

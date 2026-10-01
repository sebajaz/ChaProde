package com.chaprode.mobile.model

data class PartidoItem(
    val id: String,
    val localNombre: String,
    val localBandera: String,
    val visitanteNombre: String,
    val visitanteBandera: String,
    val fechaHora: String,
    val estado: String,
    val golesLocal: Int? = null,
    val golesVisitante: Int? = null,
    val miPronosticoLocal: Int? = null,
    val miPronosticoVisitante: Int? = null,
    val puntosGanados: Int? = null
)

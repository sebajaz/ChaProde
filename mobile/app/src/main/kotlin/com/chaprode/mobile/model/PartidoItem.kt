package com.chaprode.mobile.model

/**
 * Modelo ligero de representación de un partido y el pronóstico del usuario.
 *
 * @param id Identificador UUID del partido.
 * @param localNombre Nombre de la selección o equipo local.
 * @param localBandera Código o URL de la bandera del equipo local.
 * @param visitanteNombre Nombre de la selección o equipo visitante.
 * @param visitanteBandera Código o URL de la bandera del equipo visitante.
 * @param fechaHora Fecha y hora programada del cotejo en formato ISO-8601.
 * @param estado Estado actual del partido ("PENDIENTE", "EN_CURSO", "FINALIZADO").
 * @param golesLocal Marcador oficial anotado por el local (nulo si no inició).
 * @param golesVisitante Marcador oficial anotado por el visitante (nulo si no inició).
 * @param miPronosticoLocal Cantidad de goles pronosticados para el local.
 * @param miPronosticoVisitante Cantidad de goles pronosticados para el visitante.
 * @param puntosGanados Puntos obtenidos por el usuario (3 pleno exacto, 1 tendencia, 0 error).
 */
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

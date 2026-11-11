package com.chaprode.mobile.model

/**
 * Fila individual de la tabla de posiciones general o de liga.
 *
 * @param posicion Puesto actual en la tabla (#1, #2, etc.).
 * @param usuarioId Identificador del usuario.
 * @param username Nombre visible del participante.
 * @param puntosTotales Puntos acumulados en la competencia.
 * @param plenosExactos Cantidad de resultados exactos acertados (+3 pts c/u).
 * @param aciertosTendencia Cantidad de tendencias o ganadores acertados (+1 pto c/u).
 * @param pronosticosTotales Total de pronósticos cargados por el usuario.
 * @param isCurrentUser Bandera para destacar la fila del usuario logueado en la interfaz.
 */
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

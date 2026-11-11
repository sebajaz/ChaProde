package com.chaprode.mobile.ui.ranking

import com.chaprode.mobile.model.RankingUserItem

/**
 * Estado inmutable de la pantalla de Ranking.
 *
 * @param isLoading Indica si la tabla de posiciones se está consultando desde el servidor.
 * @param ranking Lista completa y ordenada de participantes con sus posiciones, puntos y plenos.
 * @param torneoNombre Nombre del torneo para el encabezado.
 * @param errorMessage Mensaje descriptivo si ocurre una falla de red.
 */
data class RankingUiState(
    val isLoading: Boolean = false,
    val ranking: List<RankingUserItem> = emptyList(),
    val torneoNombre: String = "Copa Mundial FIFA 2026",
    val errorMessage: String? = null
) {
    /** Primer lugar del podio (Medalla de Oro). */
    val top1: RankingUserItem?
        get() = ranking.getOrNull(0)

    /** Segundo lugar del podio (Medalla de Plata). */
    val top2: RankingUserItem?
        get() = ranking.getOrNull(1)

    /** Tercer lugar del podio (Medalla de Bronce). */
    val top3: RankingUserItem?
        get() = ranking.getOrNull(2)

    /** Resto de la tabla de posiciones a partir del puesto #4 en adelante. */
    val restOfRanking: List<RankingUserItem>
        get() = if (ranking.size > 3) ranking.drop(3) else emptyList()
}

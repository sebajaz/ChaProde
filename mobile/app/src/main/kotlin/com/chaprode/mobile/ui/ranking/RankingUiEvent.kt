package com.chaprode.mobile.ui.ranking

/**
 * Eventos e intenciones del usuario para la pantalla de Ranking y Posiciones.
 */
sealed interface RankingUiEvent {
    /** Carga la tabla de clasificación general para el torneo especificado. */
    data class LoadRanking(val torneoId: String) : RankingUiEvent
    
    /** Descarta el error mostrado en pantalla. */
    object OnDismissError : RankingUiEvent
}

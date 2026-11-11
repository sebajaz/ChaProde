package com.chaprode.mobile.ui.prediction

/**
 * Intenciones y eventos de usuario para el fixture y carga de pronósticos de partidos.
 */
sealed interface PredictionUiEvent {
    /** Carga o refresca el listado de partidos para el torneo indicado. */
    data class LoadMatches(val torneoId: String) : PredictionUiEvent
    
    /** Modificación del marcador previsto para el equipo local en un partido. */
    data class OnLocalGoalsChanged(val matchId: String, val newGoals: Int) : PredictionUiEvent
    
    /** Modificación del marcador previsto para el equipo visitante en un partido. */
    data class OnVisitorGoalsChanged(val matchId: String, val newGoals: Int) : PredictionUiEvent
    
    /** Guarda el pronóstico editado enviándolo a la API REST del backend. */
    data class OnSavePrediction(val matchId: String) : PredictionUiEvent
    
    /** Descarta mensajes de notificación de éxito o error en la interfaz. */
    object OnDismissMessage : PredictionUiEvent
}

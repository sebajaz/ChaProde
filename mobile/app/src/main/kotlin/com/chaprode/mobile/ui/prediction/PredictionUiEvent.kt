package com.chaprode.mobile.ui.prediction

sealed interface PredictionUiEvent {
    data class LoadMatches(val torneoId: String) : PredictionUiEvent
    data class OnLocalGoalsChanged(val matchId: String, val newGoals: Int) : PredictionUiEvent
    data class OnVisitorGoalsChanged(val matchId: String, val newGoals: Int) : PredictionUiEvent
    data class OnSavePrediction(val matchId: String) : PredictionUiEvent
    object OnDismissMessage : PredictionUiEvent
}

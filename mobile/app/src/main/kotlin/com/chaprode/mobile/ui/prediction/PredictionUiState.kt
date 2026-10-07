package com.chaprode.mobile.ui.prediction

import com.chaprode.mobile.model.MatchWithPredictionItem

data class PredictionUiState(
    val isLoading: Boolean = false,
    val matches: List<MatchWithPredictionItem> = emptyList(),
    val savingMatchId: String? = null,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val selectedTorneoId: String = "d96b16df-448a-43ca-9fa7-140af2e63e14",
    val torneoNombre: String = "Copa Mundial FIFA 2026"
)

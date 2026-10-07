package com.chaprode.mobile.ui.prediction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chaprode.mobile.data.repository.PredictionRepository
import com.chaprode.mobile.model.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PredictionViewModel(
    private val repository: PredictionRepository = PredictionRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(PredictionUiState())
    val uiState: StateFlow<PredictionUiState> = _uiState.asStateFlow()

    fun onEvent(event: PredictionUiEvent) {
        when (event) {
            is PredictionUiEvent.LoadMatches -> {
                loadMatches(event.torneoId)
            }
            is PredictionUiEvent.OnLocalGoalsChanged -> {
                val clamped = event.newGoals.coerceIn(0, 99)
                _uiState.update { state ->
                    val updated = state.matches.map { match ->
                        if (match.id == event.matchId) {
                            match.copy(
                                editedGolesLocal = clamped,
                                isEdited = true
                            )
                        } else match
                    }
                    state.copy(matches = updated)
                }
            }
            is PredictionUiEvent.OnVisitorGoalsChanged -> {
                val clamped = event.newGoals.coerceIn(0, 99)
                _uiState.update { state ->
                    val updated = state.matches.map { match ->
                        if (match.id == event.matchId) {
                            match.copy(
                                editedGolesVisitante = clamped,
                                isEdited = true
                            )
                        } else match
                    }
                    state.copy(matches = updated)
                }
            }
            is PredictionUiEvent.OnSavePrediction -> {
                savePrediction(event.matchId)
            }
            is PredictionUiEvent.OnDismissMessage -> {
                _uiState.update { it.copy(errorMessage = null, successMessage = null) }
            }
        }
    }

    private fun loadMatches(torneoId: String) {
        viewModelScope.launch {
            repository.getMatchesWithPredictions(torneoId).collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
                    }
                    is Resource.Success -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                matches = resource.data,
                                selectedTorneoId = torneoId
                            )
                        }
                    }
                    is Resource.Error -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = resource.message
                            )
                        }
                    }
                }
            }
        }
    }

    private fun savePrediction(matchId: String) {
        val targetMatch = _uiState.value.matches.find { it.id == matchId } ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(savingMatchId = matchId, errorMessage = null) }

            repository.submitPrediction(
                partidoId = matchId,
                golesLocal = targetMatch.editedGolesLocal,
                golesVisitante = targetMatch.editedGolesVisitante
            ).collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        _uiState.update { it.copy(savingMatchId = matchId) }
                    }
                    is Resource.Success -> {
                        _uiState.update { state ->
                            val updatedMatches = state.matches.map { match ->
                                if (match.id == matchId) {
                                    match.copy(
                                        miPronosticoLocal = targetMatch.editedGolesLocal,
                                        miPronosticoVisitante = targetMatch.editedGolesVisitante,
                                        isEdited = false
                                    )
                                } else match
                            }
                            state.copy(
                                savingMatchId = null,
                                matches = updatedMatches,
                                successMessage = "¡Pronóstico de ${targetMatch.localNombre} vs ${targetMatch.visitanteNombre} guardado!"
                            )
                        }
                    }
                    is Resource.Error -> {
                        _uiState.update {
                            it.copy(
                                savingMatchId = null,
                                errorMessage = resource.message
                            )
                        }
                    }
                }
            }
        }
    }
}

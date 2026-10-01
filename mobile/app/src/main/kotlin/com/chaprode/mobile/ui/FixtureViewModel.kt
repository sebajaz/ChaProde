package com.chaprode.mobile.ui

import com.chaprode.mobile.data.FixtureRepository
import com.chaprode.mobile.model.PartidoItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class FixtureUiState {
    object Loading : FixtureUiState()
    data class Success(val matches: List<PartidoItem>) : FixtureUiState()
    data class Error(val message: String) : FixtureUiState()
}

class FixtureViewModel(
    private val fixtureRepository: FixtureRepository = FixtureRepository(),
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Main)
) {
    private val _uiState = MutableStateFlow<FixtureUiState>(FixtureUiState.Loading)
    val uiState: StateFlow<FixtureUiState> = _uiState.asStateFlow()

    fun loadMatches(torneoId: String) {
        _uiState.value = FixtureUiState.Loading
        scope.launch {
            val result = fixtureRepository.getMatches(torneoId)
            result.onSuccess {
                _uiState.value = FixtureUiState.Success(it)
            }.onFailure {
                _uiState.value = FixtureUiState.Error(it.localizedMessage ?: "Error al cargar partidos")
            }
        }
    }
}

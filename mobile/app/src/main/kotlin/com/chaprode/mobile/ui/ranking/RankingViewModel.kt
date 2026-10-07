package com.chaprode.mobile.ui.ranking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chaprode.mobile.data.repository.RankingRepository
import com.chaprode.mobile.model.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RankingViewModel(
    private val repository: RankingRepository = RankingRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(RankingUiState())
    val uiState: StateFlow<RankingUiState> = _uiState.asStateFlow()

    fun onEvent(event: RankingUiEvent) {
        when (event) {
            is RankingUiEvent.LoadRanking -> {
                loadRanking(event.torneoId)
            }
            is RankingUiEvent.OnDismissError -> {
                _uiState.update { it.copy(errorMessage = null) }
            }
        }
    }

    private fun loadRanking(torneoId: String) {
        viewModelScope.launch {
            repository.getTournamentRanking(torneoId).collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
                    }
                    is Resource.Success -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                ranking = resource.data,
                                errorMessage = null
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
}

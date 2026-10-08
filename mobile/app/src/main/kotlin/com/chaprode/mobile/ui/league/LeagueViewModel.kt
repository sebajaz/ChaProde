package com.chaprode.mobile.ui.league

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chaprode.mobile.data.repository.LeagueRepository
import com.chaprode.mobile.model.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LeagueViewModel(
    private val repository: LeagueRepository = LeagueRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(LeagueUiState())
    val uiState: StateFlow<LeagueUiState> = _uiState.asStateFlow()

    init {
        loadLeagues()
    }

    fun onEvent(event: LeagueUiEvent) {
        when (event) {
            is LeagueUiEvent.LoadLeagues -> loadLeagues()
            is LeagueUiEvent.OnOpenCreateDialog -> {
                _uiState.update { it.copy(isCreateDialogOpen = event.open, newLeagueName = "", errorMessage = null) }
            }
            is LeagueUiEvent.OnOpenJoinDialog -> {
                _uiState.update { it.copy(isJoinDialogOpen = event.open, joinCode = "", errorMessage = null) }
            }
            is LeagueUiEvent.OnNewLeagueNameChanged -> {
                _uiState.update { it.copy(newLeagueName = event.name) }
            }
            is LeagueUiEvent.OnJoinCodeChanged -> {
                _uiState.update { it.copy(joinCode = event.code.uppercase().trim()) }
            }
            is LeagueUiEvent.OnCreateLeagueClicked -> createLeague()
            is LeagueUiEvent.OnJoinLeagueClicked -> joinLeague()
            is LeagueUiEvent.OnSelectLeague -> loadLeagueDetail(event.leagueId)
            is LeagueUiEvent.OnCloseDetail -> {
                _uiState.update { it.copy(selectedLeagueDetail = null) }
            }
            is LeagueUiEvent.OnDismissMessage -> {
                _uiState.update { it.copy(errorMessage = null, successMessage = null) }
            }
        }
    }

    private fun loadLeagues() {
        viewModelScope.launch {
            repository.getMyLeagues().collect { resource ->
                when (resource) {
                    is Resource.Loading -> _uiState.update { it.copy(isLoading = true) }
                    is Resource.Success -> _uiState.update { it.copy(isLoading = false, leagues = resource.data) }
                    is Resource.Error -> _uiState.update { it.copy(isLoading = false, errorMessage = resource.message) }
                }
            }
        }
    }

    private fun createLeague() {
        val name = _uiState.value.newLeagueName.trim()
        if (name.isBlank()) return

        // Por defecto torneo Mundial 2026 activo
        val defaultTorneoId = "activo"

        viewModelScope.launch {
            repository.createLeague(name, defaultTorneoId).collect { resource ->
                when (resource) {
                    is Resource.Loading -> _uiState.update { it.copy(isLoading = true) }
                    is Resource.Success -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                isCreateDialogOpen = false,
                                newLeagueName = "",
                                successMessage = "¡Liga '${resource.data.nombre}' creada con éxito! Código: ${resource.data.codigoAcceso}"
                            )
                        }
                        loadLeagues()
                    }
                    is Resource.Error -> _uiState.update { it.copy(isLoading = false, errorMessage = resource.message) }
                }
            }
        }
    }

    private fun joinLeague() {
        val code = _uiState.value.joinCode.trim()
        if (code.isBlank()) return

        viewModelScope.launch {
            repository.joinLeague(code).collect { resource ->
                when (resource) {
                    is Resource.Loading -> _uiState.update { it.copy(isLoading = true) }
                    is Resource.Success -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                isJoinDialogOpen = false,
                                joinCode = "",
                                successMessage = "¡Te uniste a '${resource.data.nombre}' exitosamente!"
                            )
                        }
                        loadLeagues()
                    }
                    is Resource.Error -> _uiState.update { it.copy(isLoading = false, errorMessage = resource.message) }
                }
            }
        }
    }

    private fun loadLeagueDetail(leagueId: String) {
        viewModelScope.launch {
            repository.getLeagueDetail(leagueId).collect { resource ->
                when (resource) {
                    is Resource.Loading -> _uiState.update { it.copy(isLoading = true) }
                    is Resource.Success -> _uiState.update { it.copy(isLoading = false, selectedLeagueDetail = resource.data) }
                    is Resource.Error -> _uiState.update { it.copy(isLoading = false, errorMessage = resource.message) }
                }
            }
        }
    }
}

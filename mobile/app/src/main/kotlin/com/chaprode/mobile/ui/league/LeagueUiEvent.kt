package com.chaprode.mobile.ui.league

sealed interface LeagueUiEvent {
    object LoadLeagues : LeagueUiEvent
    data class OnOpenCreateDialog(val open: Boolean) : LeagueUiEvent
    data class OnOpenJoinDialog(val open: Boolean) : LeagueUiEvent
    data class OnNewLeagueNameChanged(val name: String) : LeagueUiEvent
    data class OnJoinCodeChanged(val code: String) : LeagueUiEvent
    object OnCreateLeagueClicked : LeagueUiEvent
    object OnJoinLeagueClicked : LeagueUiEvent
    data class OnSelectLeague(val leagueId: String) : LeagueUiEvent
    object OnCloseDetail : LeagueUiEvent
    object OnDismissMessage : LeagueUiEvent
}

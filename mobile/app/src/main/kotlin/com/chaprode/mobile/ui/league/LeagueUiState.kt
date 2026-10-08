package com.chaprode.mobile.ui.league

import com.chaprode.mobile.model.LeagueDetailItem
import com.chaprode.mobile.model.LeagueItem

data class LeagueUiState(
    val isLoading: Boolean = false,
    val leagues: List<LeagueItem> = emptyList(),
    val selectedLeagueDetail: LeagueDetailItem? = null,
    val isCreateDialogOpen: Boolean = false,
    val isJoinDialogOpen: Boolean = false,
    val newLeagueName: String = "",
    val joinCode: String = "",
    val errorMessage: String? = null,
    val successMessage: String? = null
)

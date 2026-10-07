package com.chaprode.mobile.ui.ranking

sealed interface RankingUiEvent {
    data class LoadRanking(val torneoId: String) : RankingUiEvent
    object OnDismissError : RankingUiEvent
}

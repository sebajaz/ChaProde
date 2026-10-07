package com.chaprode.mobile.ui.ranking

import com.chaprode.mobile.model.RankingUserItem

data class RankingUiState(
    val isLoading: Boolean = false,
    val ranking: List<RankingUserItem> = emptyList(),
    val torneoNombre: String = "Copa Mundial FIFA 2026",
    val errorMessage: String? = null
) {
    val top1: RankingUserItem?
        get() = ranking.getOrNull(0)

    val top2: RankingUserItem?
        get() = ranking.getOrNull(1)

    val top3: RankingUserItem?
        get() = ranking.getOrNull(2)

    val restOfRanking: List<RankingUserItem>
        get() = if (ranking.size > 3) ranking.drop(3) else emptyList()
}

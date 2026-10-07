package com.chaprode.mobile.data.local.dao

import com.chaprode.mobile.data.local.entity.RankingEntity
import kotlinx.coroutines.flow.Flow

/**
 * Interface DAO preparada para Room para operaciones de ranking local.
 */
interface RankingDao {
    suspend fun insertAll(ranking: List<RankingEntity>)
    fun getRankingByTournamentFlow(torneoId: String): Flow<List<RankingEntity>>
    suspend fun clearByTournament(torneoId: String)
}

package com.chaprode.mobile.data.local.dao

import com.chaprode.mobile.data.local.entity.LeagueEntity
import kotlinx.coroutines.flow.Flow

/**
 * Interface DAO preparada para Room para operaciones locales de ligas privadas.
 */
interface LeagueDao {
    suspend fun insertAll(leagues: List<LeagueEntity>)
    fun getAllLeaguesFlow(): Flow<List<LeagueEntity>>
    suspend fun clearLeagues()
}

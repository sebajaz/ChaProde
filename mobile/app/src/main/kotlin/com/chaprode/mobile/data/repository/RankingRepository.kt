package com.chaprode.mobile.data.repository

import com.chaprode.mobile.data.local.preferences.SessionManager
import com.chaprode.mobile.data.remote.api.RankingApiService
import com.chaprode.mobile.model.RankingUserItem
import com.chaprode.mobile.model.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Repositorio de Ranking y Posiciones:
 * Provee la tabla de clasificación consumiendo el endpoint oficial de Leaderboard.
 */
class RankingRepository(
    private val apiService: RankingApiService = RankingApiService(),
    private val sessionManager: SessionManager = SessionManager()
) {

    /**
     * Consulta la tabla de posiciones del torneo indicado identificando la fila del usuario actual.
     */
    fun getTournamentRanking(torneoId: String): Flow<Resource<List<RankingUserItem>>> = flow {
        emit(Resource.Loading)
        val currentUserId = sessionManager.getUser()?.id
        val result = apiService.getTournamentRanking(torneoId, currentUserId)
        result.fold(
            onSuccess = { list ->
                emit(Resource.Success(list))
            },
            onFailure = { error ->
                emit(Resource.Error(error.localizedMessage ?: "Error al cargar la tabla de posiciones", error))
            }
        )
    }
}

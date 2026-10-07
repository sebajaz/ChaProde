package com.chaprode.mobile.data.repository

import com.chaprode.mobile.data.local.preferences.SessionManager
import com.chaprode.mobile.data.remote.api.PredictionApiService
import com.chaprode.mobile.model.MatchWithPredictionItem
import com.chaprode.mobile.model.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Repositorio de Pronósticos y Fixture (Single Source of Truth).
 * Coordina la comunicación con el Backend y la persistencia local.
 */
class PredictionRepository(
    private val apiService: PredictionApiService = PredictionApiService(),
    private val sessionManager: SessionManager = SessionManager()
) {

    fun getMatchesWithPredictions(torneoId: String): Flow<Resource<List<MatchWithPredictionItem>>> = flow {
        emit(Resource.Loading)
        val token = sessionManager.currentToken.value
        val result = apiService.getMatchesWithPredictions(torneoId, token)
        result.fold(
            onSuccess = { list ->
                emit(Resource.Success(list))
            },
            onFailure = { error ->
                emit(Resource.Error(error.localizedMessage ?: "Error al obtener partidos", error))
            }
        )
    }

    fun submitPrediction(
        partidoId: String,
        golesLocal: Int,
        golesVisitante: Int
    ): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading)
        val token = sessionManager.currentToken.value
        if (token.isNullOrBlank()) {
            emit(Resource.Error("Debes iniciar sesión para guardar un pronóstico."))
            return@flow
        }

        val result = apiService.submitPrediction(partidoId, golesLocal, golesVisitante, token)
        result.fold(
            onSuccess = {
                emit(Resource.Success(Unit))
            },
            onFailure = { error ->
                emit(Resource.Error(error.localizedMessage ?: "Error al guardar pronóstico", error))
            }
        )
    }
}

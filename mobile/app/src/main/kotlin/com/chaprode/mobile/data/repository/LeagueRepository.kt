package com.chaprode.mobile.data.repository

import com.chaprode.mobile.data.local.preferences.SessionManager
import com.chaprode.mobile.data.remote.api.LeagueApiService
import com.chaprode.mobile.model.LeagueDetailItem
import com.chaprode.mobile.model.LeagueItem
import com.chaprode.mobile.model.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class LeagueRepository(
    private val apiService: LeagueApiService = LeagueApiService(),
    private val sessionManager: SessionManager = SessionManager()
) {

    fun getMyLeagues(): Flow<Resource<List<LeagueItem>>> = flow {
        emit(Resource.Loading)
        val token = sessionManager.currentToken.value
        if (token.isNullOrBlank()) {
            emit(Resource.Error("Debes iniciar sesión para ver tus ligas."))
            return@flow
        }

        val result = apiService.getMyLeagues(token)
        result.fold(
            onSuccess = { emit(Resource.Success(it)) },
            onFailure = { emit(Resource.Error(it.localizedMessage ?: "Error al obtener ligas", it)) }
        )
    }

    fun createLeague(nombre: String, torneoId: String): Flow<Resource<LeagueItem>> = flow {
        emit(Resource.Loading)
        val token = sessionManager.currentToken.value
        if (token.isNullOrBlank()) {
            emit(Resource.Error("Debes iniciar sesión para crear una liga."))
            return@flow
        }

        val result = apiService.createLeague(nombre, torneoId, token)
        result.fold(
            onSuccess = { emit(Resource.Success(it)) },
            onFailure = { emit(Resource.Error(it.localizedMessage ?: "Error al crear la liga", it)) }
        )
    }

    fun joinLeague(codigoAcceso: String): Flow<Resource<LeagueItem>> = flow {
        emit(Resource.Loading)
        val token = sessionManager.currentToken.value
        if (token.isNullOrBlank()) {
            emit(Resource.Error("Debes iniciar sesión para unirte a una liga."))
            return@flow
        }

        val result = apiService.joinLeague(codigoAcceso, token)
        result.fold(
            onSuccess = { emit(Resource.Success(it)) },
            onFailure = { emit(Resource.Error(it.localizedMessage ?: "Error al unirse a la liga", it)) }
        )
    }

    fun getLeagueDetail(leagueId: String): Flow<Resource<LeagueDetailItem>> = flow {
        emit(Resource.Loading)
        val token = sessionManager.currentToken.value
        if (token.isNullOrBlank()) {
            emit(Resource.Error("Debes iniciar sesión para ver los detalles."))
            return@flow
        }

        val currentUserId = sessionManager.getUser()?.id
        val result = apiService.getLeagueDetail(leagueId, token, currentUserId)
        result.fold(
            onSuccess = { emit(Resource.Success(it)) },
            onFailure = { emit(Resource.Error(it.localizedMessage ?: "Error al cargar detalle", it)) }
        )
    }
}

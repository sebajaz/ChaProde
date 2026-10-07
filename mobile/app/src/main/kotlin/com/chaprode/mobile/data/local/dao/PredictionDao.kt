package com.chaprode.mobile.data.local.dao

import com.chaprode.mobile.data.local.entity.PredictionEntity
import kotlinx.coroutines.flow.Flow

/**
 * Interface DAO preparada para Room para operaciones locales de pronósticos.
 */
interface PredictionDao {
    suspend fun insertOrUpdate(prediction: PredictionEntity)
    suspend fun getPredictionByMatchId(partidoId: String): PredictionEntity?
    fun getAllPredictionsFlow(): Flow<List<PredictionEntity>>
    suspend fun clearPredictions()
}

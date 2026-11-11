package com.chaprode.mobile.data.local.dao

import com.chaprode.mobile.data.local.entity.PredictionEntity
import kotlinx.coroutines.flow.Flow

/**
 * Interface DAO preparada para Room para operaciones locales de pronósticos.
 */
interface PredictionDao {
    /** Inserta o actualiza un pronóstico en la caché local SQLite. */
    suspend fun insertOrUpdate(prediction: PredictionEntity)
    
    /** Obtiene el pronóstico local asociado a un partido específico. */
    suspend fun getPredictionByMatchId(partidoId: String): PredictionEntity?
    
    /** Flujo observable con todos los pronósticos cacheados en el dispositivo. */
    fun getAllPredictionsFlow(): Flow<List<PredictionEntity>>
    
    /** Borra todos los pronósticos locales al cerrar sesión. */
    suspend fun clearPredictions()
}

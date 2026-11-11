package com.chaprode.mobile.model

/**
 * Envoltorio genérico sellado para representar el resultado de operaciones asíncronas
 * (llamadas a la API REST o consultas a la base de datos local).
 * Modela tres estados posibles: Éxito (Success), Error y Carga en progreso (Loading).
 *
 * @param T Tipo de dato esperado en caso de respuesta exitosa.
 */
sealed class Resource<out T> {
    /** Operación completada con éxito. Contiene los datos [data] retornados. */
    data class Success<out T>(val data: T) : Resource<T>()
    
    /** Operación fallida. Contiene un mensaje descriptivo y opcionalmente la excepción original [cause]. */
    data class Error(val message: String, val cause: Throwable? = null) : Resource<Nothing>()
    
    /** Operación asíncrona iniciada y actualmente en proceso. */
    object Loading : Resource<Nothing>()
}

package com.chaprode.mobile.model

/**
 * Modelo completo que une la información de un partido con el pronóstico del usuario,
 * el estado de cierre (15 min antes), y el buffer de edición local para la interfaz.
 */
data class MatchWithPredictionItem(
    val id: String,
    val localNombre: String,
    val localBandera: String,
    val visitanteNombre: String,
    val visitanteBandera: String,
    val fechaHora: String,
    val estado: String,
    val golesLocalReal: Int? = null,
    val golesVisitanteReal: Int? = null,
    val miPronosticoLocal: Int? = null,
    val miPronosticoVisitante: Int? = null,
    val puntosGanados: Int = 0,
    val cerrado: Boolean = false,
    val minutosRestantesParaCierre: Long = 0,
    val torneoNombre: String? = null,
    val torneoCodigo: String? = null,
    val torneoLogoUrl: String? = null,
    // Estado local de edición en la UI antes de guardar
    val editedGolesLocal: Int = miPronosticoLocal ?: 0,
    val editedGolesVisitante: Int = miPronosticoVisitante ?: 0,
    val isEdited: Boolean = false
) {
    /** Determina si el usuario ya registró formalmente un pronóstico en el servidor. */
    val tienePronosticoGuardado: Boolean
        get() = miPronosticoLocal != null && miPronosticoVisitante != null
}

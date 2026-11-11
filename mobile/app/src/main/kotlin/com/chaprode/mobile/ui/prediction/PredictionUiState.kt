package com.chaprode.mobile.ui.prediction

import com.chaprode.mobile.model.MatchWithPredictionItem

/**
 * Estado inmutable de la pantalla de Fixture y Pronósticos.
 *
 * @param isLoading Indica si se está obteniendo el fixture desde el backend.
 * @param matches Lista completa de partidos con los pronósticos previos del usuario.
 * @param savingMatchId Identificador del partido que se encuentra guardando un pronóstico en este instante.
 * @param errorMessage Mensaje de error para mostrar en Snackbar en caso de fallo.
 * @param successMessage Mensaje de confirmación cuando un pronóstico se guarda correctamente.
 * @param selectedTorneoId Identificador del torneo actualmente filtrado ("activo", "todos", etc.).
 * @param torneoNombre Nombre legible del torneo en curso para visualización en encabezados.
 */
data class PredictionUiState(
    val isLoading: Boolean = false,
    val matches: List<MatchWithPredictionItem> = emptyList(),
    val savingMatchId: String? = null,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val selectedTorneoId: String = "activo",
    val torneoNombre: String = "Copa Mundial FIFA 2026"
)

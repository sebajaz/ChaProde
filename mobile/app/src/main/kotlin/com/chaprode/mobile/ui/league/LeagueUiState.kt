package com.chaprode.mobile.ui.league

import com.chaprode.mobile.model.LeagueDetailItem
import com.chaprode.mobile.model.LeagueItem

/**
 * Estado inmutable de la pantalla de Ligas Privadas.
 *
 * @param isLoading Indica si hay una operación de red en ejecución.
 * @param leagues Listado de ligas a las que pertenece el usuario.
 * @param selectedLeagueDetail Detalle completo de la liga seleccionada con su tabla interna.
 * @param isCreateDialogOpen Control de visibilidad del modal de creación.
 * @param isJoinDialogOpen Control de visibilidad del modal de unión por código.
 * @param newLeagueName Nombre ingresado temporalmente en el formulario de creación.
 * @param joinCode Código de invitación escrito para unirse.
 * @param errorMessage Mensaje descriptivo de error en caso de fallo.
 * @param successMessage Mensaje de confirmación cuando una liga es creada o unida con éxito.
 */
data class LeagueUiState(
    val isLoading: Boolean = false,
    val leagues: List<LeagueItem> = emptyList(),
    val selectedLeagueDetail: LeagueDetailItem? = null,
    val isCreateDialogOpen: Boolean = false,
    val isJoinDialogOpen: Boolean = false,
    val newLeagueName: String = "",
    val joinCode: String = "",
    val errorMessage: String? = null,
    val successMessage: String? = null
)

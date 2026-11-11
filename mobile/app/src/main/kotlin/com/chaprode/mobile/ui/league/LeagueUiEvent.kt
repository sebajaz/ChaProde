package com.chaprode.mobile.ui.league

/**
 * Contrato de eventos de usuario para la pantalla de Ligas Privadas de Amigos.
 */
sealed interface LeagueUiEvent {
    /** Carga o refresca las ligas en las que el usuario es participante o creador. */
    object LoadLeagues : LeagueUiEvent
    
    /** Abre o cierra el modal para crear una nueva liga. */
    data class OnOpenCreateDialog(val open: Boolean) : LeagueUiEvent
    
    /** Abre o cierra el modal para unirse a una liga por código alfanumérico. */
    data class OnOpenJoinDialog(val open: Boolean) : LeagueUiEvent
    
    /** Actualiza el nombre ingresado para la nueva liga. */
    data class OnNewLeagueNameChanged(val name: String) : LeagueUiEvent
    
    /** Actualiza el código de acceso escrito para unirse a una liga existente. */
    data class OnJoinCodeChanged(val code: String) : LeagueUiEvent
    
    /** Dispara la creación de la liga en el backend. */
    object OnCreateLeagueClicked : LeagueUiEvent
    
    /** Dispara la unión a la liga por código de invitación. */
    object OnJoinLeagueClicked : LeagueUiEvent
    
    /** Selecciona una liga para ver la tabla interna y lista de miembros. */
    data class OnSelectLeague(val leagueId: String) : LeagueUiEvent
    
    /** Cierra el panel de detalles de la liga seleccionada. */
    object OnCloseDetail : LeagueUiEvent
    
    /** Limpia mensajes de error o éxito temporales. */
    object OnDismissMessage : LeagueUiEvent
}

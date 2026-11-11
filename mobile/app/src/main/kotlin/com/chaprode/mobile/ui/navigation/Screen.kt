package com.chaprode.mobile.ui.navigation

/**
 * Definición centralizada de rutas de navegación en ChaProde Mobile.
 * Cada destino es un objeto tipado que encapsula su identificador de ruta unívoco
 * para el NavHost de Jetpack Navigation Compose.
 *
 * @param route Cadena de texto que identifica la ruta en el grafo de navegación.
 */
sealed class Screen(val route: String) {
    /** Pantalla de inicio de sesión con credenciales (usuario/email y contraseña). */
    object Login : Screen("login")
    
    /** Pantalla de registro para nuevos participantes del prode. */
    object Register : Screen("register")
    
    /** Pantalla de inicio: métricas destacadas, próximo partido y accesos rápidos. */
    object Home : Screen("home")
    
    /** Pantalla de fixture: listado de partidos clasificados y carga de pronósticos. */
    object Fixture : Screen("fixture")
    
    /** Pantalla de ranking: tabla de posiciones general del torneo y podio de líderes. */
    object Ranking : Screen("ranking")
    
    /** Pantalla de ligas: creación, unión por código y torneos de amigos. */
    object Leagues : Screen("leagues")
    
    /** Pantalla de perfil: identidad del usuario, estadísticas acumuladas y ajustes. */
    object Profile : Screen("profile")
}

package com.chaprode.mobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.chaprode.mobile.data.local.preferences.SessionManager
import com.chaprode.mobile.ui.auth.AuthViewModel
import com.chaprode.mobile.ui.auth.LoginScreen
import com.chaprode.mobile.ui.auth.RegisterScreen
import com.chaprode.mobile.ui.components.ChaProdeBottomNav
import com.chaprode.mobile.ui.league.LeagueViewModel
import com.chaprode.mobile.ui.navigation.Screen
import com.chaprode.mobile.ui.prediction.PredictionViewModel
import com.chaprode.mobile.ui.ranking.RankingViewModel
import com.chaprode.mobile.ui.screens.FixtureScreen
import com.chaprode.mobile.ui.screens.HomeScreen
import com.chaprode.mobile.ui.screens.LeaguesScreen
import com.chaprode.mobile.ui.screens.ProfileScreen
import com.chaprode.mobile.ui.screens.RankingScreen
import com.chaprode.mobile.ui.theme.ChaProdeBg
import com.chaprode.mobile.ui.theme.ChaProdeTheme

/**
 * =========================================================================================
 * MainActivity: Punto de entrada principal (Entry Point) de la aplicación Android ChaProde.
 * =========================================================================================
 * Responsabilidades:
 * 1. Inicializar los servicios de persistencia y configuración en el ciclo de vida inicial:
 *    - [SessionManager]: Lee el token JWT y los datos del usuario guardados en SharedPreferences.
 *    - [ApiConfig]: Detecta si corre en emulador o celular físico y resuelve la URL base del backend.
 * 2. Habilitar la visualización Edge-to-Edge (diseño a pantalla completa bajo barras de sistema).
 * 3. Cargar el tema visual de ChaProde ([ChaProdeTheme]) y renderizar el árbol composable raíz ([ChaProdeApp]).
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Inicialización del administrador de sesión (SharedPreferences locales para persistencia de autenticación)
        SessionManager.init(applicationContext)

        // 2. Inicialización de la configuración dinámica de red (IP del backend según entorno de ejecución)
        com.chaprode.mobile.data.remote.ApiConfig.init(applicationContext)

        // 3. Permite que la interfaz se extienda por detrás de la barra de estado y la barra de navegación del sistema
        enableEdgeToEdge()

        // 4. Establece la jerarquía de vistas de Jetpack Compose envuelta en el tema oscuro deportivo de ChaProde
        setContent {
            ChaProdeTheme {
                ChaProdeApp()
            }
        }
    }
}

/**
 * =========================================================================================
 * ChaProdeApp: Contenedor global de la interfaz de usuario y shell de navegación (App Shell).
 * =========================================================================================
 * Controla:
 * - El enrutador [navController] para cambiar de pantalla.
 * - Los ViewModels compartidos que contienen el estado y la lógica de negocio.
 * - La barra de navegación inferior ([ChaProdeBottomNav]), mostrándola solo en pantallas principales.
 * - El grafo de destinos ([NavHost]) conectando cada ruta con su respectiva pantalla composable.
 */
@Composable
fun ChaProdeApp() {
    // Controlador de navegación que almacena el historial de pantallas y la pila hacia atrás (BackStack)
    val navController = rememberNavController()

    // -------------------------------------------------------------------------------------
    // Instanciación de ViewModels (MVVM):
    // Centralizan la lógica de negocio, llamadas a repositorios y flujos de estado (UiState)
    // -------------------------------------------------------------------------------------
    val authViewModel = AuthViewModel()
    val predictionViewModel = PredictionViewModel()
    val rankingViewModel = RankingViewModel()
    val leagueViewModel = LeagueViewModel()

    // Observa en tiempo real la ruta actual en la que se encuentra el usuario
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // -------------------------------------------------------------------------------------
    // Lógica de visibilidad de la barra inferior (BottomNav):
    // Solo debe verse en las 5 secciones principales; en Login y Registro debe ocultarse.
    // -------------------------------------------------------------------------------------
    val mainTabs = listOf(
        Screen.Home.route,
        Screen.Fixture.route,
        Screen.Leagues.route,
        Screen.Ranking.route,
        Screen.Profile.route
    )
    val showBottomNav = currentRoute in mainTabs

    // -------------------------------------------------------------------------------------
    // Destino inicial dinámico:
    // Si ya existe una sesión guardada (token JWT válido), arranca directo en Home;
    // de lo contrario, inicia en la pantalla de Login.
    // -------------------------------------------------------------------------------------
    val startDest = if (SessionManager.isLoggedIn()) Screen.Home.route else Screen.Login.route

    // Estructura Scaffold con fondo Deep Slate y barra inferior condicional
    Scaffold(
        bottomBar = {
            // Renderizado condicional de la barra de navegación inferior de 5 secciones
            if (showBottomNav) {
                ChaProdeBottomNav(
                    currentRoute = currentRoute,
                    onNavigate = { targetScreen ->
                        // Evita navegar a la misma pantalla en la que ya se encuentra el usuario
                        if (currentRoute != targetScreen.route) {
                            navController.navigate(targetScreen.route) {
                                // Vuelve a la pantalla de inicio del grafo para evitar apilar pestañas infinitas
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true // Guarda el estado de la pestaña que dejamos (ej: scroll)
                                }
                                launchSingleTop = true // Evita duplicar pantallas al presionar dos veces la misma pestaña
                                restoreState = true    // Restaura el estado previo al volver a una pestaña
                            }
                        }
                    }
                )
            }
        },
        containerColor = ChaProdeBg
    ) { innerPadding ->
        // Contenedor Box principal que ajusta el padding inferior solo cuando la barra está visible
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = if (showBottomNav) innerPadding.calculateBottomPadding() else 0.dp)
        ) {
            // -----------------------------------------------------------------------------
            // NavHost: Define el mapa completo de pantallas y rutas de la aplicación
            // -----------------------------------------------------------------------------
            NavHost(
                navController = navController,
                startDestination = startDest
            ) {
                // ==========================================
                // 1. Pantalla de Inicio de Sesión (Login)
                // ==========================================
                composable(Screen.Login.route) {
                    LoginScreen(
                        viewModel = authViewModel,
                        onNavigateToRegister = { navController.navigate(Screen.Register.route) },
                        onLoginSuccess = {
                            // Al autenticarse correctamente, va a Home y elimina Login del historial
                            navController.navigate(Screen.Home.route) {
                                popUpTo(Screen.Login.route) { inclusive = true }
                            }
                        }
                    )
                }

                // ==========================================
                // 2. Pantalla de Registro de Usuario
                // ==========================================
                composable(Screen.Register.route) {
                    RegisterScreen(
                        onNavigateToLogin = { navController.popBackStack() },
                        onRegisterSuccess = {
                            // Al registrarse exitosamente con auto-login, va a Home y limpia el historial
                            navController.navigate(Screen.Home.route) {
                                popUpTo(Screen.Login.route) { inclusive = true }
                            }
                        }
                    )
                }

                // ==========================================
                // 3. Pantalla de Inicio (Home Dashboard)
                // ==========================================
                composable(Screen.Home.route) {
                    HomeScreen(
                        viewModel = authViewModel,
                        predictionViewModel = predictionViewModel,
                        rankingViewModel = rankingViewModel,
                        leagueViewModel = leagueViewModel,
                        onNavigateToFixture = {
                            navController.navigate(Screen.Fixture.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        onNavigateToRanking = {
                            navController.navigate(Screen.Ranking.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        onNavigateToLeagues = {
                            navController.navigate(Screen.Leagues.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        onLogout = {
                            // Cierre de sesión seguro: borra SharedPreferences y regresa a Login
                            SessionManager.clearSession()
                            navController.navigate(Screen.Login.route) {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    )
                }

                // ==========================================
                // 4. Pantalla de Fixture y Pronósticos
                // ==========================================
                composable(Screen.Fixture.route) {
                    FixtureScreen(
                        viewModel = predictionViewModel,
                        torneoId = "todos",
                        torneoNombre = "Partidos Mundiales",
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                // ==========================================
                // 5. Pantalla de Tabla de Posiciones y Podio
                // ==========================================
                composable(Screen.Ranking.route) {
                    RankingScreen(
                        viewModel = rankingViewModel,
                        torneoId = "todos",
                        torneoNombre = "Ranking Global",
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                // ==========================================
                // 6. Pantalla de Ligas Privadas
                // ==========================================
                composable(Screen.Leagues.route) {
                    LeaguesScreen(
                        viewModel = leagueViewModel,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                // ==========================================
                // 7. Pantalla de Perfil de Usuario y Ajustes
                // ==========================================
                composable(Screen.Profile.route) {
                    ProfileScreen(
                        predictionViewModel = predictionViewModel,
                        rankingViewModel = rankingViewModel,
                        onLogout = {
                            // Cierre de sesión desde el menú de ajustes de Perfil
                            SessionManager.clearSession()
                            navController.navigate(Screen.Login.route) {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    )
                }
            }
        }
    }
}

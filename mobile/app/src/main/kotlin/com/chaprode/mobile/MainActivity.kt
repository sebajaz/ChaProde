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

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        SessionManager.init(applicationContext)
        com.chaprode.mobile.data.remote.ApiConfig.init(applicationContext)
        enableEdgeToEdge()
        setContent {
            ChaProdeTheme {
                ChaProdeApp()
            }
        }
    }
}

@Composable
fun ChaProdeApp() {
    val navController = rememberNavController()
    val authViewModel = AuthViewModel()
    val predictionViewModel = PredictionViewModel()
    val rankingViewModel = RankingViewModel()
    val leagueViewModel = LeagueViewModel()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Rutas principales que muestran la barra de navegación inferior (excluye Login y Register)
    val mainTabs = listOf(
        Screen.Home.route,
        Screen.Fixture.route,
        Screen.Leagues.route,
        Screen.Ranking.route,
        Screen.Profile.route
    )
    val showBottomNav = currentRoute in mainTabs

    val startDest = if (SessionManager.isLoggedIn()) Screen.Home.route else Screen.Login.route

    Scaffold(
        bottomBar = {
            if (showBottomNav) {
                ChaProdeBottomNav(
                    currentRoute = currentRoute,
                    onNavigate = { targetScreen ->
                        if (currentRoute != targetScreen.route) {
                            navController.navigate(targetScreen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    }
                )
            }
        },
        containerColor = ChaProdeBg
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = if (showBottomNav) innerPadding.calculateBottomPadding() else 0.dp)
        ) {
            NavHost(
                navController = navController,
                startDestination = startDest
            ) {
                composable(Screen.Login.route) {
                    LoginScreen(
                        viewModel = authViewModel,
                        onNavigateToRegister = { navController.navigate(Screen.Register.route) },
                        onLoginSuccess = {
                            navController.navigate(Screen.Home.route) {
                                popUpTo(Screen.Login.route) { inclusive = true }
                            }
                        }
                    )
                }

                composable(Screen.Register.route) {
                    RegisterScreen(
                        onNavigateToLogin = { navController.popBackStack() },
                        onRegisterSuccess = {
                            navController.navigate(Screen.Home.route) {
                                popUpTo(Screen.Login.route) { inclusive = true }
                            }
                        }
                    )
                }

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
                            SessionManager.clearSession()
                            navController.navigate(Screen.Login.route) {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    )
                }

                composable(Screen.Fixture.route) {
                    FixtureScreen(
                        viewModel = predictionViewModel,
                        torneoId = "todos",
                        torneoNombre = "Partidos Mundiales",
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                composable(Screen.Ranking.route) {
                    RankingScreen(
                        viewModel = rankingViewModel,
                        torneoId = "todos",
                        torneoNombre = "Ranking Global",
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                composable(Screen.Leagues.route) {
                    LeaguesScreen(
                        viewModel = leagueViewModel,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                composable(Screen.Profile.route) {
                    ProfileScreen(
                        predictionViewModel = predictionViewModel,
                        rankingViewModel = rankingViewModel,
                        onLogout = {
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

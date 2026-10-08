package com.chaprode.mobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.chaprode.mobile.ui.auth.AuthViewModel
import com.chaprode.mobile.ui.auth.LoginScreen
import com.chaprode.mobile.ui.auth.RegisterScreen
import com.chaprode.mobile.ui.league.LeagueViewModel
import com.chaprode.mobile.ui.navigation.Screen
import com.chaprode.mobile.ui.prediction.PredictionViewModel
import com.chaprode.mobile.ui.ranking.RankingViewModel
import com.chaprode.mobile.ui.screens.FixtureScreen
import com.chaprode.mobile.ui.screens.HomeScreen
import com.chaprode.mobile.ui.screens.LeaguesScreen
import com.chaprode.mobile.ui.screens.RankingScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        com.chaprode.mobile.data.local.preferences.SessionManager.init(applicationContext)
        com.chaprode.mobile.data.remote.ApiConfig.init(applicationContext)
        enableEdgeToEdge()
        setContent {
            ChaProdeApp()
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

    val startDest = if (com.chaprode.mobile.data.local.preferences.SessionManager.isLoggedIn()) Screen.Home.route else Screen.Login.route

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        NavHost(navController = navController, startDestination = startDest) {
            composable(Screen.Login.route) {
                LoginScreen(
                    viewModel = authViewModel,
                    onNavigateToRegister = { navController.navigate(Screen.Register.route) },
                    onLoginSuccess = { navController.navigate(Screen.Home.route) }
                )
            }
            composable(Screen.Register.route) {
                RegisterScreen(
                    onNavigateToLogin = { navController.popBackStack() },
                    onRegisterSuccess = { navController.navigate(Screen.Home.route) }
                )
            }
            composable(Screen.Home.route) {
                HomeScreen(
                    viewModel = authViewModel,
                    onNavigateToFixture = { navController.navigate(Screen.Fixture.route) },
                    onNavigateToRanking = { navController.navigate(Screen.Ranking.route) },
                    onNavigateToLeagues = { navController.navigate(Screen.Leagues.route) },
                    onLogout = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.Home.route) { inclusive = true }
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
        }
    }
}

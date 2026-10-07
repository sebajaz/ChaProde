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
import com.chaprode.mobile.ui.navigation.Screen
import com.chaprode.mobile.ui.prediction.PredictionViewModel
import com.chaprode.mobile.ui.ranking.RankingViewModel
import com.chaprode.mobile.ui.screens.FixtureScreen
import com.chaprode.mobile.ui.screens.HomeScreen
import com.chaprode.mobile.ui.screens.RankingScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        NavHost(navController = navController, startDestination = Screen.Login.route) {
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
                    torneoId = "d96b16df-448a-43ca-9fa7-140af2e63e14",
                    torneoNombre = "Copa Mundial FIFA 2026",
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable(Screen.Ranking.route) {
                RankingScreen(
                    viewModel = rankingViewModel,
                    torneoId = "d96b16df-448a-43ca-9fa7-140af2e63e14",
                    torneoNombre = "Copa Mundial FIFA 2026",
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}

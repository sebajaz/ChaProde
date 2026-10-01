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
import com.chaprode.mobile.ui.AuthViewModel
import com.chaprode.mobile.ui.FixtureViewModel
import com.chaprode.mobile.ui.screens.FixtureScreen
import com.chaprode.mobile.ui.screens.HomeScreen
import com.chaprode.mobile.ui.screens.LoginScreen
import com.chaprode.mobile.ui.screens.RegisterScreen

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
    val fixtureViewModel = FixtureViewModel()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        NavHost(navController = navController, startDestination = "login") {
            composable("login") {
                LoginScreen(
                    viewModel = authViewModel,
                    onNavigateToRegister = { navController.navigate("register") },
                    onLoginSuccess = { navController.navigate("home") }
                )
            }
            composable("register") {
                RegisterScreen(
                    viewModel = authViewModel,
                    onNavigateToLogin = { navController.popBackStack() },
                    onRegisterSuccess = { navController.navigate("home") }
                )
            }
            composable("home") {
                HomeScreen(
                    viewModel = authViewModel,
                    onNavigateToFixture = { navController.navigate("fixture") },
                    onLogout = {
                        navController.navigate("login") {
                            popUpTo("home") { inclusive = true }
                        }
                    }
                )
            }
            composable("fixture") {
                FixtureScreen(
                    viewModel = fixtureViewModel,
                    torneoId = "d96b16df-448a-43ca-9fa7-140af2e63e14",
                    torneoNombre = "Copa Mundial FIFA 2026",
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}

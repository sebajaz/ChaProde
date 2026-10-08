package com.chaprode.mobile.ui.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object Home : Screen("home")
    object Fixture : Screen("fixture")
    object Ranking : Screen("ranking")
    object Leagues : Screen("leagues")
}

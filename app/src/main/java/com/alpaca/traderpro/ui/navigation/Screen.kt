package com.alpaca.traderpro.ui.navigation

sealed class Screen(val route: String) {
    object Onboarding : Screen("onboarding")
    object Home : Screen("home")
    object Logs : Screen("logs")
    object Settings : Screen("settings")
}

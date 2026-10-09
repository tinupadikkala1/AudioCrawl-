package com.audiocrawl.navigation

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Scan : Screen("scan")
    data object Results : Screen("results")
    data object Copy : Screen("copy")
    data object Settings : Screen("settings")
}

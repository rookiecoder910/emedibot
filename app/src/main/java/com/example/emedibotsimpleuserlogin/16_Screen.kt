package com.example.emedibotsimpleuserlogin

sealed class Screen(val route: String) {
    object Login : Screen("login")

    object Main : Screen("main") {
        object Home : Screen("home")
        object Schedule : Screen("schedule")
        object Settings : Screen("settings")
        }
}


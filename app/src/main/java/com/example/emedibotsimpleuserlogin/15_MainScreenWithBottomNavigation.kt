package com.example.emedibotsimpleuserlogin

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
fun MainScreenWithBottomNav(
    isDarkMode: Boolean,
    onToggleDarkMode: () -> Unit,
    onLogout: () -> Unit
) {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            BottomNavBar(navController = navController)
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Main.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Main.Home.route) {
                HomeScreen(
                    onSignOut = { onLogout() },
                    navController = navController
                )
            }

            composable("schedule") {
                ScheduleScreen()
            }

            composable(Screen.Main.Settings.route) {
                SettingsScreen(
                    isDarkMode = isDarkMode,
                    onToggleDarkMode = onToggleDarkMode,
                    onLogoutDone = { onLogout() }
                )
            }

            composable("chatbot") {
                ChatbotScreen()
            }
        }
    }
}
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
    val medicines = listOf(
        Medicine("Tablet A", "8:00 AM"),
        Medicine("Tablet B", "12:00 PM"),
        Medicine("Tablet C", "6:00 PM")
    )

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
                HomeScreen(onSignOut = {
                    // Handle sign-out and navigate back to login screen
                    onLogout()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Main.route) { inclusive = true }
                    }
                })
            }


            composable("schedule") {
                ScheduleScreen() // ViewModel will be provided automatically by Compose
            }



            composable(Screen.Main.Settings.route) {
                SettingsScreen(
                    isDarkMode = isDarkMode,
                    onToggleDarkMode = onToggleDarkMode,
                    onLogoutDone = {
                        onLogout()


                    }
                )
            }
        }
    }
}

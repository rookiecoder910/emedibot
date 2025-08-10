package com.example.emedibotsimpleuserlogin

import android.Manifest
import android.app.NotificationChannel
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Create Notification Channel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "med_reminder_channel",
                "Medicine Reminders",
                android.app.NotificationManager.IMPORTANCE_HIGH
            )
            val manager = getSystemService(android.app.NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }

        // Request notification permission (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                1001
            )
        }

        setContent {
            var isDarkMode by remember { mutableStateOf(false) }
            val colorScheme = if (isDarkMode) darkColorScheme() else lightColorScheme()

            val prefs = remember { IntroPreferences(this) }
            var hasSeenIntro by remember { mutableStateOf(false) }
            var prefsLoaded by remember { mutableStateOf(false) }
            val scope = rememberCoroutineScope()

            // Load intro flag from DataStore
            LaunchedEffect(Unit) {
                prefs.hasSeenIntro.collect { seen ->
                    hasSeenIntro = seen
                    prefsLoaded = true
                }
            }

            MaterialTheme(colorScheme = colorScheme) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    when {
                        !prefsLoaded -> {
                            // Show loading while preferences are being fetched
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator()
                            }
                        }

                        !hasSeenIntro -> {
                            // Intro Screen with Skip & Next buttons
                            IntroScreen(
                                onFinish = {
                                    scope.launch { prefs.setHasSeenIntro(true) }
                                    hasSeenIntro = true
                                },
                                onSkip = {
                                    scope.launch { prefs.setHasSeenIntro(true) }
                                    hasSeenIntro = true
                                }
                            )
                        }

                        else -> {
                            // Main App Navigation
                            val navController = rememberNavController()
                            AppNavGraph(
                                navController = navController,
                                isDarkMode = isDarkMode,
                                onToggleDarkMode = { isDarkMode = !isDarkMode }
                            )
                        }
                    }
                }
            }
        }
    }
}

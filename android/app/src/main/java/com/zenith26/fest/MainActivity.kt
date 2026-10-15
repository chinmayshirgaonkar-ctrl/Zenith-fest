package com.zenith26.fest

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.zenith26.fest.ui.screens.AdminDashboardScreen
import com.zenith26.fest.ui.screens.ServerSettingsDialog
import com.zenith26.fest.ui.screens.StudentPortalScreen
import com.zenith26.fest.ui.theme.Slate900
import com.zenith26.fest.ui.theme.ZenithTheme

enum class Screen {
    STUDENT_PORTAL,
    ADMIN_DASHBOARD
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ZenithTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Slate900
                ) {
                    var currentScreen by remember { mutableStateOf(Screen.STUDENT_PORTAL) }
                    var showSettingsDialog by remember { mutableStateOf(false) }

                    // Handle back button on Android: go back to student portal if in admin screen
                    BackHandler(enabled = currentScreen == Screen.ADMIN_DASHBOARD) {
                        currentScreen = Screen.STUDENT_PORTAL
                    }

                    Crossfade(targetState = currentScreen, label = "screenTransition") { screen ->
                        when (screen) {
                            Screen.STUDENT_PORTAL -> {
                                StudentPortalScreen(
                                    onNavigateToAdmin = { currentScreen = Screen.ADMIN_DASHBOARD },
                                    onOpenSettings = { showSettingsDialog = true }
                                )
                            }
                            Screen.ADMIN_DASHBOARD -> {
                                AdminDashboardScreen(
                                    onNavigateToStudentPortal = { currentScreen = Screen.STUDENT_PORTAL },
                                    onOpenSettings = { showSettingsDialog = true }
                                )
                            }
                        }
                    }

                    if (showSettingsDialog) {
                        ServerSettingsDialog(
                            onDismiss = { showSettingsDialog = false },
                            onSave = {
                                // Re-trigger screen refresh if needed
                            }
                        )
                    }
                }
            }
        }
    }
}

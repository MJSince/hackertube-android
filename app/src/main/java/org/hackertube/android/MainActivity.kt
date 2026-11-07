package org.hackertube.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.core.view.WindowCompat
import org.hackertube.android.data.model.ThemeMode
import org.hackertube.android.ui.navigation.HackerTubeNavHost
import org.hackertube.android.ui.theme.HackerTubeTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)

        val app = application as HackerTubeApp

        setContent {
            val themeMode by app.userPreferences.themeFlow.collectAsState(initial = ThemeMode.SYSTEM)
            val dark = when (themeMode) {
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
            }

            HackerTubeTheme(themeMode = themeMode) {
                HackerTubeNavHost(
                    videoRepository = app.videoRepository,
                    userPreferences = app.userPreferences
                )
            }
        }
    }
}

package org.hackertube.android.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.hackertube.android.BuildConfig
import org.hackertube.android.data.local.UserPreferences
import org.hackertube.android.data.model.ThemeMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(userPreferences: UserPreferences) {
    val theme by userPreferences.themeFlow.collectAsState(initial = ThemeMode.SYSTEM)
    val scope = rememberCoroutineScope()
    var showClearHistory by remember { mutableStateOf(false) }
    var showClearLiked by remember { mutableStateOf(false) }
    var showAbout by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Settings", fontWeight = FontWeight.Bold) })
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                "Appearance",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
            )
            ThemeMode.entries.forEach { mode ->
                ListItem(
                    headlineContent = {
                        Text(
                            when (mode) {
                                ThemeMode.SYSTEM -> "System"
                                ThemeMode.LIGHT -> "Light"
                                ThemeMode.DARK -> "Dark"
                            }
                        )
                    },
                    leadingContent = {
                        RadioButton(
                            selected = theme == mode,
                            onClick = { scope.launch { userPreferences.setTheme(mode) } }
                        )
                    },
                    modifier = Modifier.clickable {
                        scope.launch { userPreferences.setTheme(mode) }
                    }
                )
            }

            HorizontalDivider(Modifier.padding(vertical = 8.dp))

            Text(
                "Data",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
            )
            ListItem(
                headlineContent = { Text("Clear history") },
                modifier = Modifier.clickable { showClearHistory = true }
            )
            ListItem(
                headlineContent = { Text("Clear liked") },
                modifier = Modifier.clickable { showClearLiked = true }
            )

            HorizontalDivider(Modifier.padding(vertical = 8.dp))

            Text(
                "About",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
            )
            ListItem(
                headlineContent = { Text("About HackerTube") },
                supportingContent = { Text("Version ${BuildConfig.VERSION_NAME}") },
                modifier = Modifier.clickable { showAbout = true }
            )
            ListItem(
                headlineContent = { Text("Data credit") },
                supportingContent = {
                    Text("HackerTube dataset · ODC-By 1.0 · hackertube.org")
                }
            )
            Spacer(Modifier.height(24.dp))
        }
    }

    if (showClearHistory) {
        AlertDialog(
            onDismissRequest = { showClearHistory = false },
            title = { Text("Clear history?") },
            text = { Text("This removes your local watch history. It cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch { userPreferences.clearHistory() }
                    showClearHistory = false
                }) { Text("Clear") }
            },
            dismissButton = {
                TextButton(onClick = { showClearHistory = false }) { Text("Cancel") }
            }
        )
    }
    if (showClearLiked) {
        AlertDialog(
            onDismissRequest = { showClearLiked = false },
            title = { Text("Clear liked?") },
            text = { Text("This removes all liked videos from this device.") },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch { userPreferences.clearLiked() }
                    showClearLiked = false
                }) { Text("Clear") }
            },
            dismissButton = {
                TextButton(onClick = { showClearLiked = false }) { Text("Cancel") }
            }
        )
    }
    if (showAbout) {
        AlertDialog(
            onDismissRequest = { showAbout = false },
            title = { Text("About HackerTube") },
            text = {
                Text(
                    "The YouTube videos that Hacker News keeps telling each other to watch.\n\n" +
                        "This Android app presents the open HackerTube dataset with a native Material 3 experience. " +
                        "Videos play through YouTube. Likes, history and Watch later stay on your device.\n\n" +
                        "Data © HackerTube / ODC-By 1.0. Not affiliated with YouTube, Google, Hacker News or Y Combinator."
                )
            },
            confirmButton = {
                TextButton(onClick = { showAbout = false }) { Text("OK") }
            }
        )
    }
}

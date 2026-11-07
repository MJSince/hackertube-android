package org.hackertube.android.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.hackertube.android.data.model.ThemeMode
import org.hackertube.android.data.model.UserLibrary

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "hackertube_prefs")

class UserPreferences(private val context: Context) {

    private val likedKey = stringSetPreferencesKey("liked")
    private val watchLaterKey = stringSetPreferencesKey("watch_later")
    private val historyKey = stringPreferencesKey("history") // comma-separated, newest first
    private val themeKey = stringPreferencesKey("theme")

    val libraryFlow: Flow<UserLibrary> = context.dataStore.data.map { prefs ->
        val history = prefs[historyKey]
            ?.split(",")
            ?.filter { it.isNotBlank() }
            ?: emptyList()
        UserLibrary(
            likedIds = prefs[likedKey] ?: emptySet(),
            watchLaterIds = prefs[watchLaterKey] ?: emptySet(),
            historyIds = history
        )
    }

    val themeFlow: Flow<ThemeMode> = context.dataStore.data.map { prefs ->
        when (prefs[themeKey]) {
            "light" -> ThemeMode.LIGHT
            "dark" -> ThemeMode.DARK
            else -> ThemeMode.SYSTEM
        }
    }

    suspend fun toggleLiked(vid: String) {
        context.dataStore.edit { prefs ->
            val current = prefs[likedKey]?.toMutableSet() ?: mutableSetOf()
            if (vid in current) current.remove(vid) else current.add(vid)
            prefs[likedKey] = current
        }
    }

    suspend fun toggleWatchLater(vid: String) {
        context.dataStore.edit { prefs ->
            val current = prefs[watchLaterKey]?.toMutableSet() ?: mutableSetOf()
            if (vid in current) current.remove(vid) else current.add(vid)
            prefs[watchLaterKey] = current
        }
    }

    suspend fun addToHistory(vid: String) {
        context.dataStore.edit { prefs ->
            val current = prefs[historyKey]
                ?.split(",")
                ?.filter { it.isNotBlank() && it != vid }
                ?.toMutableList()
                ?: mutableListOf()
            current.add(0, vid)
            // Keep last 200
            prefs[historyKey] = current.take(200).joinToString(",")
        }
    }

    suspend fun clearHistory() {
        context.dataStore.edit { it.remove(historyKey) }
    }

    suspend fun clearLiked() {
        context.dataStore.edit { it.remove(likedKey) }
    }

    suspend fun setTheme(mode: ThemeMode) {
        context.dataStore.edit { prefs ->
            prefs[themeKey] = when (mode) {
                ThemeMode.LIGHT -> "light"
                ThemeMode.DARK -> "dark"
                ThemeMode.SYSTEM -> "system"
            }
        }
    }
}

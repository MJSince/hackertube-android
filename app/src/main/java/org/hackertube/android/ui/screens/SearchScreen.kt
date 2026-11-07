package org.hackertube.android.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.hackertube.android.data.local.UserPreferences
import org.hackertube.android.data.model.UserLibrary
import org.hackertube.android.data.model.Video
import org.hackertube.android.data.repository.VideoRepository
import org.hackertube.android.ui.components.VideoListItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    repository: VideoRepository,
    userPreferences: UserPreferences,
    onVideoClick: (String) -> Unit,
    onBack: () -> Unit
) {
    var query by remember { mutableStateOf("") }
    var active by remember { mutableStateOf(true) }
    var results by remember { mutableStateOf<List<Video>>(emptyList()) }
    val library by userPreferences.libraryFlow.collectAsState(initial = UserLibrary())
    val scope = rememberCoroutineScope()
    var searchJob by remember { mutableStateOf<Job?>(null) }

    LaunchedEffect(Unit) {
        repository.ensureLoaded()
    }

    Scaffold { padding ->
        Box(
            Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            SearchBar(
                query = query,
                onQueryChange = {
                    query = it
                    searchJob?.cancel()
                    searchJob = scope.launch {
                        delay(250)
                        results = if (it.isBlank()) emptyList() else repository.search(it)
                    }
                },
                onSearch = {
                    active = false
                    results = repository.search(query)
                },
                active = active,
                onActiveChange = { active = it },
                placeholder = { Text("Search talks, topics, channels…") },
                leadingIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = SearchBarDefaults.colors()
            ) {
                if (results.isEmpty() && query.isNotBlank()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            "No results for “$query”",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn {
                        items(results, key = { it.vid }) { video ->
                            VideoListItem(
                                video = video,
                                isLiked = video.vid in library.likedIds,
                                onClick = { onVideoClick(video.vid) },
                                onToggleLike = {
                                    scope.launch { userPreferences.toggleLiked(video.vid) }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

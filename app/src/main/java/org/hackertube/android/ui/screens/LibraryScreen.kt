package org.hackertube.android.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.hackertube.android.data.local.UserPreferences
import org.hackertube.android.data.model.UserLibrary
import org.hackertube.android.data.model.Video
import org.hackertube.android.data.repository.VideoRepository
import org.hackertube.android.ui.components.VideoListItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    repository: VideoRepository,
    userPreferences: UserPreferences,
    onVideoClick: (String) -> Unit
) {
    val library by userPreferences.libraryFlow.collectAsState(initial = UserLibrary())
    var tab by remember { mutableIntStateOf(0) }
    var watchLater by remember { mutableStateOf<List<Video>>(emptyList()) }
    var history by remember { mutableStateOf<List<Video>>(emptyList()) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(library) {
        repository.ensureLoaded()
        watchLater = repository.getVideosByIds(library.watchLaterIds)
        history = repository.getVideosByIds(library.historyIds)
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Library", fontWeight = FontWeight.Bold) })
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            TabRow(selectedTabIndex = tab) {
                Tab(selected = tab == 0, onClick = { tab = 0 }, text = { Text("Watch later") })
                Tab(selected = tab == 1, onClick = { tab = 1 }, text = { Text("History") })
            }
            val list = if (tab == 0) watchLater else history
            if (list.isEmpty()) {
                Text(
                    text = if (tab == 0)
                        "Nothing in Watch later yet."
                    else
                        "Your watch history is empty.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp)
                )
            } else {
                LazyColumn(Modifier.fillMaxSize()) {
                    items(list, key = { it.vid }) { video ->
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

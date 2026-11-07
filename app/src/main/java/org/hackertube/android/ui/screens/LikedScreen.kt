package org.hackertube.android.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
fun LikedScreen(
    repository: VideoRepository,
    userPreferences: UserPreferences,
    onVideoClick: (String) -> Unit
) {
    val library by userPreferences.libraryFlow.collectAsState(initial = UserLibrary())
    var videos by remember { mutableStateOf<List<Video>>(emptyList()) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(library.likedIds) {
        repository.ensureLoaded()
        videos = repository.getVideosByIds(library.likedIds)
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Liked", fontWeight = FontWeight.Bold) })
        }
    ) { padding ->
        if (videos.isEmpty()) {
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No liked videos yet.\nTap the heart on any talk to save it here.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(32.dp)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                items(videos, key = { it.vid }) { video ->
                    VideoListItem(
                        video = video,
                        isLiked = true,
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

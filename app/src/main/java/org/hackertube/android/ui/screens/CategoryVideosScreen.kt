package org.hackertube.android.ui.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import kotlinx.coroutines.launch
import org.hackertube.android.data.local.UserPreferences
import org.hackertube.android.data.model.UserLibrary
import org.hackertube.android.data.model.Video
import org.hackertube.android.data.repository.VideoRepository
import org.hackertube.android.ui.components.VideoListItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryVideosScreen(
    categoryId: String,
    categoryName: String,
    repository: VideoRepository,
    userPreferences: UserPreferences,
    onVideoClick: (String) -> Unit,
    onBack: () -> Unit
) {
    var videos by remember { mutableStateOf<List<Video>>(emptyList()) }
    val library by userPreferences.libraryFlow.collectAsState(initial = UserLibrary())
    val scope = rememberCoroutineScope()

    LaunchedEffect(categoryId) {
        repository.ensureLoaded()
        videos = repository.getByCategory(categoryId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(categoryName, fontWeight = FontWeight.Bold, maxLines = 1) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            items(videos, key = { it.vid }) { video ->
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

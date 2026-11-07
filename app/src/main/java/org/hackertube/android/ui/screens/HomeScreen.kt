package org.hackertube.android.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.hackertube.android.data.local.UserPreferences
import org.hackertube.android.data.model.Video
import org.hackertube.android.data.repository.VideoRepository
import org.hackertube.android.ui.components.VideoCard
import org.hackertube.android.ui.theme.HnOrange

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    repository: VideoRepository,
    userPreferences: UserPreferences,
    onVideoClick: (String) -> Unit,
    onSearchClick: () -> Unit
) {
    var loading by remember { mutableStateOf(true) }
    var feed by remember { mutableStateOf<List<Video>>(emptyList()) }
    var classics by remember { mutableStateOf<List<Video>>(emptyList()) }
    val library by userPreferences.libraryFlow.collectAsState(
        initial = org.hackertube.android.data.model.UserLibrary()
    )
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        repository.ensureLoaded()
        feed = repository.getHomeFeed(48)
        classics = repository.getAllVideos().take(12)
        loading = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "HackerTube",
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(onClick = onSearchClick) {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        if (loading) {
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = HnOrange)
                    Spacer(Modifier.height(16.dp))
                    Text("Loading HN classics…", style = MaterialTheme.typography.bodyMedium)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                item {
                    Text(
                        text = "HN Classics",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                    )
                }
                item {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(classics, key = { it.vid }) { video ->
                            VideoCard(
                                video = video,
                                isLiked = video.vid in library.likedIds,
                                onClick = { onVideoClick(video.vid) },
                                onToggleLike = {
                                    scope.launch { userPreferences.toggleLiked(video.vid) }
                                },
                                modifier = Modifier.width(280.dp)
                            )
                        }
                    }
                }
                item {
                    Text(
                        text = "For you",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 12.dp)
                    )
                }
                items(feed, key = { it.vid }) { video ->
                    VideoCard(
                        video = video,
                        isLiked = video.vid in library.likedIds,
                        onClick = { onVideoClick(video.vid) },
                        onToggleLike = {
                            scope.launch { userPreferences.toggleLiked(video.vid) }
                        },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }
        }
    }
}

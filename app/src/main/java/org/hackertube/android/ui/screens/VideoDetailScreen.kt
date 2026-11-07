package org.hackertube.android.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.WatchLater
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.hackertube.android.data.local.UserPreferences
import org.hackertube.android.data.model.UserLibrary
import org.hackertube.android.data.model.Video
import org.hackertube.android.data.repository.VideoRepository
import org.hackertube.android.ui.components.VideoListItem
import org.hackertube.android.ui.components.YoutubePlayer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoDetailScreen(
    vid: String,
    repository: VideoRepository,
    userPreferences: UserPreferences,
    onBack: () -> Unit,
    onRelatedClick: (String) -> Unit
) {
    var video by remember { mutableStateOf<Video?>(null) }
    var related by remember { mutableStateOf<List<Video>>(emptyList()) }
    val library by userPreferences.libraryFlow.collectAsState(initial = UserLibrary())
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    LaunchedEffect(vid) {
        repository.ensureLoaded()
        video = repository.getVideo(vid)
        related = repository.getRelated(vid)
        userPreferences.addToHistory(vid)
    }

    val v = video
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Talk", maxLines = 1) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (v != null) {
                        IconButton(onClick = {
                            val send = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, "${v.title}\n${v.youtubeWatchUrl}")
                            }
                            context.startActivity(Intent.createChooser(send, "Share"))
                        }) {
                            Icon(Icons.Default.Share, contentDescription = "Share")
                        }
                    }
                }
            )
        }
    ) { padding ->
        if (v == null) {
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("Video not found")
            }
            return@Scaffold
        }

        LazyColumn(
            Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            item {
                YoutubePlayer(
                    videoId = v.vid,
                    autoplay = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            item {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        text = v.title,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = v.channel,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(8.dp))
                    val meta = buildString {
                        v.hn?.let {
                            if (it.recommenders > 0) append("${it.recommenders} recommenders")
                            if (it.recMonths > 0) {
                                if (isNotEmpty()) append(" · ")
                                append("${it.recMonths} months on HN")
                            }
                        }
                        if (v.score > 0) {
                            if (isNotEmpty()) append(" · ")
                            append("score ${v.score}")
                        }
                    }
                    if (meta.isNotEmpty()) {
                        Text(
                            text = meta,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilledTonalButton(onClick = {
                            scope.launch { userPreferences.toggleLiked(v.vid) }
                        }) {
                            Icon(
                                if (v.vid in library.likedIds) Icons.Filled.Favorite
                                else Icons.Outlined.FavoriteBorder,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.size(6.dp))
                            Text(if (v.vid in library.likedIds) "Liked" else "Like")
                        }
                        FilledTonalButton(onClick = {
                            scope.launch { userPreferences.toggleWatchLater(v.vid) }
                        }) {
                            Icon(
                                Icons.Outlined.WatchLater,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.size(6.dp))
                            Text(
                                if (v.vid in library.watchLaterIds) "Saved"
                                else "Watch later"
                            )
                        }
                        FilledTonalButton(onClick = {
                            context.startActivity(
                                Intent(Intent.ACTION_VIEW, Uri.parse(v.youtubeWatchUrl))
                            )
                        }) {
                            Icon(
                                Icons.Default.OpenInNew,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.size(6.dp))
                            Text("YouTube")
                        }
                    }
                }
            }
            if (v.whyHn.isNotBlank()) {
                item {
                    Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                        Text(
                            "Why HN recommends it",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(v.whyHn, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
            if (v.summary.isNotBlank()) {
                item {
                    Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                        Text(
                            "Summary",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(v.summary, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
            v.quote?.takeIf { it.text.isNotBlank() }?.let { q ->
                item {
                    Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                        Text(
                            "\u201C${q.text}\u201D",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                        if (q.year.isNotBlank()) {
                            Text(
                                "— HN · ${q.year}",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
            if (v.categories.isNotEmpty()) {
                item {
                    Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                        Text(
                            "Topics",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            v.categories.joinToString(" · ") { it.name },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            if (related.isNotEmpty()) {
                item {
                    Text(
                        "Related",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
                    )
                }
                items(related, key = { it.vid }) { r ->
                    VideoListItem(
                        video = r,
                        isLiked = r.vid in library.likedIds,
                        onClick = { onRelatedClick(r.vid) },
                        onToggleLike = {
                            scope.launch { userPreferences.toggleLiked(r.vid) }
                        }
                    )
                }
            }
            item { Spacer(Modifier.height(32.dp)) }
        }
    }
}

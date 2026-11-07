package org.hackertube.android.data.repository

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.hackertube.android.data.model.Area
import org.hackertube.android.data.model.RelatedEntry
import org.hackertube.android.data.model.Taxonomy
import org.hackertube.android.data.model.Video
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.zip.GZIPInputStream

/**
 * Loads the open HackerTube dataset shipped as assets.
 * videos.jsonl is expected (optionally gzipped as videos.jsonl.gz).
 */
class VideoRepository(private val context: Context) {

    private val gson = Gson()
    private val mutex = Mutex()

    @Volatile private var videos: List<Video>? = null
    @Volatile private var videoMap: Map<String, Video>? = null
    @Volatile private var taxonomy: Taxonomy? = null
    @Volatile private var relatedMap: Map<String, List<String>>? = null

    suspend fun ensureLoaded() = withContext(Dispatchers.IO) {
        mutex.withLock {
            if (videos != null) return@withLock
            loadVideos()
            loadTaxonomy()
            loadRelated()
        }
    }

    private fun loadVideos() {
        val assetManager = context.assets
        val names = assetManager.list("")?.toList().orEmpty()
        val stream = when {
            "videos.jsonl.gz" in names -> GZIPInputStream(assetManager.open("videos.jsonl.gz"))
            "videos.jsonl" in names -> assetManager.open("videos.jsonl")
            else -> {
                // Fallback: empty catalog so UI still works
                videos = emptyList()
                videoMap = emptyMap()
                return
            }
        }
        val list = mutableListOf<Video>()
        BufferedReader(InputStreamReader(stream, Charsets.UTF_8)).use { reader ->
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                val l = line ?: continue
                if (l.isBlank()) continue
                try {
                    val v = gson.fromJson(l, Video::class.java)
                    if (v.vid.isNotBlank() && v.shelfLife != "news") {
                        list.add(v)
                    }
                } catch (_: Exception) {
                    // skip malformed lines
                }
            }
        }
        // Already sorted by score descending in the export
        videos = list
        videoMap = list.associateBy { it.vid }
    }

    private fun loadTaxonomy() {
        try {
            context.assets.open("taxonomy.json").use { input ->
                val text = input.bufferedReader().readText()
                taxonomy = gson.fromJson(text, Taxonomy::class.java)
            }
        } catch (_: Exception) {
            taxonomy = Taxonomy()
        }
    }

    private fun loadRelated() {
        try {
            val map = mutableMapOf<String, List<String>>()
            context.assets.open("related.jsonl").bufferedReader().useLines { lines ->
                lines.forEach { line ->
                    if (line.isBlank()) return@forEach
                    try {
                        val entry = gson.fromJson(line, RelatedEntry::class.java)
                        map[entry.vid] = entry.related.map { it.vid }
                    } catch (_: Exception) { }
                }
            }
            relatedMap = map
        } catch (_: Exception) {
            relatedMap = emptyMap()
        }
    }

    fun getAllVideos(): List<Video> = videos.orEmpty()

    fun getVideo(vid: String): Video? = videoMap?.get(vid)

    fun getHomeFeed(limit: Int = 60): List<Video> {
        val all = videos.orEmpty()
        if (all.isEmpty()) return emptyList()
        // Mix: top scored + some variety from middle ranks
        val top = all.take(40)
        val mid = all.drop(40).shuffled().take(20)
        return (top + mid).distinctBy { it.vid }.take(limit)
    }

    fun getLongs(limit: Int = 80): List<Video> {
        return videos.orEmpty()
            .filter { it.isLongForm || it.score >= 4.0 }
            .take(limit)
    }

    fun getByCategory(categoryId: String): List<Video> {
        return videos.orEmpty().filter { v ->
            v.categories.any { it.id == categoryId }
        }
    }

    fun getByArea(areaId: String): List<Video> {
        return videos.orEmpty().filter { v ->
            v.categories.any { it.area == areaId }
        }
    }

    fun search(query: String, limit: Int = 80): List<Video> {
        val q = query.trim().lowercase()
        if (q.isEmpty()) return emptyList()
        val terms = q.split(Regex("\\s+")).filter { it.length > 1 }
        return videos.orEmpty()
            .map { v ->
                var score = 0.0
                val title = v.title.lowercase()
                val channel = v.channel.lowercase()
                val summary = v.summary.lowercase()
                val tags = v.tags.joinToString(" ").lowercase()
                val topics = v.primaryTopic.lowercase() + " " + v.categories.joinToString(" ") { it.name.lowercase() }
                if (title.contains(q)) score += 10
                if (channel.contains(q)) score += 6
                terms.forEach { t ->
                    if (title.contains(t)) score += 3
                    if (tags.contains(t)) score += 2
                    if (topics.contains(t)) score += 2
                    if (summary.contains(t)) score += 1
                    if (channel.contains(t)) score += 1.5
                }
                score += v.score * 0.05
                v to score
            }
            .filter { it.second > 0 }
            .sortedByDescending { it.second }
            .take(limit)
            .map { it.first }
    }

    fun getRelated(vid: String, limit: Int = 12): List<Video> {
        val ids = relatedMap?.get(vid).orEmpty().take(limit)
        return ids.mapNotNull { videoMap?.get(it) }
    }

    fun getTaxonomy(): Taxonomy = taxonomy ?: Taxonomy()

    fun getAreas(): List<Area> = getTaxonomy().areas

    fun getVideosByIds(ids: Collection<String>): List<Video> {
        val map = videoMap ?: return emptyList()
        return ids.mapNotNull { map[it] }
    }
}

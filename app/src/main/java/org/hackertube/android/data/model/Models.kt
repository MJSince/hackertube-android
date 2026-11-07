package org.hackertube.android.data.model

import com.google.gson.annotations.SerializedName

data class Video(
    val vid: String,
    val url: String = "",
    val title: String = "",
    val channel: String = "",
    val summary: String = "",
    @SerializedName("why_hn") val whyHn: String = "",
    @SerializedName("content_type") val contentType: String = "",
    val level: String = "",
    @SerializedName("shelf_life") val shelfLife: String = "evergreen",
    @SerializedName("primary_topic") val primaryTopic: String = "",
    val tags: List<String> = emptyList(),
    val categories: List<CategoryRef> = emptyList(),
    val quote: Quote? = null,
    val hn: HnStats? = null,
    val score: Double = 0.0
) {
    val thumbnailUrl: String
        get() = "https://i.ytimg.com/vi/$vid/hqdefault.jpg"

    val maxresThumbnailUrl: String
        get() = "https://i.ytimg.com/vi/$vid/maxresdefault.jpg"

    val youtubeWatchUrl: String
        get() = "https://www.youtube.com/watch?v=$vid"

    val isLongForm: Boolean
        get() = contentType in listOf("course", "lecture", "talk", "documentary") || score >= 5.0
}

data class CategoryRef(
    val id: String = "",
    val name: String = "",
    val area: String = "",
    @SerializedName("area_name") val areaName: String = ""
)

data class Quote(
    val text: String = "",
    @SerializedName("hn_comment_id") val hnCommentId: Long = 0,
    val year: String = ""
)

data class HnStats(
    val mentions: Int = 0,
    val recommenders: Int = 0,
    @SerializedName("rec_months") val recMonths: Int = 0,
    @SerializedName("strong_recs") val strongRecs: Int = 0,
    @SerializedName("first_year") val firstYear: Int = 0,
    @SerializedName("last_year") val lastYear: Int = 0
)

data class Taxonomy(
    val version: String = "",
    val areas: List<Area> = emptyList()
)

data class Area(
    val id: String = "",
    val name: String = "",
    val categories: List<Category> = emptyList()
)

data class Category(
    val id: String = "",
    val name: String = "",
    val description: String = ""
)

data class RelatedEntry(
    val vid: String = "",
    val related: List<RelatedItem> = emptyList()
)

data class RelatedItem(
    val vid: String = "",
    @SerializedName("shared_recommenders") val sharedRecommenders: Int = 0
)

enum class ThemeMode {
    SYSTEM, LIGHT, DARK
}

data class UserLibrary(
    val likedIds: Set<String> = emptySet(),
    val watchLaterIds: Set<String> = emptySet(),
    val historyIds: List<String> = emptyList() // newest first
)

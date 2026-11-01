package com.example.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class DramaResponse(
    @Json(name = "success") val success: Boolean = false,
    @Json(name = "data") val data: List<DramaItem>? = emptyList(),
    @Json(name = "count") val count: Int? = null
)

@JsonClass(generateAdapter = true)
data class DramaItem(
    @Json(name = "url") val url: String = "",
    @Json(name = "image") val image: String? = null,
    @Json(name = "title") val title: String = "",
    @Json(name = "genres") val genres: String? = null,
    @Json(name = "badge") val badge: String? = null,
    @Json(name = "rating") val rating: String? = null,
    @Json(name = "country") val country: String? = null,
    @Json(name = "year") val year: String? = null,
    @Json(name = "views") val views: String? = null
) {
    val cleanImageUrl: String
        get() = if (image?.startsWith("http") == true) image else "https://k.netmovie.site/favicon.png"

    val displayCountry: String
        get() = country ?: "Korean"
}

@JsonClass(generateAdapter = true)
data class DramaDetailResponse(
    @Json(name = "success") val success: Boolean = false,
    @Json(name = "data") val data: DramaDetailData? = null
)

@JsonClass(generateAdapter = true)
data class DramaDetailData(
    @Json(name = "type") val type: String? = "drama",
    @Json(name = "title") val title: String = "",
    @Json(name = "alt_title") val altTitle: String? = null,
    @Json(name = "image") val image: String? = null,
    @Json(name = "tag") val tag: String? = null,
    @Json(name = "description") val description: String? = null,
    @Json(name = "rating") val rating: String? = null,
    @Json(name = "year") val year: String? = null,
    @Json(name = "duration") val duration: String? = null,
    @Json(name = "country") val country: String? = null,
    @Json(name = "language") val language: String? = null,
    @Json(name = "genres") val genres: String? = null,
    @Json(name = "views") val views: String? = null,
    @Json(name = "embed_url") val embedUrl: String? = null,
    @Json(name = "watch_url") val watchUrl: String? = null,
    @Json(name = "episodes") val episodes: List<DramaEpisodeItem>? = emptyList(),
    @Json(name = "related") val related: List<DramaItem>? = emptyList()
)

@JsonClass(generateAdapter = true)
data class DramaEpisodeItem(
    @Json(name = "url") val url: String = "",
    @Json(name = "title") val title: String = "",
    @Json(name = "number") val number: String? = null
)

@JsonClass(generateAdapter = true)
data class DramaWatchResponse(
    @Json(name = "success") val success: Boolean = false,
    @Json(name = "data") val data: DramaWatchData? = null
)

@JsonClass(generateAdapter = true)
data class DramaWatchData(
    @Json(name = "title") val title: String? = null,
    @Json(name = "embed_url") val embedUrl: String? = null,
    @Json(name = "download_url") val downloadUrl: String? = null
)

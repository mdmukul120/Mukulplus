package com.example.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CatalogEnvelope(
    @Json(name = "result") val result: CatalogResult? = null
)

@JsonClass(generateAdapter = true)
data class CatalogResult(
    @Json(name = "full") val full: List<CatalogCategory> = emptyList()
)

@JsonClass(generateAdapter = true)
data class CatalogCategory(
    @Json(name = "id") val id: Int = 0,
    @Json(name = "name") val name: String = "",
    @Json(name = "index") val index: Int = 0,
    @Json(name = "movies") val movies: List<MovieItem> = emptyList()
)

@JsonClass(generateAdapter = true)
data class MovieItem(
    @Json(name = "kinopoisk_id") val kinopoiskId: Long? = null,
    @Json(name = "id") val id: Long? = null,
    @Json(name = "type") val type: String? = "movie",
    @Json(name = "title_ru") val titleRu: String? = null,
    @Json(name = "title_en") val titleEn: String? = null,
    @Json(name = "title") val title: String? = null,
    @Json(name = "name") val name: String? = null,
    @Json(name = "poster") val poster: String? = null,
    @Json(name = "description") val description: String? = null,
    @Json(name = "year") val year: Any? = null, // can be int or string
    @Json(name = "imdb") val imdb: Any? = null, // can be double or object
    @Json(name = "duration") val duration: Int? = null,
    @Json(name = "player") val player: List<PlayerItem>? = null
) {
    val movieId: Long
        get() = kinopoiskId ?: id ?: 0L

    val displayTitle: String
        get() = titleEn?.takeIf { it.isNotBlank() }
            ?: title?.takeIf { it.isNotBlank() }
            ?: name?.takeIf { it.isNotBlank() }
            ?: titleRu?.takeIf { it.isNotBlank() }
            ?: "Untitled"

    val displayYear: String
        get() = when (year) {
            is Number -> year.toInt().toString()
            is String -> year
            else -> ""
        }

    val displayRating: String
        get() = when (imdb) {
            is Number -> String.format("%.1f", imdb.toDouble())
            is String -> imdb
            else -> ""
        }

    val isSeries: Boolean
        get() = type.equals("serial", ignoreCase = true)
}

@JsonClass(generateAdapter = true)
data class PlayerItem(
    @Json(name = "url") val url: String = "",
    @Json(name = "translator") val translator: String? = null,
    @Json(name = "quality") val quality: String? = null,
    @Json(name = "source") val source: String? = "iframe",
    @Json(name = "server") val server: Int? = 0
)

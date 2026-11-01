package com.example.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class IptvFeed(
    @Json(name = "title") val title: String? = null,
    @Json(name = "description") val description: String? = null,
    @Json(name = "status") val status: String? = null,
    @Json(name = "total_channels") val totalChannels: Int? = 0,
    @Json(name = "last_updated") val lastUpdated: String? = null,
    @Json(name = "channels") val channels: List<ChannelItem> = emptyList()
)

@JsonClass(generateAdapter = true)
data class ChannelItem(
    @Json(name = "id") val id: String = "",
    @Json(name = "name") val name: String = "",
    @Json(name = "tvg_name") val tvgName: String? = null,
    @Json(name = "tvg_id") val tvgId: String? = null,
    @Json(name = "logo") val logo: String? = null,
    @Json(name = "group") val group: String? = "General",
    @Json(name = "stream_url") val streamUrl: String = "",
    @Json(name = "proxy_stream_url") val proxyStreamUrl: String? = null,
    @Json(name = "status") val status: String? = "online",
    @Json(name = "latency_ms") val latencyMs: Int? = null,
    @Json(name = "source_playlist") val sourcePlaylist: String? = null,
    @Json(name = "checked_at") val checkedAt: String? = null
) {
    val cleanName: String
        get() = name.replace("MOV | ", "")
            .replace("MIXER | ", "")
            .replace("EN | ", "")
            .replace("BD | ", "")
            .trim()

    val displayGroup: String
        get() = when {
            group.isNullOrBlank() -> "Other"
            group.contains("Bangla", ignoreCase = true) -> "Bangla"
            group.contains("Sport", ignoreCase = true) -> "Sports"
            group.contains("Movie", ignoreCase = true) || group.contains("Cinema", ignoreCase = true) -> "Movies"
            group.contains("India", ignoreCase = true) || group.contains("Hindi", ignoreCase = true) -> "India"
            group.contains("News", ignoreCase = true) -> "News"
            group.contains("Music", ignoreCase = true) -> "Music"
            group.contains("Kids", ignoreCase = true) || group.contains("Cartoon", ignoreCase = true) -> "Kids"
            else -> group.trim()
        }
}

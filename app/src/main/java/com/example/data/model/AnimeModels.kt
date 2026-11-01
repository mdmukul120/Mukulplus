package com.example.data.model

data class AnimeItem(
    val title: String,
    val link: String,
    val img: String,
    val status: String = "Ongoing",
    val epInfo: String = "EP Added",
    val labels: List<String> = emptyList(),
    val excerpt: String = "",
    val episodeUrls: List<String> = emptyList()
) {
    val cleanImageUrl: String
        get() = img.replace(Regex("/s\\d+(-c)?/"), "/s500/")
            .replace(Regex("/w\\d+-h\\d+-[^/]*/"), "/s500/")
            .replace(Regex("=s\\d+(-c)?"), "=s500")
}

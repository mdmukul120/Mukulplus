package com.example.data.api

import com.example.data.model.*
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONObject
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

interface IptvService {
    @GET("api/iptv/live.json")
    suspend fun getLiveFeed(): IptvFeed
}

interface NetStreamService {
    @GET("catalog")
    suspend fun getCatalog(): CatalogEnvelope
}

interface DramaService {
    @GET("api.php")
    suspend fun getHome(@Query("action") action: String = "home"): DramaResponse

    @GET("api.php")
    suspend fun getPage(
        @Query("action") action: String = "page",
        @Query("path") path: String
    ): DramaResponse

    @GET("api.php")
    suspend fun search(
        @Query("action") action: String = "search",
        @Query("q") query: String
    ): DramaResponse

    @GET("api.php")
    suspend fun getDetail(
        @Query("action") action: String = "detail",
        @Query("url") url: String
    ): DramaDetailResponse

    @GET("api.php")
    suspend fun getWatch(
        @Query("action") action: String = "watch",
        @Query("url") url: String
    ): DramaWatchResponse
}

object NetworkClient {
    val okHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BASIC
            })
            .build()
    }

    private val moshiConverterFactory by lazy {
        MoshiConverterFactory.create()
    }

    val iptvService: IptvService by lazy {
        Retrofit.Builder()
            .baseUrl("https://mukul-ott.ai.studio/")
            .client(okHttpClient)
            .addConverterFactory(moshiConverterFactory)
            .build()
            .create(IptvService::class.java)
    }

    val netStreamService: NetStreamService by lazy {
        Retrofit.Builder()
            .baseUrl("https://mapi.elochkaigolochla.com/api/v1/")
            .client(okHttpClient)
            .addConverterFactory(moshiConverterFactory)
            .build()
            .create(NetStreamService::class.java)
    }

    val dramaService: DramaService by lazy {
        Retrofit.Builder()
            .baseUrl("https://k.netmovie.site/")
            .client(okHttpClient)
            .addConverterFactory(moshiConverterFactory)
            .build()
            .create(DramaService::class.java)
    }

    fun fetchAnimeFeed(label: String = "", query: String = ""): List<AnimeItem> {
        val url = when {
            query.isNotBlank() -> "https://www.animostream.xyz/feeds/posts/default?alt=json&max-results=30&q=${query}"
            label.isNotBlank() && label != "All" -> "https://www.animostream.xyz/feeds/posts/default/-/${label}?alt=json&max-results=30"
            else -> "https://www.animostream.xyz/feeds/posts/default?alt=json&max-results=30"
        }

        val request = Request.Builder()
            .url(url)
            .header("User-Agent", "Mozilla/5.0 (Linux; Android 14) MukulPlus/1.0")
            .build()

        val response = okHttpClient.newCall(request).execute()
        val body = response.body?.string() ?: return emptyList()

        val items = mutableListOf<AnimeItem>()
        try {
            val json = JSONObject(body)
            val feed = json.optJSONObject("feed") ?: return emptyList()
            val entries = feed.optJSONArray("entry") ?: return emptyList()

            for (i in 0 until entries.length()) {
                val entry = entries.getJSONObject(i)
                val title = entry.optJSONObject("title")?.optString("\$t") ?: "Anime"
                
                var link = ""
                val links = entry.optJSONArray("link")
                if (links != null) {
                    for (j in 0 until links.length()) {
                        val l = links.getJSONObject(j)
                        if (l.optString("rel") == "alternate") {
                            link = l.optString("href")
                            break
                        }
                    }
                }

                val mediaThumb = entry.optJSONObject("media\$thumbnail")?.optString("url") ?: ""
                val content = entry.optJSONObject("content")?.optString("\$t") ?: ""

                // Extract categories/labels
                val labelsList = mutableListOf<String>()
                val catArray = entry.optJSONArray("category")
                if (catArray != null) {
                    for (k in 0 until catArray.length()) {
                        val c = catArray.getJSONObject(k)
                        c.optString("term").takeIf { it.isNotBlank() }?.let { labelsList.add(it) }
                    }
                }

                val status = when {
                    content.contains("complete", ignoreCase = true) -> "Complete"
                    labelsList.contains("Movies") -> "Movie"
                    else -> "Ongoing"
                }

                // Extract stream links from animeData JSON in content
                val streamLinks = mutableListOf<String>()
                val m = Pattern.compile("var\\s+animeData\\s*=\\s*(\\{.*?\\});", Pattern.DOTALL).matcher(content)
                if (m.find()) {
                    try {
                        val dataJson = JSONObject(m.group(1) ?: "")
                        val s1 = dataJson.optJSONObject("s1")
                        val abyss = s1?.optJSONArray("abyss")
                        if (abyss != null) {
                            for (a in 0 until abyss.length()) {
                                streamLinks.add(abyss.getString(a))
                            }
                        }
                    } catch (e: Exception) {}
                }

                items.add(
                    AnimeItem(
                        title = title,
                        link = link,
                        img = mediaThumb,
                        status = status,
                        epInfo = if (status == "Complete") "Complete" else "Latest Dub",
                        labels = labelsList,
                        excerpt = "Hindi & Multi-Audio Anime Streaming",
                        episodeUrls = streamLinks
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return items
    }
}

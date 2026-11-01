package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.data.api.NetworkClient
import com.example.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

class MukulRepository(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("mukul_plus_prefs", Context.MODE_PRIVATE)

    private val _channels = MutableStateFlow<List<ChannelItem>>(emptyList())
    val channels: StateFlow<List<ChannelItem>> = _channels.asStateFlow()

    private val _categories = MutableStateFlow<List<CatalogCategory>>(emptyList())
    val categories: StateFlow<List<CatalogCategory>> = _categories.asStateFlow()

    private val _dramas = MutableStateFlow<List<DramaItem>>(emptyList())
    val dramas: StateFlow<List<DramaItem>> = _dramas.asStateFlow()

    private val _animeList = MutableStateFlow<List<AnimeItem>>(emptyList())
    val animeList: StateFlow<List<AnimeItem>> = _animeList.asStateFlow()

    private val _favoriteChannelIds = MutableStateFlow<Set<String>>(loadFavoriteChannelIds())
    val favoriteChannelIds: StateFlow<Set<String>> = _favoriteChannelIds.asStateFlow()

    private val _favoriteMovieIds = MutableStateFlow<Set<Long>>(loadFavoriteMovieIds())
    val favoriteMovieIds: StateFlow<Set<Long>> = _favoriteMovieIds.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    suspend fun fetchAllData(forceRefresh: Boolean = false) {
        if (!forceRefresh && _channels.value.isNotEmpty()) return

        _isLoading.value = true
        _error.value = null

        withContext(Dispatchers.IO) {
            try {
                // 1. Fetch Live IPTV Channels
                val iptvFeed = NetworkClient.iptvService.getLiveFeed()
                val liveChannels = iptvFeed.channels.filter { it.streamUrl.isNotBlank() }
                _channels.value = liveChannels
            } catch (e: Exception) {
                _error.value = "Failed to load live channels: ${e.localizedMessage}"
            }

            try {
                // 2. Fetch Catalog (NetStream Movies & Series)
                val catEnvelope = NetworkClient.netStreamService.getCatalog()
                val cats = catEnvelope.result?.full ?: emptyList()
                _categories.value = cats
            } catch (e: Exception) {}

            try {
                // 3. Fetch NetDrama (Korean, Chinese, Asian Dramas)
                val dramaRes = NetworkClient.dramaService.getHome()
                if (dramaRes.success && !dramaRes.data.isNullOrEmpty()) {
                    _dramas.value = dramaRes.data
                }
            } catch (e: Exception) {}

            try {
                // 4. Fetch Anime (NetAnimo)
                val animes = NetworkClient.fetchAnimeFeed(label = "Trending")
                if (animes.isNotEmpty()) {
                    _animeList.value = animes
                }
            } catch (e: Exception) {}

            _isLoading.value = false
        }
    }

    suspend fun fetchDramasByCategory(category: String) {
        withContext(Dispatchers.IO) {
            try {
                val res = if (category.isBlank() || category.equals("All", ignoreCase = true)) {
                    NetworkClient.dramaService.getHome()
                } else {
                    NetworkClient.dramaService.getPage(path = "/$category")
                }
                if (res.success && !res.data.isNullOrEmpty()) {
                    _dramas.value = res.data
                }
            } catch (e: Exception) {}
        }
    }

    suspend fun searchDramas(query: String) {
        withContext(Dispatchers.IO) {
            try {
                val res = NetworkClient.dramaService.search(query = query)
                if (res.success && !res.data.isNullOrEmpty()) {
                    _dramas.value = res.data
                }
            } catch (e: Exception) {}
        }
    }

    suspend fun getDramaDetail(url: String): DramaDetailData? {
        return withContext(Dispatchers.IO) {
            try {
                val res = NetworkClient.dramaService.getDetail(url = url)
                if (res.success) res.data else null
            } catch (e: Exception) {
                null
            }
        }
    }

    suspend fun getDramaWatch(url: String): DramaWatchData? {
        return withContext(Dispatchers.IO) {
            try {
                val res = NetworkClient.dramaService.getWatch(url = url)
                if (res.success) res.data else null
            } catch (e: Exception) {
                null
            }
        }
    }

    suspend fun fetchAnime(label: String = "", query: String = "") {
        withContext(Dispatchers.IO) {
            try {
                val items = NetworkClient.fetchAnimeFeed(label = label, query = query)
                if (items.isNotEmpty()) {
                    _animeList.value = items
                }
            } catch (e: Exception) {}
        }
    }

    private fun loadFavoriteChannelIds(): Set<String> {
        return prefs.getStringSet("fav_channels", emptySet())?.toSet() ?: emptySet()
    }

    private fun loadFavoriteMovieIds(): Set<Long> {
        val stringSet = prefs.getStringSet("fav_movies", emptySet()) ?: emptySet()
        return stringSet.mapNotNull { it.toLongOrNull() }.toSet()
    }

    fun toggleFavoriteChannel(channelId: String) {
        val current = _favoriteChannelIds.value.toMutableSet()
        if (current.contains(channelId)) {
            current.remove(channelId)
        } else {
            current.add(channelId)
        }
        _favoriteChannelIds.value = current
        prefs.edit().putStringSet("fav_channels", current).apply()
    }

    fun toggleFavoriteMovie(movieId: Long) {
        val current = _favoriteMovieIds.value.toMutableSet()
        if (current.contains(movieId)) {
            current.remove(movieId)
        } else {
            current.add(movieId)
        }
        _favoriteMovieIds.value = current
        val stringSet = current.map { it.toString() }.toSet()
        prefs.edit().putStringSet("fav_movies", stringSet).apply()
    }
}

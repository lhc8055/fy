package com.so.movie.danmaku

import com.google.gson.JsonParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

/**
 * 弹弹play 开放平台 API 客户端
 * 提供弹幕搜索和获取功能
 */
class DanDanPlayApi {

    private val httpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .build()
    }

    companion object {
        private const val BASE_URL = "https://api.dandanplay.net"
        private const val APP_ID = "soanime"
        private const val VERSION = "1.3.0"

        @Volatile
        private var instance: DanDanPlayApi? = null

        fun getInstance(): DanDanPlayApi {
            return instance ?: synchronized(this) {
                instance ?: DanDanPlayApi().also { instance = it }
            }
        }
    }

    /**
     * 搜索弹幕
     * @param keyword 搜索关键词（番剧标题）
     * @return 搜索结果列表
     */
    suspend fun searchDanmaku(keyword: String): List<DanDanSearchResult> = withContext(Dispatchers.IO) {
        try {
            val url = "$BASE_URL/search/episodes?anime=${java.net.URLEncoder.encode(keyword, "UTF-8")}"
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "SOAnime/$VERSION")
                .header("Accept", "application/json")
                .build()

            val response = httpClient.newCall(request).execute()
            val json = response.use {
                if (!it.isSuccessful) return@withContext emptyList()
                it.body?.string() ?: return@withContext emptyList()
            }

            val root = JsonParser.parseString(json).asJsonObject
            val animes = root.getAsJsonArray("Animes") ?: return@withContext emptyList()

            val results = mutableListOf<DanDanSearchResult>()
            for (anime in animes) {
                val animeObj = anime.asJsonObject
                val animeId = animeObj.get("AnimeId")?.asInt ?: continue
                val animeTitle = animeObj.get("AnimeTitle")?.asString ?: ""
                val episodes = animeObj.getAsJsonArray("Episodes") ?: continue

                for (ep in episodes) {
                    val epObj = ep.asJsonObject
                    val episodeId = epObj.get("EpisodeId")?.asInt ?: continue
                    val episodeTitle = epObj.get("EpisodeTitle")?.asString ?: ""
                    results.add(DanDanSearchResult(animeId, animeTitle, episodeId, episodeTitle))
                }
            }

            results
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * 获取弹幕列表
     * @param episodeId 弹弹play 剧集 ID
     * @return 弹幕列表
     */
    suspend fun getDanmaku(episodeId: Int): List<DanmakuEntry> = withContext(Dispatchers.IO) {
        try {
            val url = "$BASE_URL/comment/$episodeId?withRelated=true"
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "SOAnime/$VERSION")
                .header("Accept", "application/json")
                .build()

            val response = httpClient.newCall(request).execute()
            val json = response.use {
                if (!it.isSuccessful) return@withContext emptyList()
                it.body?.string() ?: return@withContext emptyList()
            }

            val root = JsonParser.parseString(json).asJsonObject
            val comments = root.getAsJsonArray("Comments") ?: return@withContext emptyList()

            val danmakuList = mutableListOf<DanmakuEntry>()
            for (comment in comments) {
                val commentObj = comment.asJsonObject
                val p = commentObj.get("p")?.asString ?: continue
                val m = commentObj.get("m")?.asString ?: continue
                val entry = parseDanDanComment(p, m)
                if (entry != null) {
                    danmakuList.add(entry)
                }
            }

            // 按时间排序
            danmakuList.sortedBy { it.time }
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * 通过标题和集数搜索并获取弹幕
     * @param title 番剧标题
     * @param episode 集数（从1开始）
     * @return 弹幕列表
     */
    suspend fun getDanmakuByTitle(title: String, episode: Int): List<DanmakuEntry> {
        val searchResults = searchDanmaku(title)
        if (searchResults.isEmpty()) return emptyList()

        // 找到最匹配的结果
        val matched = searchResults.find {
            it.animeTitle.contains(title, ignoreCase = true) ||
            title.contains(it.animeTitle, ignoreCase = true)
        } ?: searchResults.first()

        // 找到对应集数
        val episodeResults = searchResults.filter { it.animeId == matched.animeId }
        val targetEpisode = episodeResults.find { it.episodeTitle.contains(episode.toString()) }
            ?: episodeResults.getOrNull(episode - 1)
            ?: matched

        return getDanmaku(targetEpisode.episodeId)
    }
}

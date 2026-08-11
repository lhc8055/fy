package com.so.movie.metadata

import com.google.gson.Gson
import com.google.gson.JsonParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

/**
 * Bangumi (bgm.tv) API 客户端
 * 提供番剧/影视元数据搜索与获取
 */
class BangumiApi {

    private val gson = Gson()

    private val httpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .followRedirects(true)
            .build()
    }

    companion object {
        private const val BASE_URL = "https://api.bgm.tv"
        private const val USER_AGENT = "soanime/1.4.0 (https://github.com/soanime)"

        @Volatile
        private var instance: BangumiApi? = null

        fun getInstance(): BangumiApi {
            return instance ?: synchronized(this) {
                instance ?: BangumiApi().also { instance = it }
            }
        }
    }

    /**
     * 搜索条目
     * @param keyword 搜索关键词
     * @param type 类型 (2=动画, 6=三次元/影视, 0=全部)
     * @return 匹配的条目列表
     */
    suspend fun search(keyword: String, type: Int = 0): List<BangumiSubject> = withContext(Dispatchers.IO) {
        if (keyword.isBlank()) return@withContext emptyList()

        try {
            val encoded = URLEncoder.encode(keyword, "UTF-8")
            val typeParam = if (type > 0) "&type=$type" else ""
            val url = "$BASE_URL/search/subject/$encoded?responseGroup=large$typeParam"

            val request = Request.Builder()
                .url(url)
                .header("User-Agent", USER_AGENT)
                .header("Accept", "application/json")
                .build()

            val response = httpClient.newCall(request).execute()
            val json = response.use {
                if (!it.isSuccessful) return@withContext emptyList()
                it.body?.string() ?: return@withContext emptyList()
            }

            val root = JsonParser.parseString(json).asJsonObject
            val list = root.getAsJsonArray("list") ?: return@withContext emptyList()

            val results = mutableListOf<BangumiSubject>()
            for (item in list) {
                try {
                    val subject = gson.fromJson(item, BangumiSubject::class.java)
                    if (subject != null && subject.id > 0) {
                        results.add(subject)
                    }
                } catch (_: Exception) { }
            }

            results
        } catch (_: Exception) {
            emptyList()
        }
    }

    /**
     * 搜索并返回最佳匹配
     * @param title 标题
     * @param type 类型 (2=动画, 6=影视, 0=全部)
     * @return 最佳匹配的条目，或 null
     */
    suspend fun searchBestMatch(title: String, type: Int = 0): BangumiSubject? {
        val results = search(title, type)
        if (results.isEmpty()) return null

        // 优先精确匹配中文名
        val exactCn = results.find { it.nameCn == title }
        if (exactCn != null) return exactCn

        // 优先精确匹配原名
        val exact = results.find { it.name == title }
        if (exact != null) return exact

        // 包含匹配
        val contains = results.find {
            it.nameCn.contains(title, true) || title.contains(it.nameCn, true) ||
            it.name.contains(title, true) || title.contains(it.name, true)
        }
        if (contains != null) return contains

        // 返回第一个结果
        return results.first()
    }

    /**
     * 获取条目详情
     * @param subjectId Bangumi 条目 ID
     * @return 条目详情，或 null
     */
    suspend fun getSubject(subjectId: Int): BangumiSubject? = withContext(Dispatchers.IO) {
        if (subjectId <= 0) return@withContext null

        try {
            val url = "$BASE_URL/v0/subjects/$subjectId"
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", USER_AGENT)
                .header("Accept", "application/json")
                .build()

            val response = httpClient.newCall(request).execute()
            val json = response.use {
                if (!it.isSuccessful) return@withContext null
                it.body?.string() ?: return@withContext null
            }

            gson.fromJson(json, BangumiSubject::class.java)
        } catch (_: Exception) {
            null
        }
    }

    /**
     * 智能搜索：先搜动画(2)，再搜影视(6)，合并结果
     * @param title 搜索标题
     * @return 最佳匹配条目
     */
    suspend fun smartSearch(title: String): BangumiSubject? {
        // 清理标题（去除集数等后缀）
        val cleanTitle = title
            .replace(Regex("第\\d+集"), "")
            .replace(Regex("第[一二三四五六七八九十]+集"), "")
            .replace(Regex("EP?\\d+", RegexOption.IGNORE_CASE), "")
            .replace(Regex("\\[.*?]"), "")
            .replace(Regex("【.*?】"), "")
            .trim()

        val searchTitle = cleanTitle.ifBlank { title }

        // 先搜动画类型
        val animeResult = searchBestMatch(searchTitle, 2)
        if (animeResult != null && animeResult.score > 0) return animeResult

        // 再搜影视类型
        val realResult = searchBestMatch(searchTitle, 6)
        if (realResult != null) return realResult

        // 如果都没找到，搜全部
        return animeResult ?: searchBestMatch(searchTitle, 0)
    }
}

package com.so.movie.metadata

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentHashMap

/**
 * 元数据仓库 — 管理 Bangumi 元数据的获取与缓存
 */
class MetadataRepository private constructor(context: Context) {

    private val bangumiApi = BangumiApi.getInstance()
    private val gson = Gson()

    // 内存缓存（标题 -> 元数据）
    private val memoryCache = ConcurrentHashMap<String, BangumiSubject?>()

    // 持久化缓存
    private val prefs: SharedPreferences =
        context.getSharedPreferences("metadata_cache", Context.MODE_PRIVATE)

    companion object {
        @Volatile
        private var instance: MetadataRepository? = null

        fun getInstance(context: Context): MetadataRepository {
            return instance ?: synchronized(this) {
                instance ?: MetadataRepository(context.applicationContext).also { instance = it }
            }
        }

        private const val CACHE_PREFIX = "meta_"
        private const val CACHE_TIMESTAMP = "meta_timestamp"
        private const val CACHE_TTL = 7 * 24 * 60 * 60 * 1000L // 7 天过期
    }

    /**
     * 获取标题对应的元数据（优先缓存）
     * @param title 影视/番剧标题
     * @return Bangumi 条目，或 null
     */
    suspend fun getMetadata(title: String): BangumiSubject? {
        if (title.isBlank()) return null

        // 1. 检查内存缓存
        memoryCache[title]?.let { return it }

        // 2. 检查磁盘缓存
        val cached = loadFromDisk(title)
        if (cached != null) {
            memoryCache[title] = cached
            return cached
        }

        // 3. 从 Bangumi API 获取
        return withContext(Dispatchers.IO) {
            val subject = bangumiApi.smartSearch(title)
            // 存入缓存（即使是 null，也缓存以避免重复请求）
            memoryCache[title] = subject
            if (subject != null) {
                saveToDisk(title, subject)
            }
            subject
        }
    }

    /**
     * 批量获取元数据
     * @param titles 标题列表
     * @return 标题 -> 元数据 的映射
     */
    suspend fun getMetadataBatch(titles: List<String>): Map<String, BangumiSubject?> {
        return titles.associateWith { getMetadata(it) }
    }

    /**
     * 清除所有缓存
     */
    fun clearCache() {
        memoryCache.clear()
        prefs.edit().clear().apply()
    }

    /**
     * 清除过期缓存
     */
    fun cleanExpiredCache() {
        val now = System.currentTimeMillis()
        val keys = prefs.all.keys.filter { it.startsWith(CACHE_PREFIX) }
        for (key in keys) {
            val title = key.removePrefix(CACHE_PREFIX)
            val timestamp = prefs.getLong("${CACHE_TIMESTAMP}_$title", 0L)
            if (now - timestamp > CACHE_TTL) {
                prefs.edit().remove(key).remove("${CACHE_TIMESTAMP}_$title").apply()
            }
        }
    }

    private fun loadFromDisk(title: String): BangumiSubject? {
        val json = prefs.getString("${CACHE_PREFIX}$title", null) ?: return null
        val timestamp = prefs.getLong("${CACHE_TIMESTAMP}_$title", 0L)
        if (System.currentTimeMillis() - timestamp > CACHE_TTL) return null
        return try {
            gson.fromJson(json, BangumiSubject::class.java)
        } catch (_: Exception) {
            null
        }
    }

    private fun saveToDisk(title: String, subject: BangumiSubject) {
        try {
            val json = gson.toJson(subject)
            prefs.edit()
                .putString("${CACHE_PREFIX}$title", json)
                .putLong("${CACHE_TIMESTAMP}_$title", System.currentTimeMillis())
                .apply()
        } catch (_: Exception) { }
    }
}

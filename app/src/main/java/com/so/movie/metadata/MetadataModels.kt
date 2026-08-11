package com.so.movie.metadata

import com.google.gson.annotations.SerializedName

/**
 * Bangumi 条目元数据
 */
data class BangumiSubject(
    val id: Int = 0,
    val name: String = "",
    @SerializedName("name_cn") val nameCn: String = "",
    val summary: String = "",
    val images: BangumiImages? = null,
    val date: String = "",           // 放送日期
    @SerializedName("air_date") val airDate: String = "",
    val tags: List<BangumiTag> = emptyList(),
    val rating: BangumiRating? = null,
    val eps: Int = 0,                // 集数
    val type: Int = 0                // 2=动画 6=三次元
) {
    /** 显示名称（优先中文名） */
    val displayName: String get() = nameCn.ifBlank { name }

    /** 封面图 URL（优先 large） */
    val coverUrl: String get() = images?.large ?: images?.common ?: images?.medium ?: ""

    /** 评分 */
    val score: Float get() = rating?.score ?: 0f

    /** 评分人数 */
    val scoreCount: Int get() = rating?.total ?: 0

    /** 标签列表（取前10个） */
    val tagNames: List<String> get() = tags.take(10).map { it.name }
}

/** 条目图片 */
data class BangumiImages(
    val large: String = "",
    val common: String = "",
    val medium: String = "",
    val small: String = "",
    val grid: String = ""
)

/** 评分 */
data class BangumiRating(
    val score: Float = 0f,
    val rank: Int = 0,
    val total: Int = 0
)

/** 标签 */
data class BangumiTag(
    val name: String = "",
    val count: Int = 0
)

/** 搜索响应 */
data class BangumiSearchResponse(
    val results: Int = 0,
    val list: List<BangumiSubject> = emptyList()
)

/**
 * 元数据缓存条目
 */
data class MetadataCache(
    val title: String,
    val subject: BangumiSubject?,
    val timestamp: Long = System.currentTimeMillis()
)

package com.so.movie.danmaku

import android.graphics.Color

/**
 * 弹幕数据模型
 * 兼容弹弹play API 格式
 */
data class DanmakuEntry(
    val message: String,       // 弹幕内容
    val time: Float,           // 弹幕时间（秒）
    val type: DanmakuType,     // 弹幕类型
    val color: Int,            // 弹幕颜色（ARGB）
    val source: String = ""    // 弹幕来源
)

/**
 * 弹幕类型
 */
enum class DanmakuType(val value: Int) {
    SCROLL(1),   // 滚动弹幕（从右到左）
    BOTTOM(4),   // 底部弹幕
    TOP(5);      // 顶部弹幕

    companion object {
        fun fromValue(v: Int): DanmakuType = values().find { it.value == v } ?: SCROLL
    }
}

/**
 * 弹弹play 搜索结果
 */
data class DanDanSearchResult(
    val animeId: Int,
    val animeTitle: String,
    val episodeId: Int,
    val episodeTitle: String
)

/**
 * 弹幕设置
 */
data class DanmakuSettings(
    val enabled: Boolean = true,
    val fontSize: Float = 16f,        // sp
    val opacity: Float = 1.0f,        // 0~1
    val speed: Float = 1.0f,          // 倍速
    val maxDisplay: Int = 50,         // 最大显示数
    val showScroll: Boolean = true,   // 显示滚动弹幕
    val showTop: Boolean = true,      // 显示顶部弹幕
    val showBottom: Boolean = true    // 显示底部弹幕
)

/**
 * 解析弹弹play弹幕数据
 * 格式: {"p": "时间,类型,颜色,来源", "m": "弹幕内容"}
 */
fun parseDanDanComment(p: String, m: String): DanmakuEntry? {
    return try {
        val parts = p.split(",")
        if (parts.size < 3) return null

        val time = parts[0].toFloat()
        val type = DanmakuType.fromValue(parts[1].toInt())
        val color = parts[2].toLong(16).toInt() or 0xFF000000.toInt()
        val source = if (parts.size >= 4) parts[3] else ""

        DanmakuEntry(
            message = m,
            time = time,
            type = type,
            color = color,
            source = source
        )
    } catch (e: Exception) {
        null
    }
}

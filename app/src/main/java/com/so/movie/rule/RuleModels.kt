package com.so.movie.rule

import com.google.gson.annotations.SerializedName

/**
 * 规则数据模型 — 兼容 Kazumi 规则格式
 * 每个 Rule 对应一个视频源网站的解析规则
 */
data class Rule(
    @SerializedName("api") val api: String = "1",
    @SerializedName("type") val type: String = "anime",
    @SerializedName("name") val name: String = "",
    @SerializedName("version") val version: String = "1.0",
    @SerializedName("muliSources") val muliSources: Boolean = false,
    @SerializedName("useWebview") val useWebview: Boolean = false,
    @SerializedName("useNativePlayer") val useNativePlayer: Boolean = true,
    @SerializedName("usePost") val usePost: Boolean = false,
    @SerializedName("useLegacyParser") val useLegacyParser: Boolean = false,
    @SerializedName("adBlocker") val adBlocker: Boolean = false,
    @SerializedName("userAgent") val userAgent: String = "",
    @SerializedName("baseUrl") val baseUrl: String = "",
    @SerializedName("searchURL") val searchURL: String = "",
    @SerializedName("searchList") val searchList: String = "",
    @SerializedName("searchName") val searchName: String = "",
    @SerializedName("searchResult") val searchResult: String = "",
    @SerializedName("chapterRoads") val chapterRoads: String = "",
    @SerializedName("chapterResult") val chapterResult: String = "",
    @SerializedName("referer") val referer: String = "",
    @SerializedName("searchMode") val searchMode: String = "xpath",
    @SerializedName("chapterMode") val chapterMode: String = "xpath",
    // API 规则配置
    @SerializedName("searchApiConfig") val searchApiConfig: ApiSearchConfig? = null,
    @SerializedName("chapterApiConfig") val chapterApiConfig: ApiChapterConfig? = null,
    // 应用端字段（不参与 JSON 序列化到规则仓库）
    @Transient var enabled: Boolean = true,
    @Transient var builtin: Boolean = false,
    @Transient var lastUpdate: Long = 0L
)

/** API 搜索配置 */
data class ApiSearchConfig(
    @SerializedName("url") val url: String = "",
    @SerializedName("method") val method: String = "GET",
    @SerializedName("headers") val headers: Map<String, String> = emptyMap(),
    @SerializedName("query") val query: Map<String, String> = emptyMap(),
    @SerializedName("body") val body: String = "",
    @SerializedName("listPath") val listPath: String = "",
    @SerializedName("namePath") val namePath: String = "",
    @SerializedName("sourcePath") val sourcePath: String = "",
    @SerializedName("coverPath") val coverPath: String = ""
)

/** API 选集配置 */
data class ApiChapterConfig(
    @SerializedName("url") val url: String = "",
    @SerializedName("method") val method: String = "GET",
    @SerializedName("headers") val headers: Map<String, String> = emptyMap(),
    @SerializedName("query") val query: Map<String, String> = emptyMap(),
    @SerializedName("body") val body: String = "",
    @SerializedName("listPath") val listPath: String = "",
    @SerializedName("namePath") val namePath: String = "",
    @SerializedName("urlPath") val urlPath: String = "",
    @SerializedName("responseVariables") val responseVariables: List<ResponseVariable> = emptyList(),
    @SerializedName("pageTemplate") val pageTemplate: String = ""
)

/** 响应变量定义 */
data class ResponseVariable(
    @SerializedName("name") val name: String = "",
    @SerializedName("path") val path: String = ""
)

/** 搜索结果项 */
data class SearchResultItem(
    val title: String,
    val url: String,          // 详情页链接 或 source ID
    val cover: String = "",   // 封面图链接
    val ruleName: String = "" // 所属规则名
)

/** 播放线路 */
data class ChapterRoad(
    val name: String,          // 线路名称
    val episodes: List<ChapterEpisode>
)

/** 剧集项 */
data class ChapterEpisode(
    val name: String,          // 集名（如"第1集"）
    val url: String            // 播放页 URL
)

/** 规则搜索结果（单条规则的搜索结果） */
data class RuleSearchResult(
    val ruleName: String,
    val success: Boolean,
    val results: List<SearchResultItem> = emptyList(),
    val error: String? = null
)

/** 规则选集结果 */
data class RuleChapterResult(
    val success: Boolean,
    val roads: List<ChapterRoad> = emptyList(),
    val error: String? = null
)

/** 规则仓库索引项 */
data class RuleCatalogItem(
    val name: String,
    val url: String,
    val version: String = "",
    val description: String = ""
)

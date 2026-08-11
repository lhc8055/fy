package com.so.movie.rule

import com.google.gson.Gson
import com.google.gson.JsonParser
import com.jayway.jsonpath.JsonPath
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.FormBody
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.jsoup.Jsoup
import org.jsoup.helper.W3CDom
import org.w3c.dom.Document
import org.w3c.dom.NodeList
import java.net.URLEncoder
import java.util.concurrent.TimeUnit
import javax.xml.xpath.XPathConstants
import javax.xml.xpath.XPathFactory

/**
 * 规则引擎 — 负责执行规则搜索和选集解析
 * 支持 XPath 和 API 两种模式
 */
class RuleEngine {

    private val httpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .followRedirects(true)
            .build()
    }

    private val xpathFactory = XPathFactory.newInstance()
    private val w3cDom = W3CDom()
    private val gson = Gson()

    // ==================== 搜索 ====================

    /**
     * 使用规则搜索影视
     * @param rule 规则
     * @param keyword 搜索关键词
     * @return 搜索结果
     */
    suspend fun search(rule: Rule, keyword: String): RuleSearchResult {
        return try {
            if (rule.searchMode == "api" && rule.searchApiConfig != null) {
                searchByApi(rule, keyword)
            } else {
                searchByXpath(rule, keyword)
            }
        } catch (e: Exception) {
            RuleSearchResult(
                ruleName = rule.name,
                success = false,
                error = e.message ?: "未知错误"
            )
        }
    }

    /**
     * XPath 模式搜索
     */
    private suspend fun searchByXpath(rule: Rule, keyword: String): RuleSearchResult {
        val encodedKeyword = URLEncoder.encode(keyword, "UTF-8")
        val searchUrl = rule.searchURL.replace("@keyword", encodedKeyword)

        val html = fetchHtml(searchUrl, rule)
        val doc = Jsoup.parse(html, rule.baseUrl.ifEmpty { searchUrl })
        val w3cDoc: Document = w3cDom.fromJsoup(doc)

        val xpath = xpathFactory.newXPath()

        // 解析搜索结果列表
        val listNodes = xpath.evaluate(
            rule.searchList,
            w3cDoc,
            XPathConstants.NODESET
        ) as NodeList

        if (listNodes.length == 0) {
            return RuleSearchResult(
                ruleName = rule.name,
                success = true,
                results = emptyList()
            )
        }

        val results = mutableListOf<SearchResultItem>()
        for (i in 0 until listNodes.length) {
            try {
                val node = listNodes.item(i)

                val name = xpath.evaluate(rule.searchName, node).trim()
                var url = xpath.evaluate(rule.searchResult, node).trim()

                if (name.isNotEmpty() && url.isNotEmpty()) {
                    // 处理相对 URL
                    if (!url.startsWith("http")) {
                        url = resolveUrl(rule.baseUrl, url)
                    }
                    results.add(SearchResultItem(
                        title = name,
                        url = url,
                        ruleName = rule.name
                    ))
                }
            } catch (e: Exception) {
                // 跳过解析失败的单条结果
            }
        }

        return RuleSearchResult(
            ruleName = rule.name,
            success = true,
            results = results
        )
    }

    /**
     * API 模式搜索
     */
    private suspend fun searchByApi(rule: Rule, keyword: String): RuleSearchResult {
        val config = rule.searchApiConfig ?: return RuleSearchResult(
            ruleName = rule.name,
            success = false,
            error = "API 配置缺失"
        )

        val url = config.url.replace("@keyword", URLEncoder.encode(keyword, "UTF-8"))
        val headers = config.headers.toMutableMap()
        if (rule.userAgent.isNotEmpty()) headers["User-Agent"] = rule.userAgent

        val jsonResponse = fetchJson(url, config.method, headers, config.query, config.body, rule)
        val root = JsonParser.parseString(jsonResponse)

        // 使用 JSONPath 提取列表
        val list = try {
            JsonPath.read<List<Any>>(jsonResponse, config.listPath)
        } catch (e: Exception) {
            return RuleSearchResult(
                ruleName = rule.name,
                success = true,
                results = emptyList()
            )
        }

        val results = mutableListOf<SearchResultItem>()
        val listJson = gson.toJson(list)
        val items = JsonPath.read<List<Map<String, Any>>>("$[*]", gson.toJson(list))

        for (item in items) {
            try {
                val itemJson = gson.toJson(item)
                val name = JsonPath.read<String?>(itemJson, config.namePath) ?: ""
                val source = JsonPath.read<String?>(itemJson, config.sourcePath) ?: ""
                val cover = if (config.coverPath.isNotEmpty()) {
                    try { JsonPath.read<String?>(itemJson, config.coverPath) ?: "" } catch (e: Exception) { "" }
                } else ""

                if (name.isNotEmpty()) {
                    results.add(SearchResultItem(
                        title = name,
                        url = source,
                        cover = cover,
                        ruleName = rule.name
                    ))
                }
            } catch (e: Exception) {
                // 跳过单条解析失败
            }
        }

        return RuleSearchResult(
            ruleName = rule.name,
            success = true,
            results = results
        )
    }

    // ==================== 选集解析 ====================

    /**
     * 解析剧集列表
     * @param rule 规则
     * @param sourceUrl 详情页链接 或 source ID
     * @return 线路和剧集列表
     */
    suspend fun getChapters(rule: Rule, sourceUrl: String): RuleChapterResult {
        return try {
            if (rule.chapterMode == "api" && rule.chapterApiConfig != null) {
                getChaptersByApi(rule, sourceUrl)
            } else {
                getChaptersByXpath(rule, sourceUrl)
            }
        } catch (e: Exception) {
            RuleChapterResult(
                success = false,
                error = e.message ?: "未知错误"
            )
        }
    }

    /**
     * XPath 模式解析剧集
     */
    private suspend fun getChaptersByXpath(rule: Rule, sourceUrl: String): RuleChapterResult {
        val html = fetchHtml(sourceUrl, rule)
        val doc = Jsoup.parse(html, rule.baseUrl.ifEmpty { sourceUrl })
        val w3cDoc: Document = w3cDom.fromJsoup(doc)
        val xpath = xpathFactory.newXPath()

        // 解析播放线路
        val roadNodes = xpath.evaluate(
            rule.chapterRoads,
            w3cDoc,
            XPathConstants.NODESET
        ) as NodeList

        val roads = mutableListOf<ChapterRoad>()

        if (roadNodes.length == 0) {
            // 没有线路分组，直接解析所有剧集
            val episodes = parseEpisodes(w3cDoc, rule.chapterResult, rule, sourceUrl)
            if (episodes.isNotEmpty()) {
                roads.add(ChapterRoad("默认线路", episodes))
            }
        } else {
            for (i in 0 until roadNodes.length) {
                val roadNode = roadNodes.item(i)
                val roadName = "线路${i + 1}"
                val episodes = parseEpisodes(roadNode, rule.chapterResult, rule, sourceUrl)
                if (episodes.isNotEmpty()) {
                    roads.add(ChapterRoad(roadName, episodes))
                }
            }
        }

        return RuleChapterResult(success = true, roads = roads)
    }

    /**
     * 解析单条线路下的剧集列表
     */
    private fun parseEpisodes(
        contextNode: Any,
        chapterResultXpath: String,
        rule: Rule,
        baseUrl: String
    ): List<ChapterEpisode> {
        val xpath = xpathFactory.newXPath()
        val nodes = xpath.evaluate(
            chapterResultXpath,
            contextNode,
            XPathConstants.NODESET
        ) as NodeList

        val episodes = mutableListOf<ChapterEpisode>()
        for (i in 0 until nodes.length) {
            try {
                val node = nodes.item(i)
                val text = node.textContent.trim()
                val link = if (node.hasChildNodes()) {
                    var href = ""
                    for (j in 0 until node.childNodes.length) {
                        val child = node.childNodes.item(j)
                        if (child.nodeName.equals("a", ignoreCase = true)) {
                            href = child.attributes.getNamedItem("href")?.nodeValue ?: ""
                            break
                        }
                    }
                    href
                } else ""

                val name = if (text.isNotEmpty()) text else "第${i + 1}集"
                var url = link.ifEmpty { text }

                if (url.isNotEmpty() && !url.startsWith("http")) {
                    url = resolveUrl(rule.baseUrl.ifEmpty { baseUrl }, url)
                }

                if (url.isNotEmpty()) {
                    episodes.add(ChapterEpisode(name = name, url = url))
                }
            } catch (e: Exception) {
                // 跳过
            }
        }
        return episodes
    }

    /**
     * API 模式解析剧集
     */
    private suspend fun getChaptersByApi(rule: Rule, source: String): RuleChapterResult {
        val config = rule.chapterApiConfig ?: return RuleChapterResult(
            success = false,
            error = "API 选集配置缺失"
        )

        val url = config.url.replace("@source", source)
        val headers = config.headers.toMutableMap()
        if (rule.userAgent.isNotEmpty()) headers["User-Agent"] = rule.userAgent

        val jsonResponse = fetchJson(url, config.method, headers, config.query, config.body, rule)

        // 提取响应变量
        val variables = mutableMapOf<String, String>()
        for (v in config.responseVariables) {
            try {
                val value = JsonPath.read<String?>(jsonResponse, v.path)
                if (value != null) variables[v.name] = value
            } catch (e: Exception) { }
        }

        // 解析线路和剧集
        val list = try {
            JsonPath.read<List<Map<String, Any>>>(jsonResponse, config.listPath)
        } catch (e: Exception) {
            return RuleChapterResult(success = true, roads = emptyList())
        }

        val roads = mutableListOf<ChapterRoad>()
        for ((roadIndex, roadItem) in list.withIndex()) {
            val roadJson = gson.toJson(roadItem)
            val roadName = try {
                JsonPath.read<String?>(roadJson, config.namePath) ?: "线路${roadIndex + 1}"
            } catch (e: Exception) { "线路${roadIndex + 1}" }

            // 解析该线路下的剧集
            val episodes = mutableListOf<ChapterEpisode>()
            try {
                val episodeList = JsonPath.read<List<Map<String, Any>>>(roadJson, config.urlPath)
                for ((epIndex, ep) in episodeList.withIndex()) {
                    val epName = try {
                        JsonPath.read<String?>(gson.toJson(ep), config.namePath) ?: "第${epIndex + 1}集"
                    } catch (e: Exception) { "第${epIndex + 1}集" }

                    val epUrl = try {
                        JsonPath.read<String?>(gson.toJson(ep), config.urlPath) ?: ""
                    } catch (e: Exception) { "" }

                    // 构造播放页 URL
                    var playUrl = epUrl
                    if (config.pageTemplate.isNotEmpty()) {
                        playUrl = config.pageTemplate
                            .replace("@roadIndex", roadIndex.toString())
                            .replace("@episodeIndex", epIndex.toString())
                            .replace("@roadNumber", (roadIndex + 1).toString())
                            .replace("@episodeNumber", (epIndex + 1).toString())
                        for ((k, v) in variables) {
                            playUrl = playUrl.replace("@$k", v)
                        }
                    }

                    if (playUrl.isNotEmpty()) {
                        episodes.add(ChapterEpisode(name = epName, url = playUrl))
                    }
                }
            } catch (e: Exception) { }

            if (episodes.isNotEmpty()) {
                roads.add(ChapterRoad(roadName, episodes))
            }
        }

        return RuleChapterResult(success = true, roads = roads)
    }

    // ==================== 网络请求 ====================

    /**
     * 获取 HTML 内容
     */
    private suspend fun fetchHtml(url: String, rule: Rule): String {
        val request = buildRequest(url, "GET", rule, null, null)
        val response = httpClient.newCall(request).execute()
        return response.use {
            if (!it.isSuccessful) throw Exception("HTTP ${it.code}")
            it.body?.string() ?: ""
        }
    }

    /**
     * 获取 JSON 内容
     */
    private suspend fun fetchJson(
        url: String,
        method: String,
        headers: Map<String, String>,
        query: Map<String, String>,
        body: String,
        rule: Rule
    ): String {
        val request = buildRequest(url, method, rule, headers, body, query)
        val response = httpClient.newCall(request).execute()
        return response.use {
            if (!it.isSuccessful) throw Exception("HTTP ${it.code}")
            it.body?.string() ?: ""
        }
    }

    /**
     * 构建 OkHttp Request
     */
    private fun buildRequest(
        url: String,
        method: String,
        rule: Rule,
        extraHeaders: Map<String, String>?,
        body: String?,
        query: Map<String, String>? = null
    ): Request {
        val builder = Request.Builder()

        // 处理 URL 和 query 参数
        var finalUrl = url
        if (query != null && query.isNotEmpty()) {
            val separator = if (url.contains("?")) "&" else "?"
            val queryString = query.entries.joinToString("&") {
                "${URLEncoder.encode(it.key, "UTF-8")}=${URLEncoder.encode(it.value, "UTF-8")}"
            }
            finalUrl = "$url$separator$queryString"
        }

        builder.url(finalUrl)

        // 设置 User-Agent
        val ua = rule.userAgent.ifEmpty {
            "Mozilla/5.0 (Linux; Android 13; Pixel 7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36"
        }
        builder.header("User-Agent", ua)

        // 设置 Referer
        if (rule.referer.isNotEmpty()) {
            builder.header("Referer", rule.referer)
        } else if (rule.baseUrl.isNotEmpty()) {
            builder.header("Referer", rule.baseUrl)
        }

        // 额外 headers
        extraHeaders?.forEach { (k, v) -> builder.header(k, v) }

        // 设置请求方法和 body
        if (method.uppercase() == "POST") {
            val mediaType = "application/x-www-form-urlencoded".toMediaType()
            val requestBody = (body ?: "").toRequestBody(mediaType)
            builder.post(requestBody)
        }

        return builder.build()
    }

    // ==================== 工具方法 ====================

    /**
     * 解析相对 URL 为绝对 URL
     */
    fun resolveUrl(baseUrl: String, relativeUrl: String): String {
        return try {
            if (relativeUrl.startsWith("//")) {
                "https:$relativeUrl"
            } else if (relativeUrl.startsWith("/")) {
                val base = baseUrl.substringBefore("://") + "://" +
                    baseUrl.substringAfter("://").substringBefore("/")
                "$base$relativeUrl"
            } else if (!relativeUrl.startsWith("http")) {
                val base = baseUrl.trimEnd('/')
                "$base/$relativeUrl"
            } else {
                relativeUrl
            }
        } catch (e: Exception) {
            relativeUrl
        }
    }

    /**
     * 获取 HTTP 客户端（供视频嗅探等模块使用）
     */
    fun getHttpClient(): OkHttpClient = httpClient
}

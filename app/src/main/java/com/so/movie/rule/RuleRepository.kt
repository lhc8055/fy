package com.so.movie.rule

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

/**
 * 规则仓库 — 管理规则的下载、存储、安装
 * 规则存储在 SharedPreferences 中（JSON 序列化）
 */
class RuleRepository private constructor(private val context: Context) {

    private val gson = Gson()
    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val httpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    // Kazumi 规则仓库地址
    companion object {
        private const val PREFS_NAME = "rule_prefs"
        private const val KEY_RULES = "rules_json"
        private const val KEY_INITIALIZED = "rules_initialized"

        // Kazumi 规则仓库
        private const val RULES_REPO_BASE =
            "https://raw.githubusercontent.com/KazumiRules/KazumiRules/main"
        private const val RULES_INDEX_URL = "$RULES_REPO_BASE/index.json"

        // 内置默认规则
        val BUILTIN_RULES = listOf(
            Rule(
                api = "5",
                type = "anime",
                name = "示例规则-樱花动漫",
                version = "1.0",
                muliSources = true,
                useWebview = false,
                useNativePlayer = true,
                useLegacyParser = false,
                adBlocker = true,
                baseUrl = "https://www.yhdmp.cc/",
                searchURL = "https://www.yhdmp.cc/search.asp?searchword=@keyword",
                searchList = "//div[@class='lpic']//li",
                searchName = "//h2/a/text()",
                searchResult = "//h2/a/@href",
                chapterRoads = "//div[@class='tabs']//ul",
                chapterResult = "//li/a",
                searchMode = "xpath",
                chapterMode = "xpath",
                builtin = true
            ),
            Rule(
                api = "5",
                type = "anime",
                name = "示例规则-AGE动漫",
                version = "1.0",
                muliSources = true,
                useWebview = false,
                useNativePlayer = true,
                useLegacyParser = false,
                adBlocker = true,
                baseUrl = "https://www.agemys.org/",
                searchURL = "https://www.agemys.org/search?q=@keyword",
                searchList = "//div[@class='search-item']",
                searchName = "//div[@class='search-item-title']/a/text()",
                searchResult = "//div[@class='search-item-title']/a/@href",
                chapterRoads = "//div[@class='central']//ul",
                chapterResult = "//li/a",
                searchMode = "xpath",
                chapterMode = "xpath",
                builtin = true
            )
        )

        @Volatile
        private var instance: RuleRepository? = null

        fun getInstance(context: Context): RuleRepository {
            return instance ?: synchronized(this) {
                instance ?: RuleRepository(context.applicationContext).also { instance = it }
            }
        }
    }

    /**
     * 获取所有已安装规则
     */
    fun getAllRules(): List<Rule> {
        val json = prefs.getString(KEY_RULES, null) ?: return emptyList()
        return try {
            val type = object : TypeToken<List<Rule>>() {}.type
            gson.fromJson(json, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * 获取已启用的规则
     */
    fun getEnabledRules(): List<Rule> = getAllRules().filter { it.enabled }

    /**
     * 保存规则列表
     */
    private fun saveRules(rules: List<Rule>) {
        prefs.edit().putString(KEY_RULES, gson.toJson(rules)).apply()
    }

    /**
     * 添加单个规则
     */
    fun addRule(rule: Rule) {
        val rules = getAllRules().toMutableList()
        // 如果同名规则已存在，替换它
        rules.removeAll { it.name == rule.name }
        rules.add(rule)
        saveRules(rules)
    }

    /**
     * 删除规则
     */
    fun removeRule(ruleName: String) {
        val rules = getAllRules().filterNot { it.name == ruleName }
        saveRules(rules)
    }

    /**
     * 切换规则启用状态
     */
    fun toggleRule(ruleName: String) {
        val rules = getAllRules().map {
            if (it.name == ruleName) it.copy(enabled = !it.enabled) else it
        }
        saveRules(rules)
    }

    /**
     * 更新规则
     */
    fun updateRule(rule: Rule) {
        val rules = getAllRules().map {
            if (it.name == rule.name) rule else it
        }
        saveRules(rules)
    }

    /**
     * 初始化默认规则（仅首次安装执行）
     */
    fun initializeIfNeeded() {
        if (prefs.getBoolean(KEY_INITIALIZED, false)) return

        // 安装内置规则
        val builtinRules = BUILTIN_RULES.map { it.copy(enabled = true, builtin = true) }
        saveRules(builtinRules)
        prefs.edit().putBoolean(KEY_INITIALIZED, true).apply()
    }

    /**
     * 从远程仓库下载规则索引
     */
    suspend fun fetchRuleCatalog(): List<RuleCatalogItem> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url(RULES_INDEX_URL)
                .header("User-Agent", "Mozilla/5.0")
                .build()

            val response = httpClient.newCall(request).execute()
            val json = response.use {
                if (!it.isSuccessful) return@withContext emptyList()
                it.body?.string() ?: return@withContext emptyList()
            }

            // 解析索引 JSON（格式：[{name, url, version}, ...]）
            val type = object : TypeToken<List<RuleCatalogItem>>() {}.type
            gson.fromJson<List<RuleCatalogItem>>(json, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * 下载并安装单个规则
     */
    suspend fun downloadAndInstallRule(catalogItem: RuleCatalogItem): Boolean = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url(catalogItem.url)
                .header("User-Agent", "Mozilla/5.0")
                .build()

            val response = httpClient.newCall(request).execute()
            val json = response.use {
                if (!it.isSuccessful) return@withContext false
                it.body?.string() ?: return@withContext false
            }

            val rule = gson.fromJson(json, Rule::class.java)
            if (rule != null && rule.name.isNotEmpty()) {
                addRule(rule.copy(enabled = true, builtin = false))
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }

    /**
     * 从 JSON 字符串导入规则
     */
    fun importRuleFromJson(json: String): Boolean {
        return try {
            val rule = gson.fromJson(json, Rule::class.java)
            if (rule != null && rule.name.isNotEmpty()) {
                addRule(rule.copy(enabled = true, builtin = false))
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }

    /**
     * 导出规则为 JSON 字符串
     */
    fun exportRuleToJson(ruleName: String): String? {
        val rule = getAllRules().find { it.name == ruleName } ?: return null
        return gson.toJson(rule)
    }

    /**
     * 从 Base64 分享链接导入规则（兼容 Kazumi 分享格式）
     */
    fun importRuleFromBase64(base64: String): Boolean {
        return try {
            val json = String(android.util.Base64.decode(base64, android.util.Base64.DEFAULT))
            importRuleFromJson(json)
        } catch (e: Exception) {
            false
        }
    }

    /**
     * 导出规则为 Base64 分享链接
     */
    fun exportRuleToBase64(ruleName: String): String? {
        val json = exportRuleToJson(ruleName) ?: return null
        return android.util.Base64.encodeToString(json.toByteArray(), android.util.Base64.NO_WRAP)
    }

    /**
     * 批量更新所有规则
     */
    suspend fun updateAllRulesFromRemote(): Int = withContext(Dispatchers.IO) {
        val catalog = fetchRuleCatalog()
        var updatedCount = 0
        for (item in catalog) {
            if (downloadAndInstallRule(item)) {
                updatedCount++
            }
        }
        updatedCount
    }

    /**
     * 检查是否已初始化
     */
    fun isInitialized(): Boolean = prefs.getBoolean(KEY_INITIALIZED, false)

    /**
     * 重置所有规则（恢复出厂设置）
     */
    fun resetAllRules() {
        prefs.edit().remove(KEY_RULES).remove(KEY_INITIALIZED).apply()
        initializeIfNeeded()
    }
}

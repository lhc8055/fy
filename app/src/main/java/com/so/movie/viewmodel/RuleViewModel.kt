package com.so.movie.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.so.movie.rule.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.update

/**
 * 规则 ViewModel — 管理规则状态、搜索、选集解析
 */
class RuleViewModel(application: Application) : AndroidViewModel(application) {

    private val ruleRepository = RuleRepository.getInstance(application)
    private val ruleEngine = RuleEngine()

    // 规则列表
    private val _rules = MutableStateFlow<List<Rule>>(emptyList())
    val rules: StateFlow<List<Rule>> = _rules.asStateFlow()

    // 搜索状态
    private val _searchLoading = MutableStateFlow(false)
    val searchLoading: StateFlow<Boolean> = _searchLoading.asStateFlow()

    // 搜索结果（按规则分组）
    private val _searchResults = MutableStateFlow<List<SearchResultItem>>(emptyList())
    val searchResults: StateFlow<List<SearchResultItem>> = _searchResults.asStateFlow()

    // 选集加载状态
    private val _chapterLoading = MutableStateFlow(false)
    val chapterLoading: StateFlow<Boolean> = _chapterLoading.asStateFlow()

    // 选集结果
    private val _chapterResult = MutableStateFlow<RuleChapterResult?>(null)
    val chapterResult: StateFlow<RuleChapterResult?> = _chapterResult.asStateFlow()

    // 当前选中的搜索结果（用于选集页面）
    private val _selectedSearchResult = MutableStateFlow<SearchResultItem?>(null)
    val selectedSearchResult: StateFlow<SearchResultItem?> = _selectedSearchResult.asStateFlow()

    // 当前选中的播放 URL（供播放器使用）
    private val _currentPlayUrl = MutableStateFlow<String>("")
    val currentPlayUrl: StateFlow<String> = _currentPlayUrl.asStateFlow()

    // 当前播放的集名（供弹幕搜索使用）
    private val _currentEpisodeName = MutableStateFlow<String>("")
    val currentEpisodeName: StateFlow<String> = _currentEpisodeName.asStateFlow()

    // 远程规则目录
    private val _ruleCatalog = MutableStateFlow<List<RuleCatalogItem>>(emptyList())
    val ruleCatalog: StateFlow<List<RuleCatalogItem>> = _ruleCatalog.asStateFlow()

    // 下载进度
    private val _downloadStatus = MutableStateFlow<String>("")
    val downloadStatus: StateFlow<String> = _downloadStatus.asStateFlow()

    private var searchJob: Job? = null

    init {
        // 初始化规则
        ruleRepository.initializeIfNeeded()
        loadRules()
    }

    /**
     * 加载本地规则列表
     */
    fun loadRules() {
        _rules.value = ruleRepository.getAllRules()
    }

    /**
     * 使用所有启用的规则搜索
     */
    fun searchWithRules(keyword: String) {
        searchJob?.cancel()
        if (keyword.isBlank()) {
            _searchResults.value = emptyList()
            return
        }

        searchJob = viewModelScope.launch(Dispatchers.IO) {
            _searchLoading.value = true
            _searchResults.value = emptyList()

            val enabledRules = ruleRepository.getEnabledRules()
            if (enabledRules.isEmpty()) {
                _searchLoading.value = false
                return@launch
            }

            // 并发搜索所有规则
            val deferredResults = enabledRules.map { rule ->
                async { ruleEngine.search(rule, keyword) }
            }
            val results = deferredResults.awaitAll()

            // 合并所有结果
            val allResults = mutableListOf<SearchResultItem>()
            for (result in results) {
                if (result.success) {
                    allResults.addAll(result.results)
                }
            }

            _searchResults.value = allResults
            _searchLoading.value = false
        }
    }

    /**
     * 获取某条搜索结果的剧集列表
     */
    fun getChapters(searchResult: SearchResultItem) {
        _selectedSearchResult.value = searchResult
        viewModelScope.launch(Dispatchers.IO) {
            _chapterLoading.value = true
            _chapterResult.value = null

            val rule = _rules.value.find { it.name == searchResult.ruleName }
            if (rule == null) {
                _chapterLoading.value = false
                return@launch
            }

            val result = ruleEngine.getChapters(rule, searchResult.url)
            _chapterResult.value = result
            _chapterLoading.value = false
        }
    }

    /**
     * 设置当前播放 URL
     */
    fun setCurrentPlayUrl(url: String) {
        _currentPlayUrl.value = url
    }

    /**
     * 设置当前播放集名（供弹幕搜索）
     */
    fun setCurrentEpisodeName(name: String) {
        _currentEpisodeName.value = name
    }

    /**
     * 清空选集结果
     */
    fun clearChapters() {
        _chapterResult.value = null
    }

    /**
     * 切换规则启用状态
     */
    fun toggleRule(ruleName: String) {
        ruleRepository.toggleRule(ruleName)
        loadRules()
    }

    /**
     * 删除规则
     */
    fun removeRule(ruleName: String) {
        ruleRepository.removeRule(ruleName)
        loadRules()
    }

    /**
     * 从 JSON 导入规则
     */
    fun importRule(json: String): Boolean {
        val success = ruleRepository.importRuleFromJson(json)
        if (success) loadRules()
        return success
    }

    /**
     * 加载远程规则目录
     */
    fun fetchRuleCatalog() {
        viewModelScope.launch(Dispatchers.IO) {
            _downloadStatus.value = "正在获取规则列表..."
            val catalog = ruleRepository.fetchRuleCatalog()
            _ruleCatalog.value = catalog
            _downloadStatus.value = if (catalog.isEmpty()) {
                "获取规则列表失败，请检查网络"
            } else {
                "找到 ${catalog.size} 个可用规则"
            }
        }
    }

    /**
     * 下载并安装远程规则
     */
    fun downloadRule(catalogItem: RuleCatalogItem) {
        viewModelScope.launch(Dispatchers.IO) {
            _downloadStatus.value = "正在下载 ${catalogItem.name}..."
            val success = ruleRepository.downloadAndInstallRule(catalogItem)
            _downloadStatus.value = if (success) {
                "${catalogItem.name} 安装成功"
            } else {
                "${catalogItem.name} 安装失败"
            }
            if (success) loadRules()
        }
    }

    /**
     * 批量更新所有规则
     */
    fun updateAllRules() {
        viewModelScope.launch(Dispatchers.IO) {
            _downloadStatus.value = "正在更新所有规则..."
            val count = ruleRepository.updateAllRulesFromRemote()
            _downloadStatus.value = "更新完成，共更新 $count 个规则"
            loadRules()
        }
    }

    /**
     * 重置所有规则
     */
    fun resetAllRules() {
        ruleRepository.resetAllRules()
        loadRules()
    }

    /**
     * 获取规则引擎（供播放器模块使用）
     */
    fun getRuleEngine(): RuleEngine = ruleEngine

    /**
     * 根据规则名获取规则
     */
    fun getRuleByName(name: String): Rule? {
        return _rules.value.find { it.name == name }
    }
}

package com.so.movie.player

import android.app.Activity
import android.content.pm.ActivityInfo
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.navigation.NavController
import com.so.movie.danmaku.DanDanPlayApi
import com.so.movie.danmaku.DanmakuSettings
import com.so.movie.danmaku.DanmakuView
import com.so.movie.rule.Rule
import com.so.movie.ui.theme.Primary
import com.so.movie.viewmodel.RuleViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * 视频播放器组件 — 集成 ExoPlayer + 弹幕系统
 * 支持视频嗅探、播放控制、全屏切换、弹弹play弹幕
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoPlayer(
    navController: NavController,
    ruleViewModel: RuleViewModel,
    title: String = ""
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val playUrl by ruleViewModel.currentPlayUrl.collectAsState()
    val episodeName by ruleViewModel.currentEpisodeName.collectAsState()
    val searchResult by ruleViewModel.selectedSearchResult.collectAsState()
    val currentRule = searchResult?.ruleName?.let {
        ruleViewModel.getRuleByName(it)
    }

    // ===== 播放器状态 =====
    var videoUrl by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var loadError by remember { mutableStateOf<String?>(null) }
    var isPlaying by remember { mutableStateOf(false) }
    var currentPosition by remember { mutableLongStateOf(0L) }
    var totalDuration by remember { mutableLongStateOf(0L) }
    var showControls by remember { mutableStateOf(true) }
    var isFullscreen by remember { mutableStateOf(false) }
    var speed by remember { mutableFloatStateOf(1.0f) }
    var showSpeedMenu by remember { mutableStateOf(false) }
    var isBuffering by remember { mutableStateOf(false) }
    var isSeeking by remember { mutableStateOf(false) }

    // ===== 弹幕状态 =====
    var danmakuSettings by remember { mutableStateOf(DanmakuSettings()) }
    var danmakuEnabled by remember { mutableStateOf(true) }
    var showDanmakuSettings by remember { mutableStateOf(false) }
    var danmakuLoading by remember { mutableStateOf(false) }
    var danmakuCount by remember { mutableIntStateOf(0) }
    var hasDanmaku by remember { mutableStateOf(false) }
    var danmakuView by remember { mutableStateOf<DanmakuView?>(null) }

    // ===== ExoPlayer =====
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            playWhenReady = true
        }
    }

    // 释放资源
    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.release()
            danmakuView?.pause()
        }
    }

    // 生命周期管理
    val lifecycleOwner = LocalContext.current as? LifecycleOwner
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> {
                    exoPlayer.pause()
                    danmakuView?.pause()
                }
                Lifecycle.Event.ON_RESUME -> {
                    if (playUrl.isNotBlank()) {
                        exoPlayer.play()
                        if (danmakuEnabled) danmakuView?.play()
                    }
                }
                else -> {}
            }
        }
        lifecycleOwner?.lifecycle?.addObserver(observer)
        onDispose {
            lifecycleOwner?.lifecycle?.removeObserver(observer)
        }
    }

    // ===== 加载视频 URL =====
    LaunchedEffect(playUrl) {
        if (playUrl.isBlank()) return@LaunchedEffect

        isLoading = true
        loadError = null
        videoUrl = null
        hasDanmaku = false
        danmakuCount = 0
        danmakuView?.clear()

        val sniffer = VideoSourceSniffer(context)
        val ua = currentRule?.userAgent ?: ""
        val referer = currentRule?.referer ?: currentRule?.baseUrl ?: ""

        val resolvedUrl = sniffer.sniffVideoUrl(playUrl, ua, referer)

        if (resolvedUrl != null) {
            videoUrl = resolvedUrl
            val mediaItem = buildMediaItem(resolvedUrl, currentRule)
            exoPlayer.setMediaItem(mediaItem)
            exoPlayer.prepare()
            exoPlayer.play()
        } else {
            loadError = "无法解析视频地址"
            isLoading = false
        }
    }

    // ===== 加载弹幕 =====
    LaunchedEffect(videoUrl) {
        if (videoUrl == null) return@LaunchedEffect

        val animeTitle = searchResult?.title ?: title
        if (animeTitle.isBlank()) return@LaunchedEffect

        danmakuLoading = true
        hasDanmaku = false

        scope.launch(Dispatchers.IO) {
            val episodeNum = extractEpisodeNumber(episodeName)
            val api = DanDanPlayApi.getInstance()
            val danmakuList = api.getDanmakuByTitle(animeTitle, episodeNum)

            withContext(Dispatchers.Main) {
                if (danmakuList.isNotEmpty()) {
                    danmakuView?.setDanmaku(danmakuList)
                    danmakuCount = danmakuList.size
                    hasDanmaku = true
                    if (danmakuEnabled && isPlaying) {
                        danmakuView?.play()
                    }
                }
                danmakuLoading = false
            }
        }
    }

    // ===== 播放器状态监听 + 弹幕时间同步 =====
    LaunchedEffect(exoPlayer) {
        while (true) {
            if (!isSeeking) {
                currentPosition = exoPlayer.currentPosition
            }
            totalDuration = exoPlayer.duration.takeIf { it > 0 } ?: 0L
            val playing = exoPlayer.isPlaying
            isPlaying = playing
            isBuffering = exoPlayer.playbackState == Player.STATE_BUFFERING
            if (exoPlayer.playbackState == Player.STATE_READY) {
                isLoading = false
            }

            // 弹幕时间同步
            if (danmakuEnabled && hasDanmaku) {
                danmakuView?.updateTime(exoPlayer.currentPosition)
                if (playing) {
                    danmakuView?.play()
                } else {
                    danmakuView?.pause()
                }
            }

            delay(200)
        }
    }

    // 自动隐藏控制栏
    LaunchedEffect(showControls, isPlaying) {
        if (showControls && isPlaying) {
            delay(5000)
            showControls = false
        }
    }

    // 全屏切换
    LaunchedEffect(isFullscreen) {
        val activity = context as? Activity
        activity?.requestedOrientation = if (isFullscreen)
            ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        else
            ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }

    // ===== 主画面 =====
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (isFullscreen) Modifier.fillMaxSize()
                else Modifier.aspectRatio(16f / 9f)
            )
            .background(Color.Black)
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { showControls = !showControls }
                )
            }
    ) {
        // ExoPlayer 画面
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    player = exoPlayer
                    useController = false
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // 弹幕层 — 始终保持组合，避免重建丢失数据
        AndroidView(
            factory = { ctx ->
                DanmakuView(ctx).apply {
                    settings = danmakuSettings
                    danmakuView = this
                }
            },
            update = { view ->
                val effective = if (danmakuEnabled) danmakuSettings
                else danmakuSettings.copy(enabled = false)
                view.settings = effective
                view.visibility = if (danmakuEnabled) android.view.View.VISIBLE
                else android.view.View.GONE
            },
            modifier = Modifier.fillMaxSize()
        )

        // 加载中
        if (isLoading || isBuffering) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(40.dp))
            }
        }

        // 弹幕加载提示
        if (danmakuLoading) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 52.dp, end = 8.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.Black.copy(alpha = 0.6f))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(12.dp),
                        strokeWidth = 1.dp
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "加载弹幕", color = Color.White, fontSize = 10.sp)
                }
            }
        }

        // 错误提示
        if (loadError != null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = loadError!!, color = Color.White, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    TextButton(onClick = {
                        isLoading = true
                        loadError = null
                        if (videoUrl != null) {
                            exoPlayer.prepare()
                            exoPlayer.play()
                        }
                    }) {
                        Text("重试", color = Primary)
                    }
                }
            }
        }

        // ===== 控制栏 =====
        if (showControls && !isLoading && loadError == null) {
            // 顶部栏
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                    .align(Alignment.TopStart),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {
                    if (isFullscreen) isFullscreen = false
                    else navController.popBackStack()
                }) {
                    Icon(
                        Icons.Default.ArrowBack, "返回",
                        tint = Color.White, modifier = Modifier.size(24.dp)
                    )
                }
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 14.sp,
                    maxLines = 1,
                    modifier = Modifier.weight(1f)
                )
                // 弹幕开关
                if (hasDanmaku) {
                    IconButton(onClick = {
                        danmakuEnabled = !danmakuEnabled
                        if (danmakuEnabled) {
                            danmakuView?.seek(exoPlayer.currentPosition)
                            danmakuView?.play()
                        } else {
                            danmakuView?.pause()
                            danmakuView?.clear()
                        }
                    }) {
                        Icon(
                            Icons.Default.Subtitles, "弹幕开关",
                            tint = if (danmakuEnabled) Primary else Color.White.copy(alpha = 0.5f),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    // 弹幕设置
                    IconButton(onClick = { showDanmakuSettings = true }) {
                        Icon(
                            Icons.Default.Settings, "弹幕设置",
                            tint = Color.White, modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }

            // 底部控制栏
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                    .align(Alignment.BottomStart)
            ) {
                // 进度条（支持点击跳转）
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(formatTime(currentPosition), color = Color.White, fontSize = 11.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    // 可点击的进度条
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(24.dp) // 更大的触摸区域
                            .pointerInput(totalDuration) {
                                detectTapGestures { offset ->
                                    if (totalDuration > 0) {
                                        val ratio = (offset.x / size.width).coerceIn(0f, 1f)
                                        val seekPos = (ratio * totalDuration).toLong()
                                        exoPlayer.seekTo(seekPos)
                                        danmakuView?.seek(seekPos)
                                        currentPosition = seekPos
                                    }
                                }
                            },
                        contentAlignment = Alignment.CenterStart
                    ) {
                        // 视觉进度条
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(Color.White.copy(alpha = 0.3f))
                        ) {
                            val progress = if (totalDuration > 0) {
                                (currentPosition.toFloat() / totalDuration).coerceIn(0f, 1f)
                            } else 0f
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(progress)
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(Primary)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(formatTime(totalDuration), color = Color.White, fontSize = 11.sp)
                }

                Spacer(modifier = Modifier.height(4.dp))

                // 按钮行
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 播放/暂停
                    IconButton(onClick = {
                        if (exoPlayer.isPlaying) {
                            exoPlayer.pause()
                            danmakuView?.pause()
                        } else {
                            exoPlayer.play()
                            if (danmakuEnabled) danmakuView?.play()
                        }
                    }) {
                        Icon(
                            if (exoPlayer.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            "播放/暂停",
                            tint = Color.White, modifier = Modifier.size(24.dp)
                        )
                    }

                    // 倍速
                    Box {
                        TextButton(onClick = { showSpeedMenu = !showSpeedMenu }) {
                            Text("${speed}x", color = Color.White, fontSize = 12.sp)
                        }
                        DropdownMenu(
                            expanded = showSpeedMenu,
                            onDismissRequest = { showSpeedMenu = false }
                        ) {
                            listOf(0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 2.0f).forEach { s ->
                                DropdownMenuItem(
                                    text = { Text("${s}x") },
                                    onClick = {
                                        speed = s
                                        exoPlayer.playbackParameters = PlaybackParameters(s)
                                        showSpeedMenu = false
                                    }
                                )
                            }
                        }
                    }

                    // 全屏切换
                    IconButton(onClick = { isFullscreen = !isFullscreen }) {
                        Icon(
                            if (isFullscreen) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                            "全屏",
                            tint = Color.White, modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            // 中心播放按钮
            if (!isPlaying && !isBuffering) {
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.5f))
                        .clickable {
                            exoPlayer.play()
                            if (danmakuEnabled) danmakuView?.play()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.PlayArrow, "播放",
                        tint = Color.White, modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
    }

    // ===== 弹幕设置面板 =====
    if (showDanmakuSettings) {
        ModalBottomSheet(
            onDismissRequest = { showDanmakuSettings = false },
            containerColor = Color.White
        ) {
            DanmakuSettingsContent(
                settings = danmakuSettings,
                enabled = danmakuEnabled,
                danmakuCount = danmakuCount,
                onEnabledChange = { enabled ->
                    danmakuEnabled = enabled
                    if (enabled) {
                        danmakuView?.seek(exoPlayer.currentPosition)
                        danmakuView?.play()
                    } else {
                        danmakuView?.pause()
                        danmakuView?.clear()
                    }
                },
                onSettingsChange = { newSettings ->
                    danmakuSettings = newSettings
                    danmakuView?.settings = if (danmakuEnabled) newSettings
                    else newSettings.copy(enabled = false)
                }
            )
        }
    }
}

/**
 * 弹幕设置面板内容
 */
@Composable
private fun DanmakuSettingsContent(
    settings: DanmakuSettings,
    enabled: Boolean,
    danmakuCount: Int,
    onEnabledChange: (Boolean) -> Unit,
    onSettingsChange: (DanmakuSettings) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(bottom = 32.dp)
    ) {
        // 标题行
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "弹幕设置",
                style = MaterialTheme.typography.titleMedium,
                color = Color.Black
            )
            Text(
                text = if (danmakuCount > 0) "共 $danmakuCount 条弹幕" else "暂无弹幕",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 弹幕开关
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("开启弹幕", style = MaterialTheme.typography.bodyLarge)
            Switch(checked = enabled, onCheckedChange = onEnabledChange)
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

        // 字体大小
        Text("字体大小: ${settings.fontSize.toInt()}sp", style = MaterialTheme.typography.bodyMedium)
        Slider(
            value = settings.fontSize,
            onValueChange = { v -> onSettingsChange(settings.copy(fontSize = v)) },
            valueRange = 10f..28f,
            steps = 8,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(4.dp))

        // 透明度
        Text("透明度: ${(settings.opacity * 100).toInt()}%", style = MaterialTheme.typography.bodyMedium)
        Slider(
            value = settings.opacity,
            onValueChange = { v -> onSettingsChange(settings.copy(opacity = v)) },
            valueRange = 0.2f..1.0f,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(4.dp))

        // 滚动速度
        Text("滚动速度: ${String.format("%.1f", settings.speed)}x", style = MaterialTheme.typography.bodyMedium)
        Slider(
            value = settings.speed,
            onValueChange = { v -> onSettingsChange(settings.copy(speed = v)) },
            valueRange = 0.5f..3.0f,
            steps = 4,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(4.dp))

        // 最大显示数
        Text("最大显示数: ${settings.maxDisplay}", style = MaterialTheme.typography.bodyMedium)
        Slider(
            value = settings.maxDisplay.toFloat(),
            onValueChange = { v -> onSettingsChange(settings.copy(maxDisplay = v.toInt())) },
            valueRange = 10f..100f,
            steps = 8,
            modifier = Modifier.fillMaxWidth()
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

        // 弹幕类型开关
        Text("弹幕类型", style = MaterialTheme.typography.bodyLarge)
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("滚动弹幕", style = MaterialTheme.typography.bodyMedium)
            Switch(
                checked = settings.showScroll,
                onCheckedChange = { v -> onSettingsChange(settings.copy(showScroll = v)) }
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("顶部弹幕", style = MaterialTheme.typography.bodyMedium)
            Switch(
                checked = settings.showTop,
                onCheckedChange = { v -> onSettingsChange(settings.copy(showTop = v)) }
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("底部弹幕", style = MaterialTheme.typography.bodyMedium)
            Switch(
                checked = settings.showBottom,
                onCheckedChange = { v -> onSettingsChange(settings.copy(showBottom = v)) }
            )
        }
    }
}

/**
 * 构建 MediaItem（带 HTTP 头）
 */
private fun buildMediaItem(url: String, rule: Rule?): MediaItem {
    val builder = MediaItem.Builder().setUri(url)

    val headers = mutableMapOf<String, String>()
    val ua = rule?.userAgent?.takeIf { it.isNotEmpty() }
        ?: "Mozilla/5.0 (Linux; Android 13; Pixel 7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36"
    headers["User-Agent"] = ua

    if (rule != null) {
        val referer = rule.referer.takeIf { it.isNotEmpty() } ?: rule.baseUrl
        if (referer.isNotEmpty()) {
            headers["Referer"] = referer
        }
    }

    return builder.build()
}

/**
 * 格式化时间
 */
private fun formatTime(ms: Long): String {
    if (ms <= 0) return "00:00"
    val totalSeconds = ms / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return if (hours > 0) {
        String.format("%02d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format("%02d:%02d", minutes, seconds)
    }
}

/**
 * 从集名中提取集数
 * 支持: "第1集", "第01集", "EP01", "01", "第一集" 等
 */
private fun extractEpisodeNumber(name: String): Int {
    if (name.isBlank()) return 1

    // 尝试提取数字
    val regex = Regex("(\\d+)")
    val match = regex.find(name)
    if (match != null) {
        return match.groupValues[1].toIntOrNull()?.coerceAtLeast(1) ?: 1
    }

    // 中文数字
    val chineseNum = mapOf(
        "一" to 1, "二" to 2, "三" to 3, "四" to 4, "五" to 5,
        "六" to 6, "七" to 7, "八" to 8, "九" to 9, "十" to 10
    )
    for ((ch, num) in chineseNum) {
        if (name.contains(ch)) return num
    }

    return 1
}

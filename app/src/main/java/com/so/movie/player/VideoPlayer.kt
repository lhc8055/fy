package com.so.movie.player

import android.app.Activity
import android.content.pm.ActivityInfo
import android.net.Uri
import android.view.ViewGroup
import android.view.WindowManager
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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.ui.PlayerView
import androidx.navigation.NavController
import com.so.movie.rule.Rule
import com.so.movie.ui.theme.Primary
import com.so.movie.viewmodel.RuleViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * 视频播放器组件 — 集成 ExoPlayer
 * 支持视频嗅探、播放控制、全屏切换
 */
@Composable
fun VideoPlayer(
    navController: NavController,
    ruleViewModel: RuleViewModel,
    title: String = ""
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val playUrl by ruleViewModel.currentPlayUrl.collectAsState()
    val currentRule = ruleViewModel.selectedSearchResult.value?.ruleName?.let {
        ruleViewModel.getRuleByName(it)
    }

    // 状态
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

    // ExoPlayer 实例
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            playWhenReady = true
        }
    }

    // 释放 ExoPlayer
    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.release()
        }
    }

    // 生命周期管理
    val lifecycleOwner = LocalContext.current as? LifecycleOwner
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> exoPlayer.pause()
                Lifecycle.Event.ON_RESUME -> if (playUrl != null) exoPlayer.play()
                else -> {}
            }
        }
        lifecycleOwner?.lifecycle?.addObserver(observer)
        onDispose {
            lifecycleOwner?.lifecycle?.removeObserver(observer)
        }
    }

    // 加载视频 URL
    LaunchedEffect(playUrl) {
        if (playUrl.isBlank()) return@LaunchedEffect

        isLoading = true
        loadError = null
        videoUrl = null

        val sniffer = VideoSourceSniffer(context)
        val ua = currentRule?.userAgent ?: ""
        val referer = currentRule?.referer ?: currentRule?.baseUrl ?: ""

        val resolvedUrl = sniffer.sniffVideoUrl(playUrl, ua, referer)

        if (resolvedUrl != null) {
            videoUrl = resolvedUrl
            // 设置 MediaItem
            val mediaItem = buildMediaItem(resolvedUrl, currentRule)
            exoPlayer.setMediaItem(mediaItem)
            exoPlayer.prepare()
            exoPlayer.play()
        } else {
            loadError = "无法解析视频地址"
            isLoading = false
        }
    }

    // 播放器状态监听
    LaunchedEffect(exoPlayer) {
        while (true) {
            currentPosition = exoPlayer.currentPosition
            totalDuration = exoPlayer.duration.takeIf { it > 0 } ?: 0L
            isPlaying = exoPlayer.isPlaying
            isBuffering = exoPlayer.playbackState == Player.STATE_BUFFERING
            if (exoPlayer.playbackState == Player.STATE_READY) {
                isLoading = false
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
        // ExoPlayer PlayerView
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    player = exoPlayer
                    useController = false // 使用自定义控制栏
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                }
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

        // 错误提示
        if (loadError != null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = loadError!!,
                        color = Color.White,
                        fontSize = 14.sp
                    )
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

        // 控制栏
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
                    if (isFullscreen) {
                        isFullscreen = false
                    } else {
                        navController.popBackStack()
                    }
                }) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "返回",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 14.sp,
                    maxLines = 1,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = { /* 更多 */ }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "更多",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
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
                // 进度条
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = formatTime(currentPosition),
                        color = Color.White,
                        fontSize = 11.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    // 自定义进度条
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(Color.White.copy(alpha = 0.3f))
                            .clickable { /* TODO: 拖拽进度 */ }
                    ) {
                        val progress = if (totalDuration > 0) {
                            (currentPosition.toFloat() / totalDuration)
                        } else 0f
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(progress.coerceIn(0f, 1f))
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(Primary)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = formatTime(totalDuration),
                        color = Color.White,
                        fontSize = 11.sp
                    )
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
                        if (exoPlayer.isPlaying) exoPlayer.pause() else exoPlayer.play()
                    }) {
                        Icon(
                            imageVector = if (exoPlayer.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = "播放/暂停",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    // 倍速
                    Box {
                        TextButton(onClick = { showSpeedMenu = !showSpeedMenu }) {
                            Text(
                                text = "${speed}x",
                                color = Color.White,
                                fontSize = 12.sp
                            )
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
                            imageVector = if (isFullscreen) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                            contentDescription = "全屏",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
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
                        .clickable { exoPlayer.play() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "播放",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
    }
}

/**
 * 构建 MediaItem（带 HTTP 头）
 */
private fun buildMediaItem(url: String, rule: Rule?): MediaItem {
    val builder = MediaItem.Builder().setUri(url)

    // 设置 HTTP 头（User-Agent, Referer）
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

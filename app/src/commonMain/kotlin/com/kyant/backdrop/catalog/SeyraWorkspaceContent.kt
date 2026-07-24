package com.kyant.backdrop.catalog

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.togetherWith
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.gestures.horizontalDrag
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import com.kyant.backdrop.backdrops.LayerBackdrop
import com.kyant.backdrop.catalog.components.LiquidBottomTab
import com.kyant.backdrop.catalog.components.LiquidBottomTabs
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.colorControls
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy
import com.kyant.backdrop.highlight.Highlight
import com.kyant.backdrop.catalog.utils.BackHandler
import com.kyant.backdrop.catalog.components.LiquidToggle
import com.kyant.shapes.Capsule
import com.kyant.shapes.RoundedRectangle
import glass.app.generated.resources.Res
import glass.app.generated.resources.ic_dock_compass_40px
import glass.app.generated.resources.ic_dock_folder_40px
import glass.app.generated.resources.ic_dock_user_40px
import glass.app.generated.resources.ic_dock_wrench_40px
import glass.app.generated.resources.ic_profile_contact_32px
import glass.app.generated.resources.ic_profile_feedback_32px
import glass.app.generated.resources.ic_profile_settings_32px
import glass.app.generated.resources.ic_top_more_24px
import glass.app.generated.resources.ic_top_share_24px
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private data class SeyraCard(
    val title: String,
    val subtitle: String,
    val tint: Color,
    val category: String
)

private data class SeyraDockTab(
    val icon: DrawableResource,
    val label: String
)

private val workspaceCards = listOf(
    SeyraCard("社工查询", "综合信息查询", Color(0xFF70D7FF), "查询"),
    SeyraCard("网易云解析", "音乐搜索 / 在线播放", Color(0xFFFF5B58), "音乐"),
    SeyraCard("手机号补齐", "姓名 手机号", Color(0xFFFFC56E), "查询"),
    SeyraCard("灵感盒子", "收藏碎片 / 快速保存", Color(0xFFFF8EC7), "收藏"),
    SeyraCard("快捷工具", "常用操作 / 一键启动", Color(0xFF6EF0BC), "工具"),
    SeyraCard("数据概览", "进度统计 / 状态查看", Color(0xFF7EA8FF), "数据"),
    SeyraCard("工作流", "步骤管理 / 自动流程", Color(0xFFFF9D78), "流程"),
    SeyraCard("个人空间", "偏好设置 / 账号信息", Color(0xFF8EE7FF), "账号")
)

private val resourceCards = listOf(
    SeyraCard("辅助", "常用辅助 / 快速查找", Color(0xFF70D7FF), "辅助"),
    SeyraCard("玩机", "玩机模块 / 实用工具", Color(0xFF9B7CFF), "玩机"),
    SeyraCard("文件分组", "分类整理 / 目录管理", Color(0xFF6EF0BC), "辅助"),
    SeyraCard("辅助入口一", "辅助工具 / 快速入口", Color(0xFFFF8EC7), "辅助"),
    SeyraCard("辅助入口二", "辅助工具 / 快速入口", Color(0xFF70D7FF), "辅助"),
    SeyraCard("辅助入口三", "辅助工具 / 快速入口", Color(0xFFFFC56E), "辅助"),
    SeyraCard("辅助入口四", "辅助工具 / 快速入口", Color(0xFF7EA8FF), "辅助"),
    SeyraCard("链接仓库", "常用网址 / 入口保存", Color(0xFFFFC56E), "链接"),
    SeyraCard("赏帮", "赏帮赚钱 / 任务平台", Color(0xFFFF5B58), "辅助"),
    SeyraCard("TG账号", "超低价格 / 快速购买", Color(0xFF70D7FF), "辅助"),
    SeyraCard("辅助整合", "游戏辅助 / 一键整合", Color(0xFF9B7CFF), "辅助"),
    SeyraCard("收藏夹", "重要内容 / 快速访问", Color(0xFFFF8EC7), "收藏"),
    SeyraCard("最近使用", "最近打开 / 历史记录", Color(0xFF7EA8FF), "最近"),
    SeyraCard("香肠派对", "热门游戏 / 资源入口", Color(0xFFFFC56E), "玩机")
)

private fun formatXrayResult(raw: String): String {
    val content = extractJsonStringValue(raw, "content") ?: raw
    val decoded = decodeEscapedText(content)
        .replace("},{", "}\n{")
        .replace("}, {", "}\n{")
        .replace("}，{", "}\n{")
        .replace("\\n", "\n")
        .replace("\\r", "\n")
        .replace("\\t", " ")
        .replace("\r", "\n")
        .replace("**", "")
        .replace(Regex("""[{}\[\]"]"""), "")

    val lines = decoded
        .split('\n', ';', '；')
        .map { it.trim().trim(',', '，') }
        .filter { it.isNotBlank() }
        .map { normalizeXrayLine(it) }
        .distinct()

    if (lines.isEmpty()) {
        return raw.trim()
    }

    return groupXrayLinesByPerson(lines)
}

private fun extractJsonStringValue(raw: String, key: String): String? {
    val keyIndex = raw.indexOf("\"$key\"")
    if (keyIndex == -1) return null
    val colonIndex = raw.indexOf(':', startIndex = keyIndex)
    if (colonIndex == -1) return null
    val firstQuoteIndex = raw.indexOf('"', startIndex = colonIndex + 1)
    if (firstQuoteIndex == -1) return null

    val builder = StringBuilder()
    var escaped = false
    var index = firstQuoteIndex + 1
    while (index < raw.length) {
        val char = raw[index]
        if (escaped) {
            builder.append('\\').append(char)
            escaped = false
        } else if (char == '\\') {
            escaped = true
        } else if (char == '"') {
            return builder.toString()
        } else {
            builder.append(char)
        }
        index++
    }
    return null
}

private fun decodeEscapedText(value: String): String {
    val builder = StringBuilder()
    var index = 0
    while (index < value.length) {
        val char = value[index]
        if (char == '\\' && index + 1 < value.length) {
            when (val next = value[index + 1]) {
                'n' -> {
                    builder.append('\n')
                    index += 2
                }
                'r' -> {
                    builder.append('\n')
                    index += 2
                }
                't' -> {
                    builder.append(' ')
                    index += 2
                }
                'u' -> {
                    val hexEnd = (index + 6).coerceAtMost(value.length)
                    val hex = value.substring(index + 2, hexEnd)
                    if (hex.length == 4 && hex.all { it in '0'..'9' || it in 'a'..'f' || it in 'A'..'F' }) {
                        builder.append(hex.toInt(16).toChar())
                        index += 6
                    } else {
                        builder.append(next)
                        index += 2
                    }
                }
                '\\', '"', '/' -> {
                    builder.append(next)
                    index += 2
                }
                else -> {
                    builder.append(next)
                    index += 2
                }
            }
        } else {
            builder.append(char)
            index++
        }
    }
    return builder.toString()
}

private fun normalizeXrayLine(line: String): String {
    return line
        .replace(Regex("""(?i)\b(name|realname|username)\b"""), "姓名")
        .replace(Regex("""(?i)\b(phone|mobile|tel|telephone|mobilephone)\b"""), "手机")
        .replace(Regex("""(?i)\b(idcard|cardno|identity|identitycard|sfz)\b"""), "身份证")
        .replace(Regex("""(?i)\b(address|addr)\b"""), "地址")
        .replace(Regex("""(?i)\b(email|mail)\b"""), "邮箱")
        .replace(Regex("""\s*[:：]\s*"""), "：")
        .replace(Regex("""\s+"""), " ")
        .trim()
}

private fun groupXrayLinesByPerson(lines: List<String>): String {
    val groups = mutableListOf<List<String>>()
    val current = mutableListOf<String>()

    fun flushCurrent() {
        if (current.isNotEmpty()) {
            groups.add(current.toList())
            current.clear()
        }
    }

    lines.forEach { line ->
        if (shouldStartNewXrayPerson(line, current)) {
            flushCurrent()
        }
        current.add(line)
    }
    flushCurrent()

    if (groups.size <= 1) {
        return sortXrayGroup(lines).joinToString("\n")
    }

    return groups.mapIndexed { index, group ->
        val title = "人员 ${index + 1}"
        title + "\n" + sortXrayGroup(group).joinToString("\n")
    }.joinToString("\n\n")
}

private fun shouldStartNewXrayPerson(line: String, current: List<String>): Boolean {
    if (current.isEmpty()) return false
    val currentText = current.joinToString(" ")
    val lineHasName = hasXrayName(line)
    val lineHasPhone = hasXrayPhone(line)
    val lineHasId = hasXrayIdCard(line)

    return when {
        lineHasName && hasXrayName(currentText) -> true
        lineHasId && hasXrayIdCard(currentText) -> true
        lineHasPhone && hasXrayPhone(currentText) && (lineHasName || lineHasId || hasXrayName(currentText) || hasXrayIdCard(currentText)) -> true
        else -> false
    }
}

private fun sortXrayGroup(group: List<String>): List<String> {
    return group.distinct()
        .sortedWith(compareBy<String> { xrayLinePriority(it) }.thenBy { it })
}

private fun hasXrayName(value: String): Boolean {
    return value.contains("姓名") || value.contains("名字")
}

private fun hasXrayPhone(value: String): Boolean {
    return Regex("""1\d{10}""").containsMatchIn(value) || value.contains("手机") || value.contains("电话")
}

private fun hasXrayIdCard(value: String): Boolean {
    return Regex("""\d{17}[\dXx]""").containsMatchIn(value) || value.contains("身份证")
}

private fun xrayLinePriority(line: String): Int {
    return when {
        line.contains("姓名") || line.contains("名字") -> 0
        line.contains("手机") || line.contains("电话") || Regex("""1\d{10}""").containsMatchIn(line) -> 1
        line.contains("身份证") || Regex("""\d{17}[\dXx]""").containsMatchIn(line) -> 2
        line.contains("性别") -> 3
        line.contains("年龄") || line.contains("出生") -> 4
        line.contains("户籍") || line.contains("地址") || line.contains("地区") || line.contains("省") -> 5
        line.contains("QQ", ignoreCase = true) || line.contains("微信") -> 6
        line.contains("邮箱") || line.contains("@") -> 7
        else -> 20
    }
}

@Composable
fun SeyraSplashScreen(
    onDismiss: () -> Unit
) {
    var countdown by remember { mutableIntStateOf(3) }

    LaunchedEffect(Unit) {
        for (i in 3 downTo 1) {
            countdown = i
            delay(1000L)
        }
        onDismiss()
    }

    Box(Modifier.fillMaxSize()) {
        SeyraSplashImage(
            modifier = Modifier.fillMaxSize()
        )

        Row(
            Modifier
                .align(Alignment.TopEnd)
                .statusBarsPadding()
                .padding(top = 16f.dp, end = 16f.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismiss
                )
                .background(Color(0x66000000), shape = RoundedCornerShape(20f.dp))
                .padding(horizontal = 14f.dp, vertical = 8f.dp),
            horizontalArrangement = Arrangement.spacedBy(6f.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BasicText(
                "跳过 $countdown",
                style = TextStyle(
                    color = Color.White,
                    fontSize = 13f.sp,
                    fontWeight = FontWeight.Medium
                )
            )
        }
    }
}

@Composable
fun SeyraWorkspaceContent() {
    LaunchSplashImageUpdater()
    var showSplash by rememberSaveable { mutableStateOf(true) }

    BackdropDemoScaffold {
        SeyraWorkspace(it)
    }

    AnimatedVisibility(
        visible = showSplash,
        enter = fadeIn(tween(200)),
        exit = fadeOut(tween(400))
    ) {
        SeyraSplashScreen(
            onDismiss = { showSplash = false }
        )
    }

}

@Composable
private fun BoxScope.SeyraWorkspace(backdrop: LayerBackdrop) {
    var selectedTabIndex by rememberSaveable { mutableIntStateOf(0) }
    var showSettingsPage by rememberSaveable { mutableStateOf(false) }
    var showContactDialog by rememberSaveable { mutableStateOf(false) }
    var showMusicWebsitePage by rememberSaveable { mutableStateOf(false) }
    val shareApp = rememberShareAppAction()
    val openFeedback = rememberOpenFeedbackAction()
    val copyText = rememberCopyTextAction()

    BackHandler(enabled = showSettingsPage, onBack = { showSettingsPage = false })
    BackHandler(enabled = showMusicWebsitePage, onBack = { showMusicWebsitePage = false })

    var isAtBottom by rememberSaveable { mutableStateOf(false) }

    if (!showSettingsPage && !showMusicWebsitePage) {
        Crossfade(
            targetState = selectedTabIndex,
            animationSpec = tween(280, easing = FastOutSlowInEasing)
        ) { page ->
            SeyraPageContent(
                tabIndex = page,
                backdrop = backdrop,
                onContactClick = { showContactDialog = true },
                onFeedbackClick = openFeedback,
                onSettingsClick = { showSettingsPage = true },
                onMusicWebsiteClick = { showMusicWebsitePage = true },
                onScrollStateChange = { isAtBottom = it }
            )
        }
    }

    AnimatedContent(
        targetState = showMusicWebsitePage && !showSettingsPage,
        transitionSpec = {
            if (targetState) {
                fadeIn(tween(420, easing = FastOutSlowInEasing)) +
                scaleIn(tween(420, easing = FastOutSlowInEasing), initialScale = 0.95f) togetherWith
                fadeOut(tween(280, easing = FastOutSlowInEasing)) +
                scaleOut(tween(280, easing = FastOutSlowInEasing), targetScale = 0.95f)
            } else {
                fadeIn(tween(350, easing = FastOutSlowInEasing)) +
                scaleIn(tween(350, easing = FastOutSlowInEasing), initialScale = 0.95f) togetherWith
                fadeOut(tween(300, easing = FastOutSlowInEasing)) +
                scaleOut(tween(300, easing = FastOutSlowInEasing), targetScale = 0.95f)
            }
        },
        label = "music_website"
    ) { visible ->
        if (visible) {
            SeyraMusicWebsiteFullPage(
                onBack = { showMusicWebsitePage = false },
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }

    AnimatedContent(
        targetState = showSettingsPage,
        transitionSpec = {
            if (targetState) {
                fadeIn(tween(420, easing = FastOutSlowInEasing)) +
                scaleIn(tween(420, easing = FastOutSlowInEasing), initialScale = 0.95f) togetherWith
                fadeOut(tween(280, easing = FastOutSlowInEasing)) +
                scaleOut(tween(280, easing = FastOutSlowInEasing), targetScale = 0.95f)
            } else {
                fadeIn(tween(350, easing = FastOutSlowInEasing)) +
                scaleIn(tween(350, easing = FastOutSlowInEasing), initialScale = 0.95f) togetherWith
                fadeOut(tween(300, easing = FastOutSlowInEasing)) +
                scaleOut(tween(300, easing = FastOutSlowInEasing), targetScale = 0.95f)
            }
        },
        label = "settings_page"
    ) { visible ->
        if (visible) {
            SeyraSettingsPage(
                onBack = { showSettingsPage = false },
                backdrop = backdrop,
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }

    if (!showSettingsPage && !showMusicWebsitePage) {
        val gradientAlpha by animateFloatAsState(
            targetValue = if (isAtBottom) 0f else 1f,
            animationSpec = tween(300, easing = FastOutSlowInEasing)
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(80f.dp)
                .graphicsLayer { alpha = gradientAlpha }
                .drawBehind {
                    drawRect(
                        Brush.verticalGradient(
                            0.00f to Color.Transparent,
                            0.35f to Color(0x08FFFFFF),
                            0.65f to Color(0x10FFFFFF),
                            1.00f to Color.Transparent
                        )
                    )
                }
        )

        SeyraDock(
            selectedTabIndex = selectedTabIndex,
            onTabSelected = { selectedTabIndex = it },
            onResourcePressed = {},
            backdrop = backdrop,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(start = 38f.dp, end = 38f.dp, bottom = 12f.dp)
        )
    }

    AnimatedVisibility(
        visible = !showSettingsPage && selectedTabIndex != 3 && selectedTabIndex != 1,
        enter = fadeIn(animationSpec = tween(220)) + slideInHorizontally(
            initialOffsetX = { it / 3 },
            animationSpec = tween(280, easing = FastOutSlowInEasing)
        ),
        exit = fadeOut(animationSpec = tween(160)) + slideOutHorizontally(
            targetOffsetX = { it / 3 },
            animationSpec = tween(200, easing = FastOutSlowInEasing)
        ),
        modifier = Modifier
            .align(Alignment.TopEnd)
            .statusBarsPadding()
            .displayCutoutPadding()
            .padding(top = 18f.dp, end = 22f.dp)
    ) {
        SeyraTopActions(
            backdrop = backdrop,
            onShareClick = shareApp
        )
    }

    if (showContactDialog) {
        SeyraContactDialog(
            backdrop = backdrop,
            onCancel = { showContactDialog = false },
            onCopy = {
                copyText("@sspyj")
                showContactDialog = false
            },
            modifier = Modifier.align(Alignment.Center)
        )
    }
}

@Composable
private fun SeyraMusicWebsiteFullPage(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var backVisible by remember { mutableStateOf(false) }
    var webViewVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        backVisible = true
        delay(80)
        webViewVisible = true
    }

    Column(
        modifier
            .fillMaxSize()
            .statusBarsPadding()
            .displayCutoutPadding()
            .padding(start = 14f.dp, top = 82f.dp, end = 14f.dp, bottom = 14f.dp),
        verticalArrangement = Arrangement.spacedBy(10f.dp)
    ) {
        AnimatedVisibility(
            visible = backVisible,
            enter = fadeIn(tween(380, easing = FastOutSlowInEasing)) +
                    slideInVertically(tween(380, easing = FastOutSlowInEasing)) { -it / 5 },
            exit = fadeOut(tween(220, easing = FastOutSlowInEasing)) +
                   slideOutVertically(tween(220, easing = FastOutSlowInEasing)) { -it / 5 }
        ) {
            BasicText(
                "‹ 返回",
                modifier = Modifier
                    .padding(start = 4f.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onBack
                    ),
                style = TextStyle(
                    color = Color(0xFF008DFF),
                    fontSize = 16f.sp,
                    fontWeight = FontWeight.SemiBold
                )
            )
        }
        AnimatedVisibility(
            visible = webViewVisible,
            enter = fadeIn(tween(420, delayMillis = 100, easing = FastOutSlowInEasing)) +
                    slideInVertically(tween(420, delayMillis = 100, easing = FastOutSlowInEasing)) { it / 4 },
            exit = fadeOut(tween(260, easing = FastOutSlowInEasing)) +
                   slideOutVertically(tween(260, easing = FastOutSlowInEasing)) { it / 4 }
        ) {
            SeyraEmbeddedWebPage(
                url = "https://yy.luodian.net.cn/",
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(18f.dp))
            )
        }
    }
}

@Composable
private fun SeyraPageContent(
    tabIndex: Int,
    backdrop: LayerBackdrop,
    onContactClick: () -> Unit,
    onFeedbackClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onMusicWebsiteClick: () -> Unit,
    onScrollStateChange: (Boolean) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val listState = rememberLazyListState()
    val containerHeight = remember { mutableIntStateOf(0) }
    val containerTop = remember { mutableIntStateOf(0) }
    val dockFadeZonePx = with(density) { 100f.dp.toPx() }
    val resourceQuery = rememberSaveable { mutableStateOf("") }
    val resourceCategory = rememberSaveable { mutableStateOf("全部") }
    val resourceCategories = remember { listOf("全部", "辅助", "玩机", "收藏", "最近") }
    val filteredResourceCards = remember(resourceQuery.value, resourceCategory.value) {
        val keyword = resourceQuery.value.trim()
        resourceCards.filter { card ->
            val categoryMatched = resourceCategory.value == "全部" || card.category == resourceCategory.value
            val keywordMatched = keyword.isEmpty() ||
                card.title.contains(keyword, ignoreCase = true) ||
                card.subtitle.contains(keyword, ignoreCase = true) ||
                card.category.contains(keyword, ignoreCase = true)
            categoryMatched && keywordMatched
        }.let { list ->
            if (resourceCategory.value == "全部") {
                list.sortedBy { if (it.title.contains("使命")) 1 else 0 }
            } else list
        }
    }

    val showResourceHeader = tabIndex == 1
    var searchBarVisible by remember { mutableStateOf(false) }
    LaunchedEffect(showResourceHeader) {
        searchBarVisible = showResourceHeader
    }

    Column(
        modifier
            .fillMaxSize()
            .statusBarsPadding()
            .displayCutoutPadding()
            .onGloballyPositioned { coordinates ->
                containerTop.intValue = coordinates.positionInRoot().y.toInt()
                containerHeight.intValue = coordinates.size.height
            }
    ) {
        // 固定搜索框和分类栏（仅资源页）
        AnimatedVisibility(
            visible = searchBarVisible,
            enter = fadeIn(tween(300, easing = FastOutSlowInEasing)) +
                    slideInVertically(tween(300, easing = FastOutSlowInEasing)) { -it / 4 },
            exit = fadeOut(tween(250, easing = FastOutSlowInEasing)) +
                   slideOutVertically(tween(250, easing = FastOutSlowInEasing)) { -it / 4 }
        ) {
            Column(
                Modifier
                    .padding(horizontal = 22f.dp)
                    .padding(top = 18f.dp),
                verticalArrangement = Arrangement.spacedBy(14f.dp)
            ) {
                SeyraSearchField(
                    value = resourceQuery.value,
                    onValueChange = { resourceQuery.value = it },
                    backdrop = backdrop
                )
                SeyraCategoryChips(
                    categories = resourceCategories,
                    selectedCategory = resourceCategory.value,
                    onCategorySelected = { resourceCategory.value = it },
                    backdrop = backdrop
                )
            }
        }

        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 22f.dp,
                top = when (tabIndex) {
                    0 -> 100f.dp
                    1 -> 12f.dp
                    2 -> 82f.dp
                    3 -> 82f.dp
                    else -> 100f.dp
                },
                end = 22f.dp,
                bottom = 124f.dp
            ),
            verticalArrangement = Arrangement.spacedBy(14f.dp)
        ) {
            if (tabIndex == 0) {
                item {
                    SeyraLiquidHeaderPanel(backdrop)
                }
                item {
                    SeyraDiscoverFrostedPanel(
                        backdrop = backdrop,
                        modifier = Modifier.padding(top = 12f.dp)
                    )
                }
            } else if (tabIndex == 1) {
                val alphaProvider: (Int) -> Float = { itemBottomPx ->
                    val dockTopPx = containerTop.intValue + containerHeight.intValue - dockFadeZonePx
                    val distance = dockTopPx - itemBottomPx
                    when {
                        distance >= dockFadeZonePx -> 1f
                        distance <= 0f -> 0.2f
                        else -> max(0.2f, min(1f, 1f - (1f - distance / dockFadeZonePx) * 0.8f))
                    }
                }
                item {
                    SeyraCardGrid(
                        cards = filteredResourceCards,
                        backdrop = backdrop,
                        onCardClick = {},
                        cardRowAlpha = alphaProvider
                    )
                }
            } else if (tabIndex == 2) {
                item {
                    SeyraToolNavigationStack(
                        backdrop = backdrop,
                        onMusicWebsiteClick = onMusicWebsiteClick
                    )
                }
            } else if (tabIndex == 3) {
                item {
                    SeyraProfilePage(
                        backdrop = backdrop,
                        onContactClick = onContactClick,
                        onFeedbackClick = onFeedbackClick,
                        onSettingsClick = onSettingsClick
                    )
                }
            }
        }

        val isAtBottom by remember {
            derivedStateOf {
                val layoutInfo = listState.layoutInfo
                val lastVisible = layoutInfo.visibleItemsInfo.lastOrNull()
                lastVisible != null && (lastVisible.offset + lastVisible.size >= layoutInfo.viewportEndOffset - 4)
            }
        }
        LaunchedEffect(isAtBottom) {
            onScrollStateChange(isAtBottom)
        }
    }
}

@Composable
private fun SeyraResourcePage(
    backdrop: LayerBackdrop,
    cardRowAlpha: (Int) -> Float = { 1f }
) {
    var query by rememberSaveable { mutableStateOf("") }
    var selectedCategory by rememberSaveable { mutableStateOf("全部") }
    val categories = remember { listOf("全部", "辅助", "玩机", "收藏", "最近") }
    val filteredCards = remember(query, selectedCategory) {
        val keyword = query.trim()
        resourceCards.filter { card ->
            val categoryMatched = selectedCategory == "全部" || card.category == selectedCategory
            val keywordMatched = keyword.isEmpty() ||
                card.title.contains(keyword, ignoreCase = true) ||
                card.subtitle.contains(keyword, ignoreCase = true) ||
                card.category.contains(keyword, ignoreCase = true)
            categoryMatched && keywordMatched
        }.let { list ->
            if (selectedCategory == "全部") {
                list.sortedBy { if (it.title.contains("使命")) 1 else 0 }
            } else list
        }
    }

    Column(
        Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14f.dp)
    ) {
        SeyraSearchField(
            value = query,
            onValueChange = { query = it },
            backdrop = backdrop
        )
        SeyraCategoryChips(
            categories = categories,
            selectedCategory = selectedCategory,
            onCategorySelected = { selectedCategory = it },
            backdrop = backdrop
        )
        SeyraCardGrid(
            cards = filteredCards,
            backdrop = backdrop,
            onCardClick = {},
            cardRowAlpha = cardRowAlpha
        )
    }
}

@Composable
private fun SeyraSearchField(
    value: String,
    onValueChange: (String) -> Unit,
    backdrop: LayerBackdrop,
    modifier: Modifier = Modifier
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        textStyle = TextStyle(
            color = Color(0xFF05070A),
            fontSize = 16f.sp,
            fontWeight = FontWeight.Medium
        ),
        modifier = modifier
            .fillMaxWidth()
            .height(54f.dp)
            .drawBackdrop(
                backdrop = backdrop,
                shape = { RoundedRectangle(22f.dp) },
                effects = {
                    vibrancy()
                    blur(10f.dp.toPx())
                    lens(12f.dp.toPx(), 20f.dp.toPx())
                },
                onDrawSurface = {
                    drawRect(Color(0x76FFFFFF))
                    drawRect(Color(0x126EBBFF), blendMode = BlendMode.Screen)
                }
            )
            .padding(horizontal = 18f.dp),
        decorationBox = { innerTextField ->
            Row(
                Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BasicText(
                    "搜索",
                    style = TextStyle(
                        color = Color(0x8A05070A),
                        fontSize = 15f.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                )
                Box(Modifier.padding(start = 12f.dp).weight(1f)) {
                    if (value.isEmpty()) {
                        BasicText(
                            "输入关键词查找资源",
                            style = TextStyle(
                                color = Color(0x6605070A),
                                fontSize = 15f.sp,
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }
                    innerTextField()
                }
            }
        }
    )
}

@Composable
private fun SeyraCategoryChips(
    categories: List<String>,
    selectedCategory: String,
    onCategorySelected: (String) -> Unit,
    backdrop: LayerBackdrop
) {
    val selectedIndex = categories.indexOf(selectedCategory).coerceAtLeast(0)
    val selectedOffset = remember { Animatable(selectedIndex.toFloat()) }

    LaunchedEffect(selectedIndex) {
        selectedOffset.animateTo(
            targetValue = selectedIndex.toFloat(),
            animationSpec = tween(
                durationMillis = 260,
                easing = FastOutSlowInEasing
            )
        )
    }

    val dragThreshold = 80f
    var dragAccumulated by remember { mutableStateOf(0f) }

    BoxWithConstraints(
        Modifier
            .fillMaxWidth()
            .height(40f.dp)
            .draggable(
                orientation = androidx.compose.foundation.gestures.Orientation.Horizontal,
                state = rememberDraggableState { delta ->
                    dragAccumulated += delta
                },
                onDragStopped = {
                    if (dragAccumulated > dragThreshold) {
                        val newIdx = (selectedIndex + 1).coerceAtMost(categories.lastIndex)
                        onCategorySelected(categories[newIdx])
                    } else if (dragAccumulated < -dragThreshold) {
                        val newIdx = (selectedIndex - 1).coerceAtLeast(0)
                        onCategorySelected(categories[newIdx])
                    }
                    dragAccumulated = 0f
                }
            )
    ) {
        val density = LocalDensity.current
        val itemWidthPx = with(density) {
            maxWidth.toPx() / categories.size.coerceAtLeast(1)
        }

        Box(
            Modifier
                .graphicsLayer {
                    translationX = selectedOffset.value * itemWidthPx
                }
                .fillMaxWidth(1f / categories.size.coerceAtLeast(1))
                .height(38f.dp)
                .drawBackdrop(
                    backdrop = backdrop,
                    shape = { Capsule() },
                    effects = {
                        vibrancy()
                        blur(8f.dp.toPx())
                        lens(10f.dp.toPx(), 14f.dp.toPx(), chromaticAberration = true)
                    },
                    onDrawSurface = {
                        drawRect(Color(0x42FFFFFF))
                        drawRect(Color(0x1A2DA8FF), blendMode = BlendMode.Screen)
                    }
                )
        )

        Row(Modifier.fillMaxSize()) {
            categories.forEach { category ->
                val selected = category == selectedCategory
                BasicText(
                    category,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { onCategorySelected(category) }
                        )
                        .padding(top = 10f.dp),
                    style = TextStyle(
                        color = if (selected) Color(0xFF008DFF) else Color(0xC805070A),
                        fontSize = 14f.sp,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center
                    )
                )
            }
        }
    }
}

@Composable
private fun SeyraCardGrid(
    cards: List<SeyraCard>,
    backdrop: LayerBackdrop,
    onCardClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
    cardRowAlpha: (Int) -> Float = { 1f }
) {
    Column(
        modifier,
        verticalArrangement = Arrangement.spacedBy(14f.dp)
    ) {
        if (cards.isEmpty()) {
            SeyraEmptyResourcePanel(backdrop)
        } else {
            cards.chunked(2).forEachIndexed { rowIndex, rowCards ->
                val rowAlpha = remember { mutableStateOf(1f) }
                Row(
                    Modifier
                        .fillMaxWidth()
                        .onGloballyPositioned { coordinates ->
                            val bottomInRoot = coordinates.positionInRoot().y + coordinates.size.height
                            val newAlpha = cardRowAlpha(bottomInRoot.toInt())
                            if (abs(rowAlpha.value - newAlpha) > 0.01f) {
                                rowAlpha.value = newAlpha
                            }
                        }
                        .graphicsLayer { alpha = rowAlpha.value },
                    horizontalArrangement = Arrangement.spacedBy(14f.dp)
                ) {
                    rowCards.forEachIndexed { columnIndex, card ->
                        val cardIndex = rowIndex * 2 + columnIndex
                        SeyraLiquidCard(
                            card = card,
                            backdrop = backdrop,
                            onClick = { onCardClick(cardIndex) },
                            modifier = Modifier.weight(1f),
                            cardNumber = cardIndex
                        )
                    }
                    if (rowCards.size == 1) {
                        Spacer(Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun SeyraEmptyResourcePanel(backdrop: LayerBackdrop) {
    Box(
        Modifier
            .fillMaxWidth()
            .height(120f.dp)
            .drawBackdrop(
                backdrop = backdrop,
                shape = { RoundedRectangle(24f.dp) },
                effects = {
                    vibrancy()
                    blur(10f.dp.toPx())
                    lens(12f.dp.toPx(), 20f.dp.toPx())
                },
                onDrawSurface = {
                    drawRect(Color(0x68FFFFFF))
                    drawRect(Color(0x0F6EBBFF), blendMode = BlendMode.Screen)
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        BasicText(
            "没有找到相关资源",
            style = TextStyle(
                color = Color(0x9905070A),
                fontSize = 15f.sp,
                fontWeight = FontWeight.Medium
            )
        )
    }
}

@Composable
private fun SeyraToolNavigationStack(
    backdrop: LayerBackdrop,
    onMusicWebsiteClick: () -> Unit
) {
    var activeCardIndex by rememberSaveable { mutableIntStateOf(-1) }
    var visibleDetailIndex by rememberSaveable { mutableIntStateOf(-1) }
    val progress = remember { Animatable(0f) }
    val coroutineScope = rememberCoroutineScope()

    fun openCard(index: Int) {
        if (workspaceCards.getOrNull(index)?.title == "网易云解析") {
            onMusicWebsiteClick()
            return
        }
        if (activeCardIndex != -1) return
        activeCardIndex = index
        visibleDetailIndex = index
        coroutineScope.launch {
            progress.snapTo(0f)
            progress.animateTo(
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = 320,
                    easing = FastOutSlowInEasing
                )
            )
        }
    }

    fun closeCard() {
        if (activeCardIndex == -1) return
        coroutineScope.launch {
            progress.animateTo(
                targetValue = 0f,
                animationSpec = tween(
                    durationMillis = 280,
                    easing = FastOutSlowInEasing
                )
            )
            activeCardIndex = -1
            visibleDetailIndex = -1
        }
    }

    BackHandler(enabled = activeCardIndex != -1, onBack = { closeCard() })

    Column(
        Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(24f.dp)
    ) {
        BasicText(
            "工具",
            style = TextStyle(
                color = Color(0xFF05070A),
                fontSize = 28f.sp,
                fontWeight = FontWeight.SemiBold
            )
        )

        BoxWithConstraints(Modifier.fillMaxWidth()) {
            val density = LocalDensity.current
            val widthPx = with(density) { maxWidth.toPx() }
            val fullExitDistancePx = widthPx + with(density) { 72f.dp.toPx() }
            val detailProgress = if (visibleDetailIndex == -1) 0f else progress.value

            SeyraToolCardGrid(
                backdrop = backdrop,
                onCardClick = { openCard(it) },
                modifier = Modifier.graphicsLayer {
                    translationX = -widthPx * 0.18f * detailProgress
                    alpha = 1f - 0.08f * detailProgress
                    scaleX = 1f - 0.02f * detailProgress
                    scaleY = 1f - 0.02f * detailProgress
                }
            )

            if (visibleDetailIndex != -1) {
                val visibleCard = workspaceCards[visibleDetailIndex]
                SeyraToolDetailPage(
                    card = visibleCard,
                    backdrop = backdrop,
                    onBack = { closeCard() },
                    modifier = Modifier.graphicsLayer {
                        translationX = fullExitDistancePx * (1f - detailProgress)
                        alpha = 0.96f + 0.04f * detailProgress
                    }
                )
            }
        }
    }
}

@Composable
private fun SeyraToolCardGrid(
    backdrop: LayerBackdrop,
    onCardClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier,
        verticalArrangement = Arrangement.spacedBy(14f.dp)
    ) {
        workspaceCards.chunked(2).forEachIndexed { rowIndex, rowCards ->
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14f.dp)
            ) {
                rowCards.forEachIndexed { columnIndex, card ->
                    val cardIndex = rowIndex * 2 + columnIndex
                    SeyraLiquidCard(
                        card = card,
                        backdrop = backdrop,
                        onClick = { onCardClick(cardIndex) },
                        modifier = Modifier.weight(1f)
                    )
                }
                if (rowCards.size == 1) {
                    Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun SeyraToolDetailPage(
    card: SeyraCard,
    backdrop: LayerBackdrop,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var query by rememberSaveable { mutableStateOf("") }
    var result by rememberSaveable { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val copyText = rememberCopyTextAction()
    val isXrayTool = card.title == "社工查询"

    Column(
        modifier
            .fillMaxWidth()
            .height(532f.dp)
            .drawBackdrop(
                backdrop = backdrop,
                shape = { RoundedRectangle(30f.dp) },
                effects = {
                    vibrancy()
                    blur(14f.dp.toPx())
                    lens(18f.dp.toPx(), 30f.dp.toPx())
                },
                onDrawSurface = {
                    drawRect(Color(0x82FFFFFF))
                    drawRect(card.tint.copy(alpha = 0.18f), blendMode = BlendMode.Screen)
                }
            )
            .padding(24f.dp),
        verticalArrangement = Arrangement.spacedBy(16f.dp)
    ) {
        BasicText(
            "‹ 返回",
            modifier = Modifier.clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onBack
            ),
            style = TextStyle(
                color = Color(0xFF008DFF),
                fontSize = 16f.sp,
                fontWeight = FontWeight.SemiBold
            )
        )
        BasicText(
            card.title,
            modifier = Modifier.padding(top = 16f.dp),
            style = TextStyle(
                color = Color(0xFF05070A),
                fontSize = 26f.sp,
                fontWeight = FontWeight.SemiBold
            )
        )
        BasicText(
            card.subtitle,
            style = TextStyle(
                color = Color(0xD0111827),
                fontSize = 15f.sp,
                fontWeight = FontWeight.Medium
            )
        )
        if (isXrayTool) {
            SeyraXrayInputField(
                value = query,
                onValueChange = { query = it },
                backdrop = backdrop,
                modifier = Modifier.padding(top = 10f.dp)
            )
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10f.dp)
            ) {
                SeyraTextActionButton(
                    text = if (isLoading) "查询中" else "查询",
                    backdrop = backdrop,
                    enabled = query.isNotBlank() && !isLoading,
                    onClick = {
                        scope.launch {
                            isLoading = true
                            result = "查询中..."
                            result = runCatching {
                                val rawResult = withContext(Dispatchers.Default) {
                                    requestXrayResult(query)
                                }
                                formatXrayResult(rawResult)
                            }.getOrElse { "请求失败：${it.message ?: "未知错误"}" }
                            isLoading = false
                        }
                    },
                    modifier = Modifier.weight(1f)
                )
                SeyraTextActionButton(
                    text = "复制",
                    backdrop = backdrop,
                    enabled = result.isNotBlank() && result != "查询中...",
                    onClick = { copyText(result) },
                    modifier = Modifier.weight(1f)
                )
            }
            SeyraXrayResultPanel(
                result = result,
                backdrop = backdrop
            )
        }
    }
}

@Composable
private fun SeyraXrayInputField(
    value: String,
    onValueChange: (String) -> Unit,
    backdrop: LayerBackdrop,
    modifier: Modifier = Modifier
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        textStyle = TextStyle(
            color = Color(0xFF05070A),
            fontSize = 15f.sp,
            fontWeight = FontWeight.Medium
        ),
        modifier = modifier
            .fillMaxWidth()
            .height(52f.dp)
            .drawBackdrop(
                backdrop = backdrop,
                shape = { RoundedRectangle(20f.dp) },
                effects = {
                    vibrancy()
                    blur(10f.dp.toPx())
                    lens(12f.dp.toPx(), 20f.dp.toPx())
                },
                onDrawSurface = {
                    drawRect(Color(0x70FFFFFF))
                    drawRect(Color(0x0F6EBBFF), blendMode = BlendMode.Screen)
                }
            )
            .padding(horizontal = 16f.dp),
        decorationBox = { innerTextField ->
            Box(
                Modifier.fillMaxSize(),
                contentAlignment = Alignment.CenterStart
            ) {
                if (value.isEmpty()) {
                    BasicText(
                        "请输入查询内容",
                        style = TextStyle(
                            color = Color(0x6605070A),
                            fontSize = 15f.sp,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
                innerTextField()
            }
        }
    )
}

@Composable
private fun SeyraTextActionButton(
    text: String,
    backdrop: LayerBackdrop,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier
            .height(44f.dp)
            .drawBackdrop(
                backdrop = backdrop,
                shape = { Capsule() },
                effects = {
                    vibrancy()
                    blur(8f.dp.toPx())
                    lens(10f.dp.toPx(), 18f.dp.toPx())
                },
                onDrawSurface = {
                    drawRect(if (enabled) Color(0x7DFFFFFF) else Color(0x45FFFFFF))
                    drawRect(Color(0x146EBBFF), blendMode = BlendMode.Screen)
                }
            )
            .clickable(
                enabled = enabled,
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        BasicText(
            text,
            style = TextStyle(
                color = if (enabled) Color(0xFF008DFF) else Color(0x7005070A),
                fontSize = 14f.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )
        )
    }
}

@Composable
private fun SeyraXrayResultPanel(
    result: String,
    backdrop: LayerBackdrop
) {
    val scrollState = rememberScrollState()

    Box(
        Modifier
            .fillMaxWidth()
            .height(176f.dp)
            .drawBackdrop(
                backdrop = backdrop,
                shape = { RoundedRectangle(22f.dp) },
                effects = {
                    vibrancy()
                    blur(10f.dp.toPx())
                    lens(12f.dp.toPx(), 20f.dp.toPx())
                },
                onDrawSurface = {
                    drawRect(Color(0x62FFFFFF))
                    drawRect(Color(0x0F6EBBFF), blendMode = BlendMode.Screen)
                }
            )
            .padding(16f.dp)
    ) {
        BasicText(
            result.ifBlank { "输出结果会显示在这里" },
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState),
            style = TextStyle(
                color = if (result.isBlank()) Color(0x6605070A) else Color(0xE005070A),
                fontSize = 14f.sp,
                fontWeight = FontWeight.Medium
            )
        )
    }
}

@Composable
private fun SeyraProfilePage(
    backdrop: LayerBackdrop,
    onContactClick: () -> Unit,
    onFeedbackClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    Column(
        Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            Modifier
                .size(122f.dp)
                .clip(CircleShape)
                .drawBackdrop(
                    backdrop = backdrop,
                    shape = { Capsule() },
                    effects = {
                        vibrancy()
                        blur(8f.dp.toPx())
                        lens(14f.dp.toPx(), 24f.dp.toPx())
                    },
                    onDrawSurface = {
                        drawRect(Color(0x70FFFFFF))
                    }
                )
        )
        BasicText(
            "𝑆‌𝑒𝑦𝑟𝑎",
            modifier = Modifier.padding(top = 22f.dp),
            style = TextStyle(
                color = Color(0xFF05070A),
                fontSize = 23f.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )
        )

        Row(
            Modifier
                .fillMaxWidth()
                .padding(top = 42f.dp),
            horizontalArrangement = Arrangement.spacedBy(10f.dp)
        ) {
            SeyraProfileActionButton(
                icon = Res.drawable.ic_profile_contact_32px,
                label = "合作联系",
                backdrop = backdrop,
                onClick = onContactClick,
                modifier = Modifier.weight(1f),
                delayMs = 100
            )
            SeyraProfileActionButton(
                icon = Res.drawable.ic_profile_feedback_32px,
                label = "软件反馈",
                backdrop = backdrop,
                onClick = onFeedbackClick,
                modifier = Modifier.weight(1f),
                delayMs = 200
            )
            SeyraProfileActionButton(
                icon = Res.drawable.ic_profile_settings_32px,
                label = "设置",
                backdrop = backdrop,
                onClick = onSettingsClick,
                modifier = Modifier.weight(1f),
                delayMs = 300
            )
        }

        SeyraProfileInfoPanel(
            backdrop = backdrop,
            modifier = Modifier.padding(top = 22f.dp)
        )
    }
}

@Composable
private fun SeyraContactDialog(
    backdrop: LayerBackdrop,
    onCancel: () -> Unit,
    onCopy: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isLightTheme = !isSystemInDarkTheme()
    val contentColor = if (isLightTheme) Color.Black else Color.White
    val accentColor = if (isLightTheme) Color(0xFF0A8CFF) else Color(0xFF1495FF)
    val containerColor = if (isLightTheme) Color(0xFFFAFAFA).copy(alpha = 0.46f) else Color(0xFF121212).copy(alpha = 0.34f)
    val dimColor = if (isLightTheme) Color(0xFF202033).copy(alpha = 0.16f) else Color(0xFF000000).copy(alpha = 0.44f)
    val overlayAlpha = remember { Animatable(0f) }
    val dialogAlpha = remember { Animatable(0f) }
    val dialogScale = remember { Animatable(0.92f) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        launch {
            overlayAlpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing)
            )
        }
        launch {
            dialogAlpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 260, easing = FastOutSlowInEasing)
            )
        }
        launch {
            dialogScale.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 320, easing = FastOutSlowInEasing)
            )
        }
    }

    fun closeWithAnimation(afterClose: () -> Unit) {
        scope.launch {
            val overlayJob = launch {
                overlayAlpha.animateTo(
                    targetValue = 0f,
                    animationSpec = tween(durationMillis = 170, easing = FastOutSlowInEasing)
                )
            }
            val alphaJob = launch {
                dialogAlpha.animateTo(
                    targetValue = 0f,
                    animationSpec = tween(durationMillis = 150, easing = FastOutSlowInEasing)
                )
            }
            val scaleJob = launch {
                dialogScale.animateTo(
                    targetValue = 0.94f,
                    animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing)
                )
            }
            overlayJob.join()
            alphaJob.join()
            scaleJob.join()
            afterClose()
        }
    }

    Box(
        Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .graphicsLayer { alpha = overlayAlpha.value }
                .drawBackdrop(
                    backdrop = backdrop,
                    shape = { RoundedRectangle(0f.dp) },
                    effects = {
                        colorControls(
                            brightness = if (isLightTheme) 0.08f else -0.04f,
                            saturation = 1.28f
                        )
                        blur(if (isLightTheme) 22f.dp.toPx() else 16f.dp.toPx())
                    },
                    highlight = { Highlight.Plain },
                    onDrawSurface = {
                        drawRect(Color(0x22FFFFFF))
                        drawRect(dimColor)
                    }
                )
        )

        Column(
            modifier
                .padding(horizontal = 52f.dp)
                .fillMaxWidth()
                .graphicsLayer {
                    alpha = dialogAlpha.value
                    scaleX = dialogScale.value
                    scaleY = dialogScale.value
                }
                .drawBackdrop(
                    backdrop = backdrop,
                    shape = { RoundedRectangle(46f.dp) },
                    effects = {
                        colorControls(
                            brightness = if (isLightTheme) 0.1f else 0f,
                            saturation = 1.62f
                        )
                        blur(if (isLightTheme) 24f.dp.toPx() else 14f.dp.toPx())
                        lens(28f.dp.toPx(), 52f.dp.toPx(), depthEffect = true)
                    },
                    highlight = { Highlight.Plain },
                    onDrawSurface = { drawRect(containerColor) }
                )
        ) {
            BasicText(
                "温馨提示",
                modifier = Modifier.padding(start = 28f.dp, top = 26f.dp, end = 28f.dp),
                style = TextStyle(
                    color = contentColor,
                    fontSize = 24f.sp,
                    fontWeight = FontWeight.SemiBold
                )
            )

            BasicText(
                "联系开发者：TG搜索 @sspyj，点击一键复制。",
                modifier = Modifier
                    .then(
                        if (isLightTheme) Modifier else Modifier.graphicsLayer(blendMode = BlendMode.Plus)
                    )
                    .padding(start = 28f.dp, top = 24f.dp, end = 28f.dp),
                style = TextStyle(
                    color = contentColor.copy(alpha = 0.72f),
                    fontSize = 16f.sp,
                    fontWeight = FontWeight.Medium
                )
            )

            Row(
                Modifier
                    .padding(start = 24f.dp, top = 26f.dp, end = 24f.dp, bottom = 24f.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16f.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    Modifier
                        .clip(Capsule())
                        .background(containerColor.copy(alpha = 0.24f))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { closeWithAnimation(onCancel) }
                        )
                        .height(52f.dp)
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    BasicText(
                        "取消",
                        style = TextStyle(
                            color = contentColor,
                            fontSize = 16f.sp,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }

                Box(
                    Modifier
                        .clip(Capsule())
                        .background(accentColor)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { closeWithAnimation(onCopy) }
                        )
                        .height(52f.dp)
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    BasicText(
                        "复制",
                        style = TextStyle(
                            color = Color.White,
                            fontSize = 16f.sp,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun SeyraProfileActionButton(
    icon: DrawableResource,
    label: String,
    backdrop: LayerBackdrop,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    delayMs: Int = 0
) {
    var visible by remember { mutableStateOf(false) }
    var pressed by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(delayMs.toLong())
        visible = true
    }

    val density = LocalDensity.current
    val offsetYTarget = with(density) { 30f.dp.toPx() }

    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.92f else 1f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 400f)
    )
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(400, easing = FastOutSlowInEasing)
    )
    val offsetY by animateFloatAsState(
        targetValue = if (visible) 0f else offsetYTarget,
        animationSpec = tween(400, easing = FastOutSlowInEasing)
    )

    Column(
        modifier
            .graphicsLayer {
                this.alpha = alpha
                this.translationY = offsetY
                this.scaleX = scale
                this.scaleY = scale
            }
            .height(68f.dp)
            .drawBackdrop(
                backdrop = backdrop,
                shape = { RoundedRectangle(24f.dp) },
                effects = {
                    vibrancy()
                    blur(10f.dp.toPx())
                    lens(12f.dp.toPx(), 20f.dp.toPx())
                },
                onDrawSurface = {
                    drawRect(Color(0x82FFFFFF))
                    drawRect(Color(0x0F6EBBFF), blendMode = BlendMode.Screen)
                }
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        pressed = true
                        tryAwaitRelease()
                        pressed = false
                    },
                    onTap = { onClick() }
                )
            },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(icon),
            contentDescription = null,
            modifier = Modifier.size(28f.dp),
            colorFilter = ColorFilter.tint(Color(0xFF05070A))
        )
        BasicText(
            label,
            modifier = Modifier.padding(top = 3f.dp),
            style = TextStyle(
                color = Color(0xFF05070A),
                fontSize = 12.5f.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )
        )
    }
}

@Composable
private fun SeyraSettingsPage(
    onBack: () -> Unit,
    backdrop: LayerBackdrop,
    modifier: Modifier = Modifier
) {
    var toggleChecked1 by remember { mutableStateOf(false) }
    var toggleChecked2 by remember { mutableStateOf(false) }
    var toggleChecked3 by remember { mutableStateOf(false) }
    var backVisible by remember { mutableStateOf(false) }
    var panelVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        backVisible = true
        delay(80)
        panelVisible = true
    }

    Column(
        modifier
            .fillMaxSize()
            .statusBarsPadding()
            .displayCutoutPadding()
            .padding(start = 14f.dp, top = 82f.dp, end = 14f.dp, bottom = 14f.dp),
        verticalArrangement = Arrangement.spacedBy(14f.dp)
    ) {
        AnimatedVisibility(
            visible = backVisible,
            enter = fadeIn(tween(380, easing = FastOutSlowInEasing)) +
                    slideInVertically(tween(380, easing = FastOutSlowInEasing)) { -it / 5 },
            exit = fadeOut(tween(220, easing = FastOutSlowInEasing)) +
                   slideOutVertically(tween(220, easing = FastOutSlowInEasing)) { -it / 5 }
        ) {
            BasicText(
                "‹ 返回",
                modifier = Modifier
                    .padding(start = 4f.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onBack
                    ),
                style = TextStyle(
                    color = Color(0xDD05070A),
                    fontSize = 16f.sp,
                    fontWeight = FontWeight.SemiBold
                )
            )
        }
        AnimatedVisibility(
            visible = panelVisible,
            enter = fadeIn(tween(420, delayMillis = 100, easing = FastOutSlowInEasing)) +
                    slideInVertically(tween(420, delayMillis = 100, easing = FastOutSlowInEasing)) { it / 4 },
            exit = fadeOut(tween(260, easing = FastOutSlowInEasing)) +
                   slideOutVertically(tween(260, easing = FastOutSlowInEasing)) { it / 4 }
        ) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .drawBackdrop(
                        backdrop = backdrop,
                        shape = { RoundedRectangle(28f.dp) },
                        effects = {
                            vibrancy()
                            blur(12f.dp.toPx())
                            lens(14f.dp.toPx(), 24f.dp.toPx())
                        },
                        onDrawSurface = {
                            drawRect(Color(0x70FFFFFF))
                            drawRect(Color(0x0F6EBBFF), blendMode = BlendMode.Screen)
                        }
                    )
                    .padding(horizontal = 30f.dp, vertical = 22f.dp),
                verticalArrangement = Arrangement.spacedBy(18f.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    BasicText(
                        "测试行一",
                        style = TextStyle(
                            color = Color(0xFF05070A),
                            fontSize = 16f.sp,
                            fontWeight = FontWeight.Medium
                        )
                    )
                    LiquidToggle(
                        selected = { toggleChecked1 },
                        onSelect = { toggleChecked1 = it },
                        backdrop = backdrop
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    BasicText(
                        "测试行二",
                        style = TextStyle(
                            color = Color(0xFF05070A),
                            fontSize = 16f.sp,
                            fontWeight = FontWeight.Medium
                        )
                    )
                    LiquidToggle(
                        selected = { toggleChecked2 },
                        onSelect = { toggleChecked2 = it },
                        backdrop = backdrop
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    BasicText(
                        "测试行三",
                        style = TextStyle(
                            color = Color(0xFF05070A),
                            fontSize = 16f.sp,
                            fontWeight = FontWeight.Medium
                        )
                    )
                    LiquidToggle(
                        selected = { toggleChecked3 },
                        onSelect = { toggleChecked3 = it },
                        backdrop = backdrop
                    )
                }
            }
        }
    }
}

@Composable
private fun SeyraProfileInfoPanel(
    backdrop: LayerBackdrop,
    modifier: Modifier = Modifier
) {
    Column(
        modifier
            .fillMaxWidth()
            .height(238f.dp)
            .drawBackdrop(
                backdrop = backdrop,
                shape = { RoundedRectangle(28f.dp) },
                effects = {
                    vibrancy()
                    blur(12f.dp.toPx())
                    lens(14f.dp.toPx(), 24f.dp.toPx())
                },
                onDrawSurface = {
                    drawRect(Color(0x70FFFFFF))
                    drawRect(Color(0x0F6EBBFF), blendMode = BlendMode.Screen)
                }
            )
            .padding(horizontal = 30f.dp),
        verticalArrangement = Arrangement.spacedBy(20f.dp, Alignment.CenterVertically)
    ) {
        BasicText(
            "TG@sspyj",
            style = TextStyle(
                color = Color(0xFF05070A),
                fontSize = 18f.sp,
                fontWeight = FontWeight.SemiBold
            )
        )
        BasicText(
            "版本更新",
            style = TextStyle(
                color = Color(0xD005070A),
                fontSize = 16f.sp,
                fontWeight = FontWeight.Medium
            )
        )
        BasicText(
            "下载管理",
            style = TextStyle(
                color = Color(0xE005070A),
                fontSize = 16f.sp,
                fontWeight = FontWeight.Medium
            )
        )
        BasicText(
            "分享软件",
            style = TextStyle(
                color = Color(0xE005070A),
                fontSize = 16f.sp,
                fontWeight = FontWeight.Medium
            )
        )
        BasicText(
            "我的收藏",
            style = TextStyle(
                color = Color(0xE005070A),
                fontSize = 16f.sp,
                fontWeight = FontWeight.Medium
            )
        )
    }
}

@Composable
private fun SeyraDiscoverFrostedPanel(
    backdrop: LayerBackdrop,
    modifier: Modifier = Modifier
) {
    Box(
        modifier
            .fillMaxWidth()
            .height(318f.dp)
            .drawBackdrop(
                backdrop = backdrop,
                shape = { RoundedRectangle(28f.dp) },
                effects = {
                    vibrancy()
                    lens(16f.dp.toPx(), 32f.dp.toPx())
                }
            )
    )
}

@Composable
private fun SeyraTopActions(
    backdrop: LayerBackdrop,
    onShareClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier
            .height(48f.dp)
            .width(104f.dp)
            .drawBackdrop(
                backdrop = backdrop,
                shape = { Capsule() },
                effects = {
                    vibrancy()
                    blur(8f.dp.toPx())
                    lens(18f.dp.toPx(), 26f.dp.toPx())
                },
                onDrawSurface = {
                    drawRect(Color(0x9AFFFFFF))
                    drawRect(Color(0x1F6EBBFF), blendMode = BlendMode.Screen)
                }
            )
            .padding(horizontal = 4f.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SeyraTopActionIcon(
            icon = Res.drawable.ic_top_share_24px,
            onClick = onShareClick,
            modifier = Modifier.weight(1f)
        )
        SeyraTopActionIcon(
            icon = Res.drawable.ic_top_more_24px,
            onClick = {},
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun SeyraTopActionIcon(
    icon: DrawableResource,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier
            .fillMaxHeight()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(icon),
            contentDescription = null,
            modifier = Modifier.size(24f.dp),
            colorFilter = ColorFilter.tint(Color(0xFF05070A))
        )
    }
}

@Composable
private fun SeyraLiquidHeaderPanel(backdrop: LayerBackdrop) {
    Box(
        Modifier
            .fillMaxWidth()
            .height(176f.dp)
            .drawBackdrop(
                backdrop = backdrop,
                shape = { RoundedRectangle(32f.dp) },
                effects = {
                    vibrancy()
                    lens(16f.dp.toPx(), 32f.dp.toPx())
                }
            )
    )
}

@Composable
private fun SeyraLiquidCard(
    card: SeyraCard,
    backdrop: LayerBackdrop,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    cardNumber: Int = -1
) {
    Box(
        modifier
            .aspectRatio(1.58f)
            .drawBackdrop(
                backdrop = backdrop,
                shape = { RoundedRectangle(24f.dp) },
                effects = {
                    vibrancy()
                    blur(12f.dp.toPx())
                    lens(18f.dp.toPx(), 28f.dp.toPx())
                },
                onDrawSurface = {
                    drawRect(Color(0x78FFFFFF))
                    drawRect(card.tint.copy(alpha = 0.20f), blendMode = BlendMode.Screen)
                }
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
    ) {
        if (cardNumber >= 0) {
            BasicText(
                "${cardNumber + 1}",
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(start = 12f.dp, top = 10f.dp),
                style = TextStyle(
                    color = Color(0x6005070A),
                    fontSize = 28f.sp,
                    fontWeight = FontWeight.Bold
                )
            )
        }
    }
}

@Composable
private fun SeyraDock(
    selectedTabIndex: Int,
    onTabSelected: (Int) -> Unit,
    onResourcePressed: () -> Unit,
    backdrop: LayerBackdrop,
    modifier: Modifier = Modifier
) {
    val tabs = listOf(
        SeyraDockTab(Res.drawable.ic_dock_compass_40px, "发现"),
        SeyraDockTab(Res.drawable.ic_dock_folder_40px, "资源"),
        SeyraDockTab(Res.drawable.ic_dock_wrench_40px, "工具"),
        SeyraDockTab(Res.drawable.ic_dock_user_40px, "我的")
    )

    LiquidBottomTabs(
        selectedTabIndex = { selectedTabIndex },
        onTabSelected = onTabSelected,
        backdrop = backdrop,
        tabsCount = tabs.size,
        modifier = modifier.fillMaxWidth()
    ) {
        tabs.forEachIndexed { index, tab ->
            val selected = selectedTabIndex == index
            val color = if (selected) Color(0xFF008DFF) else Color(0xFF05070A)
            val pressModifier = if (index == 1) {
                Modifier.pointerInput(Unit) {
                    awaitEachGesture {
                        awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
                        onResourcePressed()
                        do {
                            val event = awaitPointerEvent()
                        } while (event.changes.any { it.pressed })
                    }
                }
            } else {
                Modifier
            }

            LiquidBottomTab(
                onClick = { onTabSelected(index) },
                modifier = pressModifier
            ) {
                Image(
                    painter = painterResource(tab.icon),
                    contentDescription = null,
                    modifier = Modifier.size(25f.dp),
                    colorFilter = ColorFilter.tint(color)
                )
                BasicText(
                    tab.label,
                    style = TextStyle(
                        color = color,
                        fontSize = 12f.sp,
                        fontWeight = FontWeight.Medium
                    )
                )
            }
        }
    }
}

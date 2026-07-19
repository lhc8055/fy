package com.kyant.backdrop.catalog

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.BlurEffect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kyant.backdrop.backdrops.LayerBackdrop
import com.kyant.backdrop.catalog.components.LiquidBottomTab
import com.kyant.backdrop.catalog.components.LiquidBottomTabs
import com.kyant.backdrop.catalog.utils.BackHandler
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy
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
import glass.app.generated.resources.profile_avatar
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import kotlinx.coroutines.launch

private data class SeyraCard(
    val title: String,
    val subtitle: String,
    val tint: Color
)

private data class SeyraDockTab(
    val icon: DrawableResource,
    val label: String
)

private data class SeyraCardBounds(
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float
) {
    val width: Float get() = right - left
    val height: Float get() = bottom - top
}

private data class SeyraHeroCard(
    val card: SeyraCard,
    val bounds: SeyraCardBounds
)

private data class SeyraHeroFrame(
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float
) {
    val width: Float get() = right - left
    val height: Float get() = bottom - top
}

private val workspaceCards = listOf(
    SeyraCard("社工查询", "综合信息查询", Color(0xFF70D7FF)),
    SeyraCard("三要素补齐", "姓名 手机号 身份证", Color(0xFF9B7CFF)),
    SeyraCard("手机号补齐", "姓名 手机号", Color(0xFFFFC56E)),
    SeyraCard("灵感盒子", "收藏碎片 / 快速保存", Color(0xFFFF8EC7)),
    SeyraCard("快捷工具", "常用操作 / 一键启动", Color(0xFF6EF0BC)),
    SeyraCard("数据概览", "进度统计 / 状态查看", Color(0xFF7EA8FF)),
    SeyraCard("工作流", "步骤管理 / 自动流程", Color(0xFFFF9D78)),
    SeyraCard("个人空间", "偏好设置 / 账号信息", Color(0xFF8EE7FF))
)

@Composable
fun SeyraWorkspaceContent() {
    BackdropDemoScaffold {
        SeyraWorkspace(it)
    }
}

@Composable
private fun BoxScope.SeyraWorkspace(backdrop: LayerBackdrop) {
    var selectedTabIndex by rememberSaveable { mutableIntStateOf(2) }
    var rootSize by remember { mutableStateOf(IntSize.Zero) }
    var heroCard by remember { mutableStateOf<SeyraHeroCard?>(null) }
    val heroPressProgress = remember { Animatable(0f) }
    val heroMorphProgress = remember { Animatable(0f) }
    val coroutineScope = rememberCoroutineScope()
    val shareApp = rememberShareAppAction()
    val openFeedback = rememberOpenFeedbackAction()
    val backgroundProgress = if (selectedTabIndex == 2) heroMorphProgress.value else 0f

    fun closeHeroCard() {
        coroutineScope.launch {
            heroMorphProgress.animateTo(
                targetValue = 0f,
                animationSpec = spring(
                    dampingRatio = 0.82f,
                    stiffness = 210f,
                    visibilityThreshold = 0.001f
                )
            )
            heroPressProgress.snapTo(0f)
            heroCard = null
        }
    }

    fun openHeroCard(card: SeyraCard, bounds: SeyraCardBounds) {
        if (heroCard != null) return
        heroCard = SeyraHeroCard(card, bounds)
        coroutineScope.launch {
            heroMorphProgress.snapTo(0f)
            heroPressProgress.snapTo(0f)
            heroPressProgress.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = 0.78f,
                    stiffness = 680f,
                    visibilityThreshold = 0.001f
                )
            )
            heroPressProgress.animateTo(
                targetValue = 0f,
                animationSpec = spring(
                    dampingRatio = 0.82f,
                    stiffness = 560f,
                    visibilityThreshold = 0.001f
                )
            )
            heroMorphProgress.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = 0.80f,
                    stiffness = 185f,
                    visibilityThreshold = 0.001f
                )
            )
        }
    }

    BackHandler(enabled = heroCard != null) {
        closeHeroCard()
    }

    LaunchedEffect(selectedTabIndex) {
        if (selectedTabIndex != 2 && heroCard != null) {
            heroPressProgress.snapTo(0f)
            heroMorphProgress.snapTo(0f)
            heroCard = null
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .displayCutoutPadding()
            .onSizeChanged { rootSize = it }
            .graphicsLayer {
                val blurRadius = 24f.dp.toPx() * iOSSmooth(backgroundProgress)
                renderEffect = if (blurRadius > 0.5f) BlurEffect(blurRadius, blurRadius) else null
            }
            .drawWithContent {
                drawContent()
                val dimAlpha = 0.14f * iOSSmooth(backgroundProgress)
                if (dimAlpha > 0f) {
                    drawRect(Color.Black.copy(alpha = dimAlpha))
                }
            },
        contentPadding = PaddingValues(
            start = 22f.dp,
            top = if (selectedTabIndex == 3) 82f.dp else 100f.dp,
            end = 22f.dp,
            bottom = 124f.dp
        ),
        verticalArrangement = Arrangement.spacedBy(14f.dp)
    ) {
        if (selectedTabIndex == 0) {
            item {
                SeyraLiquidHeaderPanel(backdrop)
            }
        } else if (selectedTabIndex == 2) {
            items(workspaceCards.chunked(2)) { rowCards ->
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14f.dp)
                ) {
                    rowCards.forEach { card ->
                        SeyraLiquidCard(
                            card = card,
                            backdrop = backdrop,
                            onClick = { bounds -> openHeroCard(card, bounds) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (rowCards.size == 1) {
                        Spacer(Modifier.weight(1f))
                    }
                }
            }
        } else if (selectedTabIndex == 3) {
            item {
                SeyraProfilePage(
                    backdrop = backdrop,
                    onFeedbackClick = openFeedback
                )
            }
        }
    }

    val backgroundLayerModifier = Modifier.graphicsLayer {
        val blurRadius = 16f.dp.toPx() * iOSSmooth(backgroundProgress)
        renderEffect = if (blurRadius > 0.5f) BlurEffect(blurRadius, blurRadius) else null
        alpha = 1f - 0.12f * iOSSmooth(backgroundProgress)
    }

    SeyraDock(
        selectedTabIndex = selectedTabIndex,
        onTabSelected = { selectedTabIndex = it },
        backdrop = backdrop,
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .navigationBarsPadding()
            .padding(start = 38f.dp, end = 38f.dp, bottom = 12f.dp)
            .then(backgroundLayerModifier)
    )

    if (selectedTabIndex != 3) {
        SeyraTopActions(
            backdrop = backdrop,
            onShareClick = shareApp,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .statusBarsPadding()
                .displayCutoutPadding()
                .padding(top = 18f.dp, end = 22f.dp)
                .then(backgroundLayerModifier)
        )
    }

    heroCard?.takeIf {
        selectedTabIndex == 2 &&
            rootSize.width > 0 &&
            rootSize.height > 0 &&
            it.bounds.width > 0f &&
            it.bounds.height > 0f
    }?.let { hero ->
        SeyraHeroMorphOverlay(
            hero = hero,
            rootSize = rootSize,
            backdrop = backdrop,
            pressProgress = heroPressProgress.value,
            morphProgress = heroMorphProgress.value,
            onClose = ::closeHeroCard
        )
    }
}

@Composable
private fun SeyraProfilePage(
    backdrop: LayerBackdrop,
    onFeedbackClick: () -> Unit
) {
    Column(
        Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(Res.drawable.profile_avatar),
            contentDescription = null,
            modifier = Modifier
                .size(122f.dp)
                .clip(CircleShape)
        )
        BasicText(
            "Seyra",
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
                .padding(top = 62f.dp),
            horizontalArrangement = Arrangement.spacedBy(10f.dp)
        ) {
            SeyraProfileActionButton(
                icon = Res.drawable.ic_profile_contact_32px,
                label = "合作联系",
                backdrop = backdrop,
                onClick = {},
                modifier = Modifier.weight(1f)
            )
            SeyraProfileActionButton(
                icon = Res.drawable.ic_profile_feedback_32px,
                label = "软件反馈",
                backdrop = backdrop,
                onClick = onFeedbackClick,
                modifier = Modifier.weight(1f)
            )
            SeyraProfileActionButton(
                icon = Res.drawable.ic_profile_settings_32px,
                label = "设置",
                backdrop = backdrop,
                onClick = {},
                modifier = Modifier.weight(1f)
            )
        }

        SeyraProfileInfoPanel(
            backdrop = backdrop,
            modifier = Modifier.padding(top = 22f.dp)
        )
    }
}

@Composable
private fun SeyraProfileActionButton(
    icon: DrawableResource,
    label: String,
    backdrop: LayerBackdrop,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier
            .height(68f.dp)
            .drawBackdrop(
                backdrop = backdrop,
                shape = { RoundedRectangle(16f.dp) },
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
            ),
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
                shape = { RoundedRectangle(16f.dp) },
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
            .padding(horizontal = 22f.dp, vertical = 17f.dp),
        verticalArrangement = Arrangement.spacedBy(20f.dp)
    ) {
        SeyraProfileInfoItem("+1 (567) 229-5962", "手机")
        SeyraProfileInfoItem("1", "个人简介")
        SeyraProfileInfoItem("@wtb888tg", "用户名")
    }
}

@Composable
private fun SeyraProfileInfoItem(
    value: String,
    label: String
) {
    Column(verticalArrangement = Arrangement.spacedBy(6f.dp)) {
        BasicText(
            value,
            style = TextStyle(
                color = Color(0xFF05070A),
                fontSize = 20f.sp,
                fontWeight = FontWeight.Normal
            )
        )
        BasicText(
            label,
            style = TextStyle(
                color = Color(0x8A05070A),
                fontSize = 14f.sp,
                fontWeight = FontWeight.Medium
            )
        )
    }
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
            .height(204f.dp)
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
    onClick: (SeyraCardBounds) -> Unit,
    modifier: Modifier = Modifier
) {
    var bounds by remember { mutableStateOf<SeyraCardBounds?>(null) }
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val localPress by animateFloatAsState(
        targetValue = if (pressed) 1f else 0f,
        animationSpec = spring(dampingRatio = 0.76f, stiffness = 620f),
        label = "seyra_local_card_press"
    )

    Box(
        modifier
            .aspectRatio(1.58f)
            .onGloballyPositioned { coordinates ->
                val position = coordinates.positionInRoot()
                val size = coordinates.size
                bounds = SeyraCardBounds(
                    left = position.x,
                    top = position.y,
                    right = position.x + size.width.toFloat(),
                    bottom = position.y + size.height.toFloat()
                )
            }
            .graphicsLayer {
                val inset = 0.022f * localPress
                scaleX = 1f - inset
                scaleY = 1f - inset
            }
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
                interactionSource = interactionSource,
                indication = null,
                onClick = {
                    bounds?.let(onClick)
                }
            )
            .padding(18f.dp)
    ) {
        Column(
            Modifier.align(Alignment.CenterStart),
            verticalArrangement = Arrangement.spacedBy(7f.dp)
        ) {
            BasicText(
                card.title,
                style = TextStyle(
                    color = Color(0xFF05070A),
                    fontSize = 18f.sp,
                    fontWeight = FontWeight.SemiBold
                )
            )
            BasicText(
                card.subtitle,
                style = TextStyle(
                    color = Color(0xEA111827),
                    fontSize = 12.5f.sp,
                    fontWeight = FontWeight.Medium
                )
            )
        }
    }
}

@Composable
private fun BoxScope.SeyraHeroMorphOverlay(
    hero: SeyraHeroCard,
    rootSize: IntSize,
    backdrop: LayerBackdrop,
    pressProgress: Float,
    morphProgress: Float,
    onClose: () -> Unit
) {
    val density = LocalDensity.current
    val easedMorph = iOSEaseOut(morphProgress)
    val liquid = iOSSmooth(morphProgress)
    val frame = calculateHeroFrame(
        start = hero.bounds,
        rootSize = rootSize,
        pressProgress = pressProgress,
        morphProgress = easedMorph
    )
    val radius = lerp(30f, 2f, liquid)
    val movingHighlight = lerp(-0.75f, 1.2f, iOSSmooth(morphProgress))

    Box(
        Modifier
            .fillMaxSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClose
            )
    )

    Box(
        Modifier
            .offset {
                IntOffset(frame.left.toInt(), frame.top.toInt())
            }
            .width(with(density) { frame.width.toDp() })
            .height(with(density) { frame.height.toDp() })
            .graphicsLayer {
                shadowElevation = lerp(10f, 40f, iOSSmooth(morphProgress))
                clip = false
            }
            .drawBackdrop(
                backdrop = backdrop,
                shape = { RoundedRectangle(radius.dp) },
                effects = {
                    vibrancy()
                    blur(lerp(12f, 28f, liquid).dp.toPx())
                    lens(lerp(18f, 30f, liquid).dp.toPx(), lerp(28f, 46f, liquid).dp.toPx())
                },
                onDrawSurface = {
                    drawRect(Color(0x78FFFFFF))
                    drawRect(hero.card.tint.copy(alpha = 0.20f), blendMode = BlendMode.Screen)
                    drawRect(Color.White.copy(alpha = 0.10f * liquid), blendMode = BlendMode.Screen)
                    drawRect(
                        brush = Brush.linearGradient(
                            0f to Color.Transparent,
                            0.42f to Color.White.copy(alpha = 0.24f * iOSSmooth(morphProgress)),
                            0.56f to Color.White.copy(alpha = 0.10f * iOSSmooth(morphProgress)),
                            1f to Color.Transparent,
                            start = Offset(size.width * (movingHighlight - 0.35f), 0f),
                            end = Offset(size.width * (movingHighlight + 0.35f), size.height)
                        ),
                        blendMode = BlendMode.Screen
                    )
                }
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {}
            )
            .padding(
                horizontal = with(density) { lerp(18f, 32f, liquid).toDp() },
                vertical = with(density) { lerp(18f, 36f, liquid).toDp() }
            )
    ) {
        Column(
            Modifier.align(Alignment.CenterStart),
            verticalArrangement = Arrangement.spacedBy(7f.dp)
        ) {
            BasicText(
                hero.card.title,
                style = TextStyle(
                    color = Color(0xFF05070A),
                    fontSize = 18f.sp,
                    fontWeight = FontWeight.SemiBold
                )
            )
            BasicText(
                hero.card.subtitle,
                style = TextStyle(
                    color = Color(0xEA111827),
                    fontSize = 12.5f.sp,
                    fontWeight = FontWeight.Medium
                )
            )
        }
    }
}

private fun calculateHeroFrame(
    start: SeyraCardBounds,
    rootSize: IntSize,
    pressProgress: Float,
    morphProgress: Float
): SeyraHeroFrame {
    val pressInsetX = start.width * 0.015f * pressProgress * (1f - morphProgress)
    val pressInsetY = start.height * 0.015f * pressProgress * (1f - morphProgress)

    val pressedLeft = start.left + pressInsetX
    val pressedTop = start.top + pressInsetY
    val pressedRight = start.right - pressInsetX
    val pressedBottom = start.bottom - pressInsetY

    return SeyraHeroFrame(
        left = lerp(pressedLeft, 0f, morphProgress),
        top = lerp(pressedTop, 0f, morphProgress),
        right = lerp(pressedRight, rootSize.width.toFloat(), morphProgress),
        bottom = lerp(pressedBottom, rootSize.height.toFloat(), morphProgress)
    )
}

private fun iOSEaseOut(value: Float): Float {
    val t = value.coerceIn(0f, 1f)
    val inverse = 1f - t
    return 1f - inverse * inverse * inverse
}

private fun iOSSmooth(value: Float): Float {
    val t = value.coerceIn(0f, 1f)
    return t * t * (3f - 2f * t)
}

private fun lerp(start: Float, stop: Float, fraction: Float): Float {
    return start + (stop - start) * fraction.coerceIn(0f, 1f)
}

@Composable
private fun SeyraDock(
    selectedTabIndex: Int,
    onTabSelected: (Int) -> Unit,
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

            LiquidBottomTab({ onTabSelected(index) }) {
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

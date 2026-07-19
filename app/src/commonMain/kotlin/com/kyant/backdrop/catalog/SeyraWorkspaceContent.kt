package com.kyant.backdrop.catalog

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.BlurEffect
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
    val x: Float,
    val y: Float,
    val width: Float,
    val height: Float
)

private data class SeyraOpenedCard(
    val card: SeyraCard,
    val bounds: SeyraCardBounds,
    val index: Int
)

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
    var openedCard by remember { mutableStateOf<SeyraOpenedCard?>(null) }
    val launchProgress = remember { Animatable(0f) }
    val coroutineScope = rememberCoroutineScope()
    val shareApp = rememberShareAppAction()
    val openFeedback = rememberOpenFeedbackAction()

    fun closeOpenedCard() {
        coroutineScope.launch {
            launchProgress.animateTo(
                0f,
                spring(dampingRatio = 0.9f, stiffness = 360f, visibilityThreshold = 0.001f)
            )
            openedCard = null
        }
    }

    BackHandler(enabled = openedCard != null) {
        closeOpenedCard()
    }

    LaunchedEffect(openedCard) {
        if (openedCard != null) {
            launchProgress.snapTo(0f)
            launchProgress.animateTo(
                1f,
                spring(dampingRatio = 0.72f, stiffness = 105f, visibilityThreshold = 0.001f)
            )
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .displayCutoutPadding()
            .onSizeChanged { rootSize = it }
            .graphicsLayer {
                val progress = if (selectedTabIndex == 2) launchProgress.value else 0f
                val blurRadius = 14f.dp.toPx() * progress
                renderEffect = if (blurRadius > 0.5f) BlurEffect(blurRadius, blurRadius) else null
                alpha = 1f - 0.10f * progress
                scaleX = 1f - 0.012f * progress
                scaleY = 1f - 0.012f * progress
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
                        val cardIndex = workspaceCards.indexOf(card)
                        SeyraLiquidCard(
                            card = card,
                            backdrop = backdrop,
                            onClick = { bounds ->
                                if (openedCard == null) {
                                    openedCard = SeyraOpenedCard(card, bounds, cardIndex)
                                }
                            },
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

    openedCard?.takeIf {
        selectedTabIndex == 2 &&
            rootSize.width > 0 &&
            rootSize.height > 0 &&
            it.bounds.width > 0f &&
            it.bounds.height > 0f
    }?.let { opened ->
        SeyraIOSLaunchOverlay(
            opened = opened,
            rootSize = rootSize,
            backdrop = backdrop,
            progress = launchProgress.value,
            onClose = ::closeOpenedCard
        )
    }

    SeyraDock(
        selectedTabIndex = selectedTabIndex,
        onTabSelected = { selectedTabIndex = it },
        backdrop = backdrop,
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .navigationBarsPadding()
            .padding(start = 38f.dp, end = 38f.dp, bottom = 12f.dp)
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
    var cardBounds by remember { mutableStateOf<SeyraCardBounds?>(null) }

    Box(
        modifier
            .aspectRatio(1.58f)
            .onGloballyPositioned { coordinates ->
                val position = coordinates.positionInRoot()
                val size = coordinates.size
                cardBounds = SeyraCardBounds(
                    x = position.x,
                    y = position.y,
                    width = size.width.toFloat(),
                    height = size.height.toFloat()
                )
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
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {
                    cardBounds?.let(onClick)
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
private fun BoxScope.SeyraIOSLaunchOverlay(
    opened: SeyraOpenedCard,
    rootSize: IntSize,
    backdrop: LayerBackdrop,
    progress: Float,
    onClose: () -> Unit
) {
    val density = LocalDensity.current
    val frame = rememberLaunchFrame(opened.bounds, rootSize, progress)
    val cornerRadius = lerp(24f, 0f, iosSmooth(secondStageProgress(progress)))

    Box(
        Modifier
            .fillMaxSize()
            .graphicsLayer {
                alpha = progress
            }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClose
            )
    )

    Box(
        Modifier
            .offset {
                IntOffset(frame.x.toInt(), frame.y.toInt())
            }
            .width(with(density) { frame.width.toDp() })
            .height(with(density) { frame.height.toDp() })
            .graphicsLayer {
                alpha = progress
                shadowElevation = 28f * progress
                clip = false
            }
            .drawBackdrop(
                backdrop = backdrop,
                shape = { RoundedRectangle(cornerRadius.dp) },
                effects = {
                    vibrancy()
                    blur((12f + 12f * progress).dp.toPx())
                    lens((18f + 8f * progress).dp.toPx(), (28f + 10f * progress).dp.toPx())
                },
                onDrawSurface = {
                    drawRect(Color(0x78FFFFFF))
                    drawRect(opened.card.tint.copy(alpha = 0.20f), blendMode = BlendMode.Screen)
                    drawRect(Color.White.copy(alpha = 0.10f * progress), blendMode = BlendMode.Screen)
                }
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {}
            )
            .padding(
                horizontal = with(density) { lerp(18f, 28f, progress).toDp() },
                vertical = with(density) { lerp(18f, 32f, progress).toDp() }
            )
    ) {
        Column(
            Modifier
                .align(Alignment.CenterStart)
                .graphicsLayer {
                    val contentScale = lerp(1f, 1.16f, firstStageProgress(progress)) *
                        lerp(1f, 0.96f, secondStageProgress(progress))
                    scaleX = contentScale
                    scaleY = contentScale
                    transformOrigin = androidx.compose.ui.graphics.TransformOrigin(0f, 0.5f)
                },
            verticalArrangement = Arrangement.spacedBy(7f.dp)
        ) {
            BasicText(
                opened.card.title,
                style = TextStyle(
                    color = Color(0xFF05070A),
                    fontSize = 18f.sp,
                    fontWeight = FontWeight.SemiBold
                )
            )
            BasicText(
                opened.card.subtitle,
                style = TextStyle(
                    color = Color(0xEA111827),
                    fontSize = 12.5f.sp,
                    fontWeight = FontWeight.Medium
                )
            )
        }
    }
}

private data class SeyraLaunchFrame(
    val x: Float,
    val y: Float,
    val width: Float,
    val height: Float
)

@Composable
private fun rememberLaunchFrame(
    bounds: SeyraCardBounds,
    rootSize: IntSize,
    progress: Float
): SeyraLaunchFrame {
    return remember(bounds, rootSize, progress) {
        calculateLaunchFrame(bounds, rootSize, progress)
    }
}

private fun calculateLaunchFrame(
    bounds: SeyraCardBounds,
    rootSize: IntSize,
    progress: Float
): SeyraLaunchFrame {
    val first = iosOut(firstStageProgress(progress))
    val second = iosSmooth(secondStageProgress(progress))

    val rootWidth = rootSize.width.toFloat()
    val rootHeight = rootSize.height.toFloat()
    val startCenterX = bounds.x + bounds.width / 2f
    val startCenterY = bounds.y + bounds.height / 2f
    val screenCenterX = rootWidth / 2f
    val screenCenterY = rootHeight / 2f
    val containedScale = minOf(rootWidth / bounds.width, rootHeight / bounds.height) * 0.78f
    val midWidth = bounds.width * containedScale
    val midHeight = bounds.height * containedScale
    val midCenterX = lerp(startCenterX, screenCenterX, 0.36f)
    val midCenterY = lerp(startCenterY, screenCenterY, 0.36f)

    val width = if (progress < 0.48f) {
        lerp(bounds.width, midWidth, first)
    } else {
        lerp(midWidth, rootWidth, second)
    }
    val height = if (progress < 0.48f) {
        lerp(bounds.height, midHeight, first)
    } else {
        lerp(midHeight, rootHeight, second)
    }
    val centerX = if (progress < 0.48f) {
        lerp(startCenterX, midCenterX, first)
    } else {
        lerp(midCenterX, screenCenterX, second)
    }
    val centerY = if (progress < 0.48f) {
        lerp(startCenterY, midCenterY, first)
    } else {
        lerp(midCenterY, screenCenterY, second)
    }

    return SeyraLaunchFrame(
        x = centerX - width / 2f,
        y = centerY - height / 2f,
        width = width,
        height = height
    )
}

private fun firstStageProgress(progress: Float): Float {
    return (progress / 0.48f).coerceIn(0f, 1f)
}

private fun secondStageProgress(progress: Float): Float {
    return ((progress - 0.48f) / 0.52f).coerceIn(0f, 1f)
}

private fun iosOut(value: Float): Float {
    val t = value.coerceIn(0f, 1f)
    val inverted = 1f - t
    return 1f - inverted * inverted * inverted
}

private fun iosSmooth(value: Float): Float {
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

package com.kyant.backdrop.catalog

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kyant.backdrop.backdrops.LayerBackdrop
import com.kyant.backdrop.catalog.components.LiquidBottomTab
import com.kyant.backdrop.catalog.components.LiquidBottomTabs
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
import glass.app.generated.resources.ic_top_more_24px
import glass.app.generated.resources.ic_top_share_24px
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

private data class SeyraCard(
    val title: String,
    val subtitle: String,
    val tint: Color
)

private data class SeyraDockTab(
    val icon: DrawableResource,
    val label: String
)

private val workspaceCards = listOf(
    SeyraCard("智能笔记", "整理想法 / 任务记录", Color(0xFF70D7FF)),
    SeyraCard("文件收纳", "资源分类 / 快速查找", Color(0xFF9B7CFF)),
    SeyraCard("日程面板", "今日安排 / 提醒事项", Color(0xFFFFC56E)),
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

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .displayCutoutPadding(),
        contentPadding = PaddingValues(
            start = 22f.dp,
            top = 100f.dp,
            end = 22f.dp,
            bottom = 124f.dp
        ),
        verticalArrangement = Arrangement.spacedBy(20f.dp)
    ) {
        if (selectedTabIndex == 0) {
            item {
                SeyraLiquidHeaderPanel(backdrop)
            }
        } else if (selectedTabIndex == 2) {
            items(workspaceCards.chunked(2)) { rowCards ->
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(20f.dp)
                ) {
                    rowCards.forEach { card ->
                        SeyraLiquidCard(
                            card = card,
                            backdrop = backdrop,
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

    SeyraDock(
        selectedTabIndex = selectedTabIndex,
        onTabSelected = { selectedTabIndex = it },
        backdrop = backdrop,
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .navigationBarsPadding()
            .padding(start = 38f.dp, end = 38f.dp, bottom = 12f.dp)
    )

    SeyraTopActions(
        backdrop = backdrop,
        modifier = Modifier
            .align(Alignment.TopEnd)
            .statusBarsPadding()
            .displayCutoutPadding()
            .padding(top = 18f.dp, end = 22f.dp)
    )
}

@Composable
private fun SeyraTopActions(
    backdrop: LayerBackdrop,
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
            modifier = Modifier.weight(1f)
        )
        SeyraTopActionIcon(
            icon = Res.drawable.ic_top_more_24px,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun SeyraTopActionIcon(
    icon: DrawableResource,
    modifier: Modifier = Modifier
) {
    Box(
        modifier
            .fillMaxHeight()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {}
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
    modifier: Modifier = Modifier
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
            .padding(18f.dp)
    ) {
        Column(
            Modifier.align(Alignment.TopStart),
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

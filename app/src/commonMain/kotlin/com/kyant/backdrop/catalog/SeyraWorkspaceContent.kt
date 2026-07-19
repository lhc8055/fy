package com.kyant.backdrop.catalog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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

private data class SeyraCard(
    val title: String,
    val subtitle: String,
    val symbol: String,
    val tint: Color
)

private val workspaceCards = listOf(
    SeyraCard("智能笔记", "整理想法 / 任务记录", "✦", Color(0xFF70D7FF)),
    SeyraCard("文件收纳", "资源分类 / 快速查找", "▣", Color(0xFF9B7CFF)),
    SeyraCard("日程面板", "今日安排 / 提醒事项", "◷", Color(0xFFFFC56E)),
    SeyraCard("灵感盒子", "收藏碎片 / 快速保存", "◇", Color(0xFFFF8EC7)),
    SeyraCard("快捷工具", "常用操作 / 一键启动", "⌘", Color(0xFF6EF0BC)),
    SeyraCard("数据概览", "进度统计 / 状态查看", "◌", Color(0xFF7EA8FF)),
    SeyraCard("工作流", "步骤管理 / 自动流程", "↗", Color(0xFFFF9D78)),
    SeyraCard("个人空间", "偏好设置 / 账号信息", "●", Color(0xFF8EE7FF))
)

@Composable
fun SeyraWorkspaceContent() {
    BackdropDemoScaffold {
        SeyraWorkspace(it)
    }
}

@Composable
private fun BoxScope.SeyraWorkspace(backdrop: LayerBackdrop) {
    Box(
        Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    0f to Color(0x55EAF8FF),
                    0.42f to Color(0x22B7A7FF),
                    1f to Color(0x333F7DFF)
                )
            )
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .displayCutoutPadding(),
        contentPadding = PaddingValues(
            start = 22f.dp,
            top = 24f.dp,
            end = 22f.dp,
            bottom = 124f.dp
        ),
        verticalArrangement = Arrangement.spacedBy(14f.dp)
    ) {
        item {
            SeyraHeaderGlass(backdrop)
        }

        item {
            BasicText(
                "工作区",
                Modifier.padding(top = 6f.dp, bottom = 2f.dp),
                style = TextStyle(
                    color = Color(0xEE101828),
                    fontSize = 28f.sp,
                    fontWeight = FontWeight.SemiBold
                )
            )
        }

        items(workspaceCards.chunked(2)) { rowCards ->
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14f.dp)
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

    SeyraDock(
        backdrop = backdrop,
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .navigationBarsPadding()
            .padding(start = 38f.dp, end = 38f.dp, bottom = 12f.dp)
    )
}

@Composable
private fun SeyraHeaderGlass(backdrop: LayerBackdrop) {
    Column(
        Modifier
            .fillMaxWidth()
            .height(170f.dp)
            .drawBackdrop(
                backdrop = backdrop,
                shape = { RoundedRectangle(34f.dp) },
                effects = {
                    vibrancy()
                    blur(16f.dp.toPx())
                    lens(24f.dp.toPx(), 32f.dp.toPx())
                },
                onDrawSurface = {
                    drawRect(Color(0x99FFFFFF))
                    drawRect(Color(0x221C6CFF), blendMode = BlendMode.Screen)
                }
            )
            .padding(24f.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6f.dp)) {
            BasicText(
                "Seyra",
                style = TextStyle(
                    color = Color(0xF20B1220),
                    fontSize = 34f.sp,
                    fontWeight = FontWeight.SemiBold
                )
            )
            BasicText(
                "你的液态玻璃工作台",
                style = TextStyle(
                    color = Color(0xAA1B2A41),
                    fontSize = 15f.sp,
                    fontWeight = FontWeight.Medium
                )
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(10f.dp)) {
            SeyraSmallGlassPill("今日")
            SeyraSmallGlassPill("待办 6")
        }
    }
}

@Composable
private fun SeyraSmallGlassPill(text: String) {
    Box(
        Modifier
            .background(Color(0x33FFFFFF), Capsule())
            .padding(horizontal = 14f.dp, vertical = 8f.dp),
        contentAlignment = Alignment.Center
    ) {
        BasicText(
            text,
            style = TextStyle(
                color = Color(0xCC0D1828),
                fontSize = 13f.sp,
                fontWeight = FontWeight.Medium
            )
        )
    }
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
                    color = Color(0xF20E1726),
                    fontSize = 18f.sp,
                    fontWeight = FontWeight.SemiBold
                )
            )
            BasicText(
                card.subtitle,
                style = TextStyle(
                    color = Color(0xAA1B2A41),
                    fontSize = 12.5f.sp,
                    fontWeight = FontWeight.Medium
                )
            )
        }

        BasicText(
            card.symbol,
            Modifier.align(Alignment.BottomEnd),
            style = TextStyle(
                color = card.tint.copy(alpha = 0.86f),
                fontSize = 38f.sp,
                fontWeight = FontWeight.SemiBold
            )
        )
    }
}

@Composable
private fun SeyraDock(
    backdrop: LayerBackdrop,
    modifier: Modifier = Modifier
) {
    var selectedTabIndex by rememberSaveable { mutableIntStateOf(0) }
    val tabs = listOf(
        "⌂" to "工作",
        "■" to "资源",
        "▣" to "工具",
        "●" to "我的"
    )

    LiquidBottomTabs(
        selectedTabIndex = { selectedTabIndex },
        onTabSelected = { selectedTabIndex = it },
        backdrop = backdrop,
        tabsCount = tabs.size,
        modifier = modifier.fillMaxWidth()
    ) {
        tabs.forEachIndexed { index, tab ->
            val selected = selectedTabIndex == index
            val color = if (selected) Color(0xFF159CFF) else Color(0xCC111827)

            LiquidBottomTab({ selectedTabIndex = index }) {
                BasicText(
                    tab.first,
                    style = TextStyle(
                        color = color,
                        fontSize = 24f.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                )
                BasicText(
                    tab.second,
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

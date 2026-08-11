package com.so.movie.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.so.movie.R
import com.so.movie.navigation.Screen
import com.so.movie.ui.theme.SOMovieTheme
import com.so.movie.ui.theme.TextPrimary
import com.so.movie.ui.theme.TextSecondary
import com.so.movie.ui.theme.TextTertiary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MineScreen(navController: NavController) {
    Scaffold(
        containerColor = Color(0xFFF5F7FA)
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            item {
                Text(
                    text = "我的",
                    style = MaterialTheme.typography.headlineMedium,
                    color = TextPrimary,
                    fontSize = 32.sp,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 20.dp, top = 16.dp, bottom = 24.dp)
                )
            }
            item {
                UserProfileCard(navController)
            }
            item {
                VipCard()
            }
            item {
                QuickActions(navController)
            }
            item {
                MenuCard(
                    title = "更多功能",
                    items = listOf(
                        MenuEntry(
                            iconPainter = PlaylistIconPainter,
                            title = "片单管理",
                            onClick = { navController.navigate(Screen.Playlist.route) }
                        ),
                        MenuEntry(
                            iconPainter = SettingGearIconPainter,
                            title = "播放设置",
                            onClick = { navController.navigate(Screen.PlaySetting.route) }
                        ),
                        MenuEntry(
                            iconPainter = HouseIconPainter,
                            title = "清理缓存",
                            rightText = "1.32GB",
                            onClick = { /* TODO */ }
                        ),
                        MenuEntry(
                            iconPainter = HelpIconPainter,
                            title = "帮助与反馈",
                            onClick = { /* TODO */ }
                        ),
                        MenuEntry(
                            iconPainter = SettingGearIconPainter,
                            title = "设置",
                            onClick = { navController.navigate(Screen.About.route) }
                        )
                    )
                )
                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }
}

@Composable
private fun UserProfileCard(navController: NavController) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF5F7FA))
            .clickable { /* TODO: 登录 */ }
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = "https://picsum.photos/seed/guestavatar/200/200",
            contentDescription = null,
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .border(1.dp, Color(0xFFE5E7EB), CircleShape),
            placeholder = painterResource(R.drawable.ic_launcher_foreground)
        )
        Spacer(modifier = Modifier.width(18.dp))
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = "游客",
                style = MaterialTheme.typography.headlineSmall,
                color = TextPrimary,
                fontSize = 28.sp,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "登录/注册",
                style = MaterialTheme.typography.bodyLarge,
                color = TextSecondary,
                fontSize = 16.sp
            )
        }
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = TextTertiary,
            modifier = Modifier.size(28.dp)
        )
    }
}

@Composable
private fun VipCard() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFFFFE0B2),
                        Color(0xFFFFCC80),
                        Color(0xFFE8C07D)
                    )
                )
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "开通VIP会员",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color(0xFF3A2817),
                    fontWeight = androidx.compose.ui.text.font.FontWeight.ExtraBold,
                    fontSize = 22.sp
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "享受无广告、高清画质等特权",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF5A3F25),
                    fontSize = 15.sp
                )
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(28.dp))
                    .background(Color(0xFF121212))
                    .clickable { /* TODO */ }
                    .padding(horizontal = 22.dp, vertical = 12.dp)
            ) {
                Text(
                    text = "立即开通",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
        }
    }
}

@Composable
private fun QuickActions(navController: NavController) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF5F7FA))
            .padding(horizontal = 8.dp, vertical = 20.dp),
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        ActionItem(
            iconPainter = ClockIconPainter,
            label = "观看历史",
            onClick = { /* TODO */ }
        )
        ActionItem(
            iconPainter = BookmarkIconPainter,
            label = "我的收藏",
            onClick = { /* TODO */ }
        )
        ActionItem(
            iconPainter = DownloadIconPainter,
            label = "我的下载",
            onClick = { /* TODO */ }
        )
        ActionItem(
            iconPainter = ChatBubbleIconPainter,
            label = "消息中心",
            onClick = { /* TODO */ }
        )
    }
}

@Composable
private fun ActionItem(
    iconPainter: androidx.compose.ui.graphics.painter.Painter,
    label: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 6.dp)
    ) {
        Icon(
            painter = iconPainter,
            contentDescription = null,
            tint = TextPrimary,
            modifier = Modifier.size(32.dp)
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = TextPrimary,
            fontSize = 14.sp
        )
    }
}

data class MenuEntry(
    val iconPainter: androidx.compose.ui.graphics.painter.Painter,
    val title: String,
    val rightText: String? = null,
    val onClick: () -> Unit = {}
)

@Composable
private fun MenuCard(title: String, items: List<MenuEntry>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White)
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = TextPrimary,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
            fontSize = 22.sp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, top = 20.dp, bottom = 12.dp)
        )
        items.forEachIndexed { index, item ->
            MenuRow(
                iconPainter = item.iconPainter,
                title = item.title,
                rightText = item.rightText,
                onClick = item.onClick
            )
            if (index < items.size - 1) {
                Divider(
                    color = Color(0xFFF3F4F6),
                    thickness = 0.8.dp,
                    modifier = Modifier.padding(start = 64.dp, end = 20.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
    }
}

@Composable
private fun MenuRow(
    iconPainter: androidx.compose.ui.graphics.painter.Painter,
    title: String,
    rightText: String? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = iconPainter,
            contentDescription = null,
            tint = TextPrimary,
            modifier = Modifier.size(28.dp)
        )
        Spacer(modifier = Modifier.width(20.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            color = TextPrimary,
            fontSize = 17.sp,
            modifier = Modifier.weight(1f)
        )
        if (rightText != null) {
            Text(
                text = rightText,
                style = MaterialTheme.typography.bodyMedium,
                color = TextTertiary,
                fontSize = 15.sp
            )
            Spacer(modifier = Modifier.width(10.dp))
        }
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = TextTertiary,
            modifier = Modifier.size(22.dp)
        )
    }
}

/* ========= Custom Painters for line-art icons matching the screenshot ========= */

private object ClockIconPainter : androidx.compose.ui.graphics.painter.Painter() {
    override val intrinsicSize = androidx.compose.ui.unit.IntSize(24, 24).toSize()
    private val strokeColor = Color(0xFF222222)
    override fun DrawScope.onDraw() {
        val stroke = Stroke(width = size.width * 0.08f, cap = StrokeCap.Round)
        // circle
        drawCircle(color = strokeColor, style = stroke, radius = size.width * 0.44f)
        // hand 1
        drawLine(
            color = strokeColor,
            start = center,
            end = Offset(center.x, center.y - size.height * 0.26f),
            strokeWidth = size.width * 0.08f,
            cap = StrokeCap.Round
        )
        // hand 2
        drawLine(
            color = strokeColor,
            start = center,
            end = Offset(center.x + size.width * 0.22f, center.y),
            strokeWidth = size.width * 0.08f,
            cap = StrokeCap.Round
        )
    }
}

private object BookmarkIconPainter : androidx.compose.ui.graphics.painter.Painter() {
    override val intrinsicSize = androidx.compose.ui.unit.IntSize(24, 24).toSize()
    private val strokeColor = Color(0xFF222222)
    override fun DrawScope.onDraw() {
        val sw = size.width * 0.08f
        val path = Path().apply {
            moveTo(size.width * 0.22f, size.height * 0.10f)
            lineTo(size.width * 0.78f, size.height * 0.10f)
            lineTo(size.width * 0.78f, size.height * 0.90f)
            lineTo(size.width * 0.50f, size.height * 0.70f)
            lineTo(size.width * 0.22f, size.height * 0.90f)
            close()
        }
        drawPath(path = path, color = strokeColor, style = Stroke(width = sw, cap = StrokeCap.Round))
        // triangle fill missing? keep line art
    }
}

private object DownloadIconPainter : androidx.compose.ui.graphics.painter.Painter() {
    override val intrinsicSize = androidx.compose.ui.unit.IntSize(24, 24).toSize()
    private val strokeColor = Color(0xFF222222)
    override fun DrawScope.onDraw() {
        val sw = size.width * 0.08f
        // tray
        drawLine(
            strokeColor,
            start = Offset(size.width * 0.15f, size.height * 0.80f),
            end = Offset(size.width * 0.85f, size.height * 0.80f),
            strokeWidth = sw, cap = StrokeCap.Round
        )
        drawLine(
            strokeColor,
            start = Offset(size.width * 0.15f, size.height * 0.80f),
            end = Offset(size.width * 0.15f, size.height * 0.92f),
            strokeWidth = sw, cap = StrokeCap.Round
        )
        drawLine(
            strokeColor,
            start = Offset(size.width * 0.85f, size.height * 0.80f),
            end = Offset(size.width * 0.85f, size.height * 0.92f),
            strokeWidth = sw, cap = StrokeCap.Round
        )
        // down arrow line
        drawLine(
            strokeColor,
            start = Offset(size.width * 0.5f, size.height * 0.10f),
            end = Offset(size.width * 0.5f, size.height * 0.72f),
            strokeWidth = sw, cap = StrokeCap.Round
        )
        // arrow head
        drawLine(
            strokeColor,
            start = Offset(size.width * 0.5f, size.height * 0.72f),
            end = Offset(size.width * 0.28f, size.height * 0.50f),
            strokeWidth = sw, cap = StrokeCap.Round
        )
        drawLine(
            strokeColor,
            start = Offset(size.width * 0.5f, size.height * 0.72f),
            end = Offset(size.width * 0.72f, size.height * 0.50f),
            strokeWidth = sw, cap = StrokeCap.Round
        )
    }
}

private object ChatBubbleIconPainter : androidx.compose.ui.graphics.painter.Painter() {
    override val intrinsicSize = androidx.compose.ui.unit.IntSize(24, 24).toSize()
    private val strokeColor = Color(0xFF222222)
    override fun DrawScope.onDraw() {
        val sw = size.width * 0.08f
        val path = Path().apply {
            moveTo(size.width * 0.15f, size.height * 0.20f)
            lineTo(size.width * 0.85f, size.height * 0.20f)
            lineTo(size.width * 0.85f, size.height * 0.66f)
            lineTo(size.width * 0.55f, size.height * 0.66f)
            lineTo(size.width * 0.35f, size.height * 0.86f)
            lineTo(size.width * 0.35f, size.height * 0.66f)
            lineTo(size.width * 0.15f, size.height * 0.66f)
            close()
        }
        drawPath(path = path, color = strokeColor, style = Stroke(width = sw, cap = StrokeCap.Round))
    }
}

private object PlaylistIconPainter : androidx.compose.ui.graphics.painter.Painter() {
    override val intrinsicSize = androidx.compose.ui.unit.IntSize(24, 24).toSize()
    private val strokeColor = Color(0xFF222222)
    override fun DrawScope.onDraw() {
        val sw = size.width * 0.08f
        drawRect(
            color = strokeColor,
            style = Stroke(width = sw),
            topLeft = Offset(size.width * 0.18f, size.height * 0.18f),
            size = androidx.compose.ui.geometry.Size(size.width * 0.64f, size.height * 0.64f)
        )
    }
}

private object SettingGearIconPainter : androidx.compose.ui.graphics.painter.Painter() {
    override val intrinsicSize = androidx.compose.ui.unit.IntSize(24, 24).toSize()
    private val strokeColor = Color(0xFF222222)
    override fun DrawScope.onDraw() {
        val sw = size.width * 0.08f
        // outer ring
        drawCircle(color = strokeColor, style = Stroke(width = sw), radius = size.width * 0.40f)
        // inner dot
        drawCircle(color = strokeColor, style = Stroke(width = sw), radius = size.width * 0.12f)
        // 4 spokes
        for (i in 0 until 4) {
            val angle = Math.toRadians((i * 90).toDouble())
            val r1 = size.width * 0.12f
            val r2 = size.width * 0.28f
            val cx = center.x
            val cy = center.y
            drawLine(
                color = strokeColor,
                start = Offset(cx + (kotlin.math.cos(angle) * r1).toFloat(), cy + (kotlin.math.sin(angle) * r1).toFloat()),
                end = Offset(cx + (kotlin.math.cos(angle) * r2).toFloat(), cy + (kotlin.math.sin(angle) * r2).toFloat()),
                strokeWidth = sw, cap = StrokeCap.Round
            )
        }
    }
}

private object HouseIconPainter : androidx.compose.ui.graphics.painter.Painter() {
    override val intrinsicSize = androidx.compose.ui.unit.IntSize(24, 24).toSize()
    private val strokeColor = Color(0xFF222222)
    override fun DrawScope.onDraw() {
        val sw = size.width * 0.08f
        val path = Path().apply {
            moveTo(size.width * 0.12f, size.height * 0.50f)
            lineTo(size.width * 0.50f, size.height * 0.16f)
            lineTo(size.width * 0.88f, size.height * 0.50f)
            moveTo(size.width * 0.22f, size.height * 0.44f)
            lineTo(size.width * 0.22f, size.height * 0.88f)
            lineTo(size.width * 0.78f, size.height * 0.88f)
            lineTo(size.width * 0.78f, size.height * 0.44f)
        }
        drawPath(path = path, color = strokeColor, style = Stroke(width = sw, cap = StrokeCap.Round))
        // door
        drawRect(
            color = strokeColor,
            style = Stroke(width = sw),
            topLeft = Offset(size.width * 0.40f, size.height * 0.58f),
            size = androidx.compose.ui.geometry.Size(size.width * 0.20f, size.height * 0.30f)
        )
    }
}

private object HelpIconPainter : androidx.compose.ui.graphics.painter.Painter() {
    override val intrinsicSize = androidx.compose.ui.unit.IntSize(24, 24).toSize()
    private val strokeColor = Color(0xFF222222)
    override fun DrawScope.onDraw() {
        val sw = size.width * 0.08f
        // rounded rect
        val rr = size.width * 0.18f
        drawRoundRect(
            color = strokeColor,
            style = Stroke(width = sw),
            topLeft = Offset(size.width * 0.18f, size.height * 0.18f),
            size = androidx.compose.ui.geometry.Size(size.width * 0.64f, size.height * 0.64f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(rr, rr)
        )
        // small dot inside
        drawCircle(
            color = strokeColor,
            radius = size.width * 0.05f,
            center = Offset(size.width * 0.50f, size.height * 0.62f)
        )
    }
}

private fun androidx.compose.ui.unit.IntSize.toSize(): androidx.compose.ui.geometry.Size =
    androidx.compose.ui.geometry.Size(width.toFloat(), height.toFloat())

@Preview(showBackground = true)
@Composable
fun MineScreenPreview() {
    SOMovieTheme {
        MineScreen(navController = rememberNavController())
    }
}

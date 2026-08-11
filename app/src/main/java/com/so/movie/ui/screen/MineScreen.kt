package com.so.movie.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PlaylistPlay
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
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
                    fontWeight = FontWeight.Bold,
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
                            icon = Icons.Default.PlaylistPlay,
                            title = "片单管理",
                            onClick = { navController.navigate(Screen.Playlist.route) }
                        ),
                        MenuEntry(
                            icon = Icons.Default.Tune,
                            title = "播放设置",
                            onClick = { navController.navigate(Screen.PlaySetting.route) }
                        ),
                        MenuEntry(
                            icon = Icons.Default.CleaningServices,
                            title = "清理缓存",
                            rightText = "1.32GB",
                            onClick = { /* TODO */ }
                        ),
                        MenuEntry(
                            icon = Icons.Default.HelpOutline,
                            title = "帮助与反馈",
                            onClick = { /* TODO */ }
                        ),
                        MenuEntry(
                            icon = Icons.Default.Settings,
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
                fontWeight = FontWeight.Bold
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
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
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
                    fontWeight = FontWeight.ExtraBold,
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
                    fontWeight = FontWeight.Bold,
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
            icon = Icons.Default.History,
            label = "观看历史",
            onClick = { /* TODO */ }
        )
        ActionItem(
            icon = Icons.Default.Bookmark,
            label = "我的收藏",
            onClick = { /* TODO */ }
        )
        ActionItem(
            icon = Icons.Default.Download,
            label = "我的下载",
            onClick = { /* TODO */ }
        )
        ActionItem(
            icon = Icons.AutoMirrored.Filled.Message,
            label = "消息中心",
            onClick = { /* TODO */ }
        )
    }
}

@Composable
private fun ActionItem(
    icon: ImageVector,
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
            imageVector = icon,
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
    val icon: ImageVector,
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
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, top = 20.dp, bottom = 12.dp)
        )
        items.forEachIndexed { index, item ->
            MenuRow(
                icon = item.icon,
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
    icon: ImageVector,
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
            imageVector = icon,
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
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = TextTertiary,
            modifier = Modifier.size(22.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun MineScreenPreview() {
    SOMovieTheme {
        MineScreen(navController = rememberNavController())
    }
}

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
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
import com.so.movie.ui.theme.VipGradientEnd
import com.so.movie.ui.theme.VipGradientStart

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MineScreen(navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "我的",
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        containerColor = Color(0xFFF5F7FA)
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
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
                SectionTitle("更多功能")
                MenuGroup(
                    items = listOf(
                        MenuItem(R.drawable.ic_launcher_foreground, "规则管理", onClick = { navController.navigate(Screen.RuleManagement.route) }),
                        MenuItem(R.drawable.ic_launcher_foreground, "片单管理", onClick = { navController.navigate(Screen.Playlist.route) }),
                        MenuItem(R.drawable.ic_launcher_foreground, "播放设置", onClick = { navController.navigate(Screen.PlaySetting.route) }),
                        MenuItem(R.drawable.ic_launcher_foreground, "清理缓存", rightText = "1.32GB", onClick = { /* TODO */ }),
                        MenuItem(R.drawable.ic_launcher_foreground, "帮助与反馈", onClick = { /* TODO */ }),
                        MenuItem(R.drawable.ic_launcher_foreground, "设置", onClick = { navController.navigate(Screen.About.route) })
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
            .background(Color.White)
            .clickable { /* TODO: 登录 */ }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = "https://picsum.photos/seed/avatar/200/200",
            contentDescription = null,
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
                .border(2.dp, Color(0xFFE5E7EB), CircleShape),
            placeholder = painterResource(R.drawable.ic_launcher_foreground)
        )
        Spacer(modifier = Modifier.width(14.dp))
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = "游客",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
                fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "登录/注册",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = TextTertiary
        )
    }
}

@Composable
private fun VipCard() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(Color(0xFF2C1810), Color(0xFF4A2C1A))
                )
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "开通VIP会员",
                        style = MaterialTheme.typography.titleMedium,
                        color = VipGradientStart,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "享受无广告、高清原画等特权",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFD4A574)
                )
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(VipGradientStart, VipGradientEnd)
                        )
                    )
                    .clickable { /* TODO */ }
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "立即开通",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color(0xFF3D2314),
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                    fontSize = 13.sp
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
            .background(Color.White)
            .padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        ActionItem(
            icon = android.R.drawable.ic_menu_my_calendar,
            label = "观看历史",
            onClick = { /* TODO */ }
        )
        ActionItem(
            icon = android.R.drawable.btn_star_big_on,
            label = "我的收藏",
            onClick = { /* TODO */ }
        )
        ActionItem(
            icon = android.R.drawable.stat_sys_download,
            label = "我的下载",
            onClick = { /* TODO */ }
        )
        ActionItem(
            icon = android.R.drawable.sym_action_chat,
            label = "消息中心",
            onClick = { /* TODO */ }
        )
    }
}

@Composable
private fun ActionItem(icon: Int, label: String, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = null,
            tint = TextSecondary,
            modifier = Modifier.size(28.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary,
            fontSize = 12.sp
        )
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.bodyMedium,
        color = TextTertiary,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, top = 16.dp, bottom = 8.dp),
        fontSize = 13.sp
    )
}

data class MenuItem(
    val icon: Int,
    val title: String,
    val rightText: String? = null,
    val onClick: () -> Unit = {}
)

@Composable
private fun MenuGroup(items: List<MenuItem>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
    ) {
        items.forEachIndexed { index, item ->
            MenuRow(
                icon = item.icon,
                title = item.title,
                rightText = item.rightText,
                onClick = item.onClick
            )
            if (index < items.size - 1) {
                Divider(
                    color = Color(0xFFF0F2F5),
                    thickness = 0.5.dp,
                    modifier = Modifier.padding(start = 52.dp)
                )
            }
        }
    }
}

@Composable
private fun MenuRow(
    icon: Int,
    title: String,
    rightText: String? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = null,
            tint = TextSecondary,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            color = TextPrimary,
            modifier = Modifier.weight(1f)
        )
        if (rightText != null) {
            Text(
                text = rightText,
                style = MaterialTheme.typography.bodySmall,
                color = TextTertiary
            )
            Spacer(modifier = Modifier.width(6.dp))
        }
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = TextTertiary,
            modifier = Modifier.size(18.dp)
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

package com.so.movie.ui.screen

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.so.movie.R
import com.so.movie.data.FollowItem
import com.so.movie.data.FollowStatus
import com.so.movie.data.MockData
import com.so.movie.navigation.Screen
import com.so.movie.ui.theme.Primary
import com.so.movie.ui.theme.SOMovieTheme
import com.so.movie.ui.theme.TextPrimary
import com.so.movie.ui.theme.TextSecondary
import com.so.movie.ui.theme.TextTertiary
import com.so.movie.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FollowScreen(
    navController: NavController,
    viewModel: MainViewModel = viewModel()
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val followListState by viewModel.followList.collectAsState()

    val tabs = listOf("追更中", "已完成", "放弃")
    val statusMap = mapOf(
        0 to FollowStatus.UPDATING,
        1 to FollowStatus.COMPLETED,
        2 to FollowStatus.ABANDONED
    )

    val filteredList = followListState.filter { item -> item.status == statusMap[selectedTab] }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "追剧",
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary
                    )
                },
                actions = {
                    Text(
                        text = "编辑",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Primary,
                        modifier = Modifier
                            .padding(end = 16.dp)
                            .clickable { /* TODO */ }
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        containerColor = Color(0xFFF5F7FA)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                tabs.forEachIndexed { index, tab ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                if (selectedTab == index) Primary.copy(alpha = 0.1f)
                                else Color.Transparent
                            )
                            .clickable { selectedTab = index },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = tab,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (selectedTab == index) Primary else TextSecondary
                        )
                    }
                }
            }

            if (filteredList.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            painter = painterResource(android.R.drawable.ic_menu_my_calendar),
                            contentDescription = null,
                            tint = TextTertiary,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "暂无${tabs[selectedTab]}内容",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextTertiary
                        )
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredList) { item ->
                        FollowItemCard(
                            item = item,
                            onContinueClick = {
                                navController.navigate(Screen.Player.createRoute(item.movie.id))
                            },
                            onClick = {
                                navController.navigate(Screen.Player.createRoute(item.movie.id))
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FollowItemCard(
    item: FollowItem,
    onContinueClick: () -> Unit,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            AsyncImage(
                model = item.movie.poster,
                contentDescription = item.movie.title,
                modifier = Modifier
                    .width(90.dp)
                    .aspectRatio(2f / 3f)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop,
                placeholder = painterResource(R.drawable.ic_launcher_foreground),
                error = painterResource(R.drawable.ic_launcher_foreground)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(
                modifier = Modifier
                    .weight(1f)
                    .height(135.dp)
            ) {
                Text(
                    text = item.movie.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = String.format("%.1f分", item.movie.rating),
                        style = MaterialTheme.typography.labelMedium,
                        color = Color(0xFFFF6B6B),
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = item.movie.tags.take(2).joinToString("·"),
                        style = MaterialTheme.typography.bodySmall,
                        color = TextTertiary
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "${item.movie.updateTime}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "看到第${item.watchedEpisodes}集",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextTertiary
                )
                Spacer(modifier = Modifier.weight(1f))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(Primary.copy(alpha = 0.1f))
                            .clickable(onClick = onContinueClick)
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "继续播放",
                            style = MaterialTheme.typography.labelMedium,
                            color = Primary,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun FollowScreenPreview() {
    SOMovieTheme {
        FollowScreen(navController = rememberNavController())
    }
}

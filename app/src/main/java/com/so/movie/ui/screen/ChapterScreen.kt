package com.so.movie.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.so.movie.navigation.Screen
import com.so.movie.rule.ChapterEpisode
import com.so.movie.rule.ChapterRoad
import com.so.movie.ui.theme.*
import com.so.movie.viewmodel.RuleViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChapterScreen(
    navController: NavController,
    viewModel: RuleViewModel = viewModel()
) {
    val searchResult by viewModel.selectedSearchResult.collectAsState()
    val chapterResult by viewModel.chapterResult.collectAsState()
    val chapterLoading by viewModel.chapterLoading.collectAsState()
    var selectedRoad by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = searchResult?.title ?: "选集",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        viewModel.clearChapters()
                        navController.popBackStack()
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color(0xFFF5F7FA)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                chapterLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = Primary)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "正在解析剧集列表...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextTertiary
                            )
                        }
                    }
                }

                chapterResult?.success == true && chapterResult!!.roads.isNotEmpty() -> {
                    val roads = chapterResult!!.roads

                    // 线路选择标签
                    if (roads.size > 1) {
                        LazyRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.White)
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(roads.size) { index ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(
                                            if (selectedRoad == index) Primary
                                            else Color(0xFFF0F2F5)
                                        )
                                        .clickable { selectedRoad = index }
                                        .padding(horizontal = 14.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = roads[index].name,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (selectedRoad == index) Color.White else TextSecondary
                                    )
                                }
                            }
                        }
                    }

                    // 剧集列表
                    val episodes = roads[selectedRoad.coerceAtMost(roads.size - 1)].episodes
                    Text(
                        text = "共 ${episodes.size} 集",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextTertiary,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )

                    LazyColumn(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(episodes) { episode ->
                            EpisodeItem(
                                episode = episode,
                                onClick = {
                                    viewModel.setCurrentPlayUrl(episode.url)
                                    navController.navigate(
                                        Screen.Player.createRoute(
                                            searchResult?.url.hashCode().toString() ?: "0"
                                        )
                                    )
                                }
                            )
                        }
                    }
                }

                chapterResult?.success == false -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "解析失败",
                                style = MaterialTheme.typography.titleMedium,
                                color = TextSecondary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = chapterResult?.error ?: "未知错误",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextTertiary
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            TextButton(onClick = {
                                searchResult?.let { viewModel.getChapters(it) }
                            }) {
                                Text("重试")
                            }
                        }
                    }
                }

                else -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("暂无数据", color = TextTertiary)
                    }
                }
            }
        }
    }
}

@Composable
private fun EpisodeItem(
    episode: ChapterEpisode,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color.White)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(Primary.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = episode.name.takeLastWhile { it.isDigit() }.ifEmpty { "▶" },
                style = MaterialTheme.typography.labelMedium,
                color = Primary,
                fontSize = 11.sp
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = episode.name,
            style = MaterialTheme.typography.bodyMedium,
            color = TextPrimary,
            modifier = Modifier.weight(1f)
        )
        Icon(
            painter = androidx.compose.ui.res.painterResource(android.R.drawable.ic_media_play),
            contentDescription = null,
            tint = TextTertiary,
            modifier = Modifier.size(16.dp)
        )
    }
}

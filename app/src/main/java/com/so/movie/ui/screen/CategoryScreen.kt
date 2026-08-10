package com.so.movie.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.so.movie.R
import com.so.movie.data.MockData
import com.so.movie.navigation.Screen
import com.so.movie.ui.components.Chip
import com.so.movie.ui.components.MovieCard
import com.so.movie.ui.theme.SOMovieTheme
import com.so.movie.ui.theme.TextPrimary
import com.so.movie.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryScreen(navController: NavController) {
    var selectedCategory by remember { mutableIntStateOf(0) }
    val selectedGenres = remember { mutableStateListOf("全部") }
    val selectedAreas = remember { mutableStateListOf("全部") }
    val selectedYears = remember { mutableStateListOf("全部") }
    var selectedSort by remember { mutableIntStateOf(0) }

    val filteredList = MockData.allCategoryList.filter { movie ->
        val categoryMatch = selectedCategory == 0 || movie.category == MockData.categoryTypes[selectedCategory - 1]
        val genreMatch = selectedGenres.contains("全部") || movie.tags.any { it in selectedGenres }
        val areaMatch = selectedAreas.contains("全部") || movie.area in selectedAreas
        val yearMatch = selectedYears.contains("全部") || movie.year.toString() in selectedYears
        categoryMatch && genreMatch && areaMatch && yearMatch
    }.let { list ->
        when (selectedSort) {
            0 -> list.sortedByDescending { it.rating * 100 + (Math.random() * 50).toInt() }
            1 -> list.sortedByDescending { it.year }
            2 -> list.sortedByDescending { it.rating }
            else -> list
        }
    }

    fun toggleSelection(list: MutableList<String>, item: String, allowMultiple: Boolean = false) {
        if (item == "全部") {
            list.clear()
            list.add("全部")
        } else {
            list.remove("全部")
            if (item in list) {
                list.remove(item)
                if (list.isEmpty()) list.add("全部")
            } else {
                if (!allowMultiple) {
                    list.clear()
                }
                list.add(item)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "分类",
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary
                    )
                },
                actions = {
                    IconButton(onClick = { /* TODO */ }) {
                        Icon(
                            painter = painterResource(android.R.drawable.ic_menu_search),
                            contentDescription = "搜索",
                            tint = TextSecondary
                        )
                    }
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
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                FilterSection(
                    title = "类型",
                    options = listOf("全部") + MockData.categoryTypes,
                    selected = listOf(MockData.categoryTypes.getOrNull(selectedCategory - 1) ?: "全部"),
                    onSelect = { item ->
                        if (item == "全部") selectedCategory = 0
                        else MockData.categoryTypes.indexOf(item).let { if (it >= 0) selectedCategory = it + 1 }
                    },
                    singleSelect = true
                )
                Spacer(modifier = Modifier.height(12.dp))
                FilterSection(
                    title = "剧情",
                    options = MockData.genres,
                    selected = selectedGenres,
                    onSelect = { toggleSelection(selectedGenres, it, allowMultiple = true) },
                    singleSelect = false
                )
                Spacer(modifier = Modifier.height(12.dp))
                FilterSection(
                    title = "地区",
                    options = MockData.areas,
                    selected = selectedAreas,
                    onSelect = { toggleSelection(selectedAreas, it) },
                    singleSelect = true
                )
                Spacer(modifier = Modifier.height(12.dp))
                FilterSection(
                    title = "年份",
                    options = MockData.years,
                    selected = selectedYears,
                    onSelect = { toggleSelection(selectedYears, it) },
                    singleSelect = true
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "排序",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        modifier = Modifier.width(48.dp)
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(MockData.sortTypes.size) { index ->
                            Chip(
                                text = MockData.sortTypes[index],
                                selected = selectedSort == index,
                                onClick = { selectedSort = index }
                            )
                        }
                    }
                }
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredList) { movie ->
                    MovieCard(
                        movie = movie,
                        onClick = { navController.navigate(Screen.Player.createRoute(movie.id)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
private fun FilterSection(
    title: String,
    options: List<String>,
    selected: List<String>,
    onSelect: (String) -> Unit,
    singleSelect: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary,
            modifier = Modifier
                .width(48.dp)
                .padding(top = 8.dp)
        )
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            items(options) { option ->
                Chip(
                    text = option,
                    selected = option in selected,
                    onClick = { onSelect(option) }
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CategoryScreenPreview() {
    SOMovieTheme {
        CategoryScreen(navController = rememberNavController())
    }
}

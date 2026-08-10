package com.so.movie.ui.components

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.so.movie.ui.theme.Primary
import com.so.movie.ui.theme.TextSecondary
import com.so.movie.ui.theme.TextTertiary

sealed class BottomNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector,
    val selectedIcon: ImageVector
) {
    object Home : BottomNavItem("home", "推荐", Icons.Default.Home, Icons.Default.Home)
    object Category : BottomNavItem("category", "分类", Icons.Default.Category, Icons.Default.Category)
    object Follow : BottomNavItem("follow", "追剧", Icons.Default.Favorite, Icons.Default.Favorite)
    object Mine : BottomNavItem("mine", "我的", Icons.Default.Person, Icons.Default.Person)
}

val bottomNavItems = listOf(
    BottomNavItem.Home,
    BottomNavItem.Category,
    BottomNavItem.Follow,
    BottomNavItem.Mine
)

@Composable
fun BottomNavigationBar(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        containerColor = Color.White,
        contentColor = Primary,
        tonalElevation = 8.dp,
        modifier = modifier
    ) {
        bottomNavItems.forEach { item ->
            val selected = currentRoute == item.route
            NavigationBarItem(
                selected = selected,
                onClick = { onNavigate(item.route) },
                icon = {
                    Icon(
                        painter = rememberVectorPainter(image = if (selected) item.selectedIcon else item.icon),
                        contentDescription = item.label,
                        tint = if (selected) Primary else TextTertiary,
                        modifier = Modifier.size(24.dp)
                    )
                },
                label = {
                    Text(
                        text = item.label,
                        color = if (selected) Primary else TextSecondary,
                        fontSize = 11.sp
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = Color.White
                )
            )
        }
    }
}

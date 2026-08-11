package com.so.movie

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.so.movie.navigation.Screen
import com.so.movie.ui.components.BottomNavigationBar
import com.so.movie.ui.components.bottomNavItems
import com.so.movie.ui.screen.AboutScreen
import com.so.movie.ui.screen.CategoryScreen
import com.so.movie.ui.screen.ChapterScreen
import com.so.movie.ui.screen.FollowScreen
import com.so.movie.ui.screen.HomeScreen
import com.so.movie.ui.screen.MineScreen
import com.so.movie.ui.screen.PlaySettingScreen
import com.so.movie.ui.screen.PlaylistScreen
import com.so.movie.ui.screen.PlayerScreen
import com.so.movie.ui.screen.RuleManagementScreen
import com.so.movie.ui.screen.SearchScreen
import com.so.movie.ui.theme.SOMovieTheme
import com.so.movie.viewmodel.MainViewModel
import com.so.movie.viewmodel.RuleViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SOMovieTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MovieApp()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MovieApp() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val viewModel: MainViewModel = viewModel()
    val ruleViewModel: RuleViewModel = viewModel()

    val bottomBarRoutes = bottomNavItems.map { it.route }
    val showBottomBar = currentRoute in bottomBarRoutes

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                BottomNavigationBar(
                    currentRoute = currentRoute ?: Screen.Home.route,
                    onNavigate = { route ->
                        navController.navigate(route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { paddingValues ->
        AppNavHost(
            navController = navController,
            viewModel = viewModel,
            ruleViewModel = ruleViewModel,
            paddingValues = paddingValues
        )
    }
}

@Composable
fun AppNavHost(
    navController: NavHostController,
    viewModel: MainViewModel,
    ruleViewModel: RuleViewModel,
    paddingValues: PaddingValues
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        modifier = Modifier.padding(paddingValues)
    ) {
        composable(Screen.Home.route) {
            HomeScreen(navController = navController)
        }
        composable(Screen.Category.route) {
            CategoryScreen(navController = navController)
        }
        composable(Screen.Follow.route) {
            FollowScreen(navController = navController, viewModel = viewModel)
        }
        composable(Screen.Mine.route) {
            MineScreen(navController = navController)
        }
        composable(
            route = Screen.Player.route,
            arguments = listOf(navArgument("movieId") { type = NavType.StringType })
        ) { backStackEntry ->
            val movieId = backStackEntry.arguments?.getString("movieId") ?: ""
            PlayerScreen(
                movieId = movieId,
                navController = navController,
                viewModel = viewModel,
                ruleViewModel = ruleViewModel
            )
        }
        composable(Screen.Search.route) {
            SearchScreen(
                navController = navController,
                viewModel = viewModel,
                ruleViewModel = ruleViewModel
            )
        }
        composable(Screen.Playlist.route) {
            PlaylistScreen(navController = navController)
        }
        composable(Screen.PlaySetting.route) {
            PlaySettingScreen(navController = navController)
        }
        composable(Screen.About.route) {
            AboutScreen(navController = navController)
        }
        composable(Screen.RuleManagement.route) {
            RuleManagementScreen(navController = navController)
        }
        composable(Screen.Chapter.route) {
            ChapterScreen(navController = navController)
        }
    }
}

@Composable
private fun shouldShowBottomBar(navController: NavHostController): Boolean {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    return currentRoute in bottomNavItems.map { it.route }
}

private val bottomNavItemRoutes = listOf("home", "category", "follow", "mine")

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    SOMovieTheme {
        MovieApp()
    }
}

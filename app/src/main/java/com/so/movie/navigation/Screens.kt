package com.so.movie.navigation

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Category : Screen("category")
    object Follow : Screen("follow")
    object Mine : Screen("mine")
    object Player : Screen("player/{movieId}") {
        fun createRoute(movieId: String) = "player/$movieId"
    }
    object Search : Screen("search")
    object Playlist : Screen("playlist")
    object PlaySetting : Screen("play_setting")
    object About : Screen("about")
    object RuleManagement : Screen("rule_management")
    object Chapter : Screen("chapter")
}

package com.so.movie.viewmodel

import androidx.lifecycle.ViewModel
import com.so.movie.data.FollowItem
import com.so.movie.data.FollowStatus
import com.so.movie.data.MockData
import com.so.movie.data.Movie
import com.so.movie.data.SearchHistoryItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class MainViewModel : ViewModel() {

    private val _followList = MutableStateFlow(MockData.followList)
    val followList: StateFlow<List<FollowItem>> = _followList

    private val _searchHistory = MutableStateFlow(MockData.searchHistory)
    val searchHistory: StateFlow<List<SearchHistoryItem>> = _searchHistory

    private val _selectedTab = MutableStateFlow(0)
    val selectedTab: StateFlow<Int> = _selectedTab

    fun selectTab(index: Int) {
        _selectedTab.value = index
    }

    fun getMovieById(id: String): Movie? {
        return (MockData.hotPlayingList + MockData.hotMovieList + MockData.allCategoryList)
            .firstOrNull { it.id == id }
    }

    fun searchMovies(keyword: String): List<Movie> {
        if (keyword.isBlank()) return emptyList()
        return (MockData.hotPlayingList + MockData.hotMovieList + MockData.allCategoryList)
            .filter {
                it.title.contains(keyword, ignoreCase = true) ||
                it.tags.any { tag -> tag.contains(keyword, ignoreCase = true) } ||
                it.actors.any { actor -> actor.contains(keyword, ignoreCase = true) }
            }
            .distinctBy { it.id }
    }

    fun addSearchHistory(keyword: String) {
        if (keyword.isBlank()) return
        val current = _searchHistory.value.toMutableList()
        current.removeAll { it.keyword == keyword }
        current.add(0, SearchHistoryItem(keyword, System.currentTimeMillis()))
        _searchHistory.value = current.take(10)
    }

    fun removeSearchHistory(keyword: String) {
        _searchHistory.value = _searchHistory.value.filterNot { it.keyword == keyword }
    }

    fun clearSearchHistory() {
        _searchHistory.value = emptyList()
    }

    fun toggleFollow(movie: Movie) {
        val current = _followList.value.toMutableList()
        val existing = current.find { it.movie.id == movie.id }
        if (existing != null) {
            current.remove(existing)
        } else {
            current.add(
                0,
                FollowItem(
                    movie = movie,
                    watchedEpisodes = 0,
                    lastWatchTime = System.currentTimeMillis(),
                    status = FollowStatus.UPDATING
                )
            )
        }
        _followList.value = current
    }

    fun isFollowed(movieId: String): Boolean {
        return _followList.value.any { it.movie.id == movieId }
    }

    fun updateFollowStatus(movieId: String, status: FollowStatus) {
        _followList.value = _followList.value.map {
            if (it.movie.id == movieId) it.copy(status = status) else it
        }
    }
}

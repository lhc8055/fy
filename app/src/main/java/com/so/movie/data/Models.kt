package com.so.movie.data

data class Movie(
    val id: String,
    val title: String,
    val poster: String,
    val cover: String,
    val rating: Float,
    val year: Int,
    val area: String,
    val category: String,
    val tags: List<String>,
    val description: String,
    val totalEpisodes: Int,
    val currentEpisodes: Int,
    val updateTime: String,
    val director: String,
    val actors: List<String>
)

data class FollowItem(
    val movie: Movie,
    val watchedEpisodes: Int,
    val lastWatchTime: Long,
    val status: FollowStatus
)

enum class FollowStatus {
    UPDATING, COMPLETED, ABANDONED
}

data class PlaylistItem(
    val id: String,
    val title: String,
    val cover: String,
    val movieCount: Int,
    val playCount: Int,
    val description: String,
    val type: String
)

data class SearchHistoryItem(
    val keyword: String,
    val time: Long
)

data class HotSearchItem(
    val keyword: String,
    val isHot: Boolean,
    val isNew: Boolean
)

data class VideoEpisode(
    val episode: Int,
    val title: String,
    val url: String,
    val duration: Long
)

data class VideoQuality(
    val level: Int,
    val name: String,
    val url: String
)

data class Comment(
    val id: String,
    val userId: String,
    val userName: String,
    val avatar: String,
    val content: String,
    val time: Long,
    val likeCount: Int,
    val replyCount: Int
)

data class UserInfo(
    val id: String,
    val name: String,
    val avatar: String,
    val isVip: Boolean,
    val vipExpireTime: Long
)

package com.kyant.backdrop.catalog

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

data class SeyraMusicResult(
    val audioUrl: String,
    val name: String,
    val author: String,
    val lyricUrl: String
)

interface SeyraMusicPlayerController {
    val isPlaying: Boolean
    fun play(url: String)
    fun pause()
    fun resume()
}

@Composable
expect fun rememberShareAppAction(): () -> Unit

@Composable
expect fun rememberOpenFeedbackAction(): () -> Unit

@Composable
expect fun rememberCopyTextAction(): (String) -> Unit

expect suspend fun requestXrayResult(message: String): String

expect suspend fun requestMusicResult(input: String, platform: String): SeyraMusicResult

@Composable
expect fun rememberSeyraMusicPlayerController(): SeyraMusicPlayerController

@Composable
expect fun rememberShowToastAction(): (String) -> Unit

@Composable
expect fun SeyraPreloadRemoteImages(requests: List<Pair<String, Int>>)

@Composable
expect fun rememberPreloadRemoteImagesAction(): (List<Pair<String, Int>>) -> Unit

@Composable
expect fun SeyraRemoteImage(
    url: String,
    maxBitmapSize: Int,
    modifier: Modifier = Modifier
)

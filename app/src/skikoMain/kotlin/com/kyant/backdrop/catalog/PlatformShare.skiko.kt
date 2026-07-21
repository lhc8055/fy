package com.kyant.backdrop.catalog

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier

@Composable
actual fun rememberShareAppAction(): () -> Unit {
    return remember { {} }
}

@Composable
actual fun rememberOpenFeedbackAction(): () -> Unit {
    return remember { {} }
}

@Composable
actual fun rememberCopyTextAction(): (String) -> Unit {
    return remember { {} }
}

actual suspend fun requestXrayResult(message: String): String {
    return ""
}

actual suspend fun requestMusicResult(input: String, platform: String): SeyraMusicResult {
    error("当前平台暂不支持音乐解析")
}

@Composable
actual fun rememberSeyraMusicPlayerController(): SeyraMusicPlayerController {
    return remember {
        object : SeyraMusicPlayerController {
            override val isPlaying: Boolean = false
            override fun play(url: String) = Unit
            override fun pause() = Unit
            override fun resume() = Unit
        }
    }
}

@Composable
actual fun rememberShowToastAction(): (String) -> Unit {
    return remember { {} }
}

@Composable
actual fun SeyraPreloadRemoteImages(requests: List<Pair<String, Int>>) {
}

@Composable
actual fun rememberPreloadRemoteImagesAction(): (List<Pair<String, Int>>) -> Unit {
    return remember { {} }
}

@Composable
actual fun SeyraRemoteImage(
    url: String,
    maxBitmapSize: Int,
    modifier: Modifier
) {
    Box(modifier)
}

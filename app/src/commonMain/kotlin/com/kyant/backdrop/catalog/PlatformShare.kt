package com.kyant.backdrop.catalog

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
expect fun rememberShareAppAction(): () -> Unit

@Composable
expect fun rememberOpenFeedbackAction(): () -> Unit

@Composable
expect fun rememberCopyTextAction(): (String) -> Unit

expect suspend fun requestXrayResult(message: String): String

@Composable
expect fun SeyraEmbeddedWebPage(
    url: String,
    modifier: Modifier = Modifier
)

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

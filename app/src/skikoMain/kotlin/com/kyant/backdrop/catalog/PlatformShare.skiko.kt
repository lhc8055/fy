package com.kyant.backdrop.catalog

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

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

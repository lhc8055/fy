package com.kyant.backdrop.catalog

import androidx.compose.runtime.Composable

@Composable
expect fun rememberShareAppAction(): () -> Unit

@Composable
expect fun rememberOpenFeedbackAction(): () -> Unit

@Composable
expect fun rememberCopyTextAction(): (String) -> Unit

expect suspend fun requestXrayResult(message: String): String

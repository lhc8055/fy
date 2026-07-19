package com.kyant.backdrop.catalog

import androidx.compose.runtime.Composable

@Composable
expect fun rememberShareAppAction(): () -> Unit

@Composable
expect fun rememberOpenFeedbackAction(): () -> Unit

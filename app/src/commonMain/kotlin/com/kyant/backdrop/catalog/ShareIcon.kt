package com.kyant.backdrop.catalog

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

internal val ShareIcon: ImageVector
    get() {
        if (_ShareIcon != null) return _ShareIcon!!

        _ShareIcon = ImageVector.Builder(
            name = "Share",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 960f,
            viewportHeight = 960f
        ).apply {
            // Upward arrow (triangle)
            path(
                fill = SolidColor(Color(0xFF1f1f1f))
            ) {
                moveTo(480f, 160f)
                lineTo(340f, 330f)
                lineTo(620f, 330f)
                close()
            }
            // Arrow shaft (rectangle)
            path(
                fill = SolidColor(Color(0xFF1f1f1f))
            ) {
                moveTo(440f, 300f)
                lineTo(520f, 300f)
                lineTo(520f, 560f)
                lineTo(440f, 560f)
                close()
            }
            // Box/container - left vertical bar
            path(
                fill = SolidColor(Color(0xFF1f1f1f))
            ) {
                moveTo(300f, 480f)
                lineTo(380f, 480f)
                lineTo(380f, 800f)
                lineTo(300f, 800f)
                close()
            }
            // Box/container - bottom horizontal bar
            path(
                fill = SolidColor(Color(0xFF1f1f1f))
            ) {
                moveTo(300f, 740f)
                lineTo(660f, 740f)
                lineTo(660f, 800f)
                lineTo(300f, 800f)
                close()
            }
            // Box/container - right vertical bar
            path(
                fill = SolidColor(Color(0xFF1f1f1f))
            ) {
                moveTo(580f, 480f)
                lineTo(660f, 480f)
                lineTo(660f, 800f)
                lineTo(580f, 800f)
                close()
            }
        }.build()

        return _ShareIcon!!
    }

private var _ShareIcon: ImageVector? = null

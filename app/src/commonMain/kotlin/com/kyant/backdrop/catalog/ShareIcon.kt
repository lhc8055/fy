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
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            // Arrow (upward)
            path(
                fill = SolidColor(Color(0xFF1f1f1f))
            ) {
                moveTo(12f, 3.25f)
                lineTo(6.7f, 8.55f)
                lineTo(8.12f, 9.97f)
                lineTo(11f, 7.09f)
                lineTo(11f, 15f)
                lineTo(13f, 15f)
                lineTo(13f, 7.09f)
                lineTo(15.88f, 9.97f)
                lineTo(17.3f, 8.55f)
                lineTo(12f, 3.25f)
                close()
            }
            // Box/container
            path(
                fill = SolidColor(Color(0xFF1f1f1f))
            ) {
                moveTo(5f, 11f)
                lineTo(7f, 11f)
                lineTo(7f, 18f)
                lineTo(17f, 18f)
                lineTo(17f, 11f)
                lineTo(19f, 11f)
                lineTo(19f, 18.5f)
                curveTo(19f, 19.33f, 18.33f, 20f, 17.5f, 20f)
                lineTo(6.5f, 20f)
                curveTo(5.67f, 20f, 5f, 19.33f, 5f, 18.5f)
                lineTo(5f, 11f)
                close()
            }
        }.build()

        return _ShareIcon!!
    }

private var _ShareIcon: ImageVector? = null

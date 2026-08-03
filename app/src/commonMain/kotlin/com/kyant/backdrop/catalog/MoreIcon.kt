package com.kyant.backdrop.catalog

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

internal val MoreIcon: ImageVector
    get() {
        if (_MoreIcon != null) return _MoreIcon!!

        _MoreIcon = ImageVector.Builder(
            name = "More",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            // Dot 1
            path(
                fill = SolidColor(Color(0xFF1f1f1f))
            ) {
                moveTo(4.2f, 12f)
                curveTo(4.2f, 11.006f, 5.006f, 10.2f, 6f, 10.2f)
                curveTo(6.994f, 10.2f, 7.8f, 11.006f, 7.8f, 12f)
                curveTo(7.8f, 12.994f, 6.994f, 13.8f, 6f, 13.8f)
                curveTo(5.006f, 13.8f, 4.2f, 12.994f, 4.2f, 12f)
                close()
            }
            // Dot 2
            path(
                fill = SolidColor(Color(0xFF1f1f1f))
            ) {
                moveTo(10.2f, 12f)
                curveTo(10.2f, 11.006f, 11.006f, 10.2f, 12f, 10.2f)
                curveTo(12.994f, 10.2f, 13.8f, 11.006f, 13.8f, 12f)
                curveTo(13.8f, 12.994f, 12.994f, 13.8f, 12f, 13.8f)
                curveTo(11.006f, 13.8f, 10.2f, 12.994f, 10.2f, 12f)
                close()
            }
            // Dot 3
            path(
                fill = SolidColor(Color(0xFF1f1f1f))
            ) {
                moveTo(16.2f, 12f)
                curveTo(16.2f, 11.006f, 17.006f, 10.2f, 18f, 10.2f)
                curveTo(18.994f, 10.2f, 19.8f, 11.006f, 19.8f, 12f)
                curveTo(19.8f, 12.994f, 18.994f, 13.8f, 18f, 13.8f)
                curveTo(17.006f, 13.8f, 16.2f, 12.994f, 16.2f, 12f)
                close()
            }
        }.build()

        return _MoreIcon!!
    }

private var _MoreIcon: ImageVector? = null

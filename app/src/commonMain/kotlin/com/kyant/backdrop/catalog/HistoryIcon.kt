package com.kyant.backdrop.catalog

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

internal val HistoryIcon: ImageVector
    get() {
        if (_HistoryIcon != null) return _HistoryIcon!!

        _HistoryIcon = ImageVector.Builder(
            name = "History",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 960f,
            viewportHeight = 960f
        ).apply {
            path(
                fill = SolidColor(Color(0xFF1f1f1f))
            ) {
                moveTo(480f, 840f)
                quadToRelative(-139f, 0f, -239.5f, -97f)
                reflectiveQuadTo(140f, 506f)
                quadToRelative(0f, -17f, 11.5f, -28.5f)
                reflectiveQuadTo(180f, 466f)
                quadToRelative(17f, 0f, 28.5f, 11.5f)
                reflectiveQuadTo(220f, 506f)
                quadToRelative(0f, 107f, 76.5f, 180.5f)
                reflectiveQuadTo(480f, 760f)
                quadToRelative(107f, 0f, 183.5f, -76.5f)
                reflectiveQuadTo(740f, 500f)
                quadToRelative(0f, -107f, -76.5f, -183.5f)
                reflectiveQuadTo(480f, 240f)
                lineTo(480f, 320f)
                lineTo(340f, 180f)
                lineTo(480f, 40f)
                lineTo(480f, 120f)
                quadToRelative(139f, 0f, 239.5f, 100f)
                reflectiveQuadTo(820f, 460f)
                reflectiveQuadTo(720f, 740f)
                reflectiveQuadTo(480f, 840f)
                close()
                moveTo(440f, 640f)
                lineTo(440f, 466f)
                lineTo(560f, 398f)
                lineTo(560f, 572f)
                close()
            }
        }.build()

        return _HistoryIcon!!
    }

private var _HistoryIcon: ImageVector? = null

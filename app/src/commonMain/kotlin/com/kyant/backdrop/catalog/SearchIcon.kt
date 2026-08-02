package com.kyant.backdrop.catalog

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

internal val SearchIcon: ImageVector
    get() {
        if (_SearchIcon != null) return _SearchIcon!!

        _SearchIcon = ImageVector.Builder(
            name = "Search",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 960f,
            viewportHeight = 960f
        ).apply {
            path(
                fill = SolidColor(Color(0xFF1f1f1f))
            ) {
                moveTo(784f, 840f)
                lineTo(532f, 588f)
                quadToRelative(-50f, 36f, -110f, 54f)
                reflectiveQuadTo(290f, 660f)
                quadToRelative(-136f, 0f, -231f, -95f)
                reflectiveQuadTo(-36f, 334f)
                quadToRelative(0f, -136f, 95f, -231f)
                reflectiveQuadTo(290f, 8f)
                quadToRelative(136f, 0f, 231f, 95f)
                reflectiveQuadTo(616f, 334f)
                quadToRelative(0f, 60f, -18f, 120f)
                reflectiveQuadTo(544f, 564f)
                lineTo(796f, 816f)
                close()
                moveTo(290f, 580f)
                quadToRelative(103f, 0f, 174.5f, -71.5f)
                reflectiveQuadTo(536f, 334f)
                quadToRelative(0f, -103f, -71.5f, -174.5f)
                reflectiveQuadTo(290f, 88f)
                quadToRelative(-103f, 0f, -174.5f, 71.5f)
                reflectiveQuadTo(44f, 334f)
                quadToRelative(0f, 103f, 71.5f, 174.5f)
                reflectiveQuadTo(290f, 580f)
                close()
            }
        }.build()

        return _SearchIcon!!
    }

private var _SearchIcon: ImageVector? = null

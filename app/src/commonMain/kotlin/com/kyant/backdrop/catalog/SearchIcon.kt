package com.kyant.backdrop.catalog

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

// Kazumi 右上角实际使用的是 Flutter Material Icons.search
internal val SearchIcon: ImageVector
    get() {
        if (_SearchIcon != null) return _SearchIcon!!

        _SearchIcon = ImageVector.Builder(
            name = "Search",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = SolidColor(Color(0xFF1f1f1f)),
                pathFillType = PathFillType.EvenOdd
            ) {
                moveTo(9.5f, 3f)
                arcTo(6.5f, 6.5f, 0f, true, false, 9.5f, 16f)
                arcTo(6.5f, 6.5f, 0f, true, false, 9.5f, 3f)
                close()
                moveTo(9.5f, 5f)
                arcTo(4.5f, 4.5f, 0f, true, false, 9.5f, 14f)
                arcTo(4.5f, 4.5f, 0f, true, false, 9.5f, 5f)
                close()
                moveTo(14f, 14f)
                lineTo(14.71f, 14f)
                lineTo(20f, 19.29f)
                lineTo(19.29f, 20f)
                lineTo(14f, 14.71f)
                close()
            }
        }.build()

        return _SearchIcon!!
    }

private var _SearchIcon: ImageVector? = null

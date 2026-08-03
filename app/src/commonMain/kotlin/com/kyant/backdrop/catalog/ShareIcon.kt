package com.kyant.backdrop.catalog

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
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
            path(
                fill = null,
                stroke = SolidColor(Color(0xFF1f1f1f)),
                strokeLineWidth = 56f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                // Arrow shaft
                moveTo(480f, 180f)
                lineTo(480f, 560f)
                // Arrowhead left
                moveTo(480f, 180f)
                lineTo(360f, 300f)
                // Arrowhead right
                moveTo(480f, 180f)
                lineTo(600f, 300f)
                // Box left side
                moveTo(300f, 560f)
                lineTo(300f, 800f)
                // Box bottom
                lineTo(660f, 800f)
                // Box right side
                lineTo(660f, 560f)
            }
        }.build()

        return _ShareIcon!!
    }

private var _ShareIcon: ImageVector? = null

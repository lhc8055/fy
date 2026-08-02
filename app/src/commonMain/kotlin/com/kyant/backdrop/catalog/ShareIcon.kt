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
            path(
                fill = SolidColor(Color(0xFF1f1f1f))
            ) {
                moveTo(720f, 840f)
                quadToRelative(-50f, 0f, -85f, -35f)
                reflectiveQuadTo(600f, 720f)
                quadToRelative(0f, -12f, 3f, -28f)
                lineTo(342f, 555f)
                quadToRelative(-16f, 15f, -37f, 23f)
                reflectiveQuadTo(260f, 586f)
                quadToRelative(-50f, 0f, -85f, -35f)
                reflectiveQuadTo(140f, 466f)
                quadToRelative(0f, -50f, 35f, -85f)
                reflectiveQuadTo(260f, 346f)
                quadToRelative(24f, 0f, 45f, 8f)
                reflectiveQuadTo(342f, 377f)
                lineTo(603f, 240f)
                quadToRelative(-3f, -16f, -3f, -28f)
                quadToRelative(0f, -50f, 35f, -85f)
                reflectiveQuadTo(720f, 92f)
                quadToRelative(50f, 0f, 85f, 35f)
                reflectiveQuadTo(840f, 212f)
                quadToRelative(0f, 50f, -35f, 85f)
                reflectiveQuadTo(720f, 332f)
                quadToRelative(-24f, 0f, -45f, -8f)
                reflectiveQuadTo(638f, 301f)
                lineTo(377f, 438f)
                quadToRelative(3f, 16f, 3f, 28f)
                reflectiveQuadTo(377f, 494f)
                lineTo(638f, 631f)
                quadToRelative(16f, -15f, 37f, -23f)
                reflectiveQuadTo(720f, 600f)
                quadToRelative(50f, 0f, 85f, 35f)
                reflectiveQuadTo(840f, 720f)
                quadToRelative(0f, 50f, -35f, 85f)
                reflectiveQuadTo(720f, 840f)
                close()
                moveTo(720f, 760f)
                quadToRelative(17f, 0f, 28.5f, -11.5f)
                reflectiveQuadTo(760f, 720f)
                quadToRelative(0f, -17f, -11.5f, -28.5f)
                reflectiveQuadTo(720f, 680f)
                quadToRelative(-17f, 0f, -28.5f, 11.5f)
                reflectiveQuadTo(680f, 720f)
                quadToRelative(0f, 17f, 11.5f, 28.5f)
                reflectiveQuadTo(720f, 760f)
                close()
                moveTo(260f, 506f)
                quadToRelative(17f, 0f, 28.5f, -11.5f)
                reflectiveQuadTo(300f, 466f)
                quadToRelative(0f, -17f, -11.5f, -28.5f)
                reflectiveQuadTo(260f, 426f)
                quadToRelative(-17f, 0f, -28.5f, 11.5f)
                reflectiveQuadTo(220f, 466f)
                quadToRelative(0f, 17f, 11.5f, 28.5f)
                reflectiveQuadTo(260f, 506f)
                close()
                moveTo(720f, 252f)
                quadToRelative(17f, 0f, 28.5f, -11.5f)
                reflectiveQuadTo(760f, 212f)
                quadToRelative(0f, -17f, -11.5f, -28.5f)
                reflectiveQuadTo(720f, 172f)
                quadToRelative(-17f, 0f, -28.5f, 11.5f)
                reflectiveQuadTo(680f, 212f)
                quadToRelative(0f, 17f, 11.5f, 28.5f)
                reflectiveQuadTo(720f, 252f)
                close()
            }
        }.build()

        return _ShareIcon!!
    }

private var _ShareIcon: ImageVector? = null

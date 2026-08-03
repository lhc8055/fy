package com.kyant.backdrop.catalog

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

private val menuIconColor = Color(0xFF1f1f1f)

internal val SettingsMenuIcon: ImageVector
    get() {
        if (_SettingsMenuIcon != null) return _SettingsMenuIcon!!

        _SettingsMenuIcon = ImageVector.Builder(
            name = "SettingsMenu",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(menuIconColor)) {
                // Gear shape - simplified
                moveTo(12f, 8f)
                arcTo(4f, 4f, 0f, true, false, 12f, 16f)
                arcTo(4f, 4f, 0f, true, false, 12f, 8f)
                close()
                // Gear teeth (simplified as a circle outline approach)
                moveTo(12f, 2f)
                lineTo(12.5f, 5f)
                arcTo(7f, 7f, 0f, false, true, 16.95f, 7.05f)
                lineTo(19.5f, 5.5f)
                lineTo(20.5f, 6.5f)
                lineTo(19f, 9.05f)
                arcTo(7f, 7f, 0f, false, true, 19f, 14.95f)
                lineTo(20.5f, 17.5f)
                lineTo(19.5f, 18.5f)
                lineTo(16.95f, 17f)
                arcTo(7f, 7f, 0f, false, true, 12.5f, 19f)
                lineTo(12f, 22f)
                lineTo(11.5f, 19f)
                arcTo(7f, 7f, 0f, false, true, 7.05f, 17f)
                lineTo(4.5f, 18.5f)
                lineTo(3.5f, 17.5f)
                lineTo(5f, 14.95f)
                arcTo(7f, 7f, 0f, false, true, 5f, 9.05f)
                lineTo(3.5f, 6.5f)
                lineTo(4.5f, 5.5f)
                lineTo(7.05f, 7.05f)
                arcTo(7f, 7f, 0f, false, true, 11.5f, 5f)
                lineTo(12f, 2f)
                close()
            }
        }.build()

        return _SettingsMenuIcon!!
    }

private var _SettingsMenuIcon: ImageVector? = null

internal val ShareMenuIcon: ImageVector
    get() {
        if (_ShareMenuIcon != null) return _ShareMenuIcon!!

        _ShareMenuIcon = ImageVector.Builder(
            name = "ShareMenu",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            // Three dots connected by lines (share icon)
            path(fill = SolidColor(menuIconColor)) {
                moveTo(18f, 16f)
                arcTo(2f, 2f, 0f, true, false, 18f, 20f)
                arcTo(2f, 2f, 0f, true, false, 18f, 16f)
                close()
            }
            path(fill = SolidColor(menuIconColor)) {
                moveTo(6f, 10f)
                arcTo(2f, 2f, 0f, true, false, 6f, 14f)
                arcTo(2f, 2f, 0f, true, false, 6f, 10f)
                close()
            }
            path(fill = SolidColor(menuIconColor)) {
                moveTo(18f, 4f)
                arcTo(2f, 2f, 0f, true, false, 18f, 8f)
                arcTo(2f, 2f, 0f, true, false, 18f, 4f)
                close()
            }
            path(fill = SolidColor(menuIconColor)) {
                moveTo(8f, 11f)
                lineTo(16f, 5f)
                lineTo(15f, 4f)
                lineTo(7f, 10f)
                close()
            }
            path(fill = SolidColor(menuIconColor)) {
                moveTo(8f, 13f)
                lineTo(16f, 19f)
                lineTo(15f, 20f)
                lineTo(7f, 14f)
                close()
            }
        }.build()

        return _ShareMenuIcon!!
    }

private var _ShareMenuIcon: ImageVector? = null

internal val InfoMenuIcon: ImageVector
    get() {
        if (_InfoMenuIcon != null) return _InfoMenuIcon!!

        _InfoMenuIcon = ImageVector.Builder(
            name = "InfoMenu",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            // Circle outline
            path(fill = SolidColor(menuIconColor)) {
                moveTo(12f, 2f)
                arcTo(10f, 10f, 0f, true, false, 12f, 22f)
                arcTo(10f, 10f, 0f, true, false, 12f, 2f)
                close()
            }
            // "i" dot
            path(fill = SolidColor(Color.White)) {
                moveTo(12f, 7f)
                arcTo(1.5f, 1.5f, 0f, true, false, 12f, 10f)
                arcTo(1.5f, 1.5f, 0f, true, false, 12f, 7f)
                close()
            }
            // "i" stem
            path(fill = SolidColor(Color.White)) {
                moveTo(10.5f, 11f)
                lineTo(13.5f, 11f)
                lineTo(13.5f, 18f)
                lineTo(10.5f, 18f)
                close()
            }
        }.build()

        return _InfoMenuIcon!!
    }

private var _InfoMenuIcon: ImageVector? = null

internal val CloseMenuIcon: ImageVector
    get() {
        if (_CloseMenuIcon != null) return _CloseMenuIcon!!

        _CloseMenuIcon = ImageVector.Builder(
            name = "CloseMenu",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            // X shape
            path(fill = SolidColor(menuIconColor)) {
                moveTo(6f, 6f)
                lineTo(18f, 18f)
                lineTo(16f, 20f)
                lineTo(4f, 8f)
                close()
            }
            path(fill = SolidColor(menuIconColor)) {
                moveTo(18f, 6f)
                lineTo(20f, 8f)
                lineTo(8f, 20f)
                lineTo(6f, 18f)
                close()
            }
        }.build()

        return _CloseMenuIcon!!
    }

private var _CloseMenuIcon: ImageVector? = null

package com.kyant.backdrop.catalog

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

private val iconColor = Color(0xFF1f1f1f)

// ========== 发现 - 罗盘指南针 (从参考APK提取) ==========
// viewport: 960×960
internal val DiscoverIcon: ImageVector
    get() {
        if (_DiscoverIcon != null) return _DiscoverIcon!!

        _DiscoverIcon = ImageVector.Builder(
            name = "Discover",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 960f,
            viewportHeight = 960f
        ).apply {
            // 外圆环 + 内圆镂空 + 中心支点孔 (EvenOdd 三层)
            path(
                fill = SolidColor(iconColor),
                pathFillType = PathFillType.EvenOdd
            ) {
                // 外圆
                moveTo(480f, 100f)
                quadTo(400f, 100f, 330f, 130f)
                quadTo(260f, 160f, 208f, 212f)
                quadTo(156f, 264f, 126f, 334f)
                quadTo(96f, 404f, 96f, 480f)
                quadTo(96f, 556f, 126f, 626f)
                quadTo(156f, 696f, 208f, 748f)
                quadTo(260f, 800f, 330f, 830f)
                quadTo(400f, 860f, 480f, 860f)
                quadTo(560f, 860f, 630f, 830f)
                quadTo(700f, 800f, 752f, 748f)
                quadTo(804f, 696f, 834f, 626f)
                quadTo(864f, 556f, 864f, 480f)
                quadTo(864f, 404f, 834f, 334f)
                quadTo(804f, 264f, 752f, 212f)
                quadTo(700f, 160f, 630f, 130f)
                quadTo(560f, 100f, 480f, 100f)
                close()
                // 内圆镂空
                moveTo(480f, 206f)
                quadTo(594f, 206f, 674f, 286f)
                quadTo(754f, 366f, 754f, 480f)
                quadTo(754f, 594f, 674f, 674f)
                quadTo(594f, 754f, 480f, 754f)
                quadTo(366f, 754f, 286f, 674f)
                quadTo(206f, 594f, 206f, 480f)
                quadTo(206f, 366f, 286f, 286f)
                quadTo(366f, 206f, 480f, 206f)
                close()
            }
            // 指针菱形 + 中心支点孔
            path(
                fill = SolidColor(iconColor),
                pathFillType = PathFillType.EvenOdd
            ) {
                moveTo(640f, 320f)
                lineTo(548f, 548f)
                lineTo(320f, 640f)
                lineTo(412f, 412f)
                close()
                // 从指针中心扣掉一个小孔，像标签孔/指南针转轴
                moveTo(480f, 512f)
                quadTo(466f, 512f, 456f, 502f)
                quadTo(446f, 492f, 446f, 480f)
                quadTo(446f, 466f, 456f, 456f)
                quadTo(466f, 446f, 480f, 446f)
                quadTo(494f, 446f, 504f, 456f)
                quadTo(514f, 466f, 514f, 480f)
                quadTo(514f, 492f, 504f, 502f)
                quadTo(494f, 512f, 480f, 512f)
                close()
            }
        }.build()

        return _DiscoverIcon!!
    }

private var _DiscoverIcon: ImageVector? = null

internal val DiscoverIconFilled: ImageVector
    get() = DiscoverIcon

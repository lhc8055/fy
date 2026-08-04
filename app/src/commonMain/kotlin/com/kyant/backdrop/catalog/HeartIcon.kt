package com.kyant.backdrop.catalog

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

private val iconColor = Color(0xFF1f1f1f)

// ========== 追剧 - 小电视播放屏 (原创设计，匹配参考APK风格) ==========
// viewport: 960×960，与发现/分类/我的统一坐标系
internal val HeartIcon: ImageVector
    get() {
        if (_HeartIcon != null) return _HeartIcon!!

        _HeartIcon = ImageVector.Builder(
            name = "Follow",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 960f,
            viewportHeight = 960f
        ).apply {
            path(
                fill = SolidColor(iconColor),
                pathFillType = PathFillType.EvenOdd
            ) {
                // 圆角电视外壳
                moveTo(150f, 258f)
                quadTo(150f, 218f, 190f, 218f)
                lineTo(770f, 218f)
                quadTo(810f, 218f, 810f, 258f)
                lineTo(810f, 638f)
                quadTo(810f, 678f, 770f, 678f)
                lineTo(190f, 678f)
                quadTo(150f, 678f, 150f, 638f)
                lineTo(150f, 258f)
                close()
                // 播放三角镂空
                moveTo(420f, 358f)
                lineTo(590f, 448f)
                lineTo(420f, 538f)
                close()
            }
            // 底部短脚，增强“小电视”识别度，但保持轻量
            path(fill = SolidColor(iconColor)) {
                moveTo(356f, 724f)
                quadTo(356f, 700f, 380f, 700f)
                lineTo(580f, 700f)
                quadTo(604f, 700f, 604f, 724f)
                quadTo(604f, 748f, 580f, 748f)
                lineTo(380f, 748f)
                quadTo(356f, 748f, 356f, 724f)
                close()
            }
        }.build()

        return _HeartIcon!!
    }

private var _HeartIcon: ImageVector? = null

internal val HeartIconFilled: ImageVector
    get() = HeartIcon

package com.kyant.backdrop.catalog

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

private val iconColor = Color(0xFF1f1f1f)

// ========== 分类 - 文件夹 (从参考APK提取) ==========
// viewport: 960×960
internal val CategoryIcon: ImageVector
    get() {
        if (_CategoryIcon != null) return _CategoryIcon!!

        _CategoryIcon = ImageVector.Builder(
            name = "Category",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 960f,
            viewportHeight = 960f
        ).apply {
            path(fill = SolidColor(iconColor)) {
                moveTo(148f, 236f)
                quadTo(120f, 236f, 101f, 255f)
                quadTo(82f, 274f, 82f, 302f)
                lineTo(82f, 704f)
                quadTo(82f, 732f, 101f, 751f)
                quadTo(120f, 770f, 148f, 770f)
                lineTo(812f, 770f)
                quadTo(840f, 770f, 859f, 751f)
                quadTo(878f, 732f, 878f, 704f)
                lineTo(878f, 392f)
                quadTo(878f, 364f, 859f, 345f)
                quadTo(840f, 326f, 812f, 326f)
                lineTo(508f, 326f)
                quadTo(490f, 326f, 477f, 313f)
                lineTo(417f, 253f)
                quadTo(404f, 236f, 380f, 236f)
                lineTo(148f, 236f)
                close()
            }
        }.build()

        return _CategoryIcon!!
    }

private var _CategoryIcon: ImageVector? = null

internal val CategoryIconFilled: ImageVector
    get() = CategoryIcon

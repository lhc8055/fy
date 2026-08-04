package com.kyant.backdrop.catalog

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

private val iconColor = Color(0xFF1f1f1f)

// ========== 我的 - 人物头像 (从参考APK提取) ==========
// viewport: 960×960
internal val ProfileIcon: ImageVector
    get() {
        if (_ProfileIcon != null) return _ProfileIcon!!

        _ProfileIcon = ImageVector.Builder(
            name = "Profile",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 960f,
            viewportHeight = 960f
        ).apply {
            // 外圆 + 头部镂空 + 肩部镂空 (EvenOdd)
            path(fill = SolidColor(iconColor)) {
                moveTo(480f, 110f)
                quadTo(403f, 110f, 335.5f, 139f)
                quadTo(268f, 168f, 218.5f, 218f)
                quadTo(169f, 268f, 140.5f, 335.5f)
                quadTo(112f, 403f, 112f, 480f)
                quadTo(112f, 557f, 140.5f, 624.5f)
                quadTo(169f, 692f, 218.5f, 742f)
                quadTo(268f, 792f, 335.5f, 821f)
                quadTo(403f, 850f, 480f, 850f)
                quadTo(557f, 850f, 624.5f, 821f)
                quadTo(692f, 792f, 741.5f, 742f)
                quadTo(791f, 692f, 819.5f, 624.5f)
                quadTo(848f, 557f, 848f, 480f)
                quadTo(848f, 403f, 819.5f, 335.5f)
                quadTo(791f, 268f, 741.5f, 218f)
                quadTo(692f, 168f, 624.5f, 139f)
                quadTo(557f, 110f, 480f, 110f)
                close()
                moveTo(480f, 238f)
                quadTo(540f, 238f, 579f, 277f)
                quadTo(618f, 316f, 618f, 376f)
                quadTo(618f, 436f, 579f, 475f)
                quadTo(540f, 514f, 480f, 514f)
                quadTo(420f, 514f, 381f, 475f)
                quadTo(342f, 436f, 342f, 376f)
                quadTo(342f, 316f, 381f, 277f)
                quadTo(420f, 238f, 480f, 238f)
                close()
                moveTo(480f, 760f)
                quadTo(400f, 760f, 336f, 730f)
                quadTo(272f, 700f, 230f, 648f)
                quadTo(264f, 607f, 327f, 584f)
                quadTo(390f, 560f, 480f, 560f)
                quadTo(570f, 560f, 633f, 584f)
                quadTo(696f, 607f, 730f, 648f)
                quadTo(688f, 700f, 624f, 730f)
                quadTo(560f, 760f, 480f, 760f)
                close()
            }
        }.build()

        return _ProfileIcon!!
    }

private var _ProfileIcon: ImageVector? = null

internal val ProfileIconFilled: ImageVector
    get() = ProfileIcon

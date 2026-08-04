package com.kyant.backdrop.catalog.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.text.BasicText
import androidx.compose.ui.draw.paint
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy

// iOS-style spring animation specs from Compose-Symphony
// https://github.com/jay3-yy/Compose-Symphony
private val PopupEnterSpec = spring<Float>(
    dampingRatio = 0.65f,  // Medium bounce for elastic entrance
    stiffness = 350f
)

// Exit: slight bounce, faster than enter
private val PopupExitScaleSpec = spring<Float>(
    dampingRatio = 0.9f,
    stiffness = 500f
)

private val PopupExitAlphaSpec = tween<Float>(250)

private val ItemEnterSpec = spring<Float>(
    dampingRatio = 0.7f,
    stiffness = 300f
)

data class PopupMenuItem(
    val icon: ImageVector? = null,
    val label: String,
    val onClick: () -> Unit
)

@Composable
fun LiquidGlassPopup(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    onExitComplete: () -> Unit = {},
    modifier: Modifier = Modifier,
    backdrop: Backdrop? = null,
    items: List<PopupMenuItem> = emptyList()
) {
    val scaleAnim = remember { Animatable(0f) }
    val alphaAnim = remember { Animatable(0f) }

    // Track first composition to skip exit animation on initial load
    var hasBeenExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(expanded) {
        if (expanded) {
            // Entrance: fade in first, then scale with bounce
            hasBeenExpanded = true
            alphaAnim.animateTo(1f, tween(100))
            scaleAnim.animateTo(1f, PopupEnterSpec)
        } else if (hasBeenExpanded) {
            // 退出时朝左上角收缩，同时淡出
            scaleAnim.animateTo(0f, PopupExitScaleSpec)
            alphaAnim.animateTo(0f, PopupExitAlphaSpec)
            onExitComplete()
        }
    }

    if (scaleAnim.value > 0.01f) {
        Box(
            modifier
                .fillMaxSize()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismissRequest
                ),
            contentAlignment = Alignment.TopStart
        ) {
            Column(
                Modifier
                    .graphicsLayer {
                        scaleX = scaleAnim.value
                        scaleY = scaleAnim.value
                        alpha = alphaAnim.value
                        transformOrigin = TransformOrigin(0f, 0f)
                    }
                    .padding(start = 16f.dp, top = 60f.dp)
                    .then(
                        if (backdrop != null) {
                            Modifier.drawBackdrop(
                                backdrop = backdrop,
                                shape = { RoundedCornerShape(20f.dp) },
                                effects = {
                                    vibrancy()
                                    blur(12f.dp.toPx())
                                    lens(30f.dp.toPx(), 30f.dp.toPx())
                                },
                                onDrawSurface = { drawRect(Color.Transparent) }
                            )
                        } else {
                            Modifier.background(
                                Color.White.copy(alpha = 0.12f),
                                RoundedCornerShape(20f.dp)
                            )
                        }
                    )
                    .clip(RoundedCornerShape(20f.dp))
                    .padding(vertical = 6f.dp),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.spacedBy(0f.dp)
            ) {
                items.forEachIndexed { index, item ->
                    PopupItemRow(
                        item = item,
                        index = index,
                        onDismiss = onDismissRequest
                    )
                }
            }
        }
    }
}

@Composable
private fun PopupItemRow(
    item: PopupMenuItem,
    index: Int,
    onDismiss: () -> Unit
) {
    val itemScale = remember { Animatable(0f) }
    val itemAlpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay((index * 40).toLong())
        itemAlpha.animateTo(1f, tween(120))
        itemScale.animateTo(1f, ItemEnterSpec)
    }

    val contentColor = Color.White

    Row(
        Modifier
            .graphicsLayer {
                scaleX = itemScale.value
                scaleY = itemScale.value
                alpha = itemAlpha.value
                transformOrigin = TransformOrigin(0f, 0.5f)
            }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                role = Role.Button,
                onClick = {
                    item.onClick()
                    onDismiss()
                }
            )
            .padding(horizontal = 16f.dp, vertical = 10f.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10f.dp)
    ) {
        if (item.icon != null) {
            val iconPainter = rememberVectorPainter(item.icon)
            Box(
                Modifier
                    .size(20f.dp)
                    .paint(
                        iconPainter,
                        colorFilter = ColorFilter.tint(contentColor)
                    )
            )
        }
        BasicText(
            item.label,
            style = TextStyle(
                color = contentColor,
                fontSize = 15f.sp,
                fontWeight = FontWeight.Medium
            )
        )
    }
}

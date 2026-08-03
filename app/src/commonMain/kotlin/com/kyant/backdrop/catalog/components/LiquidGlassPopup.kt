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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
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
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

// iOS-style spring animation specs from Compose-Symphony
private val PopupEnterSpec = spring<Float>(
    dampingRatio = 0.65f,
    stiffness = 350f
)

private val PopupExitSpec = spring<Float>(
    dampingRatio = 0.8f,
    stiffness = 400f
)

private val ItemEnterSpec = spring<Float>(
    dampingRatio = 0.7f,
    stiffness = 300f
)

data class PopupMenuItem(
    val icon: ImageVector? = null,
    val label: String,
    val onClick: () -> Unit
)

// Particle data for dissolve effect
private data class Particle(
    val x: Float,
    val y: Float,
    val vx: Float,
    val vy: Float,
    val delay: Float,
    val size: Float
)

@Composable
fun LiquidGlassPopup(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    onDissolveComplete: () -> Unit = {},
    modifier: Modifier = Modifier,
    backdrop: Backdrop? = null,
    items: List<PopupMenuItem> = emptyList()
) {
    val scaleAnim = remember { Animatable(0f) }
    val alphaAnim = remember { Animatable(0f) }

    // Track whether we're in the dissolving phase
    var isDissolving by remember { mutableStateOf(false) }
    val dissolveProgress = remember { Animatable(0f) }

    // Generate particles for dissolve effect
    val particles = remember {
        List(60) { index ->
            val angle = (index / 60f) * 2f * Math.PI.toFloat()
            val speed = 80f + Random.nextFloat() * 120f
            Particle(
                x = 0f,
                y = 0f,
                vx = cos(angle) * speed,
                vy = sin(angle) * speed - 50f, // bias upward
                delay = Random.nextFloat() * 0.15f,
                size = 2f + Random.nextFloat() * 3f
            )
        }
    }

    LaunchedEffect(expanded) {
        if (expanded) {
            // Entrance: fade in first, then scale with bounce
            isDissolving = false
            dissolveProgress.snapTo(0f)
            alphaAnim.animateTo(1f, tween(100))
            scaleAnim.animateTo(1f, PopupEnterSpec)
        } else {
            // Exit: start dissolve animation
            isDissolving = true
            // Quick scale down + fade
            scaleAnim.animateTo(0.3f, spring(dampingRatio = 0.6f, stiffness = 500f))
            alphaAnim.animateTo(0f, tween(150))
            // Particle dissolve
            dissolveProgress.animateTo(1f, tween(600))
            isDissolving = false
            scaleAnim.snapTo(0f)
            onDissolveComplete()
        }
    }

    if ((expanded || isDissolving) && scaleAnim.value > 0.01f || isDissolving) {
        Box(
            modifier
                .fillMaxSize()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismissRequest
                ),
            contentAlignment = Alignment.TopEnd
        ) {
            // Particle dissolve overlay
            if (isDissolving) {
                Box(
                    Modifier
                        .fillMaxSize()
                        .drawBehind {
                            val progress = dissolveProgress.value
                            if (progress > 0f) {
                                // Estimate popup center (top-right area)
                                val centerX = size.width - 16f.dp.toPx() - 60f.dp.toPx()
                                val centerY = 60f.dp.toPx() + 80f.dp.toPx()

                                particles.forEach { p ->
                                    val localProgress = ((progress - p.delay) / (1f - p.delay)).coerceIn(0f, 1f)
                                    if (localProgress > 0f) {
                                        // Apply gravity and wind (from Compose-Symphony ParticleRenderer)
                                        val t = localProgress * 0.6f
                                        val px = centerX + p.vx * t
                                        val py = centerY + p.vy * t + 0.5f * 400f * t * t // gravity
                                        val alpha = (1f - localProgress).coerceIn(0f, 1f)
                                        val particleSize = p.size * (1f - localProgress * 0.5f)

                                        drawCircle(
                                            color = Color.White.copy(alpha = alpha * 0.8f),
                                            radius = particleSize,
                                            center = Offset(px, py)
                                        )
                                    }
                                }
                            }
                        }
                )
            }

            Column(
                Modifier
                    .graphicsLayer {
                        scaleX = scaleAnim.value
                        scaleY = scaleAnim.value
                        alpha = alphaAnim.value
                        transformOrigin = TransformOrigin(1f, 0f)
                    }
                    .padding(end = 16f.dp, top = 60f.dp)
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
                transformOrigin = TransformOrigin(1f, 0.5f)
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

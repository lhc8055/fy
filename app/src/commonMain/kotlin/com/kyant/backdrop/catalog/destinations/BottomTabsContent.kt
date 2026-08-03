package com.kyant.backdrop.catalog.destinations

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.paint
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kyant.backdrop.catalog.BackdropDemoScaffold
import com.kyant.backdrop.catalog.Block
import com.kyant.backdrop.catalog.CloseMenuIcon
import com.kyant.backdrop.catalog.FlightIcon
import com.kyant.backdrop.catalog.InfoMenuIcon
import com.kyant.backdrop.catalog.MoreIcon
import com.kyant.backdrop.catalog.SettingsMenuIcon
import com.kyant.backdrop.catalog.ShareIcon
import com.kyant.backdrop.catalog.ShareMenuIcon
import com.kyant.backdrop.catalog.components.LiquidBottomTab
import com.kyant.backdrop.catalog.components.LiquidBottomTabs
import com.kyant.backdrop.catalog.components.LiquidGlassPopup
import com.kyant.backdrop.catalog.components.PopupMenuItem
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy
import com.kyant.shapes.Capsule

// Spring entrance spec for button bar (from Compose-Symphony bounceIn)
// https://github.com/jay3-yy/Compose-Symphony
private val ButtonEnterScaleSpec = spring<Float>(
    dampingRatio = 0.5f,   // Noticeable overshoot for bounce
    stiffness = 350f
)
private val ButtonEnterAlphaSpec = tween<Float>(200)

@Composable
fun BottomTabsContent() {
    val contentColor = Color.White

    val airplaneModeIcon = rememberVectorPainter(FlightIcon)
    val shareIcon = rememberVectorPainter(ShareIcon)
    val moreIcon = rememberVectorPainter(MoreIcon)
    val iconColorFilter = ColorFilter.tint(contentColor)

    val containerColor = Color.Transparent

    // Popup state
    var popupExpanded by remember { mutableStateOf(false) }
    // Controls when the top-right buttons are visible
    var buttonsVisible by remember { mutableStateOf(true) }

    // Entrance animation for the button bar (Compose-Symphony bounceIn style)
    val buttonScale = remember { Animatable(1f) }
    val buttonAlpha = remember { Animatable(1f) }

    // Trigger entrance animation when buttons become visible again
    LaunchedEffect(buttonsVisible) {
        if (buttonsVisible) {
            // Start from small + transparent, bounce to full size
            buttonScale.snapTo(0.3f)
            buttonAlpha.snapTo(0f)
            // Alpha fades in first, then scale bounces with overshoot
            buttonAlpha.animateTo(1f, ButtonEnterAlphaSpec)
            buttonScale.animateTo(1f, ButtonEnterScaleSpec)
        }
    }

    val menuItems = remember {
        listOf(
            PopupMenuItem(
                icon = SettingsMenuIcon,
                label = "Settings",
                onClick = { }
            ),
            PopupMenuItem(
                icon = ShareMenuIcon,
                label = "Share",
                onClick = { }
            ),
            PopupMenuItem(
                icon = InfoMenuIcon,
                label = "About",
                onClick = { }
            ),
            PopupMenuItem(
                icon = CloseMenuIcon,
                label = "Close",
                onClick = { }
            )
        )
    }

    BackdropDemoScaffold { backdrop ->
        BoxWithConstraints(Modifier.fillMaxSize()) {
            // Adaptive spacing: ~2.5% of screen height, clamped to 12-36dp
            val screenHeightPx = constraints.maxHeight.toFloat()
            val density = LocalDensity.current
            val adaptiveBottomPadding = with(density) {
                (screenHeightPx * 0.025f).coerceIn(
                    with(density) { 12f.dp.toPx() },
                    with(density) { 36f.dp.toPx() }
                ).toDp()
            }

            // Top-right share + more button (hidden when popup is open)
            if (buttonsVisible) {
                Row(
                    Modifier
                        .align(Alignment.TopEnd)
                        .statusBarsPadding()
                        .padding(end = 16f.dp, top = 8f.dp)
                        .height(48f.dp)
                        .graphicsLayer {
                            scaleX = buttonScale.value
                            scaleY = buttonScale.value
                            alpha = buttonAlpha.value
                            // Pivot from top-right corner
                            transformOrigin = TransformOrigin(1f, 0.5f)
                        }
                        .drawBackdrop(
                            backdrop = backdrop,
                            shape = { Capsule() },
                            effects = {
                                vibrancy()
                                blur(8f.dp.toPx())
                                lens(20f.dp.toPx(), 20f.dp.toPx())
                            },
                            onDrawSurface = { drawRect(containerColor) }
                        )
                        .padding(horizontal = 6f.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(0f.dp)
                ) {
                    // Share button
                    Box(
                        Modifier
                            .size(40f.dp)
                            .clickable(
                                interactionSource = null,
                                indication = null,
                                role = Role.Button,
                                onClick = { }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            Modifier
                                .size(22f.dp)
                                .paint(shareIcon, colorFilter = iconColorFilter)
                        )
                    }
                    // Divider
                    Spacer(
                        Modifier
                            .width(1f.dp)
                            .height(22f.dp)
                    )
                    // More button
                    Box(
                        Modifier
                            .size(40f.dp)
                            .clickable(
                                interactionSource = null,
                                indication = null,
                                role = Role.Button,
                                onClick = {
                                    popupExpanded = true
                                    buttonsVisible = false
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            Modifier
                                .size(22f.dp)
                                .paint(moreIcon, colorFilter = iconColorFilter)
                        )
                    }
                }
            }

            // Liquid glass popup menu (iOS 26 style with spring animations)
            LiquidGlassPopup(
                expanded = popupExpanded,
                onDismissRequest = { popupExpanded = false },
                onExitComplete = { buttonsVisible = true },
                backdrop = backdrop,
                items = menuItems
            )

            // Bottom dock
            Column(
                Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Bottom,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Block {
                    var selectedTabIndex by rememberSaveable { mutableIntStateOf(0) }

                    LiquidBottomTabs(
                        selectedTabIndex = { selectedTabIndex },
                        onTabSelected = { selectedTabIndex = it },
                        backdrop = backdrop,
                        tabsCount = 4,
                        modifier = Modifier.padding(horizontal = 36f.dp)
                    ) {
                        repeat(4) { index ->
                            LiquidBottomTab({ selectedTabIndex = index }) {
                                Box(
                                    Modifier
                                        .size(28f.dp)
                                        .paint(airplaneModeIcon, colorFilter = iconColorFilter)
                                )
                                BasicText(
                                    "Tab ${index + 1}",
                                    style = TextStyle(contentColor, 12f.sp)
                                )
                            }
                        }
                    }
                }
                Spacer(Modifier.height(adaptiveBottomPadding).navigationBarsPadding())
            }
        }
    }
}

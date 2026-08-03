package com.kyant.backdrop.catalog.destinations

import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.paint
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kyant.backdrop.catalog.BackdropDemoScaffold
import com.kyant.backdrop.catalog.Block
import com.kyant.backdrop.catalog.FlightIcon
import com.kyant.backdrop.catalog.MoreIcon
import com.kyant.backdrop.catalog.ShareIcon
import com.kyant.backdrop.catalog.components.LiquidBottomTab
import com.kyant.backdrop.catalog.components.LiquidBottomTabs
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy
import com.kyant.shapes.Capsule

@Composable
fun BottomTabsContent() {
    val contentColor = Color.White

    val airplaneModeIcon = rememberVectorPainter(FlightIcon)
    val shareIcon = rememberVectorPainter(ShareIcon)
    val moreIcon = rememberVectorPainter(MoreIcon)
    val iconColorFilter = ColorFilter.tint(contentColor)

    val containerColor = Color.Transparent

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

            // Top-right share + more button
            Row(
                Modifier
                    .align(Alignment.TopEnd)
                    .statusBarsPadding()
                    .padding(end = 16f.dp, top = 8f.dp)
                    .height(48f.dp)
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
                            onClick = { }
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

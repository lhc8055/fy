package com.kyant.backdrop.catalog

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import com.kyant.backdrop.backdrops.LayerBackdrop
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import glass.app.generated.resources.Res
import glass.app.generated.resources.bg_1
import glass.app.generated.resources.bg_2
import glass.app.generated.resources.bg_3
import glass.app.generated.resources.bg_4
import org.jetbrains.compose.resources.painterResource

private val backgroundImages = listOf(
    Res.drawable.bg_1,
    Res.drawable.bg_2,
    Res.drawable.bg_3,
    Res.drawable.bg_4
)

@Composable
actual fun BackdropDemoScaffold(
    modifier: Modifier,
    content: @Composable BoxScope.(backdrop: LayerBackdrop) -> Unit
) {
    Box(
        Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        val backdrop = rememberLayerBackdrop()
        var currentBgIndex by rememberSaveable { mutableIntStateOf(0) }

        Image(
            painterResource(backgroundImages[currentBgIndex]),
            null,
            Modifier
                .layerBackdrop(backdrop)
                .then(modifier)
                .fillMaxSize()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {
                        currentBgIndex = (currentBgIndex + 1) % backgroundImages.size
                    }
                ),
            contentScale = ContentScale.Crop
        )

        content(backdrop)
    }
}

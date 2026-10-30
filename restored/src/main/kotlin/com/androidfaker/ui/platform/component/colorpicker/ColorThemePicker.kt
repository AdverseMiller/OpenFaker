package com.androidfaker.ui.platform.component.colorpicker

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.materialkolor.PaletteStyle
import com.materialkolor.dynamicColorScheme
import com.materialkolor.dynamiccolor.ColorSpec
import com.materialkolor.hct.Hct
import com.materialkolor.scheme.DynamicScheme

internal object PaletteCatalog {
    const val DEFAULT_SEED = -13866427
    val styles = listOf(0, 1, 2, 3)
    val seeds: List<Int> = listOf(DEFAULT_SEED) +
        ((4..10) + (1..3)).drop(1).map { Hct.from(it * 35.0, 40.0, 40.0).toInt() }

    fun style(index: Int): PaletteStyle = when (index) {
        1 -> PaletteStyle.Neutral
        2 -> PaletteStyle.FruitSalad
        3 -> PaletteStyle.Vibrant
        4 -> PaletteStyle.Monochrome
        else -> PaletteStyle.TonalSpot
    }

    fun initialPage(seed: Int, style: Int): Int =
        if (style == 4) seeds.size else seeds.indexOf(seed).coerceAtLeast(0)
}

@Composable
fun ColorThemePicker(seedColor: Int, styleIndex: Int, dynamicColors: Boolean,
    onColorSelected: (Int, Int) -> Unit) {
    val initialPage = remember(seedColor, styleIndex) { PaletteCatalog.initialPage(seedColor, styleIndex) }
    // Like the APK, preference changes do not force an already remembered pager to jump.
    val pager = rememberPagerState(initialPage = initialPage) { PaletteCatalog.seeds.size + 1 }
    Column(Modifier.padding(top = 4.dp, bottom = 12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        HorizontalPager(pager, Modifier.fillMaxWidth().testTag("PalettePager"),
            contentPadding = PaddingValues(horizontal = 12.dp)) { page ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Top) {
                if (page < PaletteCatalog.seeds.size) {
                    val seed = PaletteCatalog.seeds[page]
                    PaletteCatalog.styles.forEach { style ->
                        ColorButton(seed, style, !dynamicColors && seedColor == seed && styleIndex == style) {
                            onColorSelected(seed, style)
                        }
                    }
                } else {
                    ColorButton(0xff000000.toInt(), 4,
                        !dynamicColors && styleIndex == 4) { onColorSelected(0xff000000.toInt(), 4) }
                }
            }
        }
        Row(Modifier.align(Alignment.CenterHorizontally).padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
            repeat(pager.pageCount) { page ->
                val size by animateDpAsState(if (page == pager.currentPage) 8.dp else 6.dp, label = "dot")
                Surface(Modifier.size(size), CircleShape, color = if (page == pager.currentPage)
                    MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant) {}
            }
        }
    }
}

@Composable
private fun RowScope.ColorButton(seed: Int, style: Int, isSelected: Boolean, onClick: () -> Unit) {
    val scheme = remember(seed, style) {
        dynamicColorScheme(seedColor = Color(seed), isDark = false, style = PaletteCatalog.style(style),
            contrastLevel = 0.0, specVersion = ColorSpec.SpecVersion.SPEC_2021,
            platform = DynamicScheme.Platform.PHONE)
    }
    val checkBackground by animateDpAsState(if (isSelected) 28.dp else 0.dp, label = "check-bg")
    val checkIcon by animateDpAsState(if (isSelected) 16.dp else 0.dp, label = "check-icon")
    Surface(onClick = onClick,
        modifier = Modifier.padding(4.dp).sizeIn(64.dp, 64.dp, 80.dp, 80.dp)
            .weight(1f, fill = false).aspectRatio(1f)
            .testTag("Palette:$seed:$style").semantics {
                selected = isSelected
                contentDescription = "${PaletteCatalog.style(style)} palette"
            },
        shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surfaceContainer) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Box(Modifier.size(48.dp).clip(CircleShape).background(scheme.primary)) {
                Box(Modifier.align(Alignment.BottomStart).size(24.dp).background(scheme.secondaryContainer))
                Box(Modifier.align(Alignment.BottomEnd).size(24.dp).background(scheme.tertiaryContainer))
                Box(Modifier.align(Alignment.Center).size(checkBackground).clip(CircleShape)
                    .background(scheme.primaryContainer), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Check, null, Modifier.size(checkIcon), tint = scheme.onPrimaryContainer)
                }
            }
        }
    }
}

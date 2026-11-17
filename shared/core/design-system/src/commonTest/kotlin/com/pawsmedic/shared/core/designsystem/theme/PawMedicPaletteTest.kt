package com.pawsmedic.shared.core.designsystem.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import kotlin.test.Test
import kotlin.test.assertTrue

class PawMedicPaletteTest {
    @Test
    fun textRemainsReadableOnBackgrounds() {
        for (palette in listOf(LightPawMedicPalette, DarkPawMedicPalette)) {
            for (background in listOf(
                palette.background, palette.surface, palette.mutedSurface, palette.subtleSurface,
                palette.softAccent, palette.mediumAccent, palette.warningSurface, palette.blueSurface,
                palette.pinkSurface, palette.violetSurface
            )) {
                for (foreground in listOf(palette.text, palette.secondaryText, palette.hint)) {
                    assertTrue(
                        contrast(foreground, background) >= 4.5f,
                        "Insufficient text contrast: $foreground on $background"
                    )
                }
            }
            for (background in listOf(palette.background, palette.surface, palette.softAccent, palette.mediumAccent)) {
                assertTrue(contrast(palette.accent, background) >= 4.5f)
            }
        }
    }

    private fun contrast(first: Color, second: Color): Float {
        val firstLuminance = first.luminance()
        val secondLuminance = second.luminance()
        return (maxOf(firstLuminance, secondLuminance) + 0.05f) /
            (minOf(firstLuminance, secondLuminance) + 0.05f)
    }
}

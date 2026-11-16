package com.pawsmedic.shared.core.designsystem.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

@Immutable
data class PawMedicPalette(
    val background: Color,
    val surface: Color,
    val mutedSurface: Color,
    val border: Color,
    val text: Color,
    val secondaryText: Color,
    val hint: Color,
    val accent: Color,
    val subtleSurface: Color,
    val separator: Color,
    val softAccent: Color,
    val mediumAccent: Color,
    val strongAccent: Color,
    val warningSurface: Color,
    val blueSurface: Color,
    val pinkSurface: Color,
    val violetSurface: Color
)

val LightPawMedicPalette = PawMedicPalette(
    background = PawMedicColors.BackgroundScreen,
    surface = PawMedicColors.White,
    mutedSurface = PawMedicColors.Gray100,
    border = PawMedicColors.Gray300,
    text = PawMedicColors.Gray900,
    secondaryText = PawMedicColors.Gray600,
    hint = PawMedicColors.Gray600,
    accent = PawMedicColors.Teal700,
    subtleSurface = PawMedicColors.Gray50,
    separator = PawMedicColors.Gray200,
    softAccent = PawMedicColors.Teal50,
    mediumAccent = PawMedicColors.Teal100,
    strongAccent = PawMedicColors.Teal800,
    warningSurface = Color(0xFFFEF9C3),
    blueSurface = Color(0xFFE0F2FE),
    pinkSurface = Color(0xFFFCE7F3),
    violetSurface = Color(0xFFF3E8FF)
)

val DarkPawMedicPalette = PawMedicPalette(
    background = Color(0xFF10181C),
    surface = Color(0xFF1A262B),
    mutedSurface = Color(0xFF26373C),
    border = Color(0xFF80949A),
    text = Color(0xFFF1F6F5),
    secondaryText = Color(0xFFCBD8D7),
    hint = Color(0xFFB2C3C2),
    accent = Color(0xFF75D6C9),
    subtleSurface = Color(0xFF202D32),
    separator = Color(0xFF425459),
    softAccent = Color(0xFF193A38),
    mediumAccent = Color(0xFF22524C),
    strongAccent = Color(0xFFADF2E6),
    warningSurface = Color(0xFF4A3B1A),
    blueSurface = Color(0xFF203C4D),
    pinkSurface = Color(0xFF4A2C40),
    violetSurface = Color(0xFF3B304C)
)

internal val LocalPawMedicPalette = staticCompositionLocalOf { LightPawMedicPalette }

object PawMedicSemanticColors {
    val current: PawMedicPalette
        @Composable get() = LocalPawMedicPalette.current
}

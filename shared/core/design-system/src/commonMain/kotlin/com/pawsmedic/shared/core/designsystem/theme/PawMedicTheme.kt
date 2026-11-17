package com.pawsmedic.shared.core.designsystem.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = PawMedicColors.Teal600,
    onPrimary = PawMedicColors.White,
    primaryContainer = PawMedicColors.Teal100,
    onPrimaryContainer = PawMedicColors.Teal800,
    secondary = PawMedicColors.Teal700,
    onSecondary = PawMedicColors.White,
    background = PawMedicColors.BackgroundScreen,
    onBackground = PawMedicColors.Gray900,
    surface = PawMedicColors.White,
    onSurface = PawMedicColors.Gray900,
    error = PawMedicColors.Error,
    onError = PawMedicColors.White,
    outline = PawMedicColors.Gray300,
    outlineVariant = PawMedicColors.Gray200
)

private val DarkColorScheme = darkColorScheme(
    primary = DarkPawMedicPalette.accent,
    onPrimary = Color(0xFF003731),
    primaryContainer = Color(0xFF075047),
    onPrimaryContainer = Color(0xFFADF2E6),
    secondary = DarkPawMedicPalette.accent,
    onSecondary = Color(0xFF003731),
    background = DarkPawMedicPalette.background,
    onBackground = DarkPawMedicPalette.text,
    surface = DarkPawMedicPalette.surface,
    onSurface = DarkPawMedicPalette.text,
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    outline = DarkPawMedicPalette.border
)

@Composable
fun PawMedicTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    CompositionLocalProvider(
        LocalPawMedicPalette provides if (darkTheme) DarkPawMedicPalette else LightPawMedicPalette
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            content = content
        )
    }
}

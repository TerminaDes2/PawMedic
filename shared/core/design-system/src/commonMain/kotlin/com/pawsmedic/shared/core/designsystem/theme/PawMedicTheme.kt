package com.pawsmedic.shared.core.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

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

private val DarkColorScheme = lightColorScheme( // Consistent clean light branding as designed
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

@Composable
fun PawMedicTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}

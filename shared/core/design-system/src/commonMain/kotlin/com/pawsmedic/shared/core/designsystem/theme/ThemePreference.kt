package com.pawsmedic.shared.core.designsystem.theme

import androidx.compose.runtime.staticCompositionLocalOf

enum class ThemePreference {
    SYSTEM, LIGHT, DARK
}

class ThemePreferenceControl(
    val selected: ThemePreference,
    val onChange: (ThemePreference) -> Unit
)

val LocalThemePreference = staticCompositionLocalOf<ThemePreferenceControl?> { null }

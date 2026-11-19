package com.pawsmedic.desktop.admin.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxSize
import java.util.prefs.Preferences

class DesktopAppearance internal constructor(dark: Boolean, english: Boolean = false) {
    var dark by mutableStateOf(dark)
        private set
    var english by mutableStateOf(english)
        private set

    fun toggleLanguage() {
        english = !english
        try {
            Preferences.userNodeForPackage(DesktopAppearance::class.java).putBoolean("english", english)
        } catch (e: SecurityException) {
            System.err.println("No se pudo guardar el idioma: ${e.message}")
        }
    }

    fun toggleDark() {
        dark = !dark
        try {
            Preferences.userNodeForPackage(DesktopAppearance::class.java).putBoolean("dark", dark)
        } catch (e: SecurityException) {
            System.err.println("No se pudo guardar la preferencia de apariencia: ${e.message}")
        }
    }
}

val LocalDesktopAppearance = staticCompositionLocalOf { DesktopAppearance(false) }

@Composable
fun DesktopAppearanceProvider(content: @Composable () -> Unit) {
    val appearance = remember {
        val dark = try {
            Preferences.userNodeForPackage(DesktopAppearance::class.java).getBoolean("dark", false)
        } catch (e: SecurityException) {
            System.err.println("No se pudo leer la preferencia de apariencia: ${e.message}")
            false
        }
        val english = try {
            Preferences.userNodeForPackage(DesktopAppearance::class.java).getBoolean("english", false)
        } catch (e: SecurityException) {
            System.err.println("No se pudo leer el idioma: ${e.message}")
            false
        }
        DesktopAppearance(dark, english)
    }
    val colors = if (appearance.dark) darkColorScheme(
        primary = Color(0xFF5EEAD4),
        background = Color(0xFF0F172A),
        surface = Color(0xFF1E293B),
        onSurface = Color(0xFFF1F5F9),
        outline = Color(0xFF475569)
    ) else lightColorScheme(
        primary = Color(0xFF0D9488),
        background = Color(0xFFF8FAFC),
        surface = Color.White,
        onSurface = Color(0xFF1E293B),
        outline = Color(0xFFE2E8F0)
    )
    CompositionLocalProvider(LocalDesktopAppearance provides appearance) {
        MaterialTheme(colorScheme = colors) {
            Surface(modifier = Modifier.fillMaxSize(), color = colors.background, content = content)
        }
    }
}

@Composable
fun AppearanceButton() {
    val appearance = LocalDesktopAppearance.current
    TextButton(onClick = appearance::toggleDark) {
        Text(if (appearance.english) {
            if (appearance.dark) "Light mode" else "Dark mode"
        } else {
            if (appearance.dark) "Modo claro" else "Modo oscuro"
        })
    }
}

@Composable
fun NavigationLanguageButton() {
    val appearance = LocalDesktopAppearance.current
    TextButton(onClick = appearance::toggleLanguage) {
        Text(if (appearance.english) "Español" else "English")
    }
}

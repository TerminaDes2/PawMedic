package com.pawsmedic.desktop.veterinary.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import com.pawsmedic.desktop.admin.ui.LocalDesktopAppearance

// Paleta de Colores Clínica & Profesional PawsMedic
val DarkSlate = Color(0xFF0F172A)        // Primario oscuro para headers y sidebars
val ClinicalEmerald = Color(0xFF0D9488)  // Acento primario clínico para botones e indicadores activos
val EmeraldHover = Color(0xFF0F766E)     // Estado pressed/hover de botones
val EmeraldLightBg: Color @Composable @ReadOnlyComposable get() = if (LocalDesktopAppearance.current.dark) Color(0xFF134E4A) else Color(0xFFCCFBF1)
val OffWhiteBg: Color @Composable @ReadOnlyComposable get() = if (LocalDesktopAppearance.current.dark) Color(0xFF0F172A) else Color(0xFFF8FAFC)
val CardWhite: Color @Composable @ReadOnlyComposable get() = if (LocalDesktopAppearance.current.dark) Color(0xFF1E293B) else Color.White
val BorderLight: Color @Composable @ReadOnlyComposable get() = if (LocalDesktopAppearance.current.dark) Color(0xFF475569) else Color(0xFFE2E8F0)
val TextPrimary: Color @Composable @ReadOnlyComposable get() = if (LocalDesktopAppearance.current.dark) Color(0xFFF1F5F9) else Color(0xFF1E293B)
val TextSecondary: Color @Composable @ReadOnlyComposable get() = if (LocalDesktopAppearance.current.dark) Color(0xFFCBD5E1) else Color(0xFF64748B)
val TextMuted: Color @Composable @ReadOnlyComposable get() = if (LocalDesktopAppearance.current.dark) Color(0xFFCBD5E1) else Color(0xFF94A3B8)

// Aliases de compatibilidad de paleta
val EmeraldGreen = ClinicalEmerald
val SelectedBg: Color @Composable @ReadOnlyComposable get() = EmeraldLightBg
val SelectedText = ClinicalEmerald

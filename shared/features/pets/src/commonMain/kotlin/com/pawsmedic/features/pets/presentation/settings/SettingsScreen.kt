package com.pawsmedic.features.pets.presentation.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import com.pawsmedic.shared.core.designsystem.components.PawMedicTopBar
import com.pawsmedic.shared.core.designsystem.theme.PawMedicColors
import com.pawsmedic.shared.core.designsystem.theme.PawMedicSemanticColors
import com.pawsmedic.shared.core.designsystem.theme.LocalThemePreference
import com.pawsmedic.shared.core.designsystem.theme.ThemePreference
import com.pawsmedic.shared.core.designsystem.language.AppLanguage
import com.pawsmedic.shared.core.designsystem.language.LocalLanguagePreference
import com.pawsmedic.features.pets.presentation.localizePetMessage

@Composable
fun SettingsScreen(
    onBackClick: () -> Unit,
    onDeleteAccount: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    val themeControl = LocalThemePreference.current
    val notificationControl = LocalNotificationSettings.current
    val languageControl = LocalLanguagePreference.current
    val language = languageControl?.selected ?: AppLanguage.SPANISH

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(PawMedicSemanticColors.current.background)
            .windowInsetsPadding(WindowInsets.statusBars)
            .windowInsetsPadding(WindowInsets.navigationBars)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        PawMedicTopBar(title = language.text("Configuración", "Settings"), onBackClick = onBackClick)
        Spacer(Modifier.height(8.dp))

        SettingsSectionTitle(language.text("Notificaciones y Recordatorios", "Notifications and reminders"))
        SettingsIconRow(Icons.Default.NotificationsActive, language.text("Recordatorios de citas", "Appointment reminders"),
            if (notificationControl == null) language.text("En desarrollo: los avisos aún no están disponibles.", "Notifications are not available yet.")
            else language.text("Recibe avisos de cambios de estado y recordatorios 24 horas y 1 hora antes.", "Receive status updates and reminders 24 hours and 1 hour before your appointment."))
        if (notificationControl != null) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(start = 36.dp, end = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(language.text("Notificaciones de citas", "Appointment notifications"), color = PawMedicSemanticColors.current.text)
                Switch(
                    checked = notificationControl.enabled,
                    onCheckedChange = notificationControl.onChange,
                    enabled = !notificationControl.busy
                )
            }
            notificationControl.error?.let { Text(localizePetMessage(it, language), color = PawMedicColors.Error) }
        }
        SettingsIconRow(Icons.Default.HealthAndSafety, language.text("Alertas de salud", "Health alerts"),
            language.text("En desarrollo: las alertas automáticas aún no están disponibles.", "Automatic health alerts are not available yet."))

        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
        SettingsSectionTitle(language.text("Preferencias de la Aplicación", "App preferences"))
        SettingsIconRow(Icons.Default.Palette, language.text("Tema visual", "Appearance"),
            if (themeControl == null) language.text("No disponible en esta plataforma.", "Not available on this platform.") else language.text("Elige la apariencia de la aplicación.", "Choose the app appearance."))
        if (themeControl != null) {
            listOf(
                ThemePreference.LIGHT to language.text("Tema claro", "Light theme"),
                ThemePreference.DARK to language.text("Tema oscuro", "Dark theme"),
                ThemePreference.SYSTEM to language.text("Predeterminado del sistema", "System default")
            ).forEach { (option, label) ->
                ChoiceRow(label, themeControl.selected == option) { themeControl.onChange(option) }
            }
        }
        SettingsIconRow(Icons.Default.Language, language.text("Idioma de la interfaz", "App language"),
            if (languageControl == null) language.text("No disponible en esta plataforma.", "Not available on this platform.")
            else language.text("Elige el idioma de la aplicación.", "Choose the app language."))
        if (languageControl != null) {
            listOf(AppLanguage.SPANISH to "Español", AppLanguage.ENGLISH to "English").forEach { (option, label) ->
                ChoiceRow(label, language == option) { languageControl.onChange(option) }
            }
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
        TextButton(
            onClick = { showDeleteConfirmation = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.DeleteForever, contentDescription = null, tint = PawMedicColors.Error)
            Text(language.text("Eliminar mi cuenta", "Delete my account"), color = PawMedicColors.Error, modifier = Modifier.padding(start = 8.dp))
        }
    }

    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text(language.text("¿Eliminar tu cuenta?", "Delete your account?")) },
            text = { Text(language.text("Se eliminarán tu perfil, mascotas y datos asociados. Esta acción no se puede deshacer.", "Your profile, pets and associated data will be deleted. This cannot be undone.")) },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteConfirmation = false
                    onDeleteAccount()
                }) { Text(language.text("Eliminar", "Delete"), color = PawMedicColors.Error) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = false }) { Text(language.text("Cancelar", "Cancel")) }
            }
        )
    }
}

@Composable
private fun SettingsSectionTitle(text: String) {
    Text(text, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = PawMedicSemanticColors.current.text)
}

@Composable
private fun SettingsIconRow(icon: ImageVector, title: String, description: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = PawMedicSemanticColors.current.accent)
        Column(modifier = Modifier.padding(start = 12.dp)) {
            Text(title, fontWeight = FontWeight.SemiBold, color = PawMedicSemanticColors.current.text)
            Text(description, fontSize = 13.sp, color = PawMedicSemanticColors.current.secondaryText)
        }
    }
}

@Composable
private fun ChoiceRow(label: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(start = 36.dp, end = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = onClick)
        Text(label, color = PawMedicSemanticColors.current.text)
    }
}

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

@Composable
fun SettingsScreen(
    onBackClick: () -> Unit,
    onDeleteAccount: () -> Unit,
    modifier: Modifier = Modifier
) {
    var appointmentReminder by remember { mutableStateOf("Push") }
    var healthAlertsEnabled by remember { mutableStateOf(true) }
    var visualTheme by remember { mutableStateOf("Predeterminado del sistema") }
    var language by remember { mutableStateOf("Español") }
    var showDeleteConfirmation by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(PawMedicColors.BackgroundScreen)
            .windowInsetsPadding(WindowInsets.statusBars)
            .windowInsetsPadding(WindowInsets.navigationBars)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        PawMedicTopBar(title = "Configuración", onBackClick = onBackClick)
        Spacer(Modifier.height(8.dp))

        SettingsSectionTitle("Notificaciones y Recordatorios")
        SettingsIconRow(Icons.Default.NotificationsActive, "Recordatorios de citas",
            "Elige cómo recibir avisos de tus consultas programadas.")
        listOf("Push", "SMS", "Correo electrónico").forEach { option ->
            ChoiceRow(option, appointmentReminder == option) { appointmentReminder = option }
        }
        SettingsIconRow(Icons.Default.HealthAndSafety, "Alertas de salud",
            "Vacunas, desparasitaciones, antipulgas y chequeos anuales.")
        SwitchRow("Avisos automáticos de salud", healthAlertsEnabled) {
            healthAlertsEnabled = it
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
        SettingsSectionTitle("Preferencias de la Aplicación")
        SettingsIconRow(Icons.Default.Palette, "Tema visual",
            "Compatible con los componentes de Material Design.")
        listOf("Tema Claro", "Tema Oscuro", "Predeterminado del sistema").forEach { option ->
            ChoiceRow(option, visualTheme == option) { visualTheme = option }
        }
        SettingsIconRow(Icons.Default.Language, "Idioma de la interfaz",
            "Selecciona el idioma que prefieras para la aplicación.")
        listOf("Español", "English").forEach { option ->
            ChoiceRow(option, language == option) { language = option }
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
        TextButton(
            onClick = { showDeleteConfirmation = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.DeleteForever, contentDescription = null, tint = PawMedicColors.Error)
            Text("Eliminar mi cuenta", color = PawMedicColors.Error, modifier = Modifier.padding(start = 8.dp))
        }
    }

    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text("¿Eliminar tu cuenta?") },
            text = { Text("Se eliminarán tu perfil, mascotas y datos asociados. Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteConfirmation = false
                    onDeleteAccount()
                }) { Text("Eliminar", color = PawMedicColors.Error) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = false }) { Text("Cancelar") }
            }
        )
    }
}

@Composable
private fun SettingsSectionTitle(text: String) {
    Text(text, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = PawMedicColors.Gray900)
}

@Composable
private fun SettingsIconRow(icon: ImageVector, title: String, description: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = PawMedicColors.Teal600)
        Column(modifier = Modifier.padding(start = 12.dp)) {
            Text(title, fontWeight = FontWeight.SemiBold, color = PawMedicColors.Gray900)
            Text(description, fontSize = 13.sp, color = PawMedicColors.Gray600)
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
        Text(label, color = PawMedicColors.Gray800)
    }
}

@Composable
private fun SwitchRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(start = 36.dp, end = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = PawMedicColors.Gray800)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

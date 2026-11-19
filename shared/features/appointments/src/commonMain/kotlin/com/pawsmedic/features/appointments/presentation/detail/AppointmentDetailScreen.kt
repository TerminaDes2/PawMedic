package com.pawsmedic.features.appointments.presentation.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pawsmedic.features.appointments.domain.model.Appointment
import com.pawsmedic.features.appointments.presentation.list.AppointmentsListViewModel
import com.pawsmedic.shared.core.designsystem.components.PawMedicTopBar
import com.pawsmedic.shared.core.designsystem.language.AppLanguage
import com.pawsmedic.shared.core.designsystem.language.LocalLanguagePreference
import com.pawsmedic.shared.core.designsystem.theme.PawMedicColors
import com.pawsmedic.shared.core.designsystem.theme.PawMedicSemanticColors

@Composable
fun AppointmentDetailScreen(
    appointment: Appointment,
    viewModel: AppointmentsListViewModel,
    onBackClick: () -> Unit,
    onCancelled: () -> Unit,
    modifier: Modifier = Modifier
) {
    val language = LocalLanguagePreference.current?.selected ?: AppLanguage.SPANISH
    var currentAppointment by remember(appointment.id) { mutableStateOf(appointment) }
    var showCancelDialog by remember { mutableStateOf(false) }
    val isPending = currentAppointment.status.contains("Pendiente", ignoreCase = true)

    Column(
        modifier = modifier.fillMaxSize()
            .background(PawMedicSemanticColors.current.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        PawMedicTopBar(
            title = language.text("Detalle de cita", "Appointment detail"),
            onBackClick = onBackClick
        )
        Spacer(Modifier.height(4.dp))
        Text(
            language.text("Consulta la información de tu próxima visita.", "Review information about your upcoming visit."),
            fontSize = 16.sp,
            color = PawMedicSemanticColors.current.secondaryText
        )
        Spacer(Modifier.height(16.dp))

        Box(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp))
                .background(PawMedicSemanticColors.current.surface)
                .border(1.dp, PawMedicSemanticColors.current.separator, RoundedCornerShape(20.dp))
                .padding(18.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
                DetailRow(Icons.Default.MedicalServices, language.text("Veterinaria", "Veterinary clinic"), currentAppointment.businessName)
                DetailRow(Icons.Default.Pets, language.text("Mascota", "Pet"), currentAppointment.petName)
                DetailRow(Icons.Default.MedicalServices, language.text("Servicio", "Service"), currentAppointment.serviceName)
                DetailRow(Icons.Default.CalendarMonth, language.text("Fecha y hora", "Date and time"), "${currentAppointment.date} • ${currentAppointment.time}")
            }
        }

        if (isPending) {
            Spacer(Modifier.height(18.dp))
            Row(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFFFF3C4)).padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Warning, null, tint = Color(0xFFB45309))
                Spacer(Modifier.width(12.dp))
                Text(
                    language.text("Tu solicitud está pendiente de aprobación.", "Your request is pending veterinary approval."),
                    color = Color(0xFF92400E),
                    fontSize = 14.sp
                )
            }

            Spacer(Modifier.height(18.dp))
            TextButton(onClick = { showCancelDialog = true }, Modifier.fillMaxWidth()) {
                Icon(Icons.Default.Delete, null, tint = PawMedicColors.Error)
                Spacer(Modifier.width(8.dp))
                Text(language.text("Cancelar cita", "Cancel appointment"), color = PawMedicColors.Error, fontWeight = FontWeight.Bold)
            }
        }
    }

    if (showCancelDialog) {
        AlertDialog(
            onDismissRequest = { showCancelDialog = false },
            title = { Text(language.text("Cancelar cita", "Cancel appointment")) },
            text = { Text(language.text("¿Deseas cancelar esta cita pendiente de confirmación?", "Do you want to cancel this pending appointment?")) },
            confirmButton = {
                Button(onClick = {
                    showCancelDialog = false
                    viewModel.cancelAppointment(currentAppointment, onCancelled)
                }) { Text(language.text("Sí, cancelar", "Yes, cancel")) }
            },
            dismissButton = {
                TextButton(onClick = { showCancelDialog = false }) { Text(language.text("Volver", "Back")) }
            }
        )
    }
}

@Composable
private fun DetailRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier.size(64.dp).clip(RoundedCornerShape(16.dp))
                .background(PawMedicSemanticColors.current.softAccent),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = PawMedicSemanticColors.current.accent, modifier = Modifier.size(30.dp))
        }
        Spacer(Modifier.width(16.dp))
        Column {
            Text(label, color = PawMedicSemanticColors.current.hint, fontSize = 15.sp)
            Text(value, color = PawMedicSemanticColors.current.text, fontSize = 17.sp, fontWeight = FontWeight.Bold)
        }
    }
}

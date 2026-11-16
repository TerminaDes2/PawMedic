package com.pawsmedic.features.appointments.presentation.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Schedule
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
import com.pawsmedic.features.appointments.domain.model.AvailableAppointmentSlot
import com.pawsmedic.features.appointments.presentation.list.AppointmentsListViewModel
import com.pawsmedic.shared.core.common.convertMillisToIsoDateString
import com.pawsmedic.shared.core.common.formatIsoToDisplayDate
import com.pawsmedic.shared.core.designsystem.components.PawMedicPrimaryButton
import com.pawsmedic.shared.core.designsystem.components.PawMedicTopBar
import com.pawsmedic.shared.core.designsystem.components.DatePickerModal
import com.pawsmedic.shared.core.designsystem.theme.PawMedicColors

@Composable
fun AppointmentDetailScreen(
    appointment: Appointment,
    viewModel: AppointmentsListViewModel,
    onBackClick: () -> Unit,
    onCancelled: () -> Unit,
    modifier: Modifier = Modifier
) {
    var currentAppointment by remember(appointment.id) { mutableStateOf(appointment) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showCancelDialog by remember { mutableStateOf(false) }
    val isPending = currentAppointment.status.contains("Pendiente", ignoreCase = true)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(PawMedicColors.BackgroundScreen)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        PawMedicTopBar(title = "Detalle de cita", onBackClick = onBackClick)
        Spacer(Modifier.height(4.dp))
        Text(
            text = "Consulta la información de tu próxima visita.",
            fontSize = 16.sp,
            color = PawMedicColors.Gray600
        )
        Spacer(Modifier.height(16.dp))
        StatusBadge(currentAppointment.status)
        Spacer(Modifier.height(16.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(PawMedicColors.White)
                .border(1.dp, PawMedicColors.Gray200, RoundedCornerShape(20.dp))
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            DetailRow(Icons.Default.MedicalServices, "Veterinaria", currentAppointment.businessName)
            DetailRow(Icons.Default.Pets, "Mascota", currentAppointment.petName)
            DetailRow(Icons.Default.MedicalServices, "Servicio", currentAppointment.serviceName)
            DetailRow(
                Icons.Default.CalendarMonth,
                "Fecha y hora",
                "${currentAppointment.date} • ${currentAppointment.time}"
            )
        }

        if (isPending) {
            Spacer(Modifier.height(18.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFFFF3C4))
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFB45309))
                Spacer(Modifier.width(12.dp))
                Text(
                    "Tu solicitud está pendiente de aprobación. Te notificaremos cuando la veterinaria confirme la cita.",
                    color = Color(0xFF92400E),
                    fontSize = 14.sp
                )
            }
            Spacer(Modifier.height(18.dp))

            TextButton(
                onClick = { showCancelDialog = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Delete, contentDescription = null, tint = PawMedicColors.Error)
                Spacer(Modifier.width(8.dp))
                Text("Cancelar cita", color = PawMedicColors.Error, fontWeight = FontWeight.Bold)
            }
        }
    }

    if (showEditDialog) {
        AppointmentEditDialog(
            appointment = currentAppointment,
            viewModel = viewModel,
            onDismiss = { showEditDialog = false },
            onSave = { date, time ->
                showEditDialog = false
                viewModel.editAppointment(currentAppointment, date, time) {
                    currentAppointment = currentAppointment.copy(date = date.trim(), time = time.trim())
                }
            }
        )
    }
    if (showCancelDialog) {
        AlertDialog(
            onDismissRequest = { showCancelDialog = false },
            title = { Text("Cancelar cita") },
            text = { Text("¿Deseas cancelar esta cita pendiente de confirmación?") },
            confirmButton = {
                Button(onClick = {
                    showCancelDialog = false
                    viewModel.cancelAppointment(currentAppointment, onCancelled)
                }) { Text("Sí, cancelar") }
            },
            dismissButton = {
                TextButton(onClick = { showCancelDialog = false }) { Text("Volver") }
            }
        )
    }
}

@Composable
private fun StatusBadge(status: String) {
    val pending = status.contains("Pendiente", ignoreCase = true)
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(if (pending) Color(0xFFFEF3C7) else PawMedicColors.Teal50)
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Text(
            status,
            color = if (pending) Color(0xFF92400E) else PawMedicColors.Teal600,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun DetailRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(PawMedicColors.Teal50),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = PawMedicColors.Teal600, modifier = Modifier.size(30.dp))
        }
        Spacer(Modifier.width(16.dp))
        Column {
            Text(label, color = PawMedicColors.Gray500, fontSize = 15.sp)
            Text(value, color = PawMedicColors.Gray900, fontSize = 17.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun AppointmentEditDialog(
    appointment: Appointment,
    viewModel: AppointmentsListViewModel,
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit
) {
    var date by remember { mutableStateOf(appointment.date) }
    var time by remember { mutableStateOf(appointment.time) }
    var dateLabel by remember { mutableStateOf(formatIsoToDisplayDate(appointment.date)) }
    var availableSlots by remember { mutableStateOf<List<AvailableAppointmentSlot>>(emptyList()) }
    var showDatePicker by remember { mutableStateOf(false) }
    var isLoadingSlots by remember { mutableStateOf(false) }
    var validationMessage by remember { mutableStateOf<String?>(null) }

    if (showDatePicker) {
        DatePickerModal(
            onDateSelected = { millis ->
                if (millis != null) {
                    date = convertMillisToIsoDateString(millis)
                    dateLabel = formatIsoToDisplayDate(date)
                    validationMessage = null
                    isLoadingSlots = true
                    viewModel.loadAvailableSlots(appointment, date) {
                        availableSlots = it
                        isLoadingSlots = false
                    }
                }
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar cita") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = { showDatePicker = true }) {
                    Text("Fecha: $dateLabel", color = PawMedicColors.Teal600)
                }
                TextButton(onClick = {
                    validationMessage = null
                    isLoadingSlots = true
                    viewModel.loadAvailableSlots(appointment, date) {
                        availableSlots = it
                        isLoadingSlots = false
                    }
                }) {
                    Text("Consultar horarios disponibles", color = PawMedicColors.Teal600)
                }
                when {
                    isLoadingSlots -> Text(
                        "Consultando horarios de la veterinaria…",
                        fontSize = 12.sp,
                        color = PawMedicColors.Gray600
                    )
                    availableSlots.isEmpty() -> Text(
                        "No hay horarios disponibles para esta fecha.",
                        fontSize = 12.sp,
                        color = PawMedicColors.Gray600
                    )
                    else -> {
                        Text(
                            "Selecciona un horario disponible:",
                            fontSize = 12.sp,
                            color = PawMedicColors.Gray600
                        )
                        availableSlots.forEach { slot ->
                            val selected = slot.label == time
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (selected) PawMedicColors.Teal50
                                        else PawMedicColors.White
                                    )
                                    .border(
                                        if (selected) 2.dp else 1.dp,
                                        if (selected) PawMedicColors.Teal600
                                        else PawMedicColors.Gray200,
                                        RoundedCornerShape(12.dp)
                                    )
                                    .clickable {
                                        time = slot.label
                                        validationMessage = null
                                    }
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Schedule,
                                    contentDescription = null,
                                    tint = PawMedicColors.Teal600,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    slot.label,
                                    fontWeight = FontWeight.Bold,
                                    color = PawMedicColors.Gray900
                                )
                            }
                        }
                    }
                }
                validationMessage?.let {
                    Text(it, color = PawMedicColors.Error, fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                val isCurrentSlot = date == appointment.date && time == appointment.time
                val isAvailableSlot = availableSlots.any { it.label == time }
                if (isCurrentSlot || isAvailableSlot) onSave(date, time)
                else validationMessage = "Selecciona un horario disponible de la veterinaria."
            }) { Text("Guardar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

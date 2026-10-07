package com.pawsmedic.features.appointments.presentation.booking

import androidx.compose.animation.Crossfade
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Warning
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pawsmedic.features.appointments.domain.model.VeterinaryService
import com.pawsmedic.features.appointments.presentation.components.AsyncBookingPetImage
import com.pawsmedic.shared.core.common.convertMillisToIsoDateString
import com.pawsmedic.shared.core.common.formatIsoToDisplayDate
import com.pawsmedic.shared.core.designsystem.components.DatePickerModal
import com.pawsmedic.shared.core.designsystem.components.PawMedicLogoIcon
import com.pawsmedic.shared.core.designsystem.components.PawMedicPrimaryButton
import com.pawsmedic.shared.core.designsystem.components.PawMedicSecondaryButton
import com.pawsmedic.shared.core.designsystem.components.PawMedicTopBar
import com.pawsmedic.shared.core.designsystem.components.TimePickerModal
import com.pawsmedic.shared.core.designsystem.theme.PawMedicColors

@Composable
fun BookingFlowScreen(
    viewModel: BookingViewModel,
    onBackClick: () -> Unit,
    onBookingComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    Crossfade(targetState = uiState.currentStep, modifier = modifier.fillMaxSize()) { step ->
        when (step) {
            BookingStep.SELECT_SERVICE -> {
                SelectServiceStep(
                    uiState = uiState,
                    onServiceSelected = viewModel::selectService,
                    onNext = viewModel::nextStep,
                    onBack = onBackClick
                )
            }

            BookingStep.SELECT_PET -> {
                SelectPetStep(
                    uiState = uiState,
                    onPetSelected = viewModel::selectPet,
                    onNext = viewModel::nextStep,
                    onBack = viewModel::previousStep
                )
            }

            BookingStep.SELECT_DATE_TIME -> {
                SelectDateTimeStep(
                    uiState = uiState,
                    onDateSelected = viewModel::selectDate,
                    onTimeSlotSelected = viewModel::selectTimeSlot,
                    onTimeFromPickerSelected = viewModel::selectTimeFromPicker,
                    onNext = viewModel::nextStep,
                    onBack = viewModel::previousStep
                )
            }

            BookingStep.CONFIRM -> {
                ConfirmBookingStep(
                    uiState = uiState,
                    onNotesChanged = viewModel::onNotesChanged,
                    onSubmit = { viewModel.submitBooking(onBookingComplete) },
                    onBack = viewModel::previousStep
                )
            }

            BookingStep.SUCCESS -> {
                BookingSuccessStep(
                    onComplete = onBookingComplete
                )
            }
        }
    }
}

// --- STEP 1: SELECT SERVICE ---
@Composable
private fun SelectServiceStep(
    uiState: BookingUiState,
    onServiceSelected: (VeterinaryService) -> Unit,
    onNext: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PawMedicColors.BackgroundScreen)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        PawMedicTopBar(
            title = "Seleccionar Servicio",
            onBackClick = onBack
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Selecciona el tipo de servicio que requiere tu mascota en ${uiState.selectedClinicName}:",
            fontSize = 14.sp,
            color = PawMedicColors.Gray600,
            lineHeight = 20.sp
        )

        Spacer(modifier = Modifier.height(20.dp))

        uiState.services.forEach { service ->
            val isSelected = service.id == uiState.selectedService?.id
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (isSelected) PawMedicColors.Teal50 else PawMedicColors.White)
                    .border(
                        width = if (isSelected) 2.dp else 1.dp,
                        color = if (isSelected) PawMedicColors.Teal600 else PawMedicColors.Gray200,
                        shape = RoundedCornerShape(16.dp)
                    )
                    .clickable { onServiceSelected(service) }
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(54.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(PawMedicColors.Teal100),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.MedicalServices,
                                contentDescription = null,
                                tint = PawMedicColors.Teal600,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column {
                            Text(
                                text = service.title,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = PawMedicColors.Gray900
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = service.description,
                                fontSize = 12.sp,
                                color = PawMedicColors.Gray500,
                                lineHeight = 16.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "⏱ Duración: ${service.durationMinutes} minutos",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = PawMedicColors.Teal600
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(if (isSelected) PawMedicColors.Teal600 else PawMedicColors.Gray200)
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        Spacer(modifier = Modifier.height(24.dp))

        PawMedicPrimaryButton(
            text = "Continuar",
            onClick = onNext
        )

        Spacer(modifier = Modifier.height(20.dp))
    }
}

// --- STEP 2: SELECT PET ---
@Composable
private fun SelectPetStep(
    uiState: BookingUiState,
    onPetSelected: (BookingPet) -> Unit,
    onNext: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PawMedicColors.BackgroundScreen)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        PawMedicTopBar(
            title = "Seleccionar Mascota",
            onBackClick = onBack
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "¿Para cuál de tus mascotas deseas agendar el servicio médico?",
            fontSize = 14.sp,
            color = PawMedicColors.Gray600,
            lineHeight = 20.sp
        )

        Spacer(modifier = Modifier.height(20.dp))

        uiState.userPets.forEach { pet ->
            val isSelected = pet.id == (uiState.selectedPet?.id ?: "pet-1")
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (isSelected) PawMedicColors.Teal50 else PawMedicColors.White)
                    .border(
                        width = if (isSelected) 2.dp else 1.dp,
                        color = if (isSelected) PawMedicColors.Teal600 else PawMedicColors.Gray200,
                        shape = RoundedCornerShape(16.dp)
                    )
                    .clickable { onPetSelected(pet) }
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        BookingPetAvatar(
                            photoUrl = pet.photoUrl,
                            species = pet.species,
                            size = 56.dp
                        )

                        Spacer(modifier = Modifier.width(14.dp))

                        Column {
                            Text(
                                text = pet.name,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = PawMedicColors.Gray900
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${pet.species} • ${pet.breed ?: "Mestizo"}",
                                fontSize = 13.sp,
                                color = PawMedicColors.Gray500
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = pet.age ?: "1 año",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = PawMedicColors.Teal600
                            )
                        }
                    }

                    if (isSelected) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(PawMedicColors.Teal600)
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "Elegido",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = PawMedicColors.White
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        Spacer(modifier = Modifier.height(28.dp))

        PawMedicPrimaryButton(
            text = "Continuar",
            onClick = onNext
        )

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
private fun BookingPetAvatar(
    photoUrl: String?,
    species: String?,
    size: Dp = 56.dp
) {
    val emoji = when {
        species?.contains("Gato", true) == true || species?.contains("Felino", true) == true -> "🐱"
        species?.contains("Perro", true) == true || species?.contains("Canino", true) == true -> "🐕"
        species?.contains("Conejo", true) == true -> "🐰"
        species?.contains("Ave", true) == true || species?.contains("Pájaro", true) == true -> "🦜"
        else -> "🐾"
    }

    if (!photoUrl.isNullOrBlank()) {
        AsyncBookingPetImage(
            photoUrl = photoUrl,
            fallbackEmoji = emoji,
            size = size
        )
    } else {
        Box(
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
                .background(PawMedicColors.Teal100),
            contentAlignment = Alignment.Center
        ) {
            Text(text = emoji, fontSize = (size.value * 0.45f).sp)
        }
    }
}

// --- STEP 3: SELECT DATE & TIME ---
@Composable
private fun SelectDateTimeStep(
    uiState: BookingUiState,
    onDateSelected: (String, Long?) -> Unit,
    onTimeSlotSelected: (String) -> Unit,
    onTimeFromPickerSelected: (Int, Int, String) -> Unit,
    onNext: () -> Unit,
    onBack: () -> Unit
) {
    var showDatePickerModal by remember { mutableStateOf(false) }
    var showTimePickerModal by remember { mutableStateOf(false) }

    if (showDatePickerModal) {
        DatePickerModal(
            onDateSelected = { selectedMillis ->
                if (selectedMillis != null) {
                    val isoDate = convertMillisToIsoDateString(selectedMillis)
                    val displayDate = formatIsoToDisplayDate(isoDate)
                    onDateSelected("Día $displayDate", selectedMillis)
                }
                showDatePickerModal = false
            },
            onDismiss = { showDatePickerModal = false }
        )
    }

    if (showTimePickerModal) {
        TimePickerModal(
            initialHour = 11,
            initialMinute = 0,
            onTimeSelected = { hour, minute, formatted ->
                onTimeFromPickerSelected(hour, minute, formatted)
            },
            onDismiss = { showTimePickerModal = false }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PawMedicColors.BackgroundScreen)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        PawMedicTopBar(
            title = "Seleccionar Fecha y Hora",
            onBackClick = onBack
        )

        Spacer(modifier = Modifier.height(12.dp))

        // --- CALENDAR DATE SELECTION ---
        Text(
            text = "Fecha de la cita (Dentro de 1 mes, solo fechas futuras)",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = PawMedicColors.Gray800,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(PawMedicColors.White)
                .border(1.dp, PawMedicColors.Teal600, RoundedCornerShape(16.dp))
                .clickable { showDatePickerModal = true }
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = null,
                        tint = PawMedicColors.Teal600,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = uiState.selectedDate,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = PawMedicColors.Teal600
                        )
                        Text(
                            text = "Toca para elegir fecha en el calendario (1 mes disponible)",
                            fontSize = 12.sp,
                            color = PawMedicColors.Gray500
                        )
                    }
                }
                Text(text = "▼", fontSize = 12.sp, color = PawMedicColors.Teal600)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- TIME SELECTION SECTION ---
        Text(
            text = "Hora de Atención (11:00 AM - 07:00 PM)",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = PawMedicColors.Gray800,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // LARGE TIME PICKER BUTTON
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(PawMedicColors.White)
                .border(1.5.dp, PawMedicColors.Teal600, RoundedCornerShape(16.dp))
                .clickable { showTimePickerModal = true }
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = PawMedicColors.Teal600,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Seleccionar hora",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = PawMedicColors.Gray900
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(PawMedicColors.Teal50)
                        .border(1.dp, PawMedicColors.Teal200, RoundedCornerShape(10.dp))
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = uiState.selectedTimeSlot,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = PawMedicColors.Teal600
                    )
                }
            }
        }

        if (uiState.errorMessage != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = uiState.errorMessage ?: "",
                fontSize = 13.sp,
                color = PawMedicColors.Error,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        PawMedicPrimaryButton(
            text = "Continuar",
            onClick = onNext
        )

        Spacer(modifier = Modifier.height(20.dp))
    }
}

// --- STEP 4: CONFIRM BOOKING ---
@Composable
private fun ConfirmBookingStep(
    uiState: BookingUiState,
    onNotesChanged: (String) -> Unit,
    onSubmit: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PawMedicColors.BackgroundScreen)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        PawMedicTopBar(
            title = "Confirmar Cita",
            onBackClick = onBack
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Revisa los detalles de tu solicitud antes de enviarla a la veterinaria.",
            fontSize = 14.sp,
            color = PawMedicColors.Gray600,
            lineHeight = 20.sp
        )

        Spacer(modifier = Modifier.height(20.dp))

        // SUMMARY CARD
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(PawMedicColors.White)
                .border(1.dp, PawMedicColors.Gray200, RoundedCornerShape(20.dp))
                .padding(18.dp)
        ) {
            Column {
                SummaryItem(
                    icon = "🏥",
                    label = "Veterinaria",
                    value = uiState.selectedClinicName
                )
                Spacer(modifier = Modifier.height(14.dp))
                SummaryItem(
                    icon = "🐾",
                    label = "Mascota",
                    value = "${uiState.selectedPet?.name ?: "Tobías"} (${uiState.selectedPet?.species ?: "Gato"} • ${uiState.selectedPet?.breed ?: "Persa Mestizo"})"
                )
                Spacer(modifier = Modifier.height(14.dp))
                SummaryItem(
                    icon = "🩺",
                    label = "Servicio",
                    value = uiState.selectedService?.title ?: "Consulta Médica General"
                )
                Spacer(modifier = Modifier.height(14.dp))
                SummaryItem(
                    icon = "📅",
                    label = "Fecha y Hora",
                    value = "${uiState.selectedDate} • ${uiState.selectedTimeSlot}"
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Notas para la veterinaria (Opcional)",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = PawMedicColors.Gray800,
            modifier = Modifier.padding(bottom = 6.dp)
        )

        OutlinedTextField(
            value = uiState.notes,
            onValueChange = onNotesChanged,
            placeholder = {
                Text(
                    text = "Ej: Presenta decaimiento leve desde ayer, o necesita refuerzo de vacunas específicas...",
                    color = PawMedicColors.Gray400,
                    fontSize = 13.sp
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(90.dp),
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PawMedicColors.Teal600,
                unfocusedBorderColor = PawMedicColors.Gray300,
                focusedContainerColor = PawMedicColors.White,
                unfocusedContainerColor = PawMedicColors.White
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // WARNING ALERT BANNER
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0xFFFEF3C7))
                .padding(14.dp)
        ) {
            Row {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = Color(0xFF92400E),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "La cita debe ser solicitada primero. Estará sujeta a la aprobación de la clínica. Te notificaremos si es confirmada o rechazada.",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF92400E),
                    lineHeight = 17.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        PawMedicPrimaryButton(
            text = "Solicitar cita",
            onClick = onSubmit,
            isLoading = uiState.isLoading
        )

        Spacer(modifier = Modifier.height(12.dp))

        PawMedicSecondaryButton(
            text = "Regresar",
            onClick = onBack
        )

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
private fun SummaryItem(
    icon: String,
    label: String,
    value: String
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(PawMedicColors.Teal50),
            contentAlignment = Alignment.Center
        ) {
            Text(text = icon, fontSize = 20.sp)
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column {
            Text(
                text = label,
                fontSize = 12.sp,
                color = PawMedicColors.Gray400
            )
            Text(
                text = value,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = PawMedicColors.Gray900
            )
        }
    }
}

// --- STEP 5: BOOKING SUCCESS ---
@Composable
private fun BookingSuccessStep(
    onComplete: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PawMedicColors.BackgroundScreen)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            PawMedicLogoIcon(size = 90.dp)

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "¡Solicitud Enviada!",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = PawMedicColors.Gray900
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Tu cita ha sido solicitada correctamente. La clínica revisará la disponibilidad y te notificará pronto.",
                fontSize = 14.sp,
                color = PawMedicColors.Gray600,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(32.dp))

            PawMedicPrimaryButton(
                text = "Ver mis citas",
                onClick = onComplete
            )
        }
    }
}

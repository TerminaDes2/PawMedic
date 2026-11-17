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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Search
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
import com.pawsmedic.shared.core.designsystem.theme.PawMedicColors
import com.pawsmedic.shared.core.designsystem.theme.PawMedicSemanticColors
import com.pawsmedic.shared.core.designsystem.language.AppLanguage
import com.pawsmedic.shared.core.designsystem.language.LocalLanguagePreference
import com.pawsmedic.shared.core.designsystem.language.petCatalogLabel
import com.pawsmedic.shared.core.designsystem.language.petAgeLabel
import com.pawsmedic.features.appointments.presentation.localizeAppointmentError

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
                    onRetry = viewModel::retryLoadBookingData,
                    onBack = onBackClick
                )
            }

            BookingStep.SELECT_PET -> {
                SelectPetStep(
                    uiState = uiState,
                    onPetSelected = viewModel::selectPet,
                    onNext = viewModel::nextStep,
                    onRetry = viewModel::retryLoadBookingData,
                    onBack = viewModel::previousStep
                )
            }

            BookingStep.SELECT_DATE_TIME -> {
                SelectDateTimeStep(
                    uiState = uiState,
                    onDateSelected = viewModel::selectDate,
                    onTimeSlotSelected = viewModel::selectTimeSlot,
                    onNext = viewModel::nextStep,
                    onBack = viewModel::previousStep
                )
            }

            BookingStep.CONFIRM -> {
                ConfirmBookingStep(
                    uiState = uiState,
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
    onRetry: () -> Unit,
    onBack: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val normalizedQuery = searchQuery.trim()
    val visibleServices = uiState.services.filter { service ->
        normalizedQuery.isBlank() ||
            service.title.contains(normalizedQuery, ignoreCase = true) ||
            service.description.contains(normalizedQuery, ignoreCase = true)
    }
    val language = LocalLanguagePreference.current?.selected ?: AppLanguage.SPANISH
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PawMedicSemanticColors.current.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        PawMedicTopBar(
            title = language.text("Seleccionar Servicio", "Select service"),
            onBackClick = onBack
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = language.text("Selecciona el tipo de servicio que requiere tu mascota en ${uiState.selectedClinicName}:", "Select the service your pet needs at ${uiState.selectedClinicName}:"),
            fontSize = 14.sp,
            color = PawMedicSemanticColors.current.secondaryText,
            lineHeight = 20.sp
        )

        Spacer(modifier = Modifier.height(20.dp))

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = "Buscar servicios")
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Clear, contentDescription = "Limpiar búsqueda")
                    }
                }
            },
            placeholder = { Text("Buscar servicio") }
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (uiState.isLoadingServices) {
            Text(language.text("Cargando servicios activos…", "Loading available services…"), fontSize = 14.sp, color = PawMedicSemanticColors.current.secondaryText)
        } else if (uiState.services.isEmpty()) {
            Text(
                text = language.text("Todavía no hay servicios disponibles para esta clínica.", "No services are available at this clinic yet."),
                fontSize = 14.sp,
                color = PawMedicSemanticColors.current.secondaryText,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        visibleServices.forEach { service ->
            val isSelected = service.id == uiState.selectedService?.id
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (isSelected) PawMedicSemanticColors.current.softAccent else PawMedicSemanticColors.current.surface)
                    .border(
                        width = if (isSelected) 2.dp else 1.dp,
                        color = if (isSelected) PawMedicColors.Teal600 else PawMedicSemanticColors.current.separator,
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
                                .background(PawMedicSemanticColors.current.mediumAccent),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.MedicalServices,
                                contentDescription = null,
                                tint = PawMedicSemanticColors.current.accent,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column {
                            Text(
                                text = service.title,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = PawMedicSemanticColors.current.text
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = service.description,
                                fontSize = 12.sp,
                                color = PawMedicSemanticColors.current.hint,
                                lineHeight = 16.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = language.text("⏱ Duración: ${service.durationMinutes} minutos", "⏱ Duration: ${service.durationMinutes} minutes"),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = PawMedicSemanticColors.current.accent
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(if (isSelected) PawMedicColors.Teal600 else PawMedicSemanticColors.current.separator)
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        uiState.errorMessage?.let { error ->
            Text(localizeAppointmentError(error, language), fontSize = 13.sp, color = PawMedicColors.Error)
            Spacer(modifier = Modifier.height(8.dp))
            PawMedicSecondaryButton(text = language.text("Reintentar", "Retry"), onClick = onRetry)
        }

        Spacer(modifier = Modifier.height(24.dp))

        PawMedicPrimaryButton(
            text = language.text("Continuar", "Continue"),
            onClick = onNext,
            enabled = visibleServices.isNotEmpty()
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
    onRetry: () -> Unit,
    onBack: () -> Unit
) {
    val language = LocalLanguagePreference.current?.selected ?: AppLanguage.SPANISH
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PawMedicSemanticColors.current.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        PawMedicTopBar(
            title = language.text("Seleccionar Mascota", "Select pet"),
            onBackClick = onBack
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = language.text("¿Para cuál de tus mascotas deseas agendar el servicio médico?", "Which pet would you like to book the service for?"),
            fontSize = 14.sp,
            color = PawMedicSemanticColors.current.secondaryText,
            lineHeight = 20.sp
        )

        Spacer(modifier = Modifier.height(20.dp))

        if (uiState.isLoadingPets) {
            Text(language.text("Cargando tus mascotas…", "Loading your pets…"), fontSize = 14.sp, color = PawMedicSemanticColors.current.secondaryText)
        } else if (uiState.userPets.isEmpty()) {
            Text(
                text = language.text("No tienes mascotas registradas para reservar una cita.", "You have no registered pets to book an appointment for."),
                fontSize = 14.sp,
                color = PawMedicSemanticColors.current.secondaryText,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        uiState.userPets.forEach { pet ->
            val isSelected = pet.id == uiState.selectedPet?.id
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (isSelected) PawMedicSemanticColors.current.softAccent else PawMedicSemanticColors.current.surface)
                    .border(
                        width = if (isSelected) 2.dp else 1.dp,
                        color = if (isSelected) PawMedicColors.Teal600 else PawMedicSemanticColors.current.separator,
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
                            photo_url = pet.photo_url,
                            species = pet.species,
                            size = 56.dp
                        )

                        Spacer(modifier = Modifier.width(14.dp))

                        Column {
                            Text(
                                text = pet.name,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = PawMedicSemanticColors.current.text
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${petCatalogLabel(pet.species, language)} • ${pet.breed?.let { petCatalogLabel(it, language) } ?: language.text("Mestizo", "Mixed breed")}",
                                fontSize = 13.sp,
                                color = PawMedicSemanticColors.current.hint
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = pet.age?.let { petAgeLabel(it, language) } ?: language.text("Edad no registrada", "Age not recorded"),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = PawMedicSemanticColors.current.accent
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
                                text = language.text("Elegido", "Selected"),
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

        uiState.errorMessage?.let { error ->
            Text(localizeAppointmentError(error, language), fontSize = 13.sp, color = PawMedicColors.Error)
            Spacer(modifier = Modifier.height(8.dp))
            PawMedicSecondaryButton(text = language.text("Reintentar", "Retry"), onClick = onRetry)
        }

        Spacer(modifier = Modifier.height(28.dp))

        PawMedicPrimaryButton(
            text = language.text("Continuar", "Continue"),
            onClick = onNext,
            enabled = uiState.userPets.isNotEmpty()
        )

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
private fun BookingPetAvatar(
    photo_url: String?,
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

    if (!photo_url.isNullOrBlank()) {
        AsyncBookingPetImage(
            photo_url = photo_url,
            fallbackEmoji = emoji,
            size = size
        )
    } else {
        Box(
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
                .background(PawMedicSemanticColors.current.mediumAccent),
            contentAlignment = Alignment.Center
        ) {
            Text(text = emoji, fontSize = (size.value * 0.45f).sp)
        }
    }
}

// --- STEP 3: SELECT DATE & TIME ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SelectDateTimeStep(
    uiState: BookingUiState,
    onDateSelected: (String, Long?, String) -> Unit,
    onTimeSlotSelected: (com.pawsmedic.features.appointments.domain.model.AvailableAppointmentSlot) -> Unit,
    onNext: () -> Unit,
    onBack: () -> Unit
) {
    val language = LocalLanguagePreference.current?.selected ?: AppLanguage.SPANISH
    var showDatePickerModal by remember { mutableStateOf(false) }

    if (showDatePickerModal) {
        val today = bookingToday()
        val limit = bookingDateLimit(today)
        val selectableDates = remember(today) {
            object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean =
                    isBookableDate(convertMillisToIsoDateString(utcTimeMillis), today)

                override fun isSelectableYear(year: Int): Boolean =
                    year in today.substring(0, 4).toInt()..limit.substring(0, 4).toInt()
            }
        }
        DatePickerModal(
            onDateSelected = { selectedMillis ->
                if (selectedMillis != null) {
                    val isoDate = convertMillisToIsoDateString(selectedMillis)
                    val displayDate = formatIsoToDisplayDate(isoDate)
                    onDateSelected(isoDate, selectedMillis, displayDate)
                }
                showDatePickerModal = false
            },
            onDismiss = { showDatePickerModal = false },
            yearRange = today.substring(0, 4).toInt()..limit.substring(0, 4).toInt(),
            selectableDates = selectableDates
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PawMedicSemanticColors.current.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        PawMedicTopBar(
            title = language.text("Seleccionar Fecha y Hora", "Select date and time"),
            onBackClick = onBack
        )

        Spacer(modifier = Modifier.height(12.dp))

        // --- CALENDAR DATE SELECTION ---
        Text(
            text = language.text("Fecha de la cita (desde mañana y hasta dentro de dos años)", "Appointment date (from tomorrow up to two years from now)"),
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = PawMedicSemanticColors.current.text,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(PawMedicSemanticColors.current.surface)
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
                        tint = PawMedicSemanticColors.current.accent,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = uiState.selectedDateLabel.ifBlank { language.text("Seleccionar fecha", "Select date") },
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = PawMedicSemanticColors.current.accent
                        )
                        Text(
                            text = language.text("Toca para elegir fecha en el calendario (1 mes disponible)", "Tap to choose a date in the calendar (1 month available)"),
                            fontSize = 12.sp,
                            color = PawMedicSemanticColors.current.hint
                        )
                    }
                }
                Text(text = "▼", fontSize = 12.sp, color = PawMedicSemanticColors.current.accent)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- TIME SELECTION SECTION ---
        Text(
            text = language.text("Horarios disponibles", "Available times"),
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = PawMedicSemanticColors.current.text,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        when {
            uiState.isLoadingSlots -> Text(
                text = language.text("Consultando horarios de la clínica…", "Checking clinic availability…"),
                fontSize = 14.sp,
                color = PawMedicSemanticColors.current.secondaryText
            )
            uiState.selectedDate.isBlank() -> Text(
                text = language.text("Selecciona una fecha para consultar los horarios disponibles.", "Select a date to see available times."),
                fontSize = 14.sp,
                color = PawMedicSemanticColors.current.secondaryText
            )
            uiState.availableTimeSlots.isEmpty() -> Text(
                text = language.text("No hay horarios disponibles para esta fecha.", "No times are available on this date."),
                fontSize = 14.sp,
                color = PawMedicSemanticColors.current.secondaryText
            )
            else -> uiState.availableTimeSlots.forEach { slot ->
                val selected = slot.startsAt == uiState.selectedSlot?.startsAt
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (selected) PawMedicSemanticColors.current.softAccent else PawMedicSemanticColors.current.surface)
                        .border(
                            width = if (selected) 2.dp else 1.dp,
                            color = if (selected) PawMedicColors.Teal600 else PawMedicSemanticColors.current.separator,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable { onTimeSlotSelected(slot) }
                        .padding(14.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            tint = PawMedicSemanticColors.current.accent,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = slot.label,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = PawMedicSemanticColors.current.text
                        )
                    }
                }
            }
        }

        if (uiState.errorMessage != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = localizeAppointmentError(uiState.errorMessage ?: "", language),
                fontSize = 13.sp,
                color = PawMedicColors.Error,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        PawMedicPrimaryButton(
            text = language.text("Continuar", "Continue"),
            onClick = onNext
        )

        Spacer(modifier = Modifier.height(20.dp))
    }
}

// --- STEP 4: CONFIRM BOOKING ---
@Composable
private fun ConfirmBookingStep(
    uiState: BookingUiState,
    onSubmit: () -> Unit,
    onBack: () -> Unit
) {
    val language = LocalLanguagePreference.current?.selected ?: AppLanguage.SPANISH
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PawMedicSemanticColors.current.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        PawMedicTopBar(
            title = language.text("Confirmar Cita", "Confirm appointment"),
            onBackClick = onBack
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = language.text("Revisa los detalles de tu solicitud antes de enviarla a la veterinaria.", "Review your request before sending it to the clinic."),
            fontSize = 14.sp,
            color = PawMedicSemanticColors.current.secondaryText,
            lineHeight = 20.sp
        )

        Spacer(modifier = Modifier.height(20.dp))

        // SUMMARY CARD
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(PawMedicSemanticColors.current.surface)
                .border(1.dp, PawMedicSemanticColors.current.separator, RoundedCornerShape(20.dp))
                .padding(18.dp)
        ) {
            Column {
                SummaryItem(
                    icon = "🏥",
                    label = language.text("Veterinaria", "Clinic"),
                    value = uiState.selectedClinicName
                )
                Spacer(modifier = Modifier.height(14.dp))
                SummaryItem(
                    icon = "🐾",
                    label = language.text("Mascota", "Pet"),
                    value = uiState.selectedPet?.let { pet ->
                        "${pet.name} (${petCatalogLabel(pet.species, language)}${pet.breed?.let { " • ${petCatalogLabel(it, language)}" }.orEmpty()})"
                    }.orEmpty()
                )
                Spacer(modifier = Modifier.height(14.dp))
                SummaryItem(
                    icon = "🩺",
                    label = language.text("Servicio", "Service"),
                    value = uiState.selectedService?.title.orEmpty()
                )
                Spacer(modifier = Modifier.height(14.dp))
                SummaryItem(
                    icon = "📅",
                    label = language.text("Fecha y Hora", "Date and time"),
                    value = "${uiState.selectedDateLabel} • ${uiState.selectedTimeSlot}"
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

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
                    text = language.text("La cita debe ser solicitada primero. Estará sujeta a la aprobación de la clínica. Podrás consultar su estado en tus citas.", "Your appointment request is subject to clinic approval. You can check its status in your appointments."),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF92400E),
                    lineHeight = 17.sp
                )
            }
        }

        uiState.errorMessage?.let { error ->
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = localizeAppointmentError(error, language),
                fontSize = 13.sp,
                color = PawMedicColors.Error
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        PawMedicPrimaryButton(
            text = language.text("Solicitar cita", "Request appointment"),
            onClick = onSubmit,
            isLoading = uiState.isLoading
        )

        Spacer(modifier = Modifier.height(12.dp))

        PawMedicSecondaryButton(
            text = language.text("Regresar", "Back"),
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
                .background(PawMedicSemanticColors.current.softAccent),
            contentAlignment = Alignment.Center
        ) {
            Text(text = icon, fontSize = 20.sp)
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column {
            Text(
                text = label,
                fontSize = 12.sp,
                color = PawMedicSemanticColors.current.hint
            )
            Text(
                text = value,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = PawMedicSemanticColors.current.text
            )
        }
    }
}

// --- STEP 5: BOOKING SUCCESS ---
@Composable
private fun BookingSuccessStep(
    onComplete: () -> Unit
) {
    val language = LocalLanguagePreference.current?.selected ?: AppLanguage.SPANISH
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PawMedicSemanticColors.current.background)
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
                text = language.text("¡Solicitud Enviada!", "Request sent!"),
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = PawMedicSemanticColors.current.text
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = language.text("Tu cita ha sido solicitada correctamente. La clínica revisará la disponibilidad y te notificará pronto.", "Your appointment has been requested. The clinic will check availability and notify you soon."),
                fontSize = 14.sp,
                color = PawMedicSemanticColors.current.secondaryText,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(32.dp))

            PawMedicPrimaryButton(
                text = language.text("Ver mis citas", "View my appointments"),
                onClick = onComplete
            )
        }
    }
}

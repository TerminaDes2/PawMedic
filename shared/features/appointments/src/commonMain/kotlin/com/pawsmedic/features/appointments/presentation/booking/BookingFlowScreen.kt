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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
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
    var selectedCategory by remember { mutableStateOf("todos") }
    val categories = listOf(
        "todos" to "Todos",
        "consulta" to "Consultas",
        "vacunacion" to "Vacunación",
        "desparasitacion" to "Desparasitación",
        "estetica" to "Estética",
        "cirugia" to "Cirugías",
        "diagnostico" to "Diagnóstico",
        "otros" to "Otros servicios"
    )
    val normalizedQuery = searchQuery.trim().lowercase()
    val filteredServices = uiState.services.filter { service ->
        val matchesCategory = selectedCategory == "todos" || service.category == selectedCategory
        val matchesQuery = normalizedQuery.isBlank() ||
            service.title.lowercase().contains(normalizedQuery) ||
            service.description.lowercase().contains(normalizedQuery)
        matchesCategory && matchesQuery
    }
    val popularServices = if (normalizedQuery.isBlank()) {
        filteredServices.take(3)
    } else {
        emptyList()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PawMedicColors.BackgroundScreen)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(start = 20.dp, top = 12.dp, end = 20.dp, bottom = 112.dp)
        ) {
        PawMedicTopBar(
            title = "Agendar cita",
            onBackClick = onBack
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "¿Qué servicio necesita tu mascota?",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = PawMedicColors.Gray900
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Selecciona el servicio que deseas agendar.",
            fontSize = 14.sp,
            color = PawMedicColors.Gray600,
            lineHeight = 20.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = "Buscar")
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Clear, contentDescription = "Limpiar búsqueda")
                    }
                }
            },
            placeholder = { Text("Buscar servicio...") },
            shape = RoundedCornerShape(14.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = "Categorías",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = PawMedicColors.Gray900
        )
        Spacer(modifier = Modifier.height(10.dp))

        categories.chunked(2).forEach { rowCategories ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                rowCategories.forEach { (key, label) ->
                    val count = if (key == "todos") {
                        uiState.services.size
                    } else {
                        uiState.services.count { it.category == key }
                    }
                    val isSelected = selectedCategory == key
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (isSelected) PawMedicColors.Teal50 else PawMedicColors.White)
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) PawMedicColors.Teal600 else PawMedicColors.Gray200,
                                shape = RoundedCornerShape(14.dp)
                            )
                            .clickable { selectedCategory = key }
                            .padding(12.dp)
                    ) {
                        Column {
                            Icon(
                                imageVector = Icons.Default.MedicalServices,
                                contentDescription = null,
                                tint = if (isSelected) PawMedicColors.Teal600 else PawMedicColors.Gray600,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = label,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = PawMedicColors.Gray900
                            )
                            Text(
                                text = "$count servicios",
                                fontSize = 11.sp,
                                color = PawMedicColors.Gray600
                            )
                        }
                    }
                }
                if (rowCategories.size == 1) Spacer(modifier = Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.height(10.dp))
        }

        if (uiState.isLoadingServices) {
            Text("Cargando servicios activos...", fontSize = 14.sp, color = PawMedicColors.Gray600)
        } else if (uiState.services.isEmpty()) {
            Text(
                text = "Todavía no hay servicios disponibles para esta clínica.",
                fontSize = 14.sp,
                color = PawMedicColors.Gray600,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        if (popularServices.isNotEmpty()) {
            Text(
                text = "Servicios populares",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = PawMedicColors.Gray900
            )
            Spacer(modifier = Modifier.height(10.dp))
            popularServices.forEach { service ->
                ServiceSelectionCard(
                    service = service,
                    isSelected = service.id == uiState.selectedService?.id,
                    onClick = { onServiceSelected(service) }
                )
                Spacer(modifier = Modifier.height(10.dp))
            }
        }

        val servicesToShow = if (popularServices.isEmpty()) filteredServices else filteredServices.drop(3)
        Text(
            text = if (normalizedQuery.isBlank()) "Todos los servicios" else "Resultados",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = PawMedicColors.Gray900
        )
        Spacer(modifier = Modifier.height(10.dp))
        if (servicesToShow.isEmpty()) {
            Text(
                text = "No encontramos servicios con estos filtros.",
                fontSize = 14.sp,
                color = PawMedicColors.Gray600
            )
        } else {
            servicesToShow.forEach { service ->
                ServiceSelectionCard(
                    service = service,
                    isSelected = service.id == uiState.selectedService?.id,
                    onClick = { onServiceSelected(service) }
                )
                Spacer(modifier = Modifier.height(10.dp))
            }
        }

        uiState.errorMessage?.let { error ->
            Text(error, fontSize = 13.sp, color = PawMedicColors.Error)
            Spacer(modifier = Modifier.height(8.dp))
            PawMedicSecondaryButton(text = "Reintentar", onClick = onRetry)
        }

        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(PawMedicColors.White)
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (uiState.selectedService == null) "Ningún servicio seleccionado" else "1 servicio seleccionado",
                        fontSize = 12.sp,
                        color = PawMedicColors.Gray600
                    )
                    uiState.selectedService?.let {
                        Text(
                            text = it.title,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = PawMedicColors.Gray900
                        )
                    }
                }
                PawMedicPrimaryButton(
                    text = "Continuar",
                    onClick = onNext,
                    enabled = uiState.selectedService != null,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun ServiceSelectionCard(
    service: VeterinaryService,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(if (isSelected) PawMedicColors.Teal50 else PawMedicColors.White)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) PawMedicColors.Teal600 else PawMedicColors.Gray200,
                shape = RoundedCornerShape(14.dp)
            )
            .clickable(onClick = onClick)
            .padding(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = if (isSelected) Icons.Default.Check else Icons.Default.MedicalServices,
                contentDescription = null,
                tint = PawMedicColors.Teal600,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(service.title, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = PawMedicColors.Gray900)
                if (service.description.isNotBlank()) {
                    Text(
                        service.description,
                        fontSize = 12.sp,
                        color = PawMedicColors.Gray600,
                        maxLines = 2
                    )
                }
                Text(
                    text = "Duración: ${service.durationMinutes} min",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = PawMedicColors.Teal600
                )
            }
        }
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

        if (uiState.isLoadingPets) {
            Text("Cargando tus mascotas…", fontSize = 14.sp, color = PawMedicColors.Gray600)
        } else if (uiState.userPets.isEmpty()) {
            Text(
                text = "No tienes mascotas registradas para reservar una cita.",
                fontSize = 14.sp,
                color = PawMedicColors.Gray600,
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
                                text = pet.age ?: "Edad no registrada",
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

        uiState.errorMessage?.let { error ->
            Text(error, fontSize = 13.sp, color = PawMedicColors.Error)
            Spacer(modifier = Modifier.height(8.dp))
            PawMedicSecondaryButton(text = "Reintentar", onClick = onRetry)
        }

        Spacer(modifier = Modifier.height(28.dp))

        PawMedicPrimaryButton(
            text = "Continuar",
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
                .background(PawMedicColors.Teal100),
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
            text = "Fecha de la cita (desde mañana y hasta dentro de dos años)",
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
                            text = uiState.selectedDateLabel.ifBlank { "Seleccionar fecha" },
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
            text = "Horarios disponibles",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = PawMedicColors.Gray800,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        when {
            uiState.isLoadingSlots -> Text(
                text = "Consultando horarios de la clínica…",
                fontSize = 14.sp,
                color = PawMedicColors.Gray600
            )
            uiState.selectedDate.isBlank() -> Text(
                text = "Selecciona una fecha para consultar los horarios disponibles.",
                fontSize = 14.sp,
                color = PawMedicColors.Gray600
            )
            uiState.availableTimeSlots.isEmpty() -> Text(
                text = "No hay horarios disponibles para esta fecha.",
                fontSize = 14.sp,
                color = PawMedicColors.Gray600
            )
            else -> uiState.availableTimeSlots.forEach { slot ->
                val selected = slot.startsAt == uiState.selectedSlot?.startsAt
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (selected) PawMedicColors.Teal50 else PawMedicColors.White)
                        .border(
                            width = if (selected) 2.dp else 1.dp,
                            color = if (selected) PawMedicColors.Teal600 else PawMedicColors.Gray200,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable { onTimeSlotSelected(slot) }
                        .padding(14.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            tint = PawMedicColors.Teal600,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = slot.label,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = PawMedicColors.Gray900
                        )
                    }
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
                    value = uiState.selectedPet?.let { pet ->
                        "${pet.name} (${pet.species}${pet.breed?.let { " • $it" }.orEmpty()})"
                    }.orEmpty()
                )
                Spacer(modifier = Modifier.height(14.dp))
                SummaryItem(
                    icon = "🩺",
                    label = "Servicio",
                    value = uiState.selectedService?.title.orEmpty()
                )
                Spacer(modifier = Modifier.height(14.dp))
                SummaryItem(
                    icon = "📅",
                    label = "Fecha y Hora",
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
                    text = "La cita debe ser solicitada primero. Estará sujeta a la aprobación de la clínica. Te notificaremos si es confirmada o rechazada.",
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
                text = error,
                fontSize = 13.sp,
                color = PawMedicColors.Error
            )
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

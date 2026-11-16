package com.pawsmedic.features.appointments.presentation.list

import androidx.compose.foundation.background
import com.pawsmedic.features.appointments.presentation.friendlyErrorMessage
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pawsmedic.features.appointments.domain.model.Appointment
import com.pawsmedic.features.appointments.domain.repository.AppointmentRepository
import com.pawsmedic.shared.core.designsystem.components.PawMedicPrimaryButton
import com.pawsmedic.shared.core.designsystem.components.PawMedicTopBar
import com.pawsmedic.shared.core.designsystem.theme.PawMedicColors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AppointmentsListUiState(
    val appointments: List<Appointment> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class AppointmentsListViewModel(
    private val repository: AppointmentRepository,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
) {
    private val _uiState = MutableStateFlow(AppointmentsListUiState())
    val uiState: StateFlow<AppointmentsListUiState> = _uiState.asStateFlow()

    init {
        loadAppointments()
    }

    fun loadAppointments() {
        scope.launch {
            _uiState.update { curr -> curr.copy(isLoading = true, errorMessage = null) }
            repository.getAppointments()
                .onSuccess { list ->
                    _uiState.update { curr -> curr.copy(appointments = list, isLoading = false) }
                }
                .onFailure { err ->
                    _uiState.update { curr -> curr.copy(isLoading = false, errorMessage = friendlyErrorMessage(err)) }
                }
        }
    }
}

// ---------------------------------------------------------------------------
// Filtros
// ---------------------------------------------------------------------------

private const val ALL = "todos"

private enum class StatusFilter(val label: String) {
    ALL("Todos los estados"),
    PENDING("Pendientes"),
    ACCEPTED("Aceptadas")
}

private fun Appointment.isPending() = status.contains("Pendiente", ignoreCase = true)
private fun Appointment.isAccepted() = status.equals("Confirmada", ignoreCase = true)

private fun categoryLabel(category: String): String = when (category) {
    ALL -> "Todas las categorías"
    "consulta" -> "Consultas"
    "vacunacion" -> "Vacunación"
    "desparasitacion" -> "Desparasitación"
    "estetica" -> "Estética"
    "cirugia" -> "Cirugías"
    "diagnostico" -> "Diagnóstico"
    "otros" -> "Otros servicios"
    else -> category.replaceFirstChar { it.uppercase() }
}

@Composable
fun AppointmentsListScreen(
    viewModel: AppointmentsListViewModel,
    onStartBooking: () -> Unit,
    onBackClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    // Estado de los filtros (sobrevive a rotación)
    var selectedCategory by rememberSaveable { mutableStateOf(ALL) }
    var selectedPet by rememberSaveable { mutableStateOf(ALL) }
    var selectedStatusName by rememberSaveable { mutableStateOf(StatusFilter.ALL.name) }
    val selectedStatus = StatusFilter.valueOf(selectedStatusName)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(PawMedicColors.BackgroundScreen)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        PawMedicTopBar(
            title = "Mis Citas Médicas",
            onBackClick = onBackClick
        )

        Spacer(modifier = Modifier.height(16.dp))

        when {
            uiState.isLoading -> {
                Text(
                    text = "Cargando tus citas…",
                    fontSize = 14.sp,
                    color = PawMedicColors.Gray500,
                    modifier = Modifier.padding(vertical = 32.dp)
                )
            }
            uiState.errorMessage != null -> {
                Text(
                    text = "${uiState.errorMessage}",
                    fontSize = 14.sp,
                    color = PawMedicColors.Error
                )
                Spacer(modifier = Modifier.height(12.dp))
                PawMedicPrimaryButton(text = "Reintentar", onClick = viewModel::loadAppointments)
            }
            uiState.appointments.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No tienes citas agendadas.",
                        fontSize = 14.sp,
                        color = PawMedicColors.Gray500
                    )
                }
            }
            else -> {
                val all = uiState.appointments

                // Opciones derivadas de las citas existentes
                val categoryOptions = listOf(ALL) + all.map { it.serviceName }.distinct()
                val petOptions = listOf(ALL) + all.map { it.petName }.distinct().sorted()

                // Si una opción ya no existe (p. ej. tras recargar), vuelve a "todos"
                val effectiveCategory = selectedCategory.takeIf { it in categoryOptions } ?: ALL
                val effectivePet = selectedPet.takeIf { it in petOptions } ?: ALL

                val filtered = all.filter { app ->
                    (effectiveCategory == ALL || app.serviceName == effectiveCategory) &&
                            (effectivePet == ALL || app.petName == effectivePet) &&
                            when (selectedStatus) {
                                StatusFilter.ALL -> true
                                StatusFilter.PENDING -> app.isPending()
                                StatusFilter.ACCEPTED -> app.isAccepted()
                            }
                }
                val hasActiveFilters = effectiveCategory != ALL ||
                        effectivePet != ALL ||
                        selectedStatus != StatusFilter.ALL

                // --- Barra de filtros ---
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterDropdownChip(
                        label = categoryLabel(effectiveCategory),
                        isActive = effectiveCategory != ALL,
                        options = categoryOptions.map { it to categoryLabel(it) },
                        selectedKey = effectiveCategory,
                        onSelected = { selectedCategory = it }
                    )
                    FilterDropdownChip(
                        label = if (effectivePet == ALL) "Todas las mascotas" else effectivePet,
                        isActive = effectivePet != ALL,
                        options = petOptions.map { it to if (it == ALL) "Todas las mascotas" else it },
                        selectedKey = effectivePet,
                        onSelected = { selectedPet = it }
                    )
                    FilterDropdownChip(
                        label = selectedStatus.label,
                        isActive = selectedStatus != StatusFilter.ALL,
                        options = StatusFilter.entries.map { it.name to it.label },
                        selectedKey = selectedStatus.name,
                        onSelected = { selectedStatusName = it }
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (filtered.size == 1) "1 cita" else "${filtered.size} citas",
                        modifier = Modifier.weight(1f),
                        fontSize = 13.sp,
                        color = PawMedicColors.Gray600
                    )
                    if (hasActiveFilters) {
                        TextButton(
                            onClick = {
                                selectedCategory = ALL
                                selectedPet = ALL
                                selectedStatusName = StatusFilter.ALL.name
                            }
                        ) {
                            Text(
                                text = "Limpiar filtros",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = PawMedicColors.Teal600
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                if (filtered.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No hay citas que coincidan con los filtros.",
                            fontSize = 14.sp,
                            color = PawMedicColors.Gray500
                        )
                    }
                } else {
                    filtered.forEach { app ->
                        AppointmentDetailCard(app = app)
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        PawMedicPrimaryButton(
            text = "Agendar nueva cita",
            onClick = onStartBooking
        )

        Spacer(modifier = Modifier.height(20.dp))
    }
}

/** Chip desplegable: muestra el valor actual y abre un menú con las opciones. */
@Composable
private fun FilterDropdownChip(
    label: String,
    isActive: Boolean,
    options: List<Pair<String, String>>, // key to label
    selectedKey: String,
    onSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val shape = RoundedCornerShape(50)

    Box {
        Row(
            modifier = Modifier
                .clip(shape)
                .background(if (isActive) PawMedicColors.Teal50 else PawMedicColors.White)
                .border(
                    width = if (isActive) 1.5.dp else 1.dp,
                    color = if (isActive) PawMedicColors.Teal600 else PawMedicColors.Gray300,
                    shape = shape
                )
                .clickable { expanded = true }
                .padding(start = 14.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                fontSize = 13.sp,
                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
                color = if (isActive) PawMedicColors.Teal600 else PawMedicColors.Gray800
            )
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint = if (isActive) PawMedicColors.Teal600 else PawMedicColors.Gray600,
                modifier = Modifier.size(18.dp)
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { (key, text) ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = text,
                            fontSize = 14.sp,
                            fontWeight = if (key == selectedKey) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    onClick = {
                        onSelected(key)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun AppointmentDetailCard(app: Appointment) {
    val isPending = app.isPending()
    val isConfirmed = app.isAccepted()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(PawMedicColors.White)
            .border(1.dp, PawMedicColors.Gray200, RoundedCornerShape(20.dp))
            .padding(18.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(PawMedicColors.Teal50),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = PawMedicColors.Teal600,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = app.businessName,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = PawMedicColors.Gray900
                        )
                        Text(
                            text = app.serviceName,
                            fontSize = 13.sp,
                            color = PawMedicColors.Gray500
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            when {
                                isPending -> Color(0xFFFEF3C7)
                                isConfirmed -> Color(0xFFE6F4EA)
                                else -> PawMedicColors.Gray100
                            }
                        )
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = app.status,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = when {
                            isPending -> Color(0xFF92400E)
                            isConfirmed -> PawMedicColors.Teal600
                            else -> PawMedicColors.Gray700
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(PawMedicColors.Gray50)
                    .padding(12.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Pets,
                            contentDescription = null,
                            tint = PawMedicColors.Gray600,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Mascota: ${app.petName}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = PawMedicColors.Gray800
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            tint = PawMedicColors.Teal600,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Fecha: ${app.date} • ${app.time}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = PawMedicColors.Teal600
                        )
                    }
                }
            }
        }
    }
}
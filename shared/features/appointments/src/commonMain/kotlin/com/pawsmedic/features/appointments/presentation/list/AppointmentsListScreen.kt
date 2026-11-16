package com.pawsmedic.features.appointments.presentation.list

import androidx.compose.foundation.background
import com.pawsmedic.features.appointments.presentation.friendlyErrorMessage
import com.pawsmedic.features.appointments.presentation.localizeAppointmentError
import com.pawsmedic.shared.core.designsystem.language.appointmentStatusLabel
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
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.pawsmedic.shared.core.designsystem.theme.PawMedicSemanticColors
import com.pawsmedic.shared.core.designsystem.language.AppLanguage
import com.pawsmedic.shared.core.designsystem.language.LocalLanguagePreference
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

@Composable
fun AppointmentsListScreen(
    viewModel: AppointmentsListViewModel,
    onStartBooking: () -> Unit,
    onBackClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val language = LocalLanguagePreference.current?.selected ?: AppLanguage.SPANISH
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(PawMedicSemanticColors.current.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        PawMedicTopBar(
            title = language.text("Mis Citas Médicas", "My appointments"),
            onBackClick = onBackClick
        )

        Spacer(modifier = Modifier.height(16.dp))

        when {
            uiState.isLoading -> {
                Text(
                    text = language.text("Cargando tus citas…", "Loading your appointments…"),
                    fontSize = 14.sp,
                    color = PawMedicSemanticColors.current.hint,
                    modifier = Modifier.padding(vertical = 32.dp)
                )
            }
            uiState.errorMessage != null -> {
                Text(
                    text = localizeAppointmentError(uiState.errorMessage.orEmpty(), language),
                    fontSize = 14.sp,
                    color = PawMedicColors.Error
                )
                Spacer(modifier = Modifier.height(12.dp))
                PawMedicPrimaryButton(text = language.text("Reintentar", "Retry"), onClick = viewModel::loadAppointments)
            }
            uiState.appointments.isEmpty() -> {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = language.text("No tienes citas agendadas.", "You have no appointments scheduled."),
                    fontSize = 14.sp,
                    color = PawMedicSemanticColors.current.hint
                )
            }
            }
            else -> {
                uiState.appointments.forEach { app ->
                    AppointmentDetailCard(app = app)
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        PawMedicPrimaryButton(
            text = language.text("Agendar nueva cita", "Book an appointment"),
            onClick = onStartBooking
        )

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
private fun AppointmentDetailCard(app: Appointment) {
    val language = LocalLanguagePreference.current?.selected ?: AppLanguage.SPANISH
    val isPending = app.status.contains("Pendiente", ignoreCase = true)
    val isConfirmed = app.status.equals("Confirmada", ignoreCase = true)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(PawMedicSemanticColors.current.surface)
            .border(1.dp, PawMedicSemanticColors.current.separator, RoundedCornerShape(20.dp))
            .padding(18.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(PawMedicSemanticColors.current.softAccent),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = PawMedicSemanticColors.current.accent,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = app.businessName,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = PawMedicSemanticColors.current.text
                        )
                        Text(
                            text = app.serviceName,
                            fontSize = 13.sp,
                            color = PawMedicSemanticColors.current.hint
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            when {
                                isPending -> Color(0xFFFEF3C7)
                                isConfirmed -> Color(0xFFE6F4EA)
                                else -> PawMedicSemanticColors.current.mutedSurface
                            }
                        )
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = appointmentStatusLabel(app.status, language),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = when {
                            isPending -> Color(0xFF92400E)
                            isConfirmed -> PawMedicColors.Teal600
                            else -> PawMedicSemanticColors.current.text
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(PawMedicSemanticColors.current.subtleSurface)
                    .padding(12.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Pets,
                            contentDescription = null,
                            tint = PawMedicSemanticColors.current.secondaryText,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = language.text("Mascota: ${app.petName}", "Pet: ${app.petName}"),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = PawMedicSemanticColors.current.text
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            tint = PawMedicSemanticColors.current.accent,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = language.text("Fecha: ${app.date} • ${app.time}", "Date: ${app.date} • ${app.time}"),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = PawMedicSemanticColors.current.accent
                        )
                    }
                }
            }
        }
    }
}

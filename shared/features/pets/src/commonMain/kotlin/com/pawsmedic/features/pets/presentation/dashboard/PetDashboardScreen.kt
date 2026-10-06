package com.pawsmedic.features.pets.presentation.dashboard

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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pawsmedic.features.appointments.domain.model.Appointment
import com.pawsmedic.features.appointments.presentation.list.AppointmentsListUiState
import com.pawsmedic.features.appointments.presentation.list.AppointmentsListViewModel
import com.pawsmedic.features.pets.domain.model.Pet
import com.pawsmedic.features.pets.presentation.PetListUiState
import com.pawsmedic.features.pets.presentation.PetListViewModel
import com.pawsmedic.features.pets.presentation.components.PetAvatarImage
import com.pawsmedic.shared.core.designsystem.components.PawMedicPrimaryButton
import com.pawsmedic.shared.core.designsystem.theme.PawMedicColors
import com.pawsmedic.shared.core.designsystem.theme.PawMedicTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun PetDashboardScreen(
    petViewModel: PetListViewModel,
    appointmentsViewModel: AppointmentsListViewModel,
    userName: String = "María",
    onNavigateToPetsList: () -> Unit,
    onNavigateToAddPet: () -> Unit,
    onNavigateToPetDetail: (String) -> Unit,
    onNavigateToAppointments: () -> Unit,
    onNavigateToMedicalHistory: () -> Unit,
    onNavigateToBookAppointment: () -> Unit,
    modifier: Modifier = Modifier
) {
    val petState by petViewModel.uiState.collectAsState()
    val appointmentsState by appointmentsViewModel.uiState.collectAsState()

    PetDashboardContent(
        petState = petState,
        appointmentsState = appointmentsState,
        userName = userName,
        onNavigateToPetsList = onNavigateToPetsList,
        onNavigateToAddPet = onNavigateToAddPet,
        onNavigateToPetDetail = onNavigateToPetDetail,
        onNavigateToAppointments = onNavigateToAppointments,
        onNavigateToMedicalHistory = onNavigateToMedicalHistory,
        onNavigateToBookAppointment = onNavigateToBookAppointment,
        modifier = modifier
    )
}

@Composable
fun PetDashboardContent(
    petState: PetListUiState,
    appointmentsState: AppointmentsListUiState,
    userName: String = "María",
    onNavigateToPetsList: () -> Unit = {},
    onNavigateToAddPet: () -> Unit = {},
    onNavigateToPetDetail: (String) -> Unit = {},
    onNavigateToAppointments: () -> Unit = {},
    onNavigateToMedicalHistory: () -> Unit = {},
    onNavigateToBookAppointment: () -> Unit = {},
    modifier: Modifier = Modifier
) {

    val selectedPet = petState.selectedPet ?: petState.pets.firstOrNull()
    val appointments = appointmentsState.appointments
    val pendingAppointments = appointments.filter { it.status.contains("Pendiente", ignoreCase = true) }
    val pendingCount = pendingAppointments.size

    var showNotificationsModal by remember { mutableStateOf(false) }

    if (showNotificationsModal) {
        AlertDialog(
            onDismissRequest = { showNotificationsModal = false },
            title = {
                Text(
                    text = "Notificaciones de Citas ($pendingCount)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Column {
                    if (pendingAppointments.isEmpty()) {
                        Text(
                            text = "No tienes citas pendientes por aprobación.",
                            fontSize = 14.sp,
                            color = PawMedicColors.Gray600
                        )
                    } else {
                        pendingAppointments.forEach { app ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFFFEF3C7))
                                    .padding(12.dp)
                            ) {
                                Column {
                                    Text(
                                        text = "⏳ ${app.serviceName}",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF92400E)
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "${app.businessName} • ${app.petName}",
                                        fontSize = 12.sp,
                                        color = PawMedicColors.Gray700
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Fecha: ${app.date} • ${app.time}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = PawMedicColors.Teal600
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showNotificationsModal = false }) {
                    Text("Cerrar", fontWeight = FontWeight.Bold, color = PawMedicColors.Teal600)
                }
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(PawMedicColors.BackgroundScreen)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // --- TOP USER HEADER ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(PawMedicColors.Teal100),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "👩🏻", fontSize = 24.sp)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Hola, $userName 👋",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = PawMedicColors.Gray900
                    )
                    Text(
                        text = "¡Tu mascota te lo agradecerá hoy!",
                        fontSize = 13.sp,
                        color = PawMedicColors.Gray500
                    )
                }
            }

            // NOTIFICATION BELL WITH PENDING BADGE
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(PawMedicColors.White)
                    .border(1.dp, PawMedicColors.Gray200, CircleShape)
                    .clickable { showNotificationsModal = true },
                contentAlignment = Alignment.Center
            ) {
                Text(text = "🔔", fontSize = 20.sp)
                if (pendingCount > 0) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(PawMedicColors.Error),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = pendingCount.toString(),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = PawMedicColors.White
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // --- ACTIVE PET BANNER ---
        if (selectedPet != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(PawMedicColors.Teal50)
                    .border(1.dp, PawMedicColors.Teal200, RoundedCornerShape(20.dp))
                    .clickable { onNavigateToPetDetail(selectedPet.id) }
                    .padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        PetAvatarImage(
                            photoUrl = selectedPet.photoUrl,
                            species = selectedPet.species,
                            size = 64.dp
                        )
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                text = selectedPet.name,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = PawMedicColors.Gray900
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${selectedPet.species} • ${selectedPet.breed ?: "Mestizo"} • ${selectedPet.age ?: "2 años"}",
                                fontSize = 13.sp,
                                color = PawMedicColors.Gray600
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(PawMedicColors.Teal600)
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "Activo",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = PawMedicColors.White
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- QUICK ACCESS SECTION ---
        Text(
            text = "Acceso Rápido",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = PawMedicColors.Gray900
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            QuickAccessCard(
                icon = "🐾",
                title = "Mis mascotas",
                backgroundColor = Color(0xFFE6F4EA),
                onClick = onNavigateToPetsList,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(10.dp))
            QuickAccessCard(
                icon = "📅",
                title = "Mis citas",
                backgroundColor = Color(0xFFFEF7E0),
                onClick = onNavigateToAppointments,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(10.dp))
            QuickAccessCard(
                icon = "📄",
                title = "Historial",
                backgroundColor = Color(0xFFF3E8FF),
                onClick = onNavigateToMedicalHistory,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- UPCOMING APPOINTMENTS SECTION ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Próximas Citas (${appointments.size})",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = PawMedicColors.Gray900
            )
            Text(
                text = "Ver todas",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = PawMedicColors.Teal600,
                modifier = Modifier.clickable { onNavigateToAppointments() }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (appointments.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(PawMedicColors.White)
                    .border(1.dp, PawMedicColors.Gray200, RoundedCornerShape(16.dp))
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No tienes citas agendadas aún.",
                    fontSize = 13.sp,
                    color = PawMedicColors.Gray500
                )
            }
        } else {
            appointments.take(2).forEachIndexed { idx, app ->
                AppointmentCard(
                    petName = app.petName,
                    doctor = "${app.serviceName} • ${app.businessName}",
                    date = "${app.date} • ${app.time}",
                    status = app.status,
                    isHighlight = idx == 0
                )
                Spacer(modifier = Modifier.height(10.dp))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        PawMedicPrimaryButton(
            text = "Agendar cita",
            onClick = onNavigateToBookAppointment
        )

        Spacer(modifier = Modifier.height(24.dp))

        // --- VETERINARIES AVAILABLE ---
        Text(
            text = "Veterinarias Disponibles",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = PawMedicColors.Gray900
        )

        Spacer(modifier = Modifier.height(12.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(PawMedicColors.White)
                .border(1.dp, PawMedicColors.Gray200, RoundedCornerShape(16.dp))
                .clickable { onNavigateToBookAppointment() }
                .padding(16.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Clinipet Central",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = PawMedicColors.Gray900
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "⭐", fontSize = 14.sp)
                        Text(
                            text = "4.9",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = PawMedicColors.Gray900,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Calle 100 #15-45, Bog.",
                    fontSize = 13.sp,
                    color = PawMedicColors.Gray500
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Urgencias, Cirugía, Consulta",
                    fontSize = 12.sp,
                    color = PawMedicColors.Gray400
                )
            }
        }
    }
}

@Composable
private fun QuickAccessCard(
    icon: String,
    title: String,
    backgroundColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .clickable { onClick() }
            .padding(vertical = 16.dp, horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = icon, fontSize = 26.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = title,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = PawMedicColors.Gray800
        )
    }
}

@Composable
private fun AppointmentCard(
    petName: String,
    doctor: String,
    date: String,
    status: String,
    isHighlight: Boolean
) {
    val isPending = status.contains("Pendiente", ignoreCase = true)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(PawMedicColors.White)
            .border(1.dp, PawMedicColors.Gray200, RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(PawMedicColors.Teal50),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "📅", fontSize = 20.sp)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = petName,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = PawMedicColors.Gray900
                    )
                    Text(
                        text = doctor,
                        fontSize = 12.sp,
                        color = PawMedicColors.Gray500
                    )
                    Text(
                        text = date,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isHighlight) Color(0xFFD97706) else PawMedicColors.Teal600,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isPending) Color(0xFFFEF3C7) else Color(0xFFE6F4EA))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = if (isPending) "Pendiente" else "Confirmada",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isPending) Color(0xFF92400E) else PawMedicColors.Teal600
                )
            }
        }
    }
}

@Preview
@Composable
fun PetDashboardScreenPreview() {
    val samplePets = listOf(
        Pet(
            id = "pet-1",
            ownerId = "owner-1",
            name = "Tobías",
            species = "Gato",
            breed = "Persa Mestizo",
            age = "2 años",
            gender = "Macho",
            allergies = "Alergia a la penicilina",
            photoUrl = null,
            medicalId = "PM-8942-A",
            weight = "4.2 kg",
            isInsured = true
        ),
        Pet(
            id = "pet-2",
            ownerId = "owner-1",
            name = "Joey",
            species = "Perro",
            breed = "Australian Shepherd",
            age = "3 años",
            gender = "Macho",
            allergies = null,
            photoUrl = null,
            medicalId = "PM-1204-B",
            weight = "15 kg",
            isInsured = true
        )
    )

    val sampleAppointments = listOf(
        Appointment(
            id = "app-1",
            petId = "pet-1",
            petName = "Tobías",
            businessId = "biz-1",
            businessName = "Clinipet Central",
            serviceId = "serv-1",
            serviceName = "Consulta General",
            date = "24 Oct 2023",
            time = "10:00 AM",
            notes = "Chequeo de rutina",
            status = "Confirmada"
        ),
        Appointment(
            id = "app-2",
            petId = "pet-1",
            petName = "Tobías",
            businessId = "biz-1",
            businessName = "Clinipet Central",
            serviceId = "serv-2",
            serviceName = "Vacunación",
            date = "28 Oct 2023",
            time = "03:30 PM",
            notes = null,
            status = "Pendiente de aprobación"
        )
    )

    PawMedicTheme {
        PetDashboardContent(
            petState = PetListUiState(
                pets = samplePets,
                selectedPet = samplePets.first()
            ),
            appointmentsState = AppointmentsListUiState(
                appointments = sampleAppointments
            )
        )
    }
}

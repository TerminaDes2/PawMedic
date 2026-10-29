package com.pawsmedic.features.pets.presentation.navigation

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Icon
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.FolderShared
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Pets
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pawsmedic.features.appointments.data.datasource.SupabaseAppointmentDataSource
import com.pawsmedic.features.appointments.data.repository.DefaultAppointmentRepository
import com.pawsmedic.features.appointments.presentation.booking.BookingFlowScreen
import com.pawsmedic.features.appointments.presentation.booking.BookingViewModel
import com.pawsmedic.features.appointments.presentation.list.AppointmentsListScreen
import com.pawsmedic.features.appointments.presentation.list.AppointmentsListViewModel
import com.pawsmedic.features.businesses.data.datasource.SupabaseBusinessDataSource
import com.pawsmedic.features.businesses.data.repository.DefaultBusinessRepository
import com.pawsmedic.features.businesses.presentation.VeterinaryDetailViewModel
import com.pawsmedic.features.businesses.presentation.VeterinaryListViewModel
import com.pawsmedic.features.businesses.presentation.detail.VeterinaryDetailScreen
import com.pawsmedic.features.businesses.presentation.list.VeterinaryListScreen
import com.pawsmedic.features.medical_records.data.datasource.MedicalRecordDataSource
import com.pawsmedic.features.medical_records.data.dto.MedicalRecordDto
import com.pawsmedic.features.medical_records.data.repository.DefaultMedicalRecordRepository
import com.pawsmedic.features.medical_records.presentation.MedicalHistoryScreen
import com.pawsmedic.features.medical_records.presentation.MedicalHistoryViewModel
import com.pawsmedic.features.pets.domain.repository.PetRepository
import com.pawsmedic.features.pets.domain.usecase.AddPetUseCase
import com.pawsmedic.features.pets.domain.usecase.DeletePetUseCase
import com.pawsmedic.features.pets.domain.usecase.GetPetByIdUseCase
import com.pawsmedic.features.pets.domain.usecase.GetPetsUseCase
import com.pawsmedic.features.pets.domain.usecase.UpdatePetUseCase
import com.pawsmedic.features.pets.presentation.PetDetailViewModel
import com.pawsmedic.features.pets.presentation.PetFormViewModel
import com.pawsmedic.features.pets.presentation.PetListViewModel
import com.pawsmedic.features.pets.presentation.dashboard.PetDashboardScreen
import com.pawsmedic.features.pets.presentation.detail.PetDetailScreen
import com.pawsmedic.features.pets.presentation.form.PetFormScreen
import com.pawsmedic.features.pets.presentation.list.PetListScreen
import com.pawsmedic.features.pets.presentation.profile.ProfileScreen
import com.pawsmedic.features.pets.presentation.profile.ProfileViewModel
import com.pawsmedic.shared.core.designsystem.theme.PawMedicColors
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.coroutines.launch

sealed interface PetTabScreen {
    data object List : PetTabScreen
    data class Detail(val petId: String) : PetTabScreen
    data object Add : PetTabScreen
    data class Edit(val petId: String) : PetTabScreen
}

sealed interface CitasTabScreen {
    data object List : CitasTabScreen
    data class Detail(val clinicId: String) : CitasTabScreen
    data class Booking(val clinicId: String, val clinicName: String) : CitasTabScreen
    data object Appointments : CitasTabScreen
}

@Composable
fun PetNavHost(
    repository: PetRepository,
    ownerId: String,
    userName: String,
    supabaseClient: SupabaseClient,
    onSignOut: suspend () -> Unit,
    modifier: Modifier = Modifier
) {
    val pagerState = rememberPagerState(initialPage = 0) { 4 }
    val coroutineScope = rememberCoroutineScope()

    var petTabScreen by remember { mutableStateOf<PetTabScreen>(PetTabScreen.List) }
    var showProfile by remember { mutableStateOf(false) }
    var citasTabScreen by remember { mutableStateOf<CitasTabScreen>(CitasTabScreen.Appointments) }
    var signOutError by remember { mutableStateOf<String?>(null) }
    var isSigningOut by remember { mutableStateOf(false) }

    // Repositories & DataSources
    val medicalRecordRepository = remember(supabaseClient, ownerId) {
        DefaultMedicalRecordRepository(
            SupabaseMedicalRecordDataSource(supabaseClient, ownerId)
        )
    }
    val businessRepository = remember(supabaseClient) {
        DefaultBusinessRepository(SupabaseBusinessDataSource(supabaseClient))
    }
    val appointmentRepository = remember(supabaseClient, ownerId) {
        DefaultAppointmentRepository(SupabaseAppointmentDataSource(supabaseClient, ownerId))
    }

    // Pet Use Cases & ViewModels
    val getPetsUseCase = remember { GetPetsUseCase(repository) }
    val getPetByIdUseCase = remember { GetPetByIdUseCase(repository) }
    val addPetUseCase = remember { AddPetUseCase(repository) }
    val updatePetUseCase = remember { UpdatePetUseCase(repository) }
    val deletePetUseCase = remember { DeletePetUseCase(repository) }

    val petListViewModel = remember { PetListViewModel(getPetsUseCase, ownerId) }
    val profileViewModel = remember(supabaseClient, ownerId) {
        ProfileViewModel(supabaseClient, ownerId)
    }
    val profileState by profileViewModel.uiState.collectAsState()
    val medicalHistoryViewModel = remember { MedicalHistoryViewModel(medicalRecordRepository) }
    val veterinaryListViewModel = remember { VeterinaryListViewModel(businessRepository) }
    val appointmentsListViewModel = remember { AppointmentsListViewModel(appointmentRepository) }

    if (signOutError != null) {
        AlertDialog(
            onDismissRequest = { signOutError = null },
            title = { Text("No se pudo cerrar la sesión") },
            text = { Text(signOutError.orEmpty()) },
            confirmButton = {
                TextButton(
                    onClick = {
                        signOutError = null
                        coroutineScope.launch {
                            isSigningOut = true
                            runCatching { onSignOut() }
                                .onFailure { signOutError = it.message ?: "Inténtalo nuevamente." }
                            isSigningOut = false
                        }
                    }
                ) {
                    Text("Reintentar")
                }
            },
            dismissButton = {
                TextButton(onClick = { signOutError = null }) {
                    Text("Cancelar")
                }
            }
        )
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            // --- BOTTOM NAVIGATION BAR ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(PawMedicColors.White)
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .padding(vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                BottomNavItem(
                    icon = Icons.Default.Home,
                    label = "Inicio",
                    isSelected = pagerState.currentPage == 0,
                    onClick = {
                        coroutineScope.launch { pagerState.animateScrollToPage(0) }
                    }
                )
                BottomNavItem(
                    icon = Icons.Default.Pets,
                    label = "Mascotas",
                    isSelected = pagerState.currentPage == 1,
                    onClick = {
                        petListViewModel.loadPets()
                        petTabScreen = PetTabScreen.List
                        coroutineScope.launch { pagerState.animateScrollToPage(1) }
                    }
                )
                BottomNavItem(
                    icon = Icons.Default.CalendarMonth,
                    label = "Citas",
                    isSelected = pagerState.currentPage == 2,
                    onClick = {
                        appointmentsListViewModel.loadAppointments()
                        citasTabScreen = CitasTabScreen.Appointments
                        coroutineScope.launch { pagerState.animateScrollToPage(2) }
                    }
                )
                BottomNavItem(
                    icon = Icons.Default.FolderShared,
                    label = "Historial",
                    isSelected = pagerState.currentPage == 3,
                    onClick = {
                        medicalHistoryViewModel.loadHistory()
                        coroutineScope.launch { pagerState.animateScrollToPage(3) }
                    }
                )
                BottomNavItem(
                    icon = Icons.Default.Logout,
                    label = if (isSigningOut) "Saliendo" else "Salir",
                    isSelected = false,
                    enabled = !isSigningOut,
                    onClick = {
                        coroutineScope.launch {
                            isSigningOut = true
                            signOutError = null
                            runCatching { onSignOut() }
                                .onFailure { signOutError = it.message ?: "Inténtalo nuevamente." }
                            isSigningOut = false
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(PawMedicColors.BackgroundScreen)
                .padding(innerPadding)
        ) {
            if (showProfile) {
                ProfileScreen(
                    viewModel = profileViewModel,
                    supabaseClient = supabaseClient,
                    pets = petListViewModel.uiState.value.pets,
                    appointmentsCount = appointmentsListViewModel.uiState.value.appointments.size,
                    onBackClick = { showProfile = false }
                )
            } else HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                when (page) {
                    0 -> {
                        PetDashboardScreen(
                            petViewModel = petListViewModel,
                            appointmentsViewModel = appointmentsListViewModel,
                            userName = userName,
                            profileFoto_url = profileState.profile?.foto_url,
                            onNavigateToPetsList = {
                                petListViewModel.loadPets()
                                petTabScreen = PetTabScreen.List
                                coroutineScope.launch { pagerState.animateScrollToPage(1) }
                            },
                            onNavigateToAddPet = {
                                petTabScreen = PetTabScreen.Add
                                coroutineScope.launch { pagerState.animateScrollToPage(1) }
                            },
                            onNavigateToPetDetail = { petId ->
                                petTabScreen = PetTabScreen.Detail(petId)
                                coroutineScope.launch { pagerState.animateScrollToPage(1) }
                            },
                            onNavigateToProfile = { showProfile = true },
                            onNavigateToAppointments = {
                                appointmentsListViewModel.loadAppointments()
                                citasTabScreen = CitasTabScreen.Appointments
                                coroutineScope.launch { pagerState.animateScrollToPage(2) }
                            },
                            onNavigateToMedicalHistory = {
                                medicalHistoryViewModel.loadHistory()
                                coroutineScope.launch { pagerState.animateScrollToPage(3) }
                            }
                        )
                    }

                    1 -> {
                        AnimatedContent(
                            targetState = petTabScreen,
                            modifier = Modifier.fillMaxSize(),
                            label = "PetTabTransition"
                        ) { screen ->
                            when (screen) {
                                PetTabScreen.List -> {
                                    PetListScreen(
                                        viewModel = petListViewModel,
                                        onNavigateToAddPet = { petTabScreen = PetTabScreen.Add },
                                        onNavigateToPetDetail = { petId ->
                                            petTabScreen = PetTabScreen.Detail(petId)
                                        }
                                    )
                                }

                                is PetTabScreen.Detail -> {
                                    val detailViewModel = remember(screen.petId) {
                                        PetDetailViewModel(getPetByIdUseCase, deletePetUseCase, screen.petId)
                                    }
                                    PetDetailScreen(
                                        viewModel = detailViewModel,
                                        onBackClick = { petTabScreen = PetTabScreen.List },
                                        onNavigateToEdit = { petId ->
                                            petTabScreen = PetTabScreen.Edit(petId)
                                        },
                                        onDeleteSuccess = {
                                            petListViewModel.loadPets()
                                            petTabScreen = PetTabScreen.List
                                        }
                                    )
                                }

                                PetTabScreen.Add -> {
                                    val addViewModel = remember {
                                        PetFormViewModel(
                                            addPetUseCase = addPetUseCase,
                                            updatePetUseCase = updatePetUseCase,
                                            getPetByIdUseCase = getPetByIdUseCase,
                                            ownerId = ownerId
                                        )
                                    }
                                    PetFormScreen(
                                        viewModel = addViewModel,
                                        onBackClick = { petTabScreen = PetTabScreen.List },
                                        supabaseClient = supabaseClient,
                                        onSaveSuccess = {
                                            petListViewModel.loadPets()
                                            petTabScreen = PetTabScreen.List
                                        }
                                    )
                                }

                                is PetTabScreen.Edit -> {
                                    val editViewModel = remember(screen.petId) {
                                        PetFormViewModel(
                                            addPetUseCase = addPetUseCase,
                                            updatePetUseCase = updatePetUseCase,
                                            getPetByIdUseCase = getPetByIdUseCase,
                                            initialPetId = screen.petId,
                                            ownerId = ownerId
                                        )
                                    }
                                    PetFormScreen(
                                        viewModel = editViewModel,
                                        onBackClick = { petTabScreen = PetTabScreen.Detail(screen.petId) },
                                        supabaseClient = supabaseClient,
                                        onSaveSuccess = {
                                            petListViewModel.loadPets()
                                            petTabScreen = PetTabScreen.Detail(screen.petId)
                                        }
                                    )
                                }
                            }
                        }
                    }

                    2 -> {
                        AnimatedContent(
                            targetState = citasTabScreen,
                            modifier = Modifier.fillMaxSize(),
                            label = "CitasTabTransition"
                        ) { screen ->
                            when (screen) {
                                CitasTabScreen.List -> {
                                    VeterinaryListScreen(
                                        viewModel = veterinaryListViewModel,
                                        onNavigateToDetail = { clinicId ->
                                            citasTabScreen = CitasTabScreen.Detail(clinicId)
                                        }
                                    )
                                }

                                is CitasTabScreen.Detail -> {
                                    val detailVm = remember(screen.clinicId) {
                                        VeterinaryDetailViewModel(businessRepository, screen.clinicId)
                                    }
                                    VeterinaryDetailScreen(
                                        viewModel = detailVm,
                                        onBackClick = { citasTabScreen = CitasTabScreen.List },
                                        onStartBooking = { clinicId ->
                                            citasTabScreen = CitasTabScreen.Booking(
                                                clinicId = clinicId,
                                                clinicName = detailVm.uiState.value.clinic?.name.orEmpty()
                                            )
                                        }
                                    )
                                }

                                is CitasTabScreen.Booking -> {
                                    val bookingVm = remember(screen.clinicId) {
                                        BookingViewModel(
                                            repository = appointmentRepository,
                                            clinicId = screen.clinicId,
                                            clinicName = screen.clinicName,
                                            ownerId = ownerId,
                                            loadPets = { petOwnerId ->
                                                repository.listPets(petOwnerId).map { pets ->
                                                    pets.map { pet ->
                                                        com.pawsmedic.features.appointments.presentation.booking.BookingPet(
                                                            id = pet.id,
                                                            name = pet.name,
                                                            species = pet.species,
                                                            breed = pet.breed,
                                                            age = pet.age,
                                                            foto_url = pet.foto_url
                                                        )
                                                    }
                                                }
                                            }
                                        )
                                    }
                                    BookingFlowScreen(
                                        viewModel = bookingVm,
                                        onBackClick = { citasTabScreen = CitasTabScreen.Detail(screen.clinicId) },
                                        onBookingComplete = {
                                            appointmentsListViewModel.loadAppointments()
                                            citasTabScreen = CitasTabScreen.Appointments
                                        }
                                    )
                                }

                                CitasTabScreen.Appointments -> {
                                    AppointmentsListScreen(
                                        viewModel = appointmentsListViewModel,
                                        onStartBooking = {
                                            veterinaryListViewModel.loadClinics()
                                            citasTabScreen = CitasTabScreen.List
                                        }
                                    )
                                }
                            }
                        }
                    }

                    3 -> {
                        val historyState by medicalHistoryViewModel.uiState.collectAsState()
                        when {
                            historyState.isLoading -> {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("Cargando el historial clínico…", color = PawMedicColors.Gray600)
                                }
                            }
                            historyState.errorMessage != null -> {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(24.dp),
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = "No se pudo cargar el historial clínico: ${historyState.errorMessage}",
                                        color = PawMedicColors.Error
                                    )
                                    Spacer(Modifier.height(12.dp))
                                    androidx.compose.material3.Button(
                                        onClick = medicalHistoryViewModel::loadHistory
                                    ) {
                                        Text("Reintentar")
                                    }
                                }
                            }
                            else -> MedicalHistoryScreen(viewModel = medicalHistoryViewModel)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BottomNavItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(enabled = enabled) { onClick() }
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isSelected) PawMedicColors.Teal600 else PawMedicColors.Gray500,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) PawMedicColors.Teal600 else PawMedicColors.Gray500
        )
    }
}

private class SupabaseMedicalRecordDataSource(
    private val supabase: SupabaseClient,
    private val ownerId: String
) : MedicalRecordDataSource {
    override suspend fun getAllRecords(): List<MedicalRecordDto> {
        require(ownerId.isNotBlank()) { "No hay una cuenta autenticada para consultar el historial." }
        val pets = supabase.postgrest["pets"]
            .select {
                filter { eq("owner_id", ownerId) }
            }
            .decodeList<OwnedPetRecord>()
        if (pets.isEmpty()) return emptyList()

        return pets.flatMap { pet ->
            getRecordsForPet(pet)
        }.sortedByDescending(MedicalRecordDto::date)
    }

    override suspend fun getRecordsByPet(petId: String): List<MedicalRecordDto> {
        require(ownerId.isNotBlank()) { "No hay una cuenta autenticada para consultar el historial." }
        val pet = supabase.postgrest["pets"]
            .select {
                filter {
                    eq("id", petId)
                    eq("owner_id", ownerId)
                }
            }
            .decodeList<OwnedPetRecord>()
            .singleOrNull() ?: return emptyList()
        return getRecordsForPet(pet)
    }

    private suspend fun getRecordsForPet(pet: OwnedPetRecord): List<MedicalRecordDto> {
        val records = supabase.postgrest["medical_records"]
            .select {
                filter { eq("pet_id", pet.id) }
            }
            .decodeList<MedicalRecordRecord>()

        return records.map { record ->
            val clinicName = supabase.postgrest["tenants"]
                .select {
                    filter { eq("id", record.tenantId) }
                }
                .decodeList<TenantRecord>()
                .singleOrNull()
                ?.name
                ?: "Clínica no disponible (${record.tenantId})"

            MedicalRecordDto(
                id = record.id,
                petId = pet.id,
                petName = pet.name,
                clinicName = clinicName,
                doctorName = "Autor: ${record.authoredBy}",
                date = record.createdAt.substringBefore('T'),
                title = record.title,
                diagnosis = record.content,
                treatment = "No especificado en el registro.",
                vetNotes = null
            )
        }
    }
}

@Serializable
private data class OwnedPetRecord(
    val id: String,
    val name: String
)

@Serializable
private data class MedicalRecordRecord(
    val id: String,
    @SerialName("pet_id") val petId: String,
    @SerialName("tenant_id") val tenantId: String,
    @SerialName("authored_by") val authoredBy: String,
    val title: String,
    val content: String,
    @SerialName("created_at") val createdAt: String
)

@Serializable
private data class TenantRecord(
    val name: String
)

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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.FolderShared
import androidx.compose.material.icons.filled.Home
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
import com.pawsmedic.features.appointments.data.datasource.InMemoryAppointmentDataSource
import com.pawsmedic.features.appointments.data.repository.DefaultAppointmentRepository
import com.pawsmedic.features.appointments.presentation.booking.BookingFlowScreen
import com.pawsmedic.features.appointments.presentation.booking.BookingPet
import com.pawsmedic.features.appointments.presentation.booking.BookingViewModel
import com.pawsmedic.features.appointments.presentation.list.AppointmentsListScreen
import com.pawsmedic.features.appointments.presentation.list.AppointmentsListViewModel
import com.pawsmedic.features.businesses.data.datasource.InMemoryBusinessDataSource
import com.pawsmedic.features.businesses.data.repository.DefaultBusinessRepository
import com.pawsmedic.features.businesses.presentation.VeterinaryDetailViewModel
import com.pawsmedic.features.businesses.presentation.VeterinaryListViewModel
import com.pawsmedic.features.businesses.presentation.detail.VeterinaryDetailScreen
import com.pawsmedic.features.businesses.presentation.list.VeterinaryListScreen
import com.pawsmedic.features.medical_records.data.datasource.InMemoryMedicalRecordDataSource
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
import com.pawsmedic.shared.core.common.rememberPhotoPicker
import com.pawsmedic.shared.core.designsystem.theme.PawMedicColors
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
    data class Booking(val clinicId: String) : CitasTabScreen
    data object Appointments : CitasTabScreen
}

@Composable
fun PetNavHost(
    repository: PetRepository,
    userName: String = "María",
    modifier: Modifier = Modifier
) {
    val pagerState = rememberPagerState(initialPage = 0) { 4 }
    val coroutineScope = rememberCoroutineScope()

    var petTabScreen by remember { mutableStateOf<PetTabScreen>(PetTabScreen.List) }
    var citasTabScreen by remember { mutableStateOf<CitasTabScreen>(CitasTabScreen.List) }

    // Repositories & DataSources
    val medicalRecordRepository = remember { DefaultMedicalRecordRepository(InMemoryMedicalRecordDataSource()) }
    val businessRepository = remember { DefaultBusinessRepository(InMemoryBusinessDataSource()) }
    val appointmentRepository = remember { DefaultAppointmentRepository(InMemoryAppointmentDataSource()) }

    // Pet Use Cases & ViewModels
    val getPetsUseCase = remember { GetPetsUseCase(repository) }
    val getPetByIdUseCase = remember { GetPetByIdUseCase(repository) }
    val addPetUseCase = remember { AddPetUseCase(repository) }
    val updatePetUseCase = remember { UpdatePetUseCase(repository) }
    val deletePetUseCase = remember { DeletePetUseCase(repository) }

    val petListViewModel = remember { PetListViewModel(getPetsUseCase) }
    val medicalHistoryViewModel = remember { MedicalHistoryViewModel(medicalRecordRepository) }
    val veterinaryListViewModel = remember { VeterinaryListViewModel(businessRepository) }
    val appointmentsListViewModel = remember { AppointmentsListViewModel(appointmentRepository) }

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
                        veterinaryListViewModel.loadClinics()
                        citasTabScreen = CitasTabScreen.List
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
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(PawMedicColors.BackgroundScreen)
                .padding(innerPadding)
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                when (page) {
                    0 -> {
                        PetDashboardScreen(
                            petViewModel = petListViewModel,
                            appointmentsViewModel = appointmentsListViewModel,
                            userName = userName,
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
                            onNavigateToAppointments = {
                                appointmentsListViewModel.loadAppointments()
                                citasTabScreen = CitasTabScreen.Appointments
                                coroutineScope.launch { pagerState.animateScrollToPage(2) }
                            },
                            onNavigateToMedicalHistory = {
                                medicalHistoryViewModel.loadHistory()
                                coroutineScope.launch { pagerState.animateScrollToPage(3) }
                            },
                            onNavigateToBookAppointment = {
                                veterinaryListViewModel.loadClinics()
                                citasTabScreen = CitasTabScreen.List
                                coroutineScope.launch { pagerState.animateScrollToPage(2) }
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
                                        PetFormViewModel(addPetUseCase, updatePetUseCase, getPetByIdUseCase)
                                    }
                                    val launchAddPhotoPicker = rememberPhotoPicker { photoUrl ->
                                        addViewModel.onPhotoUrlChanged(photoUrl)
                                    }
                                    PetFormScreen(
                                        viewModel = addViewModel,
                                        onBackClick = { petTabScreen = PetTabScreen.List },
                                        onSaveSuccess = {
                                            petListViewModel.loadPets()
                                            petTabScreen = PetTabScreen.List
                                        },
                                        onPickPhoto = launchAddPhotoPicker
                                    )
                                }

                                is PetTabScreen.Edit -> {
                                    val editViewModel = remember(screen.petId) {
                                        PetFormViewModel(
                                            addPetUseCase = addPetUseCase,
                                            updatePetUseCase = updatePetUseCase,
                                            getPetByIdUseCase = getPetByIdUseCase,
                                            initialPetId = screen.petId
                                        )
                                    }
                                    val launchEditPhotoPicker = rememberPhotoPicker { photoUrl ->
                                        editViewModel.onPhotoUrlChanged(photoUrl)
                                    }
                                    PetFormScreen(
                                        viewModel = editViewModel,
                                        onBackClick = { petTabScreen = PetTabScreen.Detail(screen.petId) },
                                        onSaveSuccess = {
                                            petListViewModel.loadPets()
                                            petTabScreen = PetTabScreen.Detail(screen.petId)
                                        },
                                        onPickPhoto = launchEditPhotoPicker
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
                                            citasTabScreen = CitasTabScreen.Booking(clinicId)
                                        }
                                    )
                                }

                                is CitasTabScreen.Booking -> {
                                    val petState by petListViewModel.uiState.collectAsState()
                                    val bookingPets = petState.pets.map { pet ->
                                        BookingPet(
                                            id = pet.id,
                                            name = pet.name,
                                            species = pet.species,
                                            breed = pet.breed,
                                            age = pet.age?.let { "$it años" },
                                            photoUrl = pet.photoUrl
                                        )
                                    }
                                    val bookingVm = remember(screen.clinicId) {
                                        BookingViewModel(appointmentRepository, initialPets = bookingPets)
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
                                        onStartBooking = { citasTabScreen = CitasTabScreen.List }
                                    )
                                }
                            }
                        }
                    }

                    3 -> {
                        MedicalHistoryScreen(
                            viewModel = medicalHistoryViewModel
                        )
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
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
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

package com.pawsmedic.features.pets.presentation.navigation

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pawsmedic.features.appointments.data.datasource.InMemoryAppointmentDataSource
import com.pawsmedic.features.appointments.data.repository.DefaultAppointmentRepository
import com.pawsmedic.features.appointments.presentation.booking.BookingFlowScreen
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

sealed interface PetScreenState {
    data object Dashboard : PetScreenState
    data object PetList : PetScreenState
    data class PetDetail(val petId: String) : PetScreenState
    data object AddPet : PetScreenState
    data class EditPet(val petId: String) : PetScreenState
    data object VeterinaryList : PetScreenState
    data class VeterinaryDetail(val clinicId: String) : PetScreenState
    data class BookingFlow(val clinicId: String) : PetScreenState
    data object AppointmentsList : PetScreenState
    data object MedicalHistory : PetScreenState
}

@Composable
fun PetNavHost(
    repository: PetRepository,
    modifier: Modifier = Modifier
) {
    var currentScreen by remember { mutableStateOf<PetScreenState>(PetScreenState.Dashboard) }

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

    Box(modifier = modifier.fillMaxSize().background(PawMedicColors.BackgroundScreen)) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.weight(1f)) {
                Crossfade(targetState = currentScreen, modifier = Modifier.fillMaxSize()) { screen ->
                    when (screen) {
                        PetScreenState.Dashboard -> {
                            PetDashboardScreen(
                                petViewModel = petListViewModel,
                                appointmentsViewModel = appointmentsListViewModel,
                                userName = "María",
                                onNavigateToPetsList = {
                                    petListViewModel.loadPets()
                                    currentScreen = PetScreenState.PetList
                                },
                                onNavigateToAddPet = { currentScreen = PetScreenState.AddPet },
                                onNavigateToPetDetail = { petId ->
                                    currentScreen = PetScreenState.PetDetail(petId)
                                },
                                onNavigateToAppointments = {
                                    appointmentsListViewModel.loadAppointments()
                                    currentScreen = PetScreenState.AppointmentsList
                                },
                                onNavigateToMedicalHistory = {
                                    medicalHistoryViewModel.loadHistory()
                                    currentScreen = PetScreenState.MedicalHistory
                                },
                                onNavigateToBookAppointment = {
                                    veterinaryListViewModel.loadClinics()
                                    currentScreen = PetScreenState.VeterinaryList
                                }
                            )
                        }

                        PetScreenState.PetList -> {
                            PetListScreen(
                                viewModel = petListViewModel,
                                onNavigateToAddPet = { currentScreen = PetScreenState.AddPet },
                                onNavigateToPetDetail = { petId ->
                                    currentScreen = PetScreenState.PetDetail(petId)
                                }
                            )
                        }

                        is PetScreenState.PetDetail -> {
                            val detailViewModel = remember(screen.petId) {
                                PetDetailViewModel(getPetByIdUseCase, deletePetUseCase, screen.petId)
                            }
                            PetDetailScreen(
                                viewModel = detailViewModel,
                                onBackClick = { currentScreen = PetScreenState.PetList },
                                onNavigateToEdit = { petId ->
                                    currentScreen = PetScreenState.EditPet(petId)
                                },
                                onDeleteSuccess = {
                                    petListViewModel.loadPets()
                                    currentScreen = PetScreenState.PetList
                                }
                            )
                        }

                        PetScreenState.AddPet -> {
                            val addViewModel = remember {
                                PetFormViewModel(addPetUseCase, updatePetUseCase, getPetByIdUseCase)
                            }
                            val launchAddPhotoPicker = rememberPhotoPicker { photoUrl ->
                                addViewModel.onPhotoUrlChanged(photoUrl)
                            }
                            PetFormScreen(
                                viewModel = addViewModel,
                                onBackClick = { currentScreen = PetScreenState.PetList },
                                onSaveSuccess = {
                                    petListViewModel.loadPets()
                                    currentScreen = PetScreenState.PetList
                                },
                                onPickPhoto = launchAddPhotoPicker
                            )
                        }

                        is PetScreenState.EditPet -> {
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
                                onBackClick = { currentScreen = PetScreenState.PetDetail(screen.petId) },
                                onSaveSuccess = {
                                    petListViewModel.loadPets()
                                    currentScreen = PetScreenState.PetDetail(screen.petId)
                                },
                                onPickPhoto = launchEditPhotoPicker
                            )
                        }

                        PetScreenState.VeterinaryList -> {
                            VeterinaryListScreen(
                                viewModel = veterinaryListViewModel,
                                onNavigateToDetail = { clinicId ->
                                    currentScreen = PetScreenState.VeterinaryDetail(clinicId)
                                }
                            )
                        }

                        is PetScreenState.VeterinaryDetail -> {
                            val detailVm = remember(screen.clinicId) {
                                VeterinaryDetailViewModel(businessRepository, screen.clinicId)
                            }
                            VeterinaryDetailScreen(
                                viewModel = detailVm,
                                onBackClick = { currentScreen = PetScreenState.VeterinaryList },
                                onStartBooking = { clinicId ->
                                    currentScreen = PetScreenState.BookingFlow(clinicId)
                                }
                            )
                        }

                        is PetScreenState.BookingFlow -> {
                            val bookingVm = remember(screen.clinicId) {
                                BookingViewModel(appointmentRepository)
                            }
                            BookingFlowScreen(
                                viewModel = bookingVm,
                                onBackClick = { currentScreen = PetScreenState.VeterinaryDetail(screen.clinicId) },
                                onBookingComplete = {
                                    appointmentsListViewModel.loadAppointments()
                                    currentScreen = PetScreenState.AppointmentsList
                                }
                            )
                        }

                        PetScreenState.AppointmentsList -> {
                            AppointmentsListScreen(
                                viewModel = appointmentsListViewModel,
                                onStartBooking = { currentScreen = PetScreenState.VeterinaryList }
                            )
                        }

                        PetScreenState.MedicalHistory -> {
                            MedicalHistoryScreen(
                                viewModel = medicalHistoryViewModel
                            )
                        }
                    }
                }
            }

            // --- BOTTOM NAVIGATION BAR ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(PawMedicColors.White)
                    .padding(vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                BottomNavItem(
                    icon = "🏠",
                    label = "Inicio",
                    isSelected = currentScreen is PetScreenState.Dashboard,
                    onClick = { currentScreen = PetScreenState.Dashboard }
                )
                BottomNavItem(
                    icon = "🐾",
                    label = "Mascotas",
                    isSelected = currentScreen is PetScreenState.PetList || currentScreen is PetScreenState.PetDetail,
                    onClick = {
                        petListViewModel.loadPets()
                        currentScreen = PetScreenState.PetList
                    }
                )
                BottomNavItem(
                    icon = "📅",
                    label = "Citas",
                    isSelected = currentScreen is PetScreenState.VeterinaryList ||
                        currentScreen is PetScreenState.VeterinaryDetail ||
                        currentScreen is PetScreenState.BookingFlow ||
                        currentScreen is PetScreenState.AppointmentsList,
                    onClick = {
                        veterinaryListViewModel.loadClinics()
                        currentScreen = PetScreenState.VeterinaryList
                    }
                )
                BottomNavItem(
                    icon = "📄",
                    label = "Historial",
                    isSelected = currentScreen is PetScreenState.MedicalHistory,
                    onClick = {
                        medicalHistoryViewModel.loadHistory()
                        currentScreen = PetScreenState.MedicalHistory
                    }
                )
            }
        }
    }
}

@Composable
private fun BottomNavItem(
    icon: String,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Text(text = icon, fontSize = 22.sp)
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) PawMedicColors.Teal600 else PawMedicColors.Gray500
        )
    }
}

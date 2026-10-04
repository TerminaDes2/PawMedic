package com.pawsmedic.features.appointments.presentation.booking

import com.pawsmedic.features.appointments.domain.model.Appointment
import com.pawsmedic.features.appointments.domain.model.VeterinaryService
import com.pawsmedic.features.appointments.domain.repository.AppointmentRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

@Serializable
data class BookingPet(
    val id: String,
    val name: String,
    val species: String,
    val breed: String? = null,
    val age: String? = null
)

enum class BookingStep {
    SELECT_SERVICE,
    SELECT_PET,
    SELECT_DATE_TIME,
    CONFIRM,
    SUCCESS
}

data class BookingUiState(
    val currentStep: BookingStep = BookingStep.SELECT_SERVICE,
    val selectedClinicId: String = "biz-1",
    val selectedClinicName: String = "Clinipet Central",
    val services: List<VeterinaryService> = listOf(
        VeterinaryService(
            id = "srv-1",
            title = "Consulta Médica General",
            description = "Valoración clínica completa para diagnóstico preventivo o revisión general.",
            durationMinutes = 30
        ),
        VeterinaryService(
            id = "srv-2",
            title = "Vacunación Anual (Refuerzos)",
            description = "Inoculación de la triple felina o vacuna antirrábica según corresponda.",
            durationMinutes = 15
        ),
        VeterinaryService(
            id = "srv-3",
            title = "Estética y Baño Clínico",
            description = "Peluquería, baño medicado, corte de uñas y limpieza general de oídos.",
            durationMinutes = 60
        ),
        VeterinaryService(
            id = "srv-4",
            title = "Desparasitación Preventiva",
            description = "Aplicación de antiparasitarios de amplio espectro internos y externos.",
            durationMinutes = 15
        )
    ),
    val selectedService: VeterinaryService? = null,
    val selectedPet: BookingPet? = null,
    val selectedDate: String = "Domingo, 15 de Nov",
    val selectedTimeSlot: String = "10:00 AM",
    val availableTimeSlots: List<String> = listOf("09:00 AM", "10:00 AM", "11:00 AM", "02:00 PM", "03:30 PM"),
    val notes: String = "",
    val isLoading: Boolean = false,
    val isConfirmed: Boolean = false,
    val errorMessage: String? = null
)

class BookingViewModel(
    private val repository: AppointmentRepository,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
) {
    private val _uiState = MutableStateFlow(BookingUiState())
    val uiState: StateFlow<BookingUiState> = _uiState.asStateFlow()

    init {
        _uiState.update { curr ->
            curr.copy(
                selectedService = curr.services.firstOrNull(),
                selectedPet = BookingPet(
                    id = "pet-1",
                    name = "Tobías",
                    species = "Gato",
                    breed = "Persa Mestizo",
                    age = "2 años"
                )
            )
        }
    }

    fun selectService(service: VeterinaryService) {
        _uiState.update { curr -> curr.copy(selectedService = service) }
    }

    fun selectPet(pet: BookingPet) {
        _uiState.update { curr -> curr.copy(selectedPet = pet) }
    }

    fun selectDate(date: String) {
        _uiState.update { curr -> curr.copy(selectedDate = date) }
    }

    fun selectTimeSlot(slot: String) {
        _uiState.update { curr -> curr.copy(selectedTimeSlot = slot) }
    }

    fun onNotesChanged(n: String) {
        _uiState.update { curr -> curr.copy(notes = n) }
    }

    fun nextStep() {
        _uiState.update { curr ->
            when (curr.currentStep) {
                BookingStep.SELECT_SERVICE -> curr.copy(currentStep = BookingStep.SELECT_PET)
                BookingStep.SELECT_PET -> curr.copy(currentStep = BookingStep.SELECT_DATE_TIME)
                BookingStep.SELECT_DATE_TIME -> curr.copy(currentStep = BookingStep.CONFIRM)
                BookingStep.CONFIRM -> curr
                BookingStep.SUCCESS -> curr
            }
        }
    }

    fun previousStep() {
        _uiState.update { curr ->
            when (curr.currentStep) {
                BookingStep.SELECT_SERVICE -> curr
                BookingStep.SELECT_PET -> curr.copy(currentStep = BookingStep.SELECT_SERVICE)
                BookingStep.SELECT_DATE_TIME -> curr.copy(currentStep = BookingStep.SELECT_PET)
                BookingStep.CONFIRM -> curr.copy(currentStep = BookingStep.SELECT_DATE_TIME)
                BookingStep.SUCCESS -> curr
            }
        }
    }

    fun submitBooking(onSuccess: () -> Unit) {
        val s = _uiState.value
        val app = Appointment(
            id = "",
            petId = s.selectedPet?.id ?: "pet-1",
            petName = "${s.selectedPet?.name ?: "Tobías"} (${s.selectedPet?.species ?: "Gato"} • ${s.selectedPet?.breed ?: "Persa Mestizo"})",
            businessId = s.selectedClinicId,
            businessName = s.selectedClinicName,
            serviceId = s.selectedService?.id ?: "srv-1",
            serviceName = s.selectedService?.title ?: "Consulta Médica General",
            date = s.selectedDate,
            time = s.selectedTimeSlot,
            notes = if (s.notes.isBlank()) null else s.notes,
            status = "Pendiente de aprobación"
        )

        scope.launch {
            _uiState.update { curr -> curr.copy(isLoading = true, errorMessage = null) }
            repository.requestAppointment(app)
                .onSuccess {
                    _uiState.update { curr -> curr.copy(isLoading = false, isConfirmed = true, currentStep = BookingStep.SUCCESS) }
                    onSuccess()
                }
                .onFailure { err ->
                    _uiState.update { curr -> curr.copy(isLoading = false, errorMessage = err.message ?: "Error al solicitar cita") }
                }
        }
    }
}

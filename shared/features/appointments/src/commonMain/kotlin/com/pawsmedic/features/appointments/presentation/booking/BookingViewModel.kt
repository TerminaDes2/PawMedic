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
    val age: String? = null,
    val photoUrl: String? = null
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
    val userPets: List<BookingPet> = listOf(
        BookingPet(
            id = "pet-1",
            name = "Tobías",
            species = "Gato",
            breed = "Persa Mestizo",
            age = "2 años",
            photoUrl = "https://images.unsplash.com/photo-1514888286974-6c03e2ca1dba"
        ),
        BookingPet(
            id = "pet-2",
            name = "Joey",
            species = "Perro",
            breed = "Australian Shepard",
            age = "14 años",
            photoUrl = "https://images.unsplash.com/photo-1543466835-00a7907e9de1"
        ),
        BookingPet(
            id = "pet-3",
            name = "Rudy",
            species = "Perro",
            breed = "Dálmata",
            age = "3 años",
            photoUrl = "https://images.unsplash.com/photo-1583511655857-d19b40a7a54e"
        ),
        BookingPet(
            id = "pet-4",
            name = "Skippy",
            species = "Gato",
            breed = "Siamés",
            age = "1 año",
            photoUrl = "https://images.unsplash.com/photo-1573865526739-10659fec78a5"
        )
    ),
    val selectedService: VeterinaryService? = null,
    val selectedPet: BookingPet? = null,
    val selectedDate: String = "Mañana (16 de Nov)",
    val selectedDateMillis: Long? = null,
    val selectedTimeSlot: String = "11:00 AM",
    val availableTimeSlots: List<String> = listOf("11:00 AM", "12:00 PM", "01:30 PM", "03:00 PM", "04:30 PM", "06:00 PM", "07:00 PM"),
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
                selectedPet = curr.userPets.firstOrNull()
            )
        }
    }

    fun selectService(service: VeterinaryService) {
        _uiState.update { curr -> curr.copy(selectedService = service, errorMessage = null) }
    }

    fun selectPet(pet: BookingPet) {
        _uiState.update { curr -> curr.copy(selectedPet = pet, errorMessage = null) }
    }

    fun selectDate(date: String, millis: Long? = null) {
        // Validation: Ensure date is in the future within 1 month (30 days)
        val todayMillis = System.currentTimeMillis()
        val oneDayMillis = 86400000L
        val maxAllowedMillis = todayMillis + (30L * oneDayMillis)

        if (millis != null) {
            if (millis <= todayMillis) {
                _uiState.update { curr -> curr.copy(errorMessage = "La fecha debe ser futura (a partir de mañana). No se permite hoy ni fechas pasadas.") }
                return
            }
            if (millis > maxAllowedMillis) {
                _uiState.update { curr -> curr.copy(errorMessage = "La fecha debe estar dentro del próximo mes (máximo 30 días).") }
                return
            }
        }

        _uiState.update { curr ->
            curr.copy(selectedDate = date, selectedDateMillis = millis, errorMessage = null)
        }
    }

    fun selectTimeSlot(slot: String) {
        _uiState.update { curr -> curr.copy(selectedTimeSlot = slot, errorMessage = null) }
    }

    fun selectTimeFromPicker(hour: Int, minute: Int, formattedTime: String) {
        // Validation: Time must be between 11:00 AM (11) and 07:00 PM (19:00)
        if (hour < 11 || hour > 19 || (hour == 19 && minute > 0)) {
            _uiState.update { curr ->
                curr.copy(errorMessage = "Horario no disponible. Selecciona una hora entre las 11:00 AM y las 07:00 PM.")
            }
            return
        }

        _uiState.update { curr ->
            curr.copy(selectedTimeSlot = formattedTime, errorMessage = null)
        }
    }

    fun onNotesChanged(n: String) {
        _uiState.update { curr -> curr.copy(notes = n) }
    }

    fun nextStep() {
        _uiState.update { curr ->
            when (curr.currentStep) {
                BookingStep.SELECT_SERVICE -> curr.copy(currentStep = BookingStep.SELECT_PET, errorMessage = null)
                BookingStep.SELECT_PET -> curr.copy(currentStep = BookingStep.SELECT_DATE_TIME, errorMessage = null)
                BookingStep.SELECT_DATE_TIME -> curr.copy(currentStep = BookingStep.CONFIRM, errorMessage = null)
                BookingStep.CONFIRM -> curr
                BookingStep.SUCCESS -> curr
            }
        }
    }

    fun previousStep() {
        _uiState.update { curr ->
            when (curr.currentStep) {
                BookingStep.SELECT_SERVICE -> curr
                BookingStep.SELECT_PET -> curr.copy(currentStep = BookingStep.SELECT_SERVICE, errorMessage = null)
                BookingStep.SELECT_DATE_TIME -> curr.copy(currentStep = BookingStep.SELECT_PET, errorMessage = null)
                BookingStep.CONFIRM -> curr.copy(currentStep = BookingStep.SELECT_DATE_TIME, errorMessage = null)
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

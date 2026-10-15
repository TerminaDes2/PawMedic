package com.pawsmedic.features.appointments.presentation.booking

import com.pawsmedic.features.appointments.domain.model.Appointment
import com.pawsmedic.features.appointments.domain.model.AvailableAppointmentSlot
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
import kotlin.random.Random

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
    val selectedClinicId: String = "",
    val selectedClinicName: String = "",
    val services: List<VeterinaryService> = emptyList(),
    val userPets: List<BookingPet> = emptyList(),
    val selectedService: VeterinaryService? = null,
    val selectedPet: BookingPet? = null,
    val selectedDate: String = "",
    val selectedDateLabel: String = "",
    val selectedDateMillis: Long? = null,
    val selectedTimeSlot: String = "",
    val selectedSlot: AvailableAppointmentSlot? = null,
    val idempotencyKey: String? = null,
    val availableTimeSlots: List<AvailableAppointmentSlot> = emptyList(),
    val isLoadingServices: Boolean = false,
    val isLoadingPets: Boolean = false,
    val isLoadingSlots: Boolean = false,
    val isLoading: Boolean = false,
    val isConfirmed: Boolean = false,
    val errorMessage: String? = null
)

class BookingViewModel(
    private val repository: AppointmentRepository,
    clinicId: String,
    clinicName: String,
    private val ownerId: String,
    private val loadPets: suspend (String) -> Result<List<BookingPet>>,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
) {
    private val _uiState = MutableStateFlow(
        BookingUiState(
            selectedClinicId = clinicId,
            selectedClinicName = clinicName
        )
    )
    val uiState: StateFlow<BookingUiState> = _uiState.asStateFlow()

    init {
        loadBookingData()
    }

    private fun loadBookingData() {
        scope.launch {
            _uiState.update {
                it.copy(isLoadingServices = true, isLoadingPets = true, errorMessage = null)
            }
            repository.getServices(_uiState.value.selectedClinicId)
                .onSuccess { services ->
                    _uiState.update {
                        it.copy(services = services, isLoadingServices = false)
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(isLoadingServices = false, errorMessage = error.message)
                    }
                }

            loadPets(ownerId)
                .onSuccess { pets ->
                    _uiState.update { it.copy(userPets = pets, isLoadingPets = false) }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(isLoadingPets = false, errorMessage = error.message)
                    }
                }
        }
    }

    fun retryLoadBookingData() = loadBookingData()

    fun selectService(service: VeterinaryService) {
        _uiState.update { curr ->
            curr.copy(
                selectedService = service,
                idempotencyKey = null,
                selectedDate = "",
                selectedDateLabel = "",
                selectedDateMillis = null,
                selectedTimeSlot = "",
                selectedSlot = null,
                availableTimeSlots = emptyList(),
                isLoadingSlots = false,
                errorMessage = null
            )
        }
    }

    fun selectPet(pet: BookingPet) {
        _uiState.update { curr ->
            curr.copy(selectedPet = pet, idempotencyKey = null, errorMessage = null)
        }
    }

    fun selectDate(date: String, millis: Long? = null, displayDate: String = date) {
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
            curr.copy(
                selectedDate = date,
                selectedDateLabel = displayDate,
                selectedDateMillis = millis,
                selectedTimeSlot = "",
                selectedSlot = null,
                idempotencyKey = null,
                availableTimeSlots = emptyList(),
                errorMessage = null,
                isLoadingSlots = true
            )
        }
        val selectedService = _uiState.value.selectedService
        val tenantId = _uiState.value.selectedClinicId
        scope.launch {
            if (selectedService == null) {
                _uiState.update { it.copy(isLoadingSlots = false) }
                return@launch
            }
            repository.getAvailableSlots(
                tenantId = tenantId,
                serviceId = selectedService.id,
                date = date
            ).onSuccess { slots ->
                _uiState.update {
                    if (it.selectedDate == date && it.selectedService?.id == selectedService.id) {
                        it.copy(availableTimeSlots = slots, isLoadingSlots = false)
                    } else {
                        it
                    }
                }
            }.onFailure { error ->
                _uiState.update {
                    if (it.selectedDate == date && it.selectedService?.id == selectedService.id) {
                        it.copy(
                            availableTimeSlots = emptyList(),
                            isLoadingSlots = false,
                            errorMessage = error.message
                        )
                    } else {
                        it
                    }
                }
            }
        }
    }

    fun selectTimeSlot(slot: AvailableAppointmentSlot) {
        _uiState.update { curr ->
            curr.copy(
                selectedTimeSlot = slot.label,
                selectedSlot = slot,
                idempotencyKey = null,
                errorMessage = null
            )
        }
    }

    fun nextStep() {
        _uiState.update { curr ->
            when (curr.currentStep) {
                BookingStep.SELECT_SERVICE -> if (curr.selectedService == null) {
                    curr.copy(errorMessage = "No hay servicios disponibles para reservar.")
                } else {
                    curr.copy(currentStep = BookingStep.SELECT_PET, errorMessage = null)
                }
                BookingStep.SELECT_PET -> if (curr.selectedPet == null) {
                    curr.copy(errorMessage = "No hay mascotas disponibles para reservar.")
                } else {
                    curr.copy(currentStep = BookingStep.SELECT_DATE_TIME, errorMessage = null)
                }
                BookingStep.SELECT_DATE_TIME -> if (curr.selectedDate.isBlank() || curr.selectedSlot == null) {
                    curr.copy(errorMessage = "Selecciona una fecha y un horario disponibles.")
                } else {
                    curr.copy(currentStep = BookingStep.CONFIRM, errorMessage = null)
                }
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
        if (s.isLoading) return
        val selectedPet = s.selectedPet
        val selectedService = s.selectedService
        if (selectedPet == null || selectedService == null || s.selectedClinicId.isBlank() ||
            s.selectedDate.isBlank() || s.selectedSlot == null
        ) {
            _uiState.update { curr ->
                curr.copy(errorMessage = "Faltan datos reales de clínica, servicio, mascota, fecha u horario.")
            }
            return
        }

        val app = Appointment(
            id = "",
            petId = selectedPet.id,
            petName = "${selectedPet.name} (${selectedPet.species}${selectedPet.breed?.let { " • $it" }.orEmpty()})",
            businessId = s.selectedClinicId,
            businessName = s.selectedClinicName,
            serviceId = selectedService.id,
            serviceName = selectedService.title,
            date = s.selectedDate,
            time = s.selectedTimeSlot,
            notes = null,
            status = "Pendiente de aprobación",
            startsAt = s.selectedSlot?.startsAt,
            endsAt = s.selectedSlot?.endsAt,
            idempotencyKey = s.idempotencyKey ?: randomUuid()
        )

        scope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    errorMessage = null,
                    idempotencyKey = app.idempotencyKey
                )
            }
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

private fun randomUuid(): String {
    val bytes = Random.Default.nextBytes(16)
    bytes[6] = ((bytes[6].toInt() and 0x0f) or 0x40).toByte()
    bytes[8] = ((bytes[8].toInt() and 0x3f) or 0x80).toByte()
    val digits = "0123456789abcdef"
    val hex = bytes.joinToString("") { byte ->
        val value = byte.toInt() and 0xff
        "${digits[value shr 4]}${digits[value and 0x0f]}"
    }
    return "${hex.substring(0, 8)}-${hex.substring(8, 12)}-${hex.substring(12, 16)}-" +
        "${hex.substring(16, 20)}-${hex.substring(20, 32)}"
}

package com.pawsmedic.features.medical_records.presentation

import com.pawsmedic.features.medical_records.domain.model.AuditLogEntry
import com.pawsmedic.features.medical_records.domain.model.ClinicalConsultation
import com.pawsmedic.features.medical_records.domain.model.ClinicalStudyRecord
import com.pawsmedic.features.medical_records.domain.model.DewormingRecord
import com.pawsmedic.features.medical_records.domain.model.PetBackground
import com.pawsmedic.features.medical_records.domain.model.PetMedicalRecord
import com.pawsmedic.features.medical_records.domain.model.PrescriptionRecord
import com.pawsmedic.features.medical_records.domain.model.VaccinationRecord
import com.pawsmedic.features.medical_records.domain.repository.MedicalRecordRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MedicalRecordUiState(
    val petId: String = "",
    val record: PetMedicalRecord? = null,
    val selectedTabIndex: Int = 0,
    val isVetUser: Boolean = false, // true for VETERINARY_BUSINESS or SUPERADMIN
    val currentUserRole: String = "USER",
    val currentUserName: String = "Dr. Veterinario",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val showNewConsultationDialog: Boolean = false,
    val showEditBackgroundDialog: Boolean = false,
    val searchQuery: String = ""
)

class MedicalRecordViewModel(
    private val repository: MedicalRecordRepository,
    private val petId: String,
    val userRole: String = "VETERINARY_BUSINESS", // Default to VET for testing or set dynamically
    val userName: String = "Dra. Ana Milena",
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
) {
    private val _uiState = MutableStateFlow(
        MedicalRecordUiState(
            petId = petId,
            isVetUser = userRole.equals("VETERINARY_BUSINESS", ignoreCase = true) ||
                userRole.equals("SUPERADMIN", ignoreCase = true) ||
                userRole.equals("VETERINARIAN", ignoreCase = true),
            currentUserRole = userRole,
            currentUserName = userName
        )
    )
    val uiState: StateFlow<MedicalRecordUiState> = _uiState.asStateFlow()

    init {
        loadRecord()
    }

    fun loadRecord() {
        scope.launch {
            _uiState.update { curr -> curr.copy(isLoading = true, errorMessage = null) }
            repository.getPetMedicalRecord(petId)
                .onSuccess { rec ->
                    _uiState.update { curr -> curr.copy(record = rec, isLoading = false) }
                }
                .onFailure { err ->
                    _uiState.update { curr -> curr.copy(isLoading = false, errorMessage = err.message ?: "Error al cargar expediente") }
                }
        }
    }

    fun onTabSelected(index: Int) {
        _uiState.update { curr -> curr.copy(selectedTabIndex = index) }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { curr -> curr.copy(searchQuery = query) }
    }

    fun openNewConsultationDialog() {
        if (!_uiState.value.isVetUser) {
            _uiState.update { curr -> curr.copy(errorMessage = "Acceso denegado: Únicamente médicos veterinarios pueden registrar consultas.") }
            return
        }
        _uiState.update { curr -> curr.copy(showNewConsultationDialog = true) }
    }

    fun dismissNewConsultationDialog() {
        _uiState.update { curr -> curr.copy(showNewConsultationDialog = false) }
    }

    fun openEditBackgroundDialog() {
        if (!_uiState.value.isVetUser) {
            _uiState.update { curr -> curr.copy(errorMessage = "Acceso denegado: Únicamente médicos veterinarios pueden modificar antecedentes clínicos.") }
            return
        }
        _uiState.update { curr -> curr.copy(showEditBackgroundDialog = true) }
    }

    fun dismissEditBackgroundDialog() {
        _uiState.update { curr -> curr.copy(showEditBackgroundDialog = false) }
    }

    fun clearMessages() {
        _uiState.update { curr -> curr.copy(errorMessage = null, successMessage = null) }
    }

    fun submitNewConsultation(
        consultation: ClinicalConsultation,
        prescriptions: List<PrescriptionRecord>,
        studies: List<ClinicalStudyRecord>
    ) {
        if (!_uiState.value.isVetUser) return

        scope.launch {
            _uiState.update { curr -> curr.copy(isLoading = true, errorMessage = null) }
            val fullConsultation = consultation.copy(
                petId = petId,
                veterinarianName = userName,
                occurredAt = if (consultation.occurredAt.isBlank()) "2026-09-21 12:00" else consultation.occurredAt
            )

            repository.saveConsultation(fullConsultation, prescriptions, studies)
                .onSuccess {
                    _uiState.update { curr ->
                        curr.copy(
                            isLoading = false,
                            showNewConsultationDialog = false,
                            successMessage = "Consulta clínica registrada correctamente"
                        )
                    }
                    loadRecord()
                }
                .onFailure { err ->
                    _uiState.update { curr ->
                        curr.copy(isLoading = false, errorMessage = err.message ?: "Error al guardar consulta")
                    }
                }
        }
    }

    fun updateBackground(background: PetBackground) {
        if (!_uiState.value.isVetUser) return

        scope.launch {
            _uiState.update { curr -> curr.copy(isLoading = true, errorMessage = null) }
            repository.updateBackground(background.copy(petId = petId, updatedBy = userName))
                .onSuccess {
                    _uiState.update { curr ->
                        curr.copy(
                            isLoading = false,
                            showEditBackgroundDialog = false,
                            successMessage = "Antecedentes médicos actualizados con éxito"
                        )
                    }
                    loadRecord()
                }
                .onFailure { err ->
                    _uiState.update { curr ->
                        curr.copy(isLoading = false, errorMessage = err.message ?: "Error al actualizar antecedentes")
                    }
                }
        }
    }
}

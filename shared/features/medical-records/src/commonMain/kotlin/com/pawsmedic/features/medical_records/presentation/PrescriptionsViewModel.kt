package com.pawsmedic.features.medical_records.presentation

import com.pawsmedic.features.medical_records.domain.model.PrescriptionRecord
import com.pawsmedic.features.medical_records.domain.repository.MedicalRecordRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PrescriptionsUiState(
    val allPrescriptions: List<PrescriptionRecord> = emptyList(),
    val filteredActivePrescriptions: List<PrescriptionRecord> = emptyList(),
    val availablePetNames: List<String> = listOf("Todas"),
    val selectedPetName: String = "Todas",
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class PrescriptionsViewModel(
    private val repository: MedicalRecordRepository,
    private val ownerId: String,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
) {
    private val _uiState = MutableStateFlow(PrescriptionsUiState())
    val uiState: StateFlow<PrescriptionsUiState> = _uiState.asStateFlow()

    init {
        loadPrescriptions()
    }

    fun loadPrescriptions() {
        scope.launch {
            _uiState.update { curr -> curr.copy(isLoading = true, errorMessage = null) }
            repository.getPrescriptionsForOwner(ownerId)
                .onSuccess { list ->
                    _uiState.update { curr ->
                        // Filter active prescriptions (endDate >= today)
                        val activeOnly = list.filter { it.isActive }
                        val petNames = (listOf("Todas") + activeOnly.map { it.petName }.filter { it.isNotBlank() }.distinct())
                        val filtered = filterPrescriptions(activeOnly, curr.selectedPetName, curr.searchQuery)
                        curr.copy(
                            allPrescriptions = list,
                            filteredActivePrescriptions = filtered,
                            availablePetNames = petNames,
                            isLoading = false
                        )
                    }
                }
                .onFailure { err ->
                    _uiState.update { curr ->
                        curr.copy(isLoading = false, errorMessage = err.message)
                    }
                }
        }
    }

    fun onPetSelected(petName: String) {
        _uiState.update { curr ->
            val activeOnly = curr.allPrescriptions.filter { it.isActive }
            val filtered = filterPrescriptions(activeOnly, petName, curr.searchQuery)
            curr.copy(selectedPetName = petName, filteredActivePrescriptions = filtered)
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { curr ->
            val activeOnly = curr.allPrescriptions.filter { it.isActive }
            val filtered = filterPrescriptions(activeOnly, curr.selectedPetName, query)
            curr.copy(searchQuery = query, filteredActivePrescriptions = filtered)
        }
    }

    private fun filterPrescriptions(list: List<PrescriptionRecord>, petName: String, query: String): List<PrescriptionRecord> {
        return list.filter { p ->
            val matchesPet = petName == "Todas" || petName == "Todos" || p.petName.equals(petName, ignoreCase = true)
            val matchesQuery = query.isBlank() ||
                p.drugName.contains(query, ignoreCase = true) ||
                p.prescribingVetName.contains(query, ignoreCase = true) ||
                p.petName.contains(query, ignoreCase = true)
            matchesPet && matchesQuery
        }
    }
}

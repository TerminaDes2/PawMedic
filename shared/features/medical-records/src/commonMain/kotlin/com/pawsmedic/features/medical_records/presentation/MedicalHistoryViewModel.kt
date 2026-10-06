package com.pawsmedic.features.medical_records.presentation

import com.pawsmedic.features.medical_records.domain.model.MedicalRecord
import com.pawsmedic.features.medical_records.domain.repository.MedicalRecordRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MedicalHistoryUiState(
    val records: List<MedicalRecord> = emptyList(),
    val filteredRecords: List<MedicalRecord> = emptyList(),
    val selectedPetName: String = "Tobías",
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class MedicalHistoryViewModel(
    private val repository: MedicalRecordRepository,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
) {
    private val _uiState = MutableStateFlow(MedicalHistoryUiState())
    val uiState: StateFlow<MedicalHistoryUiState> = _uiState.asStateFlow()

    init {
        loadHistory()
    }

    fun loadHistory() {
        scope.launch {
            _uiState.update { curr -> curr.copy(isLoading = true, errorMessage = null) }
            repository.getAllMedicalRecords()
                .onSuccess { list ->
                    _uiState.update { curr ->
                        val filtered = filterRecords(list, curr.selectedPetName, curr.searchQuery)
                        curr.copy(records = list, filteredRecords = filtered, isLoading = false)
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
            val filtered = filterRecords(curr.records, petName, curr.searchQuery)
            curr.copy(selectedPetName = petName, filteredRecords = filtered)
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { curr ->
            val filtered = filterRecords(curr.records, curr.selectedPetName, query)
            curr.copy(searchQuery = query, filteredRecords = filtered)
        }
    }

    private fun filterRecords(list: List<MedicalRecord>, petName: String, query: String): List<MedicalRecord> {
        return list.filter { record ->
            val matchesPet = petName == "Todas" || record.petName.equals(petName, ignoreCase = true)
            val matchesQuery = query.isBlank() ||
                record.title.contains(query, ignoreCase = true) ||
                record.diagnosis.contains(query, ignoreCase = true) ||
                record.doctorName.contains(query, ignoreCase = true)
            matchesPet && matchesQuery
        }
    }
}

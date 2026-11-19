    package com.pawsmedic.features.businesses.presentation

import com.pawsmedic.features.businesses.domain.model.Business
import com.pawsmedic.features.businesses.domain.repository.BusinessRepository
import com.pawsmedic.features.businesses.data.datasource.PublicProduct
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class VeterinaryListUiState(
    val clinics: List<Business> = emptyList(),
    val searchQuery: String = "",
    val selectedFilter: String = "Todos",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class VeterinaryListViewModel(
    private val repository: BusinessRepository,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
) {
    private val _uiState = MutableStateFlow(VeterinaryListUiState())
    val uiState: StateFlow<VeterinaryListUiState> = _uiState.asStateFlow()
    private var allClinics: List<Business> = emptyList()

    init {
        loadClinics()
    }

    fun loadClinics() {
        scope.launch {
            _uiState.update { curr -> curr.copy(isLoading = true, errorMessage = null) }
            repository.getBusinesses()
                .onSuccess { list ->
                    allClinics = list
                    _uiState.update { curr -> curr.copy(clinics = list, isLoading = false) }
                }
                .onFailure { err ->
                    _uiState.update { curr -> curr.copy(isLoading = false, errorMessage = err.message) }
                }
        }
    }

    fun onSearchQueryChanged(q: String) {
        val normalizedQuery = q.trim().replace(Regex("\\s+"), " ")
        if (normalizedQuery == _uiState.value.searchQuery && !_uiState.value.isLoading) return

        _uiState.update { curr ->
            curr.copy(
                searchQuery = normalizedQuery,
                clinics = applyLocalFilters(normalizedQuery, curr.selectedFilter)
            )
        }
    }

    fun retrySearch() = onSearchQueryChanged(_uiState.value.searchQuery)

    fun onFilterSelected(filter: String) {
        _uiState.update { curr ->
            curr.copy(
                selectedFilter = filter,
                clinics = applyLocalFilters(curr.searchQuery, filter)
            )
        }
    }

    private fun applyLocalFilters(query: String, categoryFilter: String): List<Business> =
        allClinics.filter { clinic ->
            val matchesQuery = query.isBlank() ||
                clinic.name.contains(query, ignoreCase = true) ||
                clinic.address?.contains(query, ignoreCase = true) == true ||
                clinic.specialties.any { it.contains(query, ignoreCase = true) } ||
                clinic.serviceCategories.any { it.contains(query, ignoreCase = true) }

            val matchesCategory = categoryFilter == "Todos" ||
                clinic.serviceCategories.any { normalizeCategory(it) == normalizeCategory(categoryFilter) }

            matchesQuery && matchesCategory
        }

    private fun normalizeCategory(category: String): String {
        val normalized = category.trim().lowercase()
            .replace("á", "a").replace("é", "e").replace("í", "i")
            .replace("ó", "o").replace("ú", "u")
        return when {
            normalized.contains("vacun") -> "vacunacion"
            normalized.contains("despar") -> "desparasitacion"
            normalized.contains("cirug") -> "cirugia"
            normalized.contains("estet") -> "estetica"
            normalized.contains("consulta") -> "consulta"
            else -> normalized
        }
    }
}

data class VeterinaryDetailUiState(
    val clinic: Business? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val products: List<PublicProduct> = emptyList(),
    val productsLoading: Boolean = false,
    val productsError: Boolean = false
)

class VeterinaryDetailViewModel(
    private val repository: BusinessRepository,
    private val clinicId: String,
    private val loadProducts: suspend (String) -> List<PublicProduct>,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
) {
    private val _uiState = MutableStateFlow(VeterinaryDetailUiState())
    val uiState: StateFlow<VeterinaryDetailUiState> = _uiState.asStateFlow()

    init {
        loadClinic()
    }

    fun loadClinic() {
        scope.launch {
            _uiState.update { curr -> curr.copy(isLoading = true, errorMessage = null) }
            repository.getBusinessById(clinicId)
                .onSuccess { b ->
                    _uiState.update { curr -> curr.copy(clinic = b, isLoading = false) }
                    if (b != null) refreshProducts()
                }
                .onFailure { err ->
                    _uiState.update { curr -> curr.copy(isLoading = false, errorMessage = err.message) }
                }
        }
    }

    fun refreshProducts() {
        scope.launch {
            _uiState.update { it.copy(productsLoading = true, productsError = false, products = emptyList()) }
            try {
                val products = loadProducts(clinicId)
                _uiState.update { it.copy(products = products, productsLoading = false) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update { it.copy(productsLoading = false, productsError = true) }
            }
        }
    }
}

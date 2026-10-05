package com.pawsmedic.features.pets.presentation

import com.pawsmedic.features.pets.domain.model.Pet
import com.pawsmedic.features.pets.domain.usecase.AddPetUseCase
import com.pawsmedic.features.pets.domain.usecase.DeletePetUseCase
import com.pawsmedic.features.pets.domain.usecase.GetPetByIdUseCase
import com.pawsmedic.features.pets.domain.usecase.GetPetsUseCase
import com.pawsmedic.features.pets.domain.usecase.UpdatePetUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// --- PET LIST UI STATE & VIEWMODEL ---
data class PetListUiState(
    val pets: List<Pet> = emptyList(),
    val selectedPet: Pet? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class PetListViewModel(
    private val getPetsUseCase: GetPetsUseCase,
    private val ownerId: String = "owner-1",
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
) {
    private val _uiState = MutableStateFlow(PetListUiState())
    val uiState: StateFlow<PetListUiState> = _uiState.asStateFlow()

    init {
        loadPets()
    }

    fun loadPets() {
        scope.launch {
            _uiState.update { curr -> curr.copy(isLoading = true, errorMessage = null) }
            getPetsUseCase(ownerId)
                .onSuccess { list ->
                    _uiState.update { curr ->
                        curr.copy(
                            pets = list,
                            selectedPet = curr.selectedPet ?: list.firstOrNull(),
                            isLoading = false
                        )
                    }
                }
                .onFailure { err ->
                    _uiState.update { curr ->
                        curr.copy(
                            isLoading = false,
                            errorMessage = err.message ?: "Error al cargar mascotas"
                        )
                    }
                }
        }
    }

    fun selectPet(pet: Pet) {
        _uiState.update { curr -> curr.copy(selectedPet = pet) }
    }
}

// --- PET DETAIL UI STATE & VIEWMODEL ---
data class PetDetailUiState(
    val pet: Pet? = null,
    val isLoading: Boolean = false,
    val isDeleting: Boolean = false,
    val isDeleted: Boolean = false,
    val errorMessage: String? = null
)

class PetDetailViewModel(
    private val getPetByIdUseCase: GetPetByIdUseCase,
    private val deletePetUseCase: DeletePetUseCase,
    private val petId: String,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
) {
    private val _uiState = MutableStateFlow(PetDetailUiState())
    val uiState: StateFlow<PetDetailUiState> = _uiState.asStateFlow()

    init {
        loadPet()
    }

    fun loadPet() {
        scope.launch {
            _uiState.update { curr -> curr.copy(isLoading = true, errorMessage = null) }
            getPetByIdUseCase(petId)
                .onSuccess { p ->
                    _uiState.update { curr -> curr.copy(pet = p, isLoading = false) }
                }
                .onFailure { err ->
                    _uiState.update { curr -> curr.copy(isLoading = false, errorMessage = err.message) }
                }
        }
    }

    fun deletePet(onSuccess: () -> Unit) {
        scope.launch {
            _uiState.update { curr -> curr.copy(isDeleting = true, errorMessage = null) }
            deletePetUseCase(petId)
                .onSuccess {
                    _uiState.update { curr -> curr.copy(isDeleting = false, isDeleted = true) }
                    onSuccess()
                }
                .onFailure { err ->
                    _uiState.update { curr ->
                        curr.copy(isDeleting = false, errorMessage = err.message ?: "Error al eliminar")
                    }
                }
        }
    }
}

// --- PET FORM UI STATE & VIEWMODEL ---
data class PetFormUiState(
    val id: String = "",
    val name: String = "",
    val species: String = "Gato",
    val breed: String = "",
    val age: String = "",
    val gender: String = "Macho",
    val allergies: String = "",
    val photoUrl: String = "",
    val isEditMode: Boolean = false,
    val isLoading: Boolean = false,
    val isSaved: Boolean = false,
    val errorMessage: String? = null
)

class PetFormViewModel(
    private val addPetUseCase: AddPetUseCase,
    private val updatePetUseCase: UpdatePetUseCase,
    private val getPetByIdUseCase: GetPetByIdUseCase,
    private val initialPetId: String? = null,
    private val ownerId: String = "owner-1",
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
) {
    private val _uiState = MutableStateFlow(PetFormUiState())
    val uiState: StateFlow<PetFormUiState> = _uiState.asStateFlow()

    init {
        if (!initialPetId.isNullOrBlank()) {
            loadPetForEdit(initialPetId)
        }
    }

    private fun loadPetForEdit(id: String) {
        scope.launch {
            _uiState.update { curr -> curr.copy(isLoading = true) }
            getPetByIdUseCase(id).onSuccess { p ->
                if (p != null) {
                    _uiState.update { curr ->
                        curr.copy(
                            id = p.id,
                            name = p.name,
                            species = p.species,
                            breed = p.breed ?: "",
                            age = p.age ?: "",
                            gender = p.gender ?: "Macho",
                            allergies = p.allergies ?: "",
                            photoUrl = p.photoUrl ?: "",
                            isEditMode = true,
                            isLoading = false
                        )
                    }
                } else {
                    _uiState.update { curr -> curr.copy(isLoading = false) }
                }
            }
        }
    }

    fun onNameChanged(v: String) = _uiState.update { curr -> curr.copy(name = v, errorMessage = null) }
    fun onSpeciesChanged(v: String) = _uiState.update { curr -> curr.copy(species = v, errorMessage = null) }
    fun onBreedChanged(v: String) = _uiState.update { curr -> curr.copy(breed = v, errorMessage = null) }
    fun onAgeChanged(v: String) = _uiState.update { curr -> curr.copy(age = v, errorMessage = null) }
    fun onGenderChanged(v: String) = _uiState.update { curr -> curr.copy(gender = v, errorMessage = null) }
    fun onAllergiesChanged(v: String) = _uiState.update { curr -> curr.copy(allergies = v, errorMessage = null) }
    fun onPhotoUrlChanged(v: String) = _uiState.update { curr -> curr.copy(photoUrl = v, errorMessage = null) }

    fun savePet(onSuccess: () -> Unit) {
        val s = _uiState.value
        if (s.name.isBlank()) {
            _uiState.update { curr -> curr.copy(errorMessage = "El nombre de la mascota es obligatorio") }
            return
        }
        if (s.species.isBlank()) {
            _uiState.update { curr -> curr.copy(errorMessage = "La especie es obligatoria") }
            return
        }

        val pet = Pet(
            id = s.id,
            ownerId = ownerId,
            name = s.name,
            species = s.species,
            breed = if (s.breed.isBlank()) null else s.breed,
            age = if (s.age.isBlank()) null else s.age,
            gender = s.gender,
            allergies = if (s.allergies.isBlank()) null else s.allergies,
            photoUrl = if (s.photoUrl.isBlank()) "https://images.unsplash.com/photo-1514888286974-6c03e2ca1dba" else s.photoUrl,
            medicalId = "PM-${(1000..9999).random()}-A"
        )

        scope.launch {
            _uiState.update { curr -> curr.copy(isLoading = true, errorMessage = null) }
            val result = if (s.isEditMode) updatePetUseCase(pet) else addPetUseCase(pet)
            result
                .onSuccess {
                    _uiState.update { curr -> curr.copy(isLoading = false, isSaved = true) }
                    onSuccess()
                }
                .onFailure { err ->
                    _uiState.update { curr ->
                        curr.copy(isLoading = false, errorMessage = err.message ?: "Error al guardar")
                    }
                }
        }
    }
}

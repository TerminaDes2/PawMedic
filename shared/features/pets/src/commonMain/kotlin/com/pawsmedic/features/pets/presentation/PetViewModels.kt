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
enum class AgeInputMode {
    APPROXIMATE,
    EXACT_DATE
}

data class PetFormUiState(
    val id: String = "",
    val name: String = "",
    val species: String = "Canino",
    val breed: String = "Mestizo Canino",
    val ageMode: AgeInputMode = AgeInputMode.APPROXIMATE,
    val approximateAge: String = "2 años",
    val exactBirthDate: String = "2024-05-10",
    val weightKg: String = "",
    val gender: String = "Macho",
    val allergies: String = "",
    val photoUrl: String = "",
    val isEditMode: Boolean = false,
    val isLoading: Boolean = false,
    val isSaved: Boolean = false,
    val errorMessage: String? = null
) {
    val availableSpecies = listOf("Canino", "Felino", "Avíparo")

    val availableBreeds: List<String>
        get() = when (species) {
            "Canino" -> listOf("Mestizo Canino", "Golden Retriever", "Labrador Retriever", "Pastor Alemán", "Poodle", "Bulldog", "Beagle", "Siberian Husky", "Chihuahua", "Pug", "Otro")
            "Felino" -> listOf("Mestizo Felino", "Persa", "Siamés", "Maine Coon", "Bengalí", "Sphynx", "Ragdoll", "British Shorthair", "Otro")
            "Avíparo" -> listOf("Mestizo Avíparo", "Loro Real", "Canario", "Perico Australiano", "Cacatúa", "Agapornis", "Guacamayo", "Otro")
            else -> listOf("Mestizo", "Otro")
        }
}

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
                            species = if (p.species.contains("Gato", true)) "Felino" else if (p.species.contains("Perro", true)) "Canino" else p.species,
                            breed = p.breed ?: "Mestizo",
                            approximateAge = p.age ?: "2 años",
                            weightKg = p.weight?.replace(" kg", "")?.replace(" lbs", "") ?: "",
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

    fun onNameChanged(v: String) {
        // Restricción: No permite números ni caracteres especiales
        val filtered = v.filter { it.isLetter() || it.isWhitespace() }
        _uiState.update { curr -> curr.copy(name = filtered, errorMessage = null) }
    }

    fun onSpeciesChanged(newSpecies: String) {
        val defaultBreed = when (newSpecies) {
            "Canino" -> "Mestizo Canino"
            "Felino" -> "Mestizo Felino"
            "Avíparo" -> "Mestizo Avíparo"
            else -> "Mestizo"
        }
        _uiState.update { curr ->
            curr.copy(species = newSpecies, breed = defaultBreed, errorMessage = null)
        }
    }

    fun onBreedChanged(v: String) = _uiState.update { curr -> curr.copy(breed = v, errorMessage = null) }

    fun onAgeModeChanged(mode: AgeInputMode) = _uiState.update { curr -> curr.copy(ageMode = mode, errorMessage = null) }

    fun onApproximateAgeChanged(v: String) = _uiState.update { curr -> curr.copy(approximateAge = v, errorMessage = null) }

    fun onExactBirthDateChanged(v: String) = _uiState.update { curr -> curr.copy(exactBirthDate = v, errorMessage = null) }

    fun onWeightChanged(v: String) {
        val filtered = v.filter { it.isDigit() || it == '.' }
        _uiState.update { curr -> curr.copy(weightKg = filtered, errorMessage = null) }
    }

    fun onGenderChanged(v: String) = _uiState.update { curr -> curr.copy(gender = v, errorMessage = null) }

    fun onAllergiesChanged(v: String) = _uiState.update { curr -> curr.copy(allergies = v, errorMessage = null) }

    fun onPhotoUrlChanged(v: String) = _uiState.update { curr -> curr.copy(photoUrl = v, errorMessage = null) }

    fun calculateBirthDateFromText(ageInput: String): String? {
        if (ageInput.isBlank()) return null
        val regex = Regex("(\\d+)\\s*(año|ano|mes|semana)?", RegexOption.IGNORE_CASE)
        val matchResult = regex.find(ageInput.trim()) ?: return null

        val amount = matchResult.groupValues[1].toIntOrNull() ?: return null
        val unit = matchResult.groupValues.getOrNull(2)?.lowercase() ?: "año"

        var year = 2026
        var month = 10
        var day = 5

        when {
            unit.startsWith("año") || unit.startsWith("ano") -> {
                year -= amount
            }
            unit.startsWith("mes") -> {
                val totalMonths = year * 12 + (month - 1) - amount
                year = totalMonths / 12
                month = (totalMonths % 12) + 1
            }
            unit.startsWith("semana") -> {
                val totalDays = amount * 7
                val monthsToSubtract = totalDays / 30
                val daysRemainder = totalDays % 30
                year -= monthsToSubtract / 12
                month -= monthsToSubtract % 12
                if (month <= 0) {
                    year -= 1
                    month += 12
                }
                day -= daysRemainder
                if (day <= 0) {
                    month -= 1
                    if (month <= 0) {
                        year -= 1
                        month += 12
                    }
                    day += 30
                }
            }
            else -> year -= amount
        }

        val monthStr = month.toString().padStart(2, '0')
        val dayStr = day.toString().padStart(2, '0')
        return "$year-$monthStr-$dayStr"
    }

    fun savePet(onSuccess: () -> Unit) {
        val s = _uiState.value

        // Validation 1: Name length >= 2
        if (s.name.trim().length < 2) {
            _uiState.update { curr -> curr.copy(errorMessage = "El nombre de la mascota debe tener al menos 2 caracteres (sin números ni símbolos)") }
            return
        }

        // Validation 2: Species required
        if (s.species.isBlank()) {
            _uiState.update { curr -> curr.copy(errorMessage = "Selecciona la especie de la mascota") }
            return
        }

        // Validation 3: Age calculation
        val calculatedBirthDate: String? = if (s.ageMode == AgeInputMode.EXACT_DATE) {
            if (s.exactBirthDate.isBlank()) null else s.exactBirthDate
        } else {
            calculateBirthDateFromText(s.approximateAge)
        }

        if (calculatedBirthDate == null) {
            _uiState.update { curr -> curr.copy(errorMessage = "Formato de edad inválido. Usa ej. '3 años' o '5 meses'") }
            return
        }

        val displayAge = if (s.ageMode == AgeInputMode.EXACT_DATE) s.exactBirthDate else s.approximateAge
        val displayWeight = if (s.weightKg.isNotBlank()) "${s.weightKg} kg" else "4.5 kg"

        val pet = Pet(
            id = s.id,
            ownerId = ownerId,
            name = s.name.trim(),
            species = s.species,
            breed = if (s.breed.isBlank()) "Mestizo" else s.breed,
            age = displayAge,
            gender = s.gender,
            allergies = if (s.allergies.isBlank()) null else s.allergies,
            photoUrl = if (s.photoUrl.isBlank()) "https://images.unsplash.com/photo-1514888286974-6c03e2ca1dba" else s.photoUrl,
            medicalId = "PM-${(1000..9999).random()}-A",
            weight = displayWeight
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
                        curr.copy(isLoading = false, errorMessage = err.message ?: "Error al guardar mascota")
                    }
                }
        }
    }
}

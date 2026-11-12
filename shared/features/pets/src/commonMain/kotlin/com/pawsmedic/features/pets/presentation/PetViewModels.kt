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
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest

// --- PET LIST UI STATE & VIEWMODEL ---
data class PetListUiState(
    val pets: List<Pet> = emptyList(),
    val selectedPet: Pet? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class PetListViewModel(
    private val getPetsUseCase: GetPetsUseCase,
    private val ownerId: String,
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
                        val selectedPet = curr.selectedPet
                            ?.let { selected -> list.firstOrNull { it.id == selected.id } }
                            ?: list.firstOrNull()
                        curr.copy(
                            pets = list,
                            selectedPet = selectedPet,
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

val DEFAULT_SPECIES_OPTIONS = listOf(
    SpeciesOption(1, "Canino"),
    SpeciesOption(2, "Felino"),
    SpeciesOption(3, "Ave"),
    SpeciesOption(4, "Roedor"),
    SpeciesOption(5, "Otro")
)

val DEFAULT_BREED_OPTIONS = listOf(
    // Canino (speciesId = 1)
    BreedOption(101, "Mestizo / Criollo", 1, SpeciesOption(1, "Canino")),
    BreedOption(102, "Labrador Retriever", 1, SpeciesOption(1, "Canino")),
    BreedOption(103, "Golden Retriever", 1, SpeciesOption(1, "Canino")),
    BreedOption(104, "Pastor Alemán", 1, SpeciesOption(1, "Canino")),
    BreedOption(105, "Poodle / Caniche", 1, SpeciesOption(1, "Canino")),
    BreedOption(106, "Bulldog", 1, SpeciesOption(1, "Canino")),
    BreedOption(107, "Beagle", 1, SpeciesOption(1, "Canino")),
    BreedOption(108, "Chihuahua", 1, SpeciesOption(1, "Canino")),
    BreedOption(109, "Pug", 1, SpeciesOption(1, "Canino")),
    BreedOption(110, "Schnauzer", 1, SpeciesOption(1, "Canino")),
    BreedOption(111, "Boxer", 1, SpeciesOption(1, "Canino")),
    BreedOption(112, "Dachshund (Teckel)", 1, SpeciesOption(1, "Canino")),
    BreedOption(113, "Shih Tzu", 1, SpeciesOption(1, "Canino")),
    BreedOption(114, "Yorkshire Terrier", 1, SpeciesOption(1, "Canino")),
    BreedOption(115, "Pomerania", 1, SpeciesOption(1, "Canino")),
    BreedOption(116, "Husky Siberiano", 1, SpeciesOption(1, "Canino")),
    BreedOption(117, "Rottweiler", 1, SpeciesOption(1, "Canino")),
    BreedOption(118, "Doberman", 1, SpeciesOption(1, "Canino")),
    BreedOption(119, "Gran Danés", 1, SpeciesOption(1, "Canino")),
    BreedOption(120, "Otro / Desconocido", 1, SpeciesOption(1, "Canino")),

    // Felino (speciesId = 2)
    BreedOption(201, "Mestizo Doméstico", 2, SpeciesOption(2, "Felino")),
    BreedOption(202, "Siamés", 2, SpeciesOption(2, "Felino")),
    BreedOption(203, "Persa", 2, SpeciesOption(2, "Felino")),
    BreedOption(204, "Maine Coon", 2, SpeciesOption(2, "Felino")),
    BreedOption(205, "Bengalí", 2, SpeciesOption(2, "Felino")),
    BreedOption(206, "Ragdoll", 2, SpeciesOption(2, "Felino")),
    BreedOption(207, "Sphynx (Esfinge)", 2, SpeciesOption(2, "Felino")),
    BreedOption(208, "British Shorthair", 2, SpeciesOption(2, "Felino")),
    BreedOption(209, "Angora", 2, SpeciesOption(2, "Felino")),
    BreedOption(210, "Otro / Desconocido", 2, SpeciesOption(2, "Felino")),

    // Ave (speciesId = 3)
    BreedOption(301, "Perico / Periquito", 3, SpeciesOption(3, "Ave")),
    BreedOption(302, "Canario", 3, SpeciesOption(3, "Ave")),
    BreedOption(303, "Ninfa", 3, SpeciesOption(3, "Ave")),
    BreedOption(304, "Loro", 3, SpeciesOption(3, "Ave")),
    BreedOption(305, "Cacatúa", 3, SpeciesOption(3, "Ave")),
    BreedOption(306, "Agapornis", 3, SpeciesOption(3, "Ave")),
    BreedOption(307, "Otro / Desconocido", 3, SpeciesOption(3, "Ave")),

    // Roedor (speciesId = 4)
    BreedOption(401, "Hámster", 4, SpeciesOption(4, "Roedor")),
    BreedOption(402, "Cuy / Cobaya", 4, SpeciesOption(4, "Roedor")),
    BreedOption(403, "Conejo", 4, SpeciesOption(4, "Roedor")),
    BreedOption(404, "Chinchilla", 4, SpeciesOption(4, "Roedor")),
    BreedOption(405, "Rata doméstica", 4, SpeciesOption(4, "Roedor")),
    BreedOption(406, "Otro / Desconocido", 4, SpeciesOption(4, "Roedor")),

    // Otro (speciesId = 5)
    BreedOption(501, "Mestizo / General", 5, SpeciesOption(5, "Otro"))
)

data class PetFormUiState(
    val id: String = "",
    val name: String = "",
    val species: String = "",
    val breed: String = "",
    val breedId: Int? = null,
    val speciesId: Int? = null,
    val speciesOptions: List<SpeciesOption> = DEFAULT_SPECIES_OPTIONS,
    val breeds: List<BreedOption> = DEFAULT_BREED_OPTIONS,
    val gender: String = "",
    val photo_url: String = "",
    val ageMode: AgeInputMode = AgeInputMode.EXACT_DATE,
    val ageNumber: String = "",
    val ageUnit: String = "Años",
    val exactBirthDate: String = "",
    val weightKg: String = "",
    val allergies: String = "",
    val isEditMode: Boolean = false,
    val isLoading: Boolean = false,
    val isSaved: Boolean = false,
    val errorMessage: String? = null
) {
    val availableBreeds: List<BreedOption>
        get() {
            val targetSpeciesId = speciesId ?: speciesOptions.firstOrNull {
                it.name.equals(species, ignoreCase = true)
            }?.id
            val filtered = breeds.filter { it.speciesId == targetSpeciesId }
            return if (targetSpeciesId != null) filtered else breeds
        }
}

@Serializable
data class BreedOption(
    val id: Int,
    val name: String,
    @SerialName("species_id") val speciesId: Int,
    val species: SpeciesOption? = null
) {
    val speciesName: String get() = species?.name.orEmpty()
}

@Serializable
data class SpeciesOption(val id: Int, val name: String)

class PetFormViewModel(
    private val addPetUseCase: AddPetUseCase,
    private val updatePetUseCase: UpdatePetUseCase,
    private val getPetByIdUseCase: GetPetByIdUseCase,
    private val initialPetId: String? = null,
    private val ownerId: String,
    private val supabase: SupabaseClient,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
) {
    private val _uiState = MutableStateFlow(PetFormUiState())
    val uiState: StateFlow<PetFormUiState> = _uiState.asStateFlow()

    init {
        loadCatalogs()
        if (!initialPetId.isNullOrBlank()) {
            loadPetForEdit(initialPetId)
        }
    }

    private fun loadCatalogs() {
        scope.launch {
            runCatching {
                val speciesList = supabase.postgrest["species"]
                    .select { }
                    .decodeList<SpeciesOption>()
                val speciesMap = speciesList.associateBy { it.id }
                val breedList = supabase.postgrest["breeds"]
                    .select { }
                    .decodeList<BreedOption>()
                    .map { breed -> breed.copy(species = speciesMap[breed.speciesId]) }
                speciesList to breedList
            }.onSuccess { (species, breeds) ->
                _uiState.update { curr ->
                    curr.copy(
                        speciesOptions = if (species.isNotEmpty()) species.sortedBy(SpeciesOption::name) else curr.speciesOptions,
                        breeds = if (breeds.isNotEmpty()) breeds.sortedBy(BreedOption::name) else curr.breeds
                    )
                }
            }.onFailure {
                // Keep default options without error text blocking the user
            }
        }
    }

    private fun loadPetForEdit(id: String) {
        scope.launch {
            _uiState.update { curr -> curr.copy(isLoading = true) }
            getPetByIdUseCase(id).onSuccess { p ->
                if (p != null) {
                    val ageStr = p.age.orEmpty()
                    val digits = ageStr.filter { it.isDigit() }
                    val num = digits
                    val unit = when {
                        ageStr.contains("semana", true) -> "Semanas"
                        ageStr.contains("mes", true) -> "Meses"
                        else -> "Años"
                    }

                    val resolvedSpeciesName = if (p.species.contains("Gato", true)) "Felino" else if (p.species.contains("Perro", true)) "Canino" else p.species
                    val currentOptions = _uiState.value.speciesOptions
                    val currentBreeds = _uiState.value.breeds
                    val resolvedSpeciesId = p.speciesId ?: currentOptions.firstOrNull {
                        it.name.equals(resolvedSpeciesName, ignoreCase = true) || resolvedSpeciesName.contains(it.name, ignoreCase = true)
                    }?.id
                    val resolvedBreedName = p.breed ?: "Mestizo"
                    val resolvedBreedId = p.breedId ?: currentBreeds.firstOrNull {
                        it.name.equals(resolvedBreedName, ignoreCase = true)
                    }?.id

                    _uiState.update { curr ->
                        curr.copy(
                            id = p.id,
                            name = p.name,
                            species = resolvedSpeciesName,
                            breed = resolvedBreedName,
                            breedId = resolvedBreedId,
                            speciesId = resolvedSpeciesId,
                            gender = p.gender.orEmpty(),
                            photo_url = p.photo_url.orEmpty(),
                            ageNumber = num,
                            ageUnit = unit,
                            exactBirthDate = p.dateOfBirth.orEmpty(),
                            weightKg = p.weight?.replace(" kg", "")?.replace(" lbs", "") ?: "",
                            allergies = p.allergies ?: "",
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
        val filtered = v.filter { it.isLetter() || it.isWhitespace() }
        _uiState.update { curr -> curr.copy(name = filtered, errorMessage = null) }
    }

    fun onSpeciesChanged(option: SpeciesOption) {
        _uiState.update {
            it.copy(
                species = option.name,
                speciesId = option.id,
                breed = "",
                breedId = null,
                errorMessage = null
            )
        }
    }

    fun onBreedChanged(option: BreedOption) = _uiState.update {
        it.copy(
            breed = option.name,
            breedId = option.id,
            species = option.speciesName,
            speciesId = option.speciesId,
            errorMessage = null
        )
    }

    fun onGenderChanged(v: String) = _uiState.update { curr -> curr.copy(gender = v, errorMessage = null) }

    fun onPhotoUrlChanged(v: String) = _uiState.update { curr -> curr.copy(photo_url = v, errorMessage = null) }

    fun onPhotoUploadError(message: String) {
        _uiState.update { curr -> curr.copy(errorMessage = message) }
    }

    fun onAgeModeChanged(mode: AgeInputMode) = _uiState.update { curr -> curr.copy(ageMode = mode, errorMessage = null) }

    fun onAgeNumberChanged(v: String) {
        if (v.all { it.isDigit() }) {
            _uiState.update { curr -> curr.copy(ageNumber = v, errorMessage = null) }
        }
    }

    fun onAgeUnitChanged(unit: String) = _uiState.update { curr -> curr.copy(ageUnit = unit, errorMessage = null) }

    fun calculateAgeFromBirthDate(birthDateStr: String): Pair<String, String>? {
        val cleanStr = birthDateStr.trim()
        val parts = cleanStr.split("-")
        if (parts.size != 3) return null
        val bYear = parts[0].toIntOrNull() ?: return null
        val bMonth = parts[1].toIntOrNull() ?: return null
        val bDay = parts[2].toIntOrNull() ?: return null
        val todayIso = com.pawsmedic.shared.core.common.convertMillisToIsoDateString(System.currentTimeMillis())
        if (cleanStr > todayIso) return null
        val today = todayIso.split("-")
        val cYear = today[0].toInt()
        val cMonth = today[1].toInt()
        val cDay = today[2].toInt()

        val totalMonths = (cYear * 12 + cMonth) - (bYear * 12 + bMonth)
        val totalDays = (totalMonths * 30) + (cDay - bDay)

        if (totalDays <= 0) return Pair("1", "Semanas")

        return when {
            totalDays < 30 -> {
                val weeks = maxOf(1, totalDays / 7)
                Pair(weeks.toString(), "Semanas")
            }
            totalMonths < 12 -> {
                val months = maxOf(1, totalMonths)
                Pair(months.toString(), "Meses")
            }
            else -> {
                val years = maxOf(1, cYear - bYear)
                Pair(years.toString(), "Años")
            }
        }
    }

    fun onExactBirthDateChanged(v: String) {
        val agePair = calculateAgeFromBirthDate(v)
        _uiState.update { curr ->
            curr.copy(
                exactBirthDate = v,
                ageNumber = agePair?.first ?: curr.ageNumber,
                ageUnit = agePair?.second ?: curr.ageUnit,
                errorMessage = null
            )
        }
    }

    fun onWeightChanged(v: String) {
        val filtered = v.filter { it.isDigit() || it == '.' }
        _uiState.update { curr -> curr.copy(weightKg = filtered, errorMessage = null) }
    }

    fun onAllergiesChanged(v: String) = _uiState.update { curr -> curr.copy(allergies = v, errorMessage = null) }

    fun calculateBirthDate(ageNumberString: String, ageUnit: String): String? {
        val amount = ageNumberString.toIntOrNull() ?: return null
        if (amount <= 0) return null

        val today = com.pawsmedic.shared.core.common.convertMillisToIsoDateString(System.currentTimeMillis())
            .split("-")
        var year = today[0].toInt()
        var month = today[1].toInt()
        var day = today[2].toInt()

        when (ageUnit) {
            "Años" -> year -= amount
            "Meses" -> {
                val totalMonths = year * 12 + (month - 1) - amount
                year = totalMonths / 12
                month = (totalMonths % 12) + 1
            }
            "Semanas" -> {
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

        // Validation 2: Species & Breed required
        if (s.species.isBlank()) {
            _uiState.update { curr -> curr.copy(errorMessage = "Selecciona la especie de la mascota") }
            return
        }
        val hasBreedsForSpecies = s.availableBreeds.isNotEmpty()
        if (hasBreedsForSpecies && s.breed.isBlank()) {
            _uiState.update { curr -> curr.copy(errorMessage = "Selecciona la raza de la mascota") }
            return
        }

        val resolvedSpeciesId = s.speciesId ?: s.speciesOptions.firstOrNull {
            it.name.equals(s.species, ignoreCase = true)
        }?.id ?: 1

        val resolvedBreedId = s.breedId ?: s.breeds.firstOrNull {
            it.name.equals(s.breed, ignoreCase = true)
        }?.id

        // Validation 3: Age calculation
        val calculatedBirthDate: String? = if (s.ageMode == AgeInputMode.EXACT_DATE) {
            if (s.exactBirthDate.isBlank()) null else s.exactBirthDate
        } else {
            calculateBirthDate(s.ageNumber, s.ageUnit)
        }

        if (calculatedBirthDate == null) {
            _uiState.update { curr ->
                curr.copy(errorMessage = "Ingresa una fecha de nacimiento válida o una edad aproximada.")
            }
            return
        }
        val today = com.pawsmedic.shared.core.common.convertMillisToIsoDateString(System.currentTimeMillis())
        if (calculatedBirthDate > today) {
            _uiState.update { curr ->
                curr.copy(errorMessage = "La fecha de nacimiento no puede ser futura.")
            }
            return
        }
        if (s.weightKg.isNotBlank() && (s.weightKg.toDoubleOrNull()?.let { it > 0 } != true)) {
            _uiState.update { curr -> curr.copy(errorMessage = "Ingresa un peso mayor a cero.") }
            return
        }

        val pet = Pet(
            id = s.id,
            ownerId = ownerId,
            name = s.name.trim(),
            species = s.species,
            breed = s.breed.takeIf(String::isNotBlank),
            breedId = resolvedBreedId,
            speciesId = resolvedSpeciesId,
            gender = s.gender.takeIf(String::isNotBlank),
            age = if (s.ageNumber.isBlank()) null else "${s.ageNumber} ${s.ageUnit}",
            dateOfBirth = calculatedBirthDate,
            allergies = if (s.allergies.isBlank()) null else s.allergies,
            photo_url = s.photo_url.takeIf(String::isNotBlank),
            weight = s.weightKg.takeIf(String::isNotBlank)?.let { "$it kg" }
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

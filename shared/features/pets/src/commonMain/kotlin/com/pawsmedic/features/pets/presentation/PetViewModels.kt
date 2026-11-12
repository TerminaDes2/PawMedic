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
    SpeciesOption(4, "Reptil"),
    SpeciesOption(5, "Conejo"),
    SpeciesOption(6, "Cobaya / Cuy"),
    SpeciesOption(7, "Hámster"),
    SpeciesOption(8, "Hurón"),
    SpeciesOption(9, "Erizo"),
    SpeciesOption(10, "Chinchilla y Degú")
)

val DEFAULT_BREED_OPTIONS = listOf(
    // Canino (speciesId = 1)
    BreedOption(1, "Labrador Retriever", 1, SpeciesOption(1, "Canino")),
    BreedOption(2, "Pastor Alemán", 1, SpeciesOption(1, "Canino")),
    BreedOption(3, "Golden Retriever", 1, SpeciesOption(1, "Canino")),
    BreedOption(4, "Bulldog Francés", 1, SpeciesOption(1, "Canino")),
    BreedOption(5, "Bulldog Inglés", 1, SpeciesOption(1, "Canino")),
    BreedOption(6, "Poodle (Caniche)", 1, SpeciesOption(1, "Canino")),
    BreedOption(7, "Beagle", 1, SpeciesOption(1, "Canino")),
    BreedOption(8, "Chihuahua", 1, SpeciesOption(1, "Canino")),
    BreedOption(9, "Rottweiler", 1, SpeciesOption(1, "Canino")),
    BreedOption(10, "Yorkshire Terrier", 1, SpeciesOption(1, "Canino")),
    BreedOption(11, "Boxer", 1, SpeciesOption(1, "Canino")),
    BreedOption(12, "Dachshund (Salchicha)", 1, SpeciesOption(1, "Canino")),
    BreedOption(13, "Husky Siberiano", 1, SpeciesOption(1, "Canino")),
    BreedOption(14, "Dóberman", 1, SpeciesOption(1, "Canino")),
    BreedOption(15, "Schnauzer Miniatura", 1, SpeciesOption(1, "Canino")),
    BreedOption(16, "Shih Tzu", 1, SpeciesOption(1, "Canino")),
    BreedOption(17, "Pomerania", 1, SpeciesOption(1, "Canino")),
    BreedOption(18, "Border Collie", 1, SpeciesOption(1, "Canino")),
    BreedOption(19, "Pug", 1, SpeciesOption(1, "Canino")),
    BreedOption(20, "Pitbull (American Pit Bull Terrier)", 1, SpeciesOption(1, "Canino")),

    // Felino (speciesId = 2)
    BreedOption(21, "Gato Doméstico de Pelo Corto (Común Europeo)", 2, SpeciesOption(2, "Felino")),
    BreedOption(22, "Siamés", 2, SpeciesOption(2, "Felino")),
    BreedOption(23, "Persa", 2, SpeciesOption(2, "Felino")),
    BreedOption(24, "Maine Coon", 2, SpeciesOption(2, "Felino")),
    BreedOption(25, "British Shorthair", 2, SpeciesOption(2, "Felino")),
    BreedOption(26, "Ragdoll", 2, SpeciesOption(2, "Felino")),
    BreedOption(27, "Sphynx (Esfinge)", 2, SpeciesOption(2, "Felino")),
    BreedOption(28, "Bengalí (Bengal)", 2, SpeciesOption(2, "Felino")),
    BreedOption(29, "Abisinio", 2, SpeciesOption(2, "Felino")),
    BreedOption(30, "Angora Turco", 2, SpeciesOption(2, "Felino")),
    BreedOption(31, "Azul Ruso", 2, SpeciesOption(2, "Felino")),
    BreedOption(32, "Birmano", 2, SpeciesOption(2, "Felino")),
    BreedOption(33, "Scottish Fold", 2, SpeciesOption(2, "Felino")),
    BreedOption(34, "Exótico de Pelo Corto", 2, SpeciesOption(2, "Felino")),
    BreedOption(35, "Bosque de Noruega", 2, SpeciesOption(2, "Felino")),
    BreedOption(36, "Manx", 2, SpeciesOption(2, "Felino")),
    BreedOption(37, "Tonkinés", 2, SpeciesOption(2, "Felino")),
    BreedOption(38, "Balinés", 2, SpeciesOption(2, "Felino")),
    BreedOption(39, "Mau Egipcio", 2, SpeciesOption(2, "Felino")),
    BreedOption(40, "Gato Doméstico de Pelo Largo", 2, SpeciesOption(2, "Felino")),

    // Ave (speciesId = 3)
    BreedOption(41, "Periquito Australiano (Budgerigar)", 3, SpeciesOption(3, "Ave")),
    BreedOption(42, "Ninfa (Cockatiel)", 3, SpeciesOption(3, "Ave")),
    BreedOption(43, "Agapornis (Inseparables)", 3, SpeciesOption(3, "Ave")),
    BreedOption(44, "Canario", 3, SpeciesOption(3, "Ave")),
    BreedOption(45, "Guacamayo (Ara)", 3, SpeciesOption(3, "Ave")),
    BreedOption(46, "Yaco (Loro Gris Africano)", 3, SpeciesOption(3, "Ave")),
    BreedOption(47, "Cacatúa Galerita / Alba", 3, SpeciesOption(3, "Ave")),
    BreedOption(48, "Perico Monje (Cotorra Argentina)", 3, SpeciesOption(3, "Ave")),
    BreedOption(49, "Pinzón Cebras (Zebra Finch)", 3, SpeciesOption(3, "Ave")),
    BreedOption(50, "Conura de sol (Sun Conure)", 3, SpeciesOption(3, "Ave")),
    BreedOption(51, "Loro Eclecto", 3, SpeciesOption(3, "Ave")),
    BreedOption(52, "Perico de Bourke", 3, SpeciesOption(3, "Ave")),
    BreedOption(53, "Paloma Diamante", 3, SpeciesOption(3, "Ave")),
    BreedOption(54, "Lorito Lory", 3, SpeciesOption(3, "Ave")),
    BreedOption(55, "Cacatúa Rosada (Galah)", 3, SpeciesOption(3, "Ave")),
    BreedOption(56, "Perico Australiano Multicolor", 3, SpeciesOption(3, "Ave")),
    BreedOption(57, "Cacatúa de Leadbeater", 3, SpeciesOption(3, "Ave")),
    BreedOption(58, "Lorículo", 3, SpeciesOption(3, "Ave")),
    BreedOption(59, "Perico de Kramer", 3, SpeciesOption(3, "Ave")),
    BreedOption(60, "Cacatúa Enana", 3, SpeciesOption(3, "Ave")),

    // Reptil (speciesId = 4)
    BreedOption(61, "Gecko Leopardo", 4, SpeciesOption(4, "Reptil")),
    BreedOption(62, "Dragón Barbudo", 4, SpeciesOption(4, "Reptil")),
    BreedOption(63, "Serpiente del Maíz (Corn Snake)", 4, SpeciesOption(4, "Reptil")),
    BreedOption(64, "Pitón Real (Ball Python)", 4, SpeciesOption(4, "Reptil")),
    BreedOption(65, "Tortuga de orejas rojas", 4, SpeciesOption(4, "Reptil")),
    BreedOption(66, "Tortuga Rusa", 4, SpeciesOption(4, "Reptil")),
    BreedOption(67, "Iguana Verde", 4, SpeciesOption(4, "Reptil")),
    BreedOption(68, "Serpiente Rey (Lampropeltis)", 4, SpeciesOption(4, "Reptil")),
    BreedOption(69, "Gecko Crestado", 4, SpeciesOption(4, "Reptil")),
    BreedOption(70, "Uromastyx", 4, SpeciesOption(4, "Reptil")),
    BreedOption(71, "Tortuga Sulcata", 4, SpeciesOption(4, "Reptil")),
    BreedOption(72, "Anolis Verde", 4, SpeciesOption(4, "Reptil")),
    BreedOption(73, "Serpiente de Garter", 4, SpeciesOption(4, "Reptil")),
    BreedOption(74, "Pitón Alfombra", 4, SpeciesOption(4, "Reptil")),
    BreedOption(75, "Gecko de cola gorda africano", 4, SpeciesOption(4, "Reptil")),
    BreedOption(76, "Tortuga de caja", 4, SpeciesOption(4, "Reptil")),
    BreedOption(77, "Gecko Tokay", 4, SpeciesOption(4, "Reptil")),
    BreedOption(78, "Serpiente de leche (Milk Snake)", 4, SpeciesOption(4, "Reptil")),
    BreedOption(79, "Skink de lengua azul", 4, SpeciesOption(4, "Reptil")),
    BreedOption(80, "Tortuga griega", 4, SpeciesOption(4, "Reptil")),

    // Conejo (speciesId = 5)
    BreedOption(81, "Conejo Belier (Lop)", 5, SpeciesOption(5, "Conejo")),
    BreedOption(82, "Conejo Cabeza de León", 5, SpeciesOption(5, "Conejo")),
    BreedOption(83, "Conejo Enano Holandés", 5, SpeciesOption(5, "Conejo")),
    BreedOption(84, "Conejo Neocelandés", 5, SpeciesOption(5, "Conejo")),
    BreedOption(85, "Conejo Angora", 5, SpeciesOption(5, "Conejo")),
    BreedOption(86, "Conejo Gigante de Flandes", 5, SpeciesOption(5, "Conejo")),
    BreedOption(87, "Conejo Rex", 5, SpeciesOption(5, "Conejo")),
    BreedOption(88, "Conejo California", 5, SpeciesOption(5, "Conejo")),
    BreedOption(89, "Conejo Arlequín", 5, SpeciesOption(5, "Conejo")),
    BreedOption(90, "Conejo Mini Lop", 5, SpeciesOption(5, "Conejo")),
    BreedOption(91, "Conejo Holandés", 5, SpeciesOption(5, "Conejo")),
    BreedOption(92, "Conejo Chinchilla", 5, SpeciesOption(5, "Conejo")),
    BreedOption(93, "Conejo Hotot", 5, SpeciesOption(5, "Conejo")),
    BreedOption(94, "Conejo Tan", 5, SpeciesOption(5, "Conejo")),
    BreedOption(95, "Conejo Polaco", 5, SpeciesOption(5, "Conejo")),
    BreedOption(96, "Conejo Himalayo", 5, SpeciesOption(5, "Conejo")),
    BreedOption(97, "Conejo Angora Inglés", 5, SpeciesOption(5, "Conejo")),
    BreedOption(98, "Conejo Cashmere Lop", 5, SpeciesOption(5, "Conejo")),
    BreedOption(99, "Conejo Americano", 5, SpeciesOption(5, "Conejo")),
    BreedOption(100, "Conejo Silver Marten", 5, SpeciesOption(5, "Conejo"))
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
                        speciesOptions = if (species.isNotEmpty()) {
                            species
                                .filterNot { it.name.equals("Otro", ignoreCase = true) }
                                .sortedBy(SpeciesOption::name)
                        } else {
                            curr.speciesOptions
                        },
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

                    val currentOptions = _uiState.value.speciesOptions
                    val currentBreeds = _uiState.value.breeds
                    val resolvedSpecies = p.speciesId?.let { speciesId ->
                        currentOptions.firstOrNull { it.id == speciesId }
                    }
                    val resolvedSpeciesId = p.speciesId ?: resolvedSpecies?.id
                    val speciesBreeds = currentBreeds.filter {
                        resolvedSpeciesId != null && it.speciesId == resolvedSpeciesId
                    }
                    val resolvedBreed = p.breedId?.let { breedId ->
                        speciesBreeds.firstOrNull { it.id == breedId }
                    }
                    val resolvedBreedId = resolvedBreed?.id ?: p.breedId

                    _uiState.update { curr ->
                        curr.copy(
                            id = p.id,
                            name = p.name,
                            species = resolvedSpecies?.name ?: p.species,
                            breed = resolvedBreed?.name.orEmpty(),
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

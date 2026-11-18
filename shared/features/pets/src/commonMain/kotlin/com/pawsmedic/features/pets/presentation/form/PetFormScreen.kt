package com.pawsmedic.features.pets.presentation.form

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pawsmedic.features.pets.presentation.AgeInputMode
import com.pawsmedic.features.pets.presentation.BreedOption
import com.pawsmedic.features.pets.presentation.PetFormUiState
import com.pawsmedic.features.pets.presentation.PetFormViewModel
import com.pawsmedic.features.pets.presentation.SpeciesOption
import com.pawsmedic.features.pets.presentation.components.PetAvatarImage
import com.pawsmedic.shared.core.common.convertMillisToIsoDateString
import com.pawsmedic.shared.core.common.formatIsoToDisplayDate
import com.pawsmedic.shared.core.common.rememberPhotoPicker
import com.pawsmedic.shared.core.designsystem.components.DatePickerModal
import com.pawsmedic.shared.core.designsystem.components.PawMedicPrimaryButton
import com.pawsmedic.shared.core.designsystem.components.PawMedicSecondaryButton
import com.pawsmedic.shared.core.designsystem.components.PawMedicTextField
import com.pawsmedic.shared.core.designsystem.components.PawMedicTopBar
import com.pawsmedic.shared.core.designsystem.theme.PawMedicColors
import io.github.jan.supabase.SupabaseClient

private const val TOTAL_STEPS = 3

@Composable
fun PetFormScreen(
    viewModel: PetFormViewModel,
    onBackClick: () -> Unit,
    onSaveSuccess: () -> Unit,
    supabaseClient: SupabaseClient,
    onPickPhoto: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val defaultPhotoPicker = rememberPhotoPicker(
        supabaseClient = supabaseClient,
        onPhotoPicked = viewModel::onPhotoUrlChanged,
        onUploadError = viewModel::onPhotoUploadError
    )
    var step by rememberSaveable { mutableIntStateOf(0) }
    var stepError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            onSaveSuccess()
        }
    }
    LaunchedEffect(step) {
        stepError = null
    }

    fun goBack() {
        if (step > 0) step-- else onBackClick()
    }

    fun onNext() {
        val error = validateStep(step, uiState)
        if (error != null) {
            stepError = error
            return
        }
        stepError = null
        if (step < TOTAL_STEPS - 1) {
            step++
        } else {
            viewModel.savePet(onSaveSuccess)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(PawMedicColors.BackgroundScreen)
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        PawMedicTopBar(
            title = if (uiState.isEditMode) "Editar Mascota" else "Registrar Mascota",
            onBackClick = ::goBack
        )

        Spacer(modifier = Modifier.height(12.dp))
        StepProgress(currentStep = step)
        Spacer(modifier = Modifier.height(20.dp))

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
        ) {
            when (step) {
                0 -> SpeciesBreedStep(uiState, viewModel)
                1 -> BasicInfoStep(
                    uiState = uiState,
                    viewModel = viewModel,
                    onPickPhoto = { onPickPhoto?.invoke() ?: defaultPhotoPicker() }
                )
                else -> AgeWeightStep(uiState, viewModel)
            }

            val message = stepError ?: if (step == TOTAL_STEPS - 1) uiState.errorMessage else null
            if (message != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = message,
                    fontSize = 13.sp,
                    color = PawMedicColors.Error,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        Spacer(modifier = Modifier.height(8.dp))
        PawMedicPrimaryButton(
            text = when {
                step < TOTAL_STEPS - 1 -> "Continuar"
                uiState.isEditMode -> "Guardar cambios"
                else -> "Guardar mascota"
            },
            onClick = ::onNext,
            isLoading = uiState.isLoading && step == TOTAL_STEPS - 1
        )
        PawMedicSecondaryButton(
            text = if (step == 0) "Cancelar" else "Atrás",
            onClick = ::goBack
        )
        Spacer(modifier = Modifier.height(8.dp))
    }
}

private fun validateStep(step: Int, uiState: PetFormUiState): String? = when (step) {
    0 -> when {
        uiState.species.isBlank() -> "Selecciona la especie de tu mascota"
        uiState.availableBreeds.isNotEmpty() && uiState.breed.isBlank() -> "Selecciona la raza"
        else -> null
    }
    1 -> {
        val name = uiState.name.trim()
        when {
            name.length < 2 || !name.all { it.isLetter() || it == ' ' } ->
                "El nombre debe tener mínimo 2 letras y no incluir números ni símbolos"
            uiState.gender.isBlank() -> "Selecciona el sexo de tu mascota"
            else -> null
        }
    }
    else -> when {
        uiState.ageMode == AgeInputMode.APPROXIMATE &&
            (uiState.ageNumber.toIntOrNull() ?: 0) <= 0 -> "Ingresa una edad aproximada válida"
        uiState.ageMode == AgeInputMode.EXACT_DATE && uiState.exactBirthDate.isBlank() ->
            "Selecciona la fecha de nacimiento"
        (uiState.weightKg.toDoubleOrNull() ?: 0.0) <= 0.0 ->
            "Ingresa un peso válido en kilos"
        else -> null
    }
}

@Composable
private fun StepProgress(currentStep: Int) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth()) {
            repeat(TOTAL_STEPS) { index ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(5.dp)
                        .clip(RoundedCornerShape(50))
                        .background(
                            if (index <= currentStep) PawMedicColors.Teal600
                            else PawMedicColors.Gray200
                        )
                )
                if (index < TOTAL_STEPS - 1) Spacer(modifier = Modifier.width(6.dp))
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Paso ${currentStep + 1} de $TOTAL_STEPS",
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = PawMedicColors.Gray600
        )
    }
}

@Composable
private fun StepHeader(title: String, subtitle: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(title, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = PawMedicColors.Gray900)
        Spacer(modifier = Modifier.height(6.dp))
        Text(subtitle, fontSize = 14.sp, color = PawMedicColors.Gray600)
        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
private fun SpeciesBreedStep(uiState: PetFormUiState, viewModel: PetFormViewModel) {
    StepHeader(
        title = "¿Qué tipo de mascota tienes?",
        subtitle = "Elige la especie y la raza para personalizar su perfil."
    )
    uiState.speciesOptions.chunked(2).forEachIndexed { rowIndex, rowItems ->
        if (rowIndex > 0) Spacer(modifier = Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            rowItems.forEachIndexed { index, option ->
                SpeciesCard(
                    name = option.name,
                    isSelected = uiState.species == option.name,
                    onClick = { viewModel.onSpeciesChanged(option) },
                    modifier = Modifier.weight(1f)
                )
                if (index == 0) Spacer(modifier = Modifier.width(12.dp))
            }
            if (rowItems.size == 1) {
                Spacer(modifier = Modifier.width(12.dp))
                Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
    Spacer(modifier = Modifier.height(24.dp))
    FormDropdownField(
        label = "Raza",
        selectedValue = uiState.breed,
        placeholder = if (uiState.species.isBlank()) "Selecciona primero la especie" else "Selecciona la raza",
        options = uiState.availableBreeds.map(BreedOption::name),
        enabled = uiState.species.isNotBlank() && uiState.availableBreeds.isNotEmpty(),
        onOptionSelected = { name ->
            uiState.availableBreeds.firstOrNull { it.name == name }?.let(viewModel::onBreedChanged)
        }
    )
}

@Composable
private fun SpeciesCard(
    name: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(16.dp)
    Box(
        modifier = modifier
            .height(140.dp)
            .clip(shape)
            .background(if (isSelected) PawMedicColors.Teal50 else PawMedicColors.White)
            .border(
                if (isSelected) 2.dp else 1.dp,
                if (isSelected) PawMedicColors.Teal600 else PawMedicColors.Gray200,
                shape
            )
            .clickable(onClick = onClick)
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = speciesIconFor(name).vector(),
                contentDescription = name,
                tint = if (isSelected) PawMedicColors.Teal600 else PawMedicColors.Gray600,
                modifier = Modifier.size(52.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                name,
                fontSize = 14.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                color = PawMedicColors.Gray800,
                textAlign = TextAlign.Center
            )
        }
        if (isSelected) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(24.dp)
                    .clip(androidx.compose.foundation.shape.CircleShape)
                    .background(PawMedicColors.Teal600),
                contentAlignment = Alignment.Center
            ) {
                Text("✓", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = PawMedicColors.White)
            }
        }
    }
}

@Composable
private fun BasicInfoStep(
    uiState: PetFormUiState,
    viewModel: PetFormViewModel,
    onPickPhoto: () -> Unit
) {
    StepHeader(
        title = "Cuéntanos sobre tu mascota",
        subtitle = "Su nombre, sexo y una foto para reconocerla fácilmente."
    )
    PhotoPickerCard(uiState.photo_url, uiState.species, uiState.isEditMode, onPickPhoto)
    Spacer(modifier = Modifier.height(16.dp))
    PawMedicTextField(
        value = uiState.name,
        onValueChange = viewModel::onNameChanged,
        label = "Nombre de la mascota (Mínimo 2 letras, sin números)",
        placeholder = "Ej. Max",
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
    )
    Spacer(modifier = Modifier.height(14.dp))
    GenderSelector(uiState.gender, viewModel::onGenderChanged)
}

@Composable
private fun AgeWeightStep(uiState: PetFormUiState, viewModel: PetFormViewModel) {
    StepHeader(
        title = "Edad y peso",
        subtitle = "Esta información ayuda a darle un mejor seguimiento de salud."
    )
    AgeInputSection(uiState, viewModel)
    Spacer(modifier = Modifier.height(14.dp))
    PawMedicTextField(
        value = uiState.weightKg,
        onValueChange = viewModel::onWeightChanged,
        label = "Peso aproximado (en kilos)",
        placeholder = "Ej. 4.5",
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next)
    )
    Spacer(modifier = Modifier.height(14.dp))
    PawMedicTextField(
        value = uiState.allergies,
        onValueChange = viewModel::onAllergiesChanged,
        label = "Notas relevantes (Alergias, cuidados)",
        placeholder = "Alergias, cuidados especiales, temperamento...",
        singleLine = false,
        modifier = Modifier.height(100.dp)
    )
}

@Composable
private fun PhotoPickerCard(
    photo_url: String,
    species: String,
    isEditMode: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(PawMedicColors.Teal50)
            .border(1.dp, PawMedicColors.Teal200, RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 20.dp, horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            PetAvatarImage(
                photo_url = photo_url.takeIf(String::isNotBlank),
                species = species,
                size = if (photo_url.isBlank()) 64.dp else 80.dp
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = when {
                    photo_url.isNotBlank() && isEditMode -> "Cambiar foto de perfil"
                    photo_url.isNotBlank() -> "Foto seleccionada (Click para cambiar)"
                    else -> "Subir foto de tu mascota"
                },
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = PawMedicColors.Teal600,
                textAlign = TextAlign.Center
            )
            if (photo_url.isBlank() && !isEditMode) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Formatos aceptados: JPG, PNG (Max 5MB)",
                    fontSize = 12.sp,
                    color = PawMedicColors.Gray400,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun GenderSelector(
    selectedGender: String,
    onGenderSelected: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Sexo de la mascota",
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = PawMedicColors.Gray800,
            modifier = Modifier.padding(bottom = 6.dp)
        )
        Row(modifier = Modifier.fillMaxWidth()) {
            GenderChip(
                text = "Macho",
                isSelected = selectedGender == "Macho",
                selectedColor = Color(0xFFE0F2FE),
                selectedBorderColor = Color(0xFF7DD3FC),
                onClick = { onGenderSelected("Macho") },
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(10.dp))
            GenderChip(
                text = "Hembra",
                isSelected = selectedGender == "Hembra",
                selectedColor = Color(0xFFFCE7F3),
                selectedBorderColor = Color(0xFFF9A8D4),
                onClick = { onGenderSelected("Hembra") },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun GenderChip(
    text: String,
    isSelected: Boolean,
    selectedColor: Color,
    selectedBorderColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(46.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(if (isSelected) selectedColor else PawMedicColors.White)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) selectedBorderColor else PawMedicColors.Gray300,
                shape = RoundedCornerShape(10.dp)
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 14.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = PawMedicColors.Gray700
        )
    }
}

@Composable
private fun AgeInputSection(
    uiState: PetFormUiState,
    viewModel: PetFormViewModel
) {
    var expanded by remember { mutableStateOf(false) }
    var showDatePickerModal by remember { mutableStateOf(false) }
    val timeUnits = listOf("Semanas", "Meses", "Años")

    if (showDatePickerModal) {
        DatePickerModal(
            onDateSelected = { selectedMillis ->
                if (selectedMillis != null) {
                    val isoDate = convertMillisToIsoDateString(selectedMillis)
                    viewModel.onExactBirthDateChanged(isoDate)
                }
                showDatePickerModal = false
            },
            onDismiss = { showDatePickerModal = false }
        )
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Edad de la mascota",
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = PawMedicColors.Gray800,
            modifier = Modifier.padding(bottom = 6.dp)
        )
        Row(modifier = Modifier.fillMaxWidth()) {
            AgeModeChip(
                text = "Edad aproximada",
                isSelected = uiState.ageMode == AgeInputMode.APPROXIMATE,
                onClick = { viewModel.onAgeModeChanged(AgeInputMode.APPROXIMATE) },
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(10.dp))
            AgeModeChip(
                text = "Fecha nacimiento",
                isSelected = uiState.ageMode == AgeInputMode.EXACT_DATE,
                onClick = { viewModel.onAgeModeChanged(AgeInputMode.EXACT_DATE) },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (uiState.ageMode == AgeInputMode.APPROXIMATE) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 1. Campo de texto numérico
                Box(modifier = Modifier.weight(1f)) {
                    PawMedicTextField(
                        value = uiState.ageNumber,
                        onValueChange = viewModel::onAgeNumberChanged,
                        label = "Número",
                        placeholder = "Ej. 3",
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Next
                        )
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // 2. Dropdown Menu para la unidad de tiempo
                Box(
                    modifier = Modifier
                        .padding(top = 22.dp)
                        .height(56.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, PawMedicColors.Teal200, RoundedCornerShape(12.dp))
                        .clickable { expanded = true }
                        .padding(horizontal = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = uiState.ageUnit,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = PawMedicColors.Teal600
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "▼",
                            fontSize = 11.sp,
                            color = PawMedicColors.Teal600
                        )
                    }

                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        timeUnits.forEach { unit ->
                            DropdownMenuItem(
                                text = { Text(text = unit, fontSize = 14.sp) },
                                onClick = {
                                    viewModel.onAgeUnitChanged(unit)
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        } else {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Fecha de nacimiento",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = PawMedicColors.Gray800,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showDatePickerModal = true }
                ) {
                    OutlinedTextField(
                        value = if (uiState.exactBirthDate.isNotBlank()) formatIsoToDisplayDate(uiState.exactBirthDate) else "",
                        onValueChange = {},
                        readOnly = true,
                        placeholder = {
                            Text(
                                text = "Selecciona en el calendario (dd-MM-yy)",
                                color = PawMedicColors.Gray400,
                                fontSize = 14.sp
                            )
                        },
                        trailingIcon = {
                            Text(
                                text = "📅",
                                fontSize = 18.sp,
                                modifier = Modifier.clickable { showDatePickerModal = true }
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PawMedicColors.Teal600,
                            unfocusedBorderColor = PawMedicColors.Gray300,
                            focusedContainerColor = PawMedicColors.White,
                            unfocusedContainerColor = PawMedicColors.White,
                            focusedTextColor = PawMedicColors.Gray900,
                            unfocusedTextColor = PawMedicColors.Gray900
                        )
                    )
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clickable { showDatePickerModal = true }
                    )
                }

                if (uiState.exactBirthDate.isNotBlank() && uiState.ageNumber.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(PawMedicColors.Teal50)
                            .border(1.dp, PawMedicColors.Teal200, RoundedCornerShape(8.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "Edad calculada: ${uiState.ageNumber} ${uiState.ageUnit}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = PawMedicColors.Teal600
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FormDropdownField(
    label: String,
    selectedValue: String,
    placeholder: String,
    options: List<String>,
    onOptionSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    var expanded by remember { mutableStateOf(false) }
    LaunchedEffect(enabled, options) {
        if (!enabled) {
            expanded = false
        }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = PawMedicColors.Gray800,
            modifier = Modifier.padding(bottom = 6.dp)
        )
        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = selectedValue.ifBlank { placeholder },
                onValueChange = {},
                readOnly = true,
                enabled = enabled,
                trailingIcon = {
                    Text(
                        text = "▼",
                        fontSize = 12.sp,
                        color = if (enabled) PawMedicColors.Gray600 else PawMedicColors.Gray400
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PawMedicColors.Teal600,
                    unfocusedBorderColor = PawMedicColors.Gray300,
                    focusedContainerColor = PawMedicColors.White,
                    unfocusedContainerColor = PawMedicColors.White,
                    disabledBorderColor = PawMedicColors.Gray200,
                    disabledContainerColor = Color(0xFFF9FAFB),
                    disabledTextColor = PawMedicColors.Gray400,
                    focusedTextColor = if (selectedValue.isBlank()) PawMedicColors.Gray400 else PawMedicColors.Gray900,
                    unfocusedTextColor = if (selectedValue.isBlank()) PawMedicColors.Gray400 else PawMedicColors.Gray900
                )
            )
            if (enabled) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clickable { expanded = !expanded }
                )
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.fillMaxWidth(0.85f)
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(text = option, fontSize = 14.sp) },
                        onClick = {
                            onOptionSelected(option)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun AgeModeChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(42.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(if (isSelected) PawMedicColors.Teal50 else PawMedicColors.White)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) PawMedicColors.Teal600 else PawMedicColors.Gray300,
                shape = RoundedCornerShape(10.dp)
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 13.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) PawMedicColors.Teal600 else PawMedicColors.Gray700
        )
    }
}

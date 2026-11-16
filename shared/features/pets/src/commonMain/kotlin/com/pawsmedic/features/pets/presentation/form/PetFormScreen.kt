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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.pawsmedic.features.pets.presentation.localizePetMessage
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
import com.pawsmedic.shared.core.designsystem.theme.PawMedicSemanticColors
import com.pawsmedic.shared.core.designsystem.language.AppLanguage
import com.pawsmedic.shared.core.designsystem.language.LocalLanguagePreference
import com.pawsmedic.shared.core.designsystem.language.petCatalogLabel
import io.github.jan.supabase.SupabaseClient

@Composable
fun PetFormScreen(
    viewModel: PetFormViewModel,
    onBackClick: () -> Unit,
    onSaveSuccess: () -> Unit,
    supabaseClient: SupabaseClient,
    onPickPhoto: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val language = LocalLanguagePreference.current?.selected ?: AppLanguage.SPANISH
    val uiState by viewModel.uiState.collectAsState()
    val defaultPhotoPicker = rememberPhotoPicker(
        supabaseClient = supabaseClient,
        onPhotoPicked = viewModel::onPhotoUrlChanged,
        onUploadError = viewModel::onPhotoUploadError
    )

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            onSaveSuccess()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(PawMedicSemanticColors.current.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        PawMedicTopBar(
            title = if (uiState.isEditMode) language.text("Editar Mascota", "Edit pet") else language.text("Registrar Mascota", "Register pet"),
            onBackClick = onBackClick
        )

        Spacer(modifier = Modifier.height(16.dp))

        PhotoPickerCard(
            photo_url = uiState.photo_url,
            species = uiState.species,
            isEditMode = uiState.isEditMode,
            onClick = { onPickPhoto?.invoke() ?: defaultPhotoPicker() }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // --- FIELD 1: NOMBRE (Sin números ni caracteres especiales, min 2 letras) ---
        PawMedicTextField(
            value = uiState.name,
            onValueChange = viewModel::onNameChanged,
            label = language.text("Nombre de la mascota (Mínimo 2 letras, sin números)", "Pet name (at least 2 letters, no numbers)"),
            placeholder = language.text("Ej. Max", "E.g. Max"),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
        )

        Spacer(modifier = Modifier.height(14.dp))

        FormDropdownField(
            label = language.text("Especie", "Species"),
            selectedValue = uiState.species,
            placeholder = language.text("Selecciona la especie", "Select species"),
            options = uiState.speciesOptions.map(SpeciesOption::name),
            enabled = uiState.speciesOptions.isNotEmpty(),
            onOptionSelected = { name ->
                uiState.speciesOptions.firstOrNull { it.name == name }
                    ?.let(viewModel::onSpeciesChanged)
            }
        )

        Spacer(modifier = Modifier.height(14.dp))

        // --- FIELD 3: RAZA (Dropdown dinámico según especie) ---
        FormDropdownField(
            label = language.text("Raza", "Breed"),
            selectedValue = uiState.breed,
            placeholder = if (uiState.speciesId == null && uiState.species.isBlank()) {
                language.text("Selecciona primero la especie", "Select a species first")
            } else {
                language.text("Selecciona la raza", "Select breed")
            },
            options = uiState.availableBreeds.map(BreedOption::name),
            enabled = (uiState.speciesId != null || uiState.species.isNotBlank()) && uiState.availableBreeds.isNotEmpty(),
            onOptionSelected = { name ->
                uiState.availableBreeds.firstOrNull { it.name == name }
                    ?.let(viewModel::onBreedChanged)
            }
        )

        Spacer(modifier = Modifier.height(14.dp))

        GenderSelector(
            selectedGender = uiState.gender,
            onGenderSelected = viewModel::onGenderChanged
        )

        Spacer(modifier = Modifier.height(14.dp))

        // --- FIELD 4: EDAD (Input numérico + Dropdown de unidades OR Modal DatePicker) ---
        AgeInputSection(uiState = uiState, viewModel = viewModel)

        Spacer(modifier = Modifier.height(14.dp))

        // --- FIELD 5: PESO EN KILOS ---
        PawMedicTextField(
            value = uiState.weightKg,
            onValueChange = viewModel::onWeightChanged,
            label = language.text("Peso aproximado (en kilos)", "Approximate weight (kg)"),
            placeholder = language.text("Ej. 4.5", "E.g. 4.5"),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Decimal,
                imeAction = ImeAction.Next
            )
        )

        Spacer(modifier = Modifier.height(14.dp))

        // --- FIELD 6: NOTAS RELEVANTES ---
        PawMedicTextField(
            value = uiState.allergies,
            onValueChange = viewModel::onAllergiesChanged,
            label = language.text("Notas relevantes (Alergias, cuidados)", "Relevant notes (allergies, care)"),
            placeholder = language.text("Alergias, cuidados especiales, temperamento...", "Allergies, special care, temperament..."),
            singleLine = false,
            modifier = Modifier.height(100.dp)
        )

        if (uiState.errorMessage != null) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = localizePetMessage(uiState.errorMessage!!, language),
                fontSize = 13.sp,
                color = PawMedicColors.Error,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        PawMedicPrimaryButton(
            text = if (uiState.isEditMode) language.text("Guardar cambios", "Save changes") else language.text("Guardar mascota", "Save pet"),
            onClick = { viewModel.savePet(onSaveSuccess) },
            isLoading = uiState.isLoading
        )

        Spacer(modifier = Modifier.height(12.dp))

        PawMedicSecondaryButton(
            text = language.text("Cancelar", "Cancel"),
            onClick = onBackClick
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun PhotoPickerCard(
    photo_url: String,
    species: String,
    isEditMode: Boolean,
    onClick: () -> Unit
) {
    val language = LocalLanguagePreference.current?.selected ?: AppLanguage.SPANISH
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(PawMedicSemanticColors.current.softAccent)
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
                    photo_url.isNotBlank() && isEditMode -> language.text("Cambiar foto de perfil", "Change profile photo")
                    photo_url.isNotBlank() -> language.text("Foto seleccionada (Click para cambiar)", "Photo selected (tap to change)")
                    else -> language.text("Subir foto de tu mascota", "Upload your pet's photo")
                },
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = PawMedicSemanticColors.current.accent,
                textAlign = TextAlign.Center
            )
            if (photo_url.isBlank() && !isEditMode) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = language.text("Formatos aceptados: JPG, PNG (Max 5MB)", "Accepted formats: JPG, PNG (max 5 MB)"),
                    fontSize = 12.sp,
                    color = PawMedicSemanticColors.current.hint,
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
    val language = LocalLanguagePreference.current?.selected ?: AppLanguage.SPANISH
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = language.text("Sexo de la mascota", "Pet's sex"),
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = PawMedicSemanticColors.current.text,
            modifier = Modifier.padding(bottom = 6.dp)
        )
        Row(modifier = Modifier.fillMaxWidth()) {
            GenderChip(
                text = language.text("Macho", "Male"),
                isSelected = selectedGender == "Macho",
                selectedColor = PawMedicSemanticColors.current.blueSurface,
                selectedBorderColor = Color(0xFF7DD3FC),
                onClick = { onGenderSelected("Macho") },
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(10.dp))
            GenderChip(
                text = language.text("Hembra", "Female"),
                isSelected = selectedGender == "Hembra",
                selectedColor = PawMedicSemanticColors.current.pinkSurface,
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
            .background(if (isSelected) selectedColor else PawMedicSemanticColors.current.surface)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) selectedBorderColor else PawMedicSemanticColors.current.border,
                shape = RoundedCornerShape(10.dp)
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 14.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = PawMedicSemanticColors.current.text
        )
    }
}

@Composable
private fun AgeInputSection(
    uiState: PetFormUiState,
    viewModel: PetFormViewModel
) {
    val language = LocalLanguagePreference.current?.selected ?: AppLanguage.SPANISH
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
            text = language.text("Edad de la mascota", "Pet's age"),
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = PawMedicSemanticColors.current.text,
            modifier = Modifier.padding(bottom = 6.dp)
        )
        Row(modifier = Modifier.fillMaxWidth()) {
            AgeModeChip(
                text = language.text("Edad aproximada", "Approximate age"),
                isSelected = uiState.ageMode == AgeInputMode.APPROXIMATE,
                onClick = { viewModel.onAgeModeChanged(AgeInputMode.APPROXIMATE) },
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(10.dp))
            AgeModeChip(
                text = language.text("Fecha nacimiento", "Date of birth"),
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
                        label = language.text("Número", "Number"),
                        placeholder = language.text("Ej. 3", "E.g. 3"),
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
                            text = ageUnitLabel(uiState.ageUnit, language),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = PawMedicSemanticColors.current.accent
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "▼",
                            fontSize = 11.sp,
                            color = PawMedicSemanticColors.current.accent
                        )
                    }

                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        timeUnits.forEach { unit ->
                            DropdownMenuItem(
                                text = { Text(text = ageUnitLabel(unit, language), fontSize = 14.sp) },
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
                    text = language.text("Fecha de nacimiento", "Date of birth"),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = PawMedicSemanticColors.current.text,
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
                                text = language.text("Selecciona en el calendario (dd-MM-yy)", "Select from the calendar (dd-MM-yy)"),
                                color = PawMedicSemanticColors.current.hint,
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
                            unfocusedBorderColor = PawMedicSemanticColors.current.border,
                            focusedContainerColor = PawMedicSemanticColors.current.surface,
                            unfocusedContainerColor = PawMedicSemanticColors.current.surface,
                            focusedTextColor = PawMedicSemanticColors.current.text,
                            unfocusedTextColor = PawMedicSemanticColors.current.text
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
                            .background(PawMedicSemanticColors.current.softAccent)
                            .border(1.dp, PawMedicColors.Teal200, RoundedCornerShape(8.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = language.text("Edad calculada: ${uiState.ageNumber} ${uiState.ageUnit}", "Calculated age: ${uiState.ageNumber} ${ageUnitLabel(uiState.ageUnit, language)}"),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = PawMedicSemanticColors.current.accent
                        )
                    }
                }
            }
        }
    }
}

private fun ageUnitLabel(unit: String, language: AppLanguage): String = language.text(unit, when (unit) {
    "Semanas" -> "Weeks"
    "Meses" -> "Months"
    "Años" -> "Years"
    else -> unit
})

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
    val language = LocalLanguagePreference.current?.selected ?: AppLanguage.SPANISH
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
            color = PawMedicSemanticColors.current.text,
            modifier = Modifier.padding(bottom = 6.dp)
        )
        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = if (selectedValue.isBlank()) placeholder else petCatalogLabel(selectedValue, language),
                onValueChange = {},
                readOnly = true,
                enabled = enabled,
                trailingIcon = {
                    Text(
                        text = "▼",
                        fontSize = 12.sp,
                        color = if (enabled) PawMedicSemanticColors.current.secondaryText else PawMedicSemanticColors.current.hint
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PawMedicColors.Teal600,
                    unfocusedBorderColor = PawMedicSemanticColors.current.border,
                    focusedContainerColor = PawMedicSemanticColors.current.surface,
                    unfocusedContainerColor = PawMedicSemanticColors.current.surface,
                    disabledBorderColor = PawMedicSemanticColors.current.separator,
                    disabledContainerColor = PawMedicSemanticColors.current.subtleSurface,
                    disabledTextColor = PawMedicSemanticColors.current.hint,
                    focusedTextColor = if (selectedValue.isBlank()) PawMedicSemanticColors.current.hint else PawMedicSemanticColors.current.text,
                    unfocusedTextColor = if (selectedValue.isBlank()) PawMedicSemanticColors.current.hint else PawMedicSemanticColors.current.text
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
                        text = { Text(text = petCatalogLabel(option, language), fontSize = 14.sp) },
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
            .background(if (isSelected) PawMedicSemanticColors.current.softAccent else PawMedicSemanticColors.current.surface)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) PawMedicColors.Teal600 else PawMedicSemanticColors.current.border,
                shape = RoundedCornerShape(10.dp)
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 13.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) PawMedicSemanticColors.current.accent else PawMedicSemanticColors.current.text
        )
    }
}

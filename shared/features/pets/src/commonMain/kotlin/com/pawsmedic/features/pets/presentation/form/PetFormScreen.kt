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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pawsmedic.features.pets.presentation.AgeInputMode
import com.pawsmedic.features.pets.presentation.PetFormViewModel
import com.pawsmedic.features.pets.presentation.components.PetAvatarImage
import com.pawsmedic.shared.core.common.rememberPhotoPicker
import com.pawsmedic.shared.core.designsystem.components.PawMedicPrimaryButton
import com.pawsmedic.shared.core.designsystem.components.PawMedicSecondaryButton
import com.pawsmedic.shared.core.designsystem.components.PawMedicTextField
import com.pawsmedic.shared.core.designsystem.components.PawMedicTopBar
import com.pawsmedic.shared.core.designsystem.theme.PawMedicColors

@Composable
fun PetFormScreen(
    viewModel: PetFormViewModel,
    onBackClick: () -> Unit,
    onSaveSuccess: () -> Unit,
    onPickPhoto: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    val defaultPhotoPicker = rememberPhotoPicker { photoUrl ->
        viewModel.onPhotoUrlChanged(photoUrl)
    }

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            onSaveSuccess()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(PawMedicColors.BackgroundScreen)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        PawMedicTopBar(
            title = if (uiState.isEditMode) "Editar Mascota" else "Registrar Mascota",
            onBackClick = onBackClick
        )

        Spacer(modifier = Modifier.height(16.dp))

        // --- PHOTO UPLOAD / EDIT AREA ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(PawMedicColors.Teal50)
                .border(
                    width = 1.dp,
                    color = PawMedicColors.Teal200,
                    shape = RoundedCornerShape(20.dp)
                )
                .clickable { onPickPhoto?.invoke() ?: defaultPhotoPicker() }
                .padding(vertical = 20.dp, horizontal = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                if (uiState.photoUrl.isNotBlank()) {
                    PetAvatarImage(
                        photoUrl = uiState.photoUrl,
                        species = uiState.species,
                        size = 80.dp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = if (uiState.isEditMode) "Cambiar foto de perfil" else "Foto seleccionada (Click para cambiar)",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = PawMedicColors.Teal600,
                        textAlign = TextAlign.Center
                    )
                } else {
                    PetAvatarImage(
                        photoUrl = null,
                        species = uiState.species,
                        size = 64.dp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = if (uiState.isEditMode) "Cambiar foto de perfil" else "Subir foto de tu mascota",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = PawMedicColors.Teal600
                    )

                    if (!uiState.isEditMode) {
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

        Spacer(modifier = Modifier.height(20.dp))

        // --- FIELD 1: NOMBRE (Sin números ni caracteres especiales, min 2 letras) ---
        PawMedicTextField(
            value = uiState.name,
            onValueChange = viewModel::onNameChanged,
            label = "Nombre de la mascota (Mínimo 2 letras, sin números)",
            placeholder = "Ej. Max",
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
        )

        Spacer(modifier = Modifier.height(14.dp))

        // --- FIELD 2: ESPECIE (Dropdown: Canino, Felino, Avíparo) ---
        FormDropdownField(
            label = "Especie",
            selectedValue = uiState.species,
            options = uiState.availableSpecies,
            onOptionSelected = viewModel::onSpeciesChanged
        )

        Spacer(modifier = Modifier.height(14.dp))

        // --- FIELD 3: RAZA (Dropdown dinámico según especie) ---
        FormDropdownField(
            label = "Raza",
            selectedValue = uiState.breed,
            options = uiState.availableBreeds,
            onOptionSelected = viewModel::onBreedChanged
        )

        Spacer(modifier = Modifier.height(14.dp))

        // --- FIELD 4: EDAD (Fecha de nacimiento vs. Edad aproximada) ---
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

            Spacer(modifier = Modifier.height(8.dp))

            if (uiState.ageMode == AgeInputMode.APPROXIMATE) {
                PawMedicTextField(
                    value = uiState.approximateAge,
                    onValueChange = viewModel::onApproximateAgeChanged,
                    label = "",
                    placeholder = "Ej. 3 años, 5 meses o 2 semanas",
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                )
            } else {
                PawMedicTextField(
                    value = uiState.exactBirthDate,
                    onValueChange = viewModel::onExactBirthDateChanged,
                    label = "",
                    placeholder = "Formato YYYY-MM-DD (Ej. 2024-05-10)",
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Next
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // --- FIELD 5: PESO EN KILOS ---
        PawMedicTextField(
            value = uiState.weightKg,
            onValueChange = viewModel::onWeightChanged,
            label = "Peso aproximado (en kilos)",
            placeholder = "Ej. 4.5",
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Decimal,
                imeAction = ImeAction.Next
            )
        )

        Spacer(modifier = Modifier.height(14.dp))

        // --- FIELD 6: SEXO / GENDER SELECTOR ---
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Sexo",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = PawMedicColors.Gray800,
                modifier = Modifier.padding(bottom = 6.dp)
            )
            Row(modifier = Modifier.fillMaxWidth()) {
                GenderChip(
                    text = "Macho",
                    isSelected = uiState.gender == "Macho",
                    onClick = { viewModel.onGenderChanged("Macho") },
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(12.dp))
                GenderChip(
                    text = "Hembra",
                    isSelected = uiState.gender == "Hembra",
                    onClick = { viewModel.onGenderChanged("Hembra") },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // --- FIELD 7: NOTAS RELEVANTES ---
        PawMedicTextField(
            value = uiState.allergies,
            onValueChange = viewModel::onAllergiesChanged,
            label = "Notas relevantes (Alergias, cuidados)",
            placeholder = "Alergias, cuidados especiales, temperamento...",
            singleLine = false,
            modifier = Modifier.height(100.dp)
        )

        if (uiState.errorMessage != null) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = uiState.errorMessage!!,
                fontSize = 13.sp,
                color = PawMedicColors.Error,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        PawMedicPrimaryButton(
            text = if (uiState.isEditMode) "Guardar cambios" else "Guardar mascota",
            onClick = { viewModel.savePet(onSaveSuccess) },
            isLoading = uiState.isLoading
        )

        Spacer(modifier = Modifier.height(12.dp))

        PawMedicSecondaryButton(
            text = "Cancelar",
            onClick = onBackClick
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun FormDropdownField(
    label: String,
    selectedValue: String,
    options: List<String>,
    onOptionSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

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
                value = selectedValue,
                onValueChange = {},
                readOnly = true,
                trailingIcon = {
                    Text(text = "▼", fontSize = 12.sp, color = PawMedicColors.Gray500)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = true },
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
                    .clickable { expanded = true }
            )
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

@Composable
private fun GenderChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(48.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) PawMedicColors.Teal50 else PawMedicColors.White)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) PawMedicColors.Teal600 else PawMedicColors.Gray300,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 14.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) PawMedicColors.Teal600 else PawMedicColors.Gray700
        )
    }
}

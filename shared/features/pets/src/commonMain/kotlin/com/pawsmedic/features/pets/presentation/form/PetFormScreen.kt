package com.pawsmedic.features.pets.presentation.form

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pawsmedic.features.pets.presentation.PetFormViewModel
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
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

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
                .clickable { }
                .padding(vertical = 24.dp, horizontal = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(PawMedicColors.White),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "📷", fontSize = 28.sp)
                }

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

        Spacer(modifier = Modifier.height(20.dp))

        // --- FORM FIELDS ---
        PawMedicTextField(
            value = uiState.name,
            onValueChange = viewModel::onNameChanged,
            label = "Nombre de la mascota",
            placeholder = "Ej. Max",
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
        )

        Spacer(modifier = Modifier.height(14.dp))

        PawMedicTextField(
            value = uiState.species,
            onValueChange = viewModel::onSpeciesChanged,
            label = "Especie",
            placeholder = "Ej. Perro, Gato, Ave",
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
        )

        Spacer(modifier = Modifier.height(14.dp))

        PawMedicTextField(
            value = uiState.breed,
            onValueChange = viewModel::onBreedChanged,
            label = "Raza",
            placeholder = "Ej. Golden Retriever",
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
        )

        Spacer(modifier = Modifier.height(14.dp))

        PawMedicTextField(
            value = uiState.age,
            onValueChange = viewModel::onAgeChanged,
            label = "Edad",
            placeholder = "Ej. 3 años",
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
        )

        Spacer(modifier = Modifier.height(14.dp))

        // --- SEXO / GENDER SELECTOR ---
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

package com.pawsmedic.features.pets.presentation.detail

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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pawsmedic.features.pets.presentation.PetDetailViewModel
import com.pawsmedic.features.pets.presentation.components.PetAvatarImage
import com.pawsmedic.shared.core.designsystem.components.PawMedicPrimaryButton
import com.pawsmedic.shared.core.designsystem.components.PawMedicTopBar
import com.pawsmedic.shared.core.designsystem.theme.PawMedicColors
import com.pawsmedic.shared.core.designsystem.theme.PawMedicSemanticColors
import com.pawsmedic.shared.core.designsystem.language.AppLanguage
import com.pawsmedic.shared.core.designsystem.language.LocalLanguagePreference
import com.pawsmedic.shared.core.designsystem.language.petCatalogLabel

@Composable
fun PetDetailScreen(
    viewModel: PetDetailViewModel,
    onBackClick: () -> Unit,
    onNavigateToEdit: (String) -> Unit,
    onNavigateToMedicalRecord: (String) -> Unit = {},
    onDeleteSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    val language = LocalLanguagePreference.current?.selected ?: AppLanguage.SPANISH
    val uiState by viewModel.uiState.collectAsState()
    val pet = uiState.pet
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(text = language.text("Eliminar mascota", "Delete pet"), fontWeight = FontWeight.Bold) },
            text = { Text(language.text("¿Estás seguro de que deseas eliminar a ${pet?.name ?: "esta mascota"}? Esta acción no se puede deshacer.", "Are you sure you want to delete ${pet?.name ?: "this pet"}? This action cannot be undone.")) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        viewModel.deletePet(onDeleteSuccess)
                    }
                ) {
                    Text(text = language.text("Eliminar", "Delete"), color = PawMedicColors.Error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(text = language.text("Cancelar", "Cancel"), color = PawMedicSemanticColors.current.text)
                }
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(PawMedicSemanticColors.current.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        PawMedicTopBar(
            title = language.text("Detalle de Mascota", "Pet details"),
            onBackClick = onBackClick
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (pet != null) {
            // --- PET PROFILE HEADER ---
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .clip(CircleShape)
                        .border(3.dp, PawMedicColors.Teal600, CircleShape)
                ) {
                    key(pet.id, pet.photo_url) {
                        PetAvatarImage(
                            photo_url = pet.photo_url,
                            species = pet.species,
                            size = 110.dp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = pet.name,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = PawMedicSemanticColors.current.text
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = pet.breed?.let { petCatalogLabel(it, language) } ?: language.text("Raza no registrada", "Breed not recorded"),
                    fontSize = 13.sp,
                    color = PawMedicSemanticColors.current.hint
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- 4 STAT CARDS GRID ---
            Row(modifier = Modifier.fillMaxWidth()) {
                StatCard(
                    title = language.text("Especie", "Species"),
                    value = petCatalogLabel(pet.species, language),
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(12.dp))
                StatCard(
                    title = language.text("Raza", "Breed"),
                    value = pet.breed?.let { petCatalogLabel(it, language) } ?: language.text("Mestizo", "Mixed breed"),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                StatCard(
                    title = language.text("Edad", "Age"),
                    value = pet.dateOfBirth?.let { language.text("Nac. $it", "Born $it") } ?: language.text("No registrada", "Not recorded"),
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(12.dp))
                StatCard(
                    title = language.text("Notas de Cuidado", "Care notes"),
                    value = pet.allergies ?: language.text("Sin notas registradas", "No notes recorded"),
                    isHighlight = true,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // --- HISTORIAL CLINICO SECTION ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = language.text("Historial Clínico", "Medical history"),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = PawMedicSemanticColors.current.text
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = language.text("Aún no hay registros clínicos vinculados a esta mascota.", "There are no medical records linked to this pet yet."),
                fontSize = 14.sp,
                color = PawMedicSemanticColors.current.hint,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { onNavigateToMedicalRecord(pet.id) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PawMedicColors.Teal600)
            ) {
                Text(
                    text = language.text("Abrir expediente médico", "Open medical record"),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OutlinedButton(
                    onClick = { onNavigateToEdit(pet.id) },
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = androidx.compose.ui.graphics.SolidColor(PawMedicSemanticColors.current.border)
                    )
                ) {
                    Text(
                        text = language.text("Editar información", "Edit information"),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = PawMedicSemanticColors.current.text
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Button(
                    onClick = { showDeleteDialog = true },
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFEE2E2),
                        contentColor = Color(0xFFDC2626)
                    )
                ) {
                    Text(
                        text = language.text("Eliminar mascota", "Delete pet"),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    isHighlight: Boolean = false,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(if (isHighlight) PawMedicSemanticColors.current.warningSurface else PawMedicSemanticColors.current.surface)
            .border(
                width = 1.dp,
                color = if (isHighlight) Color(0xFFFDE047) else PawMedicSemanticColors.current.separator,
                shape = RoundedCornerShape(16.dp)
            )
            .padding(14.dp)
    ) {
        Column {
            Text(
                text = title,
                fontSize = 12.sp,
                color = PawMedicSemanticColors.current.hint
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = PawMedicSemanticColors.current.text
            )
        }
    }
}

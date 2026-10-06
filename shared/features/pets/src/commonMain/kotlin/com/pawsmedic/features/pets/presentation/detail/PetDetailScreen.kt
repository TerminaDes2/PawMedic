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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pawsmedic.features.pets.presentation.PetDetailViewModel
import com.pawsmedic.features.pets.presentation.components.PetAvatarImage
import com.pawsmedic.shared.core.designsystem.components.PawMedicPrimaryButton
import com.pawsmedic.shared.core.designsystem.components.PawMedicTopBar
import com.pawsmedic.shared.core.designsystem.theme.PawMedicColors

@Composable
fun PetDetailScreen(
    viewModel: PetDetailViewModel,
    onBackClick: () -> Unit,
    onNavigateToEdit: (String) -> Unit,
    onDeleteSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val pet = uiState.pet
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(text = "Eliminar mascota", fontWeight = FontWeight.Bold) },
            text = { Text("¿Estás seguro de que deseas eliminar a ${pet?.name ?: "esta mascota"}? Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        viewModel.deletePet(onDeleteSuccess)
                    }
                ) {
                    Text(text = "Eliminar", color = PawMedicColors.Error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(text = "Cancelar", color = PawMedicColors.Gray700)
                }
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(PawMedicColors.BackgroundScreen)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        PawMedicTopBar(
            title = "Detalle de Mascota",
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
                    PetAvatarImage(
                        photoUrl = pet.photoUrl,
                        species = pet.species,
                        size = 110.dp
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = pet.name,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = PawMedicColors.Gray900
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "ID Médico: ${pet.medicalId ?: "PM-8942-A"}",
                    fontSize = 13.sp,
                    color = PawMedicColors.Gray500
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- 4 STAT CARDS GRID ---
            Row(modifier = Modifier.fillMaxWidth()) {
                StatCard(
                    title = "Especie",
                    value = pet.species,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(12.dp))
                StatCard(
                    title = "Raza",
                    value = pet.breed ?: "Mestizo",
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                StatCard(
                    title = "Edad",
                    value = pet.age ?: "2 años",
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(12.dp))
                StatCard(
                    title = "Notas de Cuidado",
                    value = pet.allergies ?: "Sin alergias registradas",
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
                    text = "Historial Clínico",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = PawMedicColors.Gray900
                )
                Text(
                    text = "Ver completo",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = PawMedicColors.Teal600,
                    modifier = Modifier.clickable { }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            MedicalHistoryItem(
                title = "Vacunación Triple Felina",
                dateAndDoctor = "15 Oct 2023 • Dr. Carlos Ruiz"
            )

            Spacer(modifier = Modifier.height(10.dp))

            MedicalHistoryItem(
                title = "Control de Parásitos",
                dateAndDoctor = "02 Sep 2023 • Dra. Ana Milena"
            )

            Spacer(modifier = Modifier.height(32.dp))

            // --- ACTIONS ---
            PawMedicPrimaryButton(
                text = "Agendar cita",
                onClick = { }
            )

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
                        brush = androidx.compose.ui.graphics.SolidColor(PawMedicColors.Gray300)
                    )
                ) {
                    Text(
                        text = "Editar información",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = PawMedicColors.Gray800
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
                        text = "Eliminar mascota",
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
            .background(if (isHighlight) Color(0xFFFEF9C3) else PawMedicColors.White)
            .border(
                width = 1.dp,
                color = if (isHighlight) Color(0xFFFDE047) else PawMedicColors.Gray200,
                shape = RoundedCornerShape(16.dp)
            )
            .padding(14.dp)
    ) {
        Column {
            Text(
                text = title,
                fontSize = 12.sp,
                color = PawMedicColors.Gray500
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = PawMedicColors.Gray900
            )
        }
    }
}

@Composable
private fun MedicalHistoryItem(
    title: String,
    dateAndDoctor: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(PawMedicColors.White)
            .border(1.dp, PawMedicColors.Gray200, RoundedCornerShape(14.dp))
            .padding(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(PawMedicColors.Teal50),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "📄", fontSize = 18.sp)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = PawMedicColors.Gray900
                )
                Text(
                    text = dateAndDoctor,
                    fontSize = 12.sp,
                    color = PawMedicColors.Gray500
                )
            }
        }
    }
}

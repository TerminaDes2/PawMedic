package com.pawsmedic.features.medical_records.presentation

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
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
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
import com.pawsmedic.features.medical_records.domain.model.PrescriptionRecord
import com.pawsmedic.shared.core.common.formatIsoToDisplayDate
import com.pawsmedic.shared.core.designsystem.components.PawMedicTopBar
import com.pawsmedic.shared.core.designsystem.theme.PawMedicColors

@Composable
fun PrescriptionsListScreen(
    viewModel: PrescriptionsViewModel,
    onBackClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    var dropdownExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(PawMedicColors.BackgroundScreen)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        PawMedicTopBar(
            title = "Recetas Médicas",
            onBackClick = onBackClick
        )

        Spacer(modifier = Modifier.height(16.dp))

        // --- PET SELECTOR DROPDOWN CARD ---
        Box(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(PawMedicColors.White)
                    .border(1.dp, PawMedicColors.Gray200, RoundedCornerShape(16.dp))
                    .clickable { dropdownExpanded = true }
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(PawMedicColors.Teal50),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "💊", fontSize = 20.sp)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Mostrando recetas de: ${uiState.selectedPetName}",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = PawMedicColors.Gray900
                        )
                    }
                    Text(text = "▼", fontSize = 14.sp, color = PawMedicColors.Gray500)
                }
            }

            DropdownMenu(
                expanded = dropdownExpanded,
                onDismissRequest = { dropdownExpanded = false },
                modifier = Modifier.fillMaxWidth(0.85f)
            ) {
                uiState.availablePetNames.forEach { name ->
                    DropdownMenuItem(
                        text = { Text(text = name, fontWeight = FontWeight.Medium) },
                        onClick = {
                            viewModel.onPetSelected(name)
                            dropdownExpanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- SEARCH BAR ---
        OutlinedTextField(
            value = uiState.searchQuery,
            onValueChange = viewModel::onSearchQueryChanged,
            placeholder = {
                Text(
                    text = "Buscar medicamentos, doctores, mascotas...",
                    color = PawMedicColors.Gray400,
                    fontSize = 14.sp
                )
            },
            leadingIcon = {
                Text(text = "🔍", fontSize = 16.sp, color = PawMedicColors.Gray400)
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PawMedicColors.Teal600,
                unfocusedBorderColor = PawMedicColors.Gray300,
                focusedContainerColor = PawMedicColors.White,
                unfocusedContainerColor = PawMedicColors.White
            )
        )

        Spacer(modifier = Modifier.height(20.dp))

        // --- ACTIVE PRESCRIPTIONS LIST ---
        if (uiState.filteredActivePrescriptions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "💊", fontSize = 40.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "No tienes recetas activas en este momento.",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = PawMedicColors.Gray800
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Las recetas con fecha de caducidad vencida solo están disponibles en el expediente completo de cada mascota.",
                        fontSize = 12.sp,
                        color = PawMedicColors.Gray500,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }
        } else {
            uiState.filteredActivePrescriptions.forEach { prescription ->
                ActivePrescriptionCard(prescription = prescription)
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun ActivePrescriptionCard(prescription: PrescriptionRecord) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(PawMedicColors.White)
            .border(1.dp, PawMedicColors.Gray200, RoundedCornerShape(20.dp))
            .padding(18.dp)
    ) {
        Column {
            // Card Header: Status Badge & Expiration Date
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFE6F4EA))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "🟢 RECETA ACTIVA",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = PawMedicColors.Teal600
                    )
                }

                Text(
                    text = "Vence: ${prescription.endDate?.let { formatIsoToDisplayDate(it) } ?: "Sin fecha"}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFD97706)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Drug Name & Pet Tag
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = prescription.drugName,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = PawMedicColors.Gray900
                )

                if (prescription.petName.isNotBlank()) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(PawMedicColors.Teal50)
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "🐾 ${prescription.petName}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = PawMedicColors.Teal600
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Prescrita por: ${prescription.prescribingVetName.ifBlank { "Dr. Veterinario" }} • ${prescription.clinicName}",
                fontSize = 12.sp,
                color = PawMedicColors.Gray500
            )

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = PawMedicColors.Gray100)
            Spacer(modifier = Modifier.height(12.dp))

            // Dosis & Vía & Frecuencia
            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("DOSIS Y UNIDAD", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = PawMedicColors.Gray400)
                    Text("${prescription.dosage} ${prescription.unit}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = PawMedicColors.Gray800)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("VÍA", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = PawMedicColors.Gray400)
                    Text(prescription.route, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = PawMedicColors.Gray800)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("FRECUENCIA", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = PawMedicColors.Gray400)
                    Text(prescription.frequency, fontSize = 14.sp, color = PawMedicColors.Gray800)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("DURACIÓN", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = PawMedicColors.Gray400)
                    Text(prescription.duration, fontSize = 14.sp, color = PawMedicColors.Gray800)
                }
            }

            if (prescription.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(12.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFFEF9C3))
                        .border(1.dp, Color(0xFFFDE047), RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Column {
                        Text(
                            text = "📌 INSTRUCCIONES VETERINARIAS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF854D0E),
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = prescription.notes,
                            fontSize = 13.sp,
                            color = Color(0xFF713F12),
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }
    }
}

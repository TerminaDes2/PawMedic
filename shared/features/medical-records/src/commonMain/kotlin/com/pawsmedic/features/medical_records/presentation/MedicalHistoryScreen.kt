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
import com.pawsmedic.features.medical_records.domain.model.MedicalRecord
import com.pawsmedic.shared.core.designsystem.components.PawMedicTopBar
import com.pawsmedic.shared.core.designsystem.theme.PawMedicColors

@Composable
fun MedicalHistoryScreen(
    viewModel: MedicalHistoryViewModel,
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
            title = "Historial Clínico",
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
                            Text(text = "🐱", fontSize = 20.sp)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Mostrando historial de: ${uiState.selectedPetName}",
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
                listOf("Tobías", "Joey", "Rudy", "Todas").forEach { name ->
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
                    text = "Buscar vacunas, diagnósticos, doctores...",
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

        // --- MEDICAL RECORDS CARDS LIST ---
        if (uiState.filteredRecords.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No se encontraron registros médicos.",
                    fontSize = 14.sp,
                    color = PawMedicColors.Gray500
                )
            }
        } else {
            uiState.filteredRecords.forEach { record ->
                MedicalRecordCard(record = record)
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun MedicalRecordCard(record: MedicalRecord) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(PawMedicColors.White)
            .border(1.dp, PawMedicColors.Gray200, RoundedCornerShape(20.dp))
            .padding(18.dp)
    ) {
        Column {
            // Card Header: Date & Clinic Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = record.date,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = PawMedicColors.Teal600
                )

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFE6F4EA))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = record.clinicName,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = PawMedicColors.Teal600
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = record.title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = PawMedicColors.Gray900
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = "Atendido por: ${record.doctorName}",
                fontSize = 13.sp,
                color = PawMedicColors.Gray500
            )

            Spacer(modifier = Modifier.height(14.dp))

            // DIAGNOSTICO
            Text(
                text = "DIAGNÓSTICO",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = PawMedicColors.Gray400,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = record.diagnosis,
                fontSize = 14.sp,
                color = PawMedicColors.Gray700,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // TRATAMIENTO
            Text(
                text = "TRATAMIENTO",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = PawMedicColors.Gray400,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = record.treatment,
                fontSize = 14.sp,
                color = PawMedicColors.Gray700,
                lineHeight = 20.sp
            )

            // NOTAS DEL VETERINARIO (HIGHLIGHT BOX)
            if (!record.vetNotes.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(14.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFECFDF5))
                        .padding(12.dp)
                ) {
                    Column {
                        Text(
                            text = "NOTAS DEL VETERINARIO",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = PawMedicColors.Teal600,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = record.vetNotes,
                            fontSize = 13.sp,
                            color = PawMedicColors.Gray800,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }
    }
}

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
import com.pawsmedic.shared.core.designsystem.theme.PawMedicSemanticColors
import com.pawsmedic.shared.core.designsystem.language.AppLanguage
import com.pawsmedic.shared.core.designsystem.language.LocalLanguagePreference

@Composable
fun MedicalHistoryScreen(
    viewModel: MedicalHistoryViewModel,
    onBackClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val language = LocalLanguagePreference.current?.selected ?: AppLanguage.SPANISH
    val uiState by viewModel.uiState.collectAsState()
    var dropdownExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(PawMedicSemanticColors.current.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        PawMedicTopBar(
            title = language.text("Recetas Médicas", "Medical Prescriptions"),
            onBackClick = onBackClick
        )

        Spacer(modifier = Modifier.height(16.dp))

        // --- PET SELECTOR DROPDOWN CARD ---
        Box(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(PawMedicSemanticColors.current.surface)
                    .border(1.dp, PawMedicSemanticColors.current.separator, RoundedCornerShape(16.dp))
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
                                .background(PawMedicSemanticColors.current.softAccent),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "🐾", fontSize = 20.sp)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = language.text("Mostrando recetas de: ${uiState.selectedPetName}", "Showing prescriptions for: ${if (uiState.selectedPetName == "Todas") "All" else uiState.selectedPetName}"),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = PawMedicSemanticColors.current.text
                        )
                    }
                    Text(text = "▼", fontSize = 14.sp, color = PawMedicSemanticColors.current.hint)
                }
            }

            DropdownMenu(
                expanded = dropdownExpanded,
                onDismissRequest = { dropdownExpanded = false },
                modifier = Modifier.fillMaxWidth(0.85f)
            ) {
                uiState.availablePetNames.forEach { name ->
                    DropdownMenuItem(
                        text = { Text(text = if (name == "Todas") language.text(name, "All") else name, fontWeight = FontWeight.Medium) },
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
                    text = language.text("Buscar vacunas, diagnósticos, doctores...", "Search vaccines, diagnoses, doctors..."),
                    color = PawMedicSemanticColors.current.hint,
                    fontSize = 14.sp
                )
            },
            leadingIcon = {
                Text(text = "🔍", fontSize = 16.sp, color = PawMedicSemanticColors.current.hint)
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PawMedicSemanticColors.current.accent,
                unfocusedBorderColor = PawMedicSemanticColors.current.border,
                focusedContainerColor = PawMedicSemanticColors.current.surface,
                unfocusedContainerColor = PawMedicSemanticColors.current.surface
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
                    text = language.text(
                        "No se encontraron registros médicos para tus mascotas.",
                        "No medical records found for your pets."
                    ),
                    fontSize = 14.sp,
                    color = PawMedicSemanticColors.current.hint
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
    val language = LocalLanguagePreference.current?.selected ?: AppLanguage.SPANISH
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(PawMedicSemanticColors.current.surface)
            .border(1.dp, PawMedicSemanticColors.current.separator, RoundedCornerShape(20.dp))
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
                    color = PawMedicSemanticColors.current.accent
                )

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(PawMedicSemanticColors.current.softAccent)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = record.clinicName,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = PawMedicSemanticColors.current.accent
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "${record.petName}: ${record.title}",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = PawMedicSemanticColors.current.text
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = language.text("Atendido por: ${record.doctorName}", "Attended by: ${record.doctorName}"),
                fontSize = 13.sp,
                color = PawMedicSemanticColors.current.hint
            )

            Spacer(modifier = Modifier.height(14.dp))

            // DIAGNOSTICO
            Text(
                text = language.text("DIAGNÓSTICO", "DIAGNOSIS"),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = PawMedicSemanticColors.current.hint,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = record.diagnosis,
                fontSize = 14.sp,
                color = PawMedicSemanticColors.current.text,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // TRATAMIENTO
            Text(
                text = language.text("TRATAMIENTO", "TREATMENT"),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = PawMedicSemanticColors.current.hint,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = record.treatment,
                fontSize = 14.sp,
                color = PawMedicSemanticColors.current.text,
                lineHeight = 20.sp
            )

            // NOTAS DEL VETERINARIO (HIGHLIGHT BOX)
            if (!record.vetNotes.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(14.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(PawMedicSemanticColors.current.softAccent)
                        .padding(12.dp)
                ) {
                    Column {
                        Text(
                            text = language.text("NOTAS DEL VETERINARIO", "VETERINARIAN'S NOTES"),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = PawMedicSemanticColors.current.accent,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = record.vetNotes,
                            fontSize = 13.sp,
                            color = PawMedicSemanticColors.current.text,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }
    }
}

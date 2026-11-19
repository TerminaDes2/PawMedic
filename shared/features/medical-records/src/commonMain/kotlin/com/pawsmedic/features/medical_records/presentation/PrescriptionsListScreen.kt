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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pawsmedic.features.medical_records.domain.model.PrescriptionRecord
import com.pawsmedic.shared.core.common.formatIsoToDisplayDate
import com.pawsmedic.shared.core.designsystem.components.PawMedicTopBar
import com.pawsmedic.shared.core.designsystem.language.AppLanguage
import com.pawsmedic.shared.core.designsystem.language.LocalLanguagePreference
import com.pawsmedic.shared.core.designsystem.theme.PawMedicSemanticColors

@Composable
fun PrescriptionsListScreen(
    viewModel: PrescriptionsViewModel,
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
            title = language.text("Recetas Médicas", "Prescriptions"),
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
                            Text(text = "💊", fontSize = 20.sp)
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
                        text = { Text(text = if (name == "Todas") language.text("Todas", "All") else name, fontWeight = FontWeight.Medium) },
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
                    text = language.text("Buscar medicamentos, doctores, mascotas...", "Search medications, doctors, pets..."),
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
                        text = language.text("No tienes recetas activas en este momento.", "You have no active prescriptions right now."),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = PawMedicSemanticColors.current.text
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = language.text("Las recetas con fecha de caducidad vencida solo están disponibles en el expediente completo de cada mascota.", "Expired prescriptions are only available in each pet's full medical record."),
                        fontSize = 12.sp,
                        color = PawMedicSemanticColors.current.hint,
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
            // Card Header: Status Badge & Expiration Date
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(PawMedicSemanticColors.current.softAccent)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = language.text("🟢 RECETA ACTIVA", "🟢 ACTIVE PRESCRIPTION"),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = PawMedicSemanticColors.current.accent
                    )
                }

                Text(
                    text = language.text("Vence: ${prescription.endDate?.let { formatIsoToDisplayDate(it) } ?: "Sin fecha"}", "Expires: ${prescription.endDate?.let { formatIsoToDisplayDate(it) } ?: "No date"}"),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = PawMedicSemanticColors.current.accent
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
                    color = PawMedicSemanticColors.current.text
                )

                if (prescription.petName.isNotBlank()) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(PawMedicSemanticColors.current.mediumAccent)
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "🐾 ${prescription.petName}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = PawMedicSemanticColors.current.accent
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = language.text("Prescrita por: ${prescription.prescribingVetName.ifBlank { "Dr. Veterinario" }} • ${prescription.clinicName}", "Prescribed by: ${prescription.prescribingVetName.ifBlank { "Veterinarian" }} • ${prescription.clinicName}"),
                fontSize = 12.sp,
                color = PawMedicSemanticColors.current.hint
            )

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = PawMedicSemanticColors.current.separator)
            Spacer(modifier = Modifier.height(12.dp))

            // Dosis & Vía & Frecuencia
            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(language.text("DOSIS Y UNIDAD", "DOSAGE AND UNIT"), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = PawMedicSemanticColors.current.hint)
                    Text("${prescription.dosage} ${prescription.unit}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = PawMedicSemanticColors.current.text)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(language.text("VÍA", "ROUTE"), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = PawMedicSemanticColors.current.hint)
                    Text(prescription.route, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = PawMedicSemanticColors.current.text)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(language.text("FRECUENCIA", "FREQUENCY"), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = PawMedicSemanticColors.current.hint)
                    Text(prescription.frequency, fontSize = 14.sp, color = PawMedicSemanticColors.current.text)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(language.text("DURACIÓN", "DURATION"), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = PawMedicSemanticColors.current.hint)
                    Text(prescription.duration, fontSize = 14.sp, color = PawMedicSemanticColors.current.text)
                }
            }

            if (prescription.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(12.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(PawMedicSemanticColors.current.warningSurface)
                        .border(1.dp, PawMedicSemanticColors.current.border, RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Column {
                        Text(
                            text = language.text("📌 INSTRUCCIONES VETERINARIAS", "📌 VETERINARY INSTRUCTIONS"),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = PawMedicSemanticColors.current.accent,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = prescription.notes,
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

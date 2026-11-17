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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pawsmedic.features.medical_records.domain.model.ClinicalConsultation
import com.pawsmedic.features.medical_records.domain.model.ClinicalStudyRecord
import com.pawsmedic.features.medical_records.domain.model.PetBackground
import com.pawsmedic.features.medical_records.domain.model.PetMedicalRecord
import com.pawsmedic.features.medical_records.domain.model.PrescriptionRecord
import com.pawsmedic.features.medical_records.presentation.dialog.EditBackgroundDialog
import com.pawsmedic.features.medical_records.presentation.dialog.NewConsultationDialog
import com.pawsmedic.shared.core.designsystem.components.PawMedicTopBar
import com.pawsmedic.shared.core.designsystem.theme.PawMedicColors
import com.pawsmedic.shared.core.designsystem.theme.PawMedicSemanticColors
import com.pawsmedic.shared.core.designsystem.language.AppLanguage
import com.pawsmedic.shared.core.designsystem.language.LocalLanguagePreference

@Composable
fun MedicalRecordDetailScreen(
    viewModel: MedicalRecordViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val language = LocalLanguagePreference.current?.selected ?: AppLanguage.SPANISH
    val uiState by viewModel.uiState.collectAsState()
    val record = uiState.record
    val isVet = uiState.isVetUser

    if (uiState.showNewConsultationDialog) {
        NewConsultationDialog(
            petId = uiState.petId,
            vetName = uiState.currentUserName,
            onDismiss = { viewModel.dismissNewConsultationDialog() },
            onConfirm = { consultation, prescriptions, studies ->
                viewModel.submitNewConsultation(consultation, prescriptions, studies)
            }
        )
    }

    if (uiState.showEditBackgroundDialog && record != null) {
        EditBackgroundDialog(
            initialBackground = record.background,
            onDismiss = { viewModel.dismissEditBackgroundDialog() },
            onConfirm = { updatedBg ->
                viewModel.updateBackground(updatedBg)
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(PawMedicSemanticColors.current.background)
    ) {
        PawMedicTopBar(
            title = language.text("Expediente Médico Digital", "Digital medical record"),
            onBackClick = onBackClick
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // ROLE BADGE BANNER
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isVet) PawMedicSemanticColors.current.softAccent else PawMedicSemanticColors.current.blueSurface)
                    .border(1.dp, if (isVet) PawMedicColors.Teal600 else Color(0xFF3B82F6), RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (isVet) "🩺" else "👁️",
                            fontSize = 20.sp
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (isVet) language.text("Modo Médico Veterinario (Lectura y Escritura)", "Veterinarian mode (read and write)") else language.text("Modo Propietario (Exclusivo Lectura)", "Owner mode (read only)"),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = PawMedicSemanticColors.current.text
                            )
                            Text(
                                text = if (isVet) language.text("Puede registrar consultas, recetas, diagnósticos y estudios", "You can record consultations, prescriptions, diagnoses and tests") else language.text("Puede consultar antecedentes, consultas, recetas y estudios de su mascota", "You can view your pet's history, consultations, prescriptions and tests"),
                                fontSize = 11.sp,
                                color = PawMedicSemanticColors.current.secondaryText
                            )
                        }
                    }

                    if (isVet) {
                        Button(
                            onClick = { viewModel.openNewConsultationDialog() },
                            colors = ButtonDefaults.buttonColors(containerColor = PawMedicColors.Teal600),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(36.dp)
                        ) {
                            Text(language.text("+ Consulta", "+ Consultation"), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (record != null) {
                // PET HEADER SUMMARY CARD
                PetSummaryCard(record = record)

                Spacer(modifier = Modifier.height(16.dp))

                // TABS ROW
                val tabs = listOf(
                    language.text("Resumen General", "Overview"),
                    language.text("Antecedentes", "Background"),
                    language.text("Consultas (SOAP)", "Consultations (SOAP)"),
                    language.text("Examen Físico", "Physical exam"),
                    language.text("Diagnósticos & Estudios", "Diagnoses & tests"),
                    language.text("Recetas & Fármacos", "Prescriptions & medication"),
                    language.text("Bitácora Auditoría", "Audit log")
                )

                ScrollableTabRow(
                    selectedTabIndex = uiState.selectedTabIndex,
                    edgePadding = 0.dp,
                    containerColor = Color.Transparent,
                    divider = {}
                ) {
                    tabs.forEachIndexed { idx, title ->
                        Tab(
                            selected = uiState.selectedTabIndex == idx,
                            onClick = { viewModel.onTabSelected(idx) },
                            text = {
                                Text(
                                    text = title,
                                    fontSize = 12.sp,
                                    fontWeight = if (uiState.selectedTabIndex == idx) FontWeight.Bold else FontWeight.Medium,
                                    color = if (uiState.selectedTabIndex == idx) PawMedicColors.Teal600 else PawMedicSemanticColors.current.secondaryText
                                )
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // TAB CONTENTS
                when (uiState.selectedTabIndex) {
                    0 -> GeneralSummaryTab(record = record)
                    1 -> BackgroundTab(record = record, isVet = isVet, onEditBackground = { viewModel.openEditBackgroundDialog() })
                    2 -> ConsultationsTab(record = record, isVet = isVet, onNewConsultation = { viewModel.openNewConsultationDialog() })
                    3 -> PhysicalExamTab(record = record)
                    4 -> DiagnosesAndStudiesTab(record = record)
                    5 -> PrescriptionsTab(record = record)
                    6 -> AuditTrailTab(record = record)
                }
            } else if (uiState.errorMessage != null) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    Text(
                        text = language.text("No se pudo cargar el expediente: ${uiState.errorMessage}", "Could not load the medical record: ${uiState.errorMessage}"),
                        color = PawMedicColors.Error
                    )
                    Button(onClick = viewModel::loadRecord) {
                        Text(language.text("Reintentar", "Retry"))
                    }
                }
            } else {
                Box(
                    modifier = Modifier.fillMaxWidth().height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = language.text("Cargando expediente médico...", "Loading medical record..."), color = PawMedicSemanticColors.current.hint)
                }
            }
        }
    }
}

@Composable
private fun PetSummaryCard(record: PetMedicalRecord) {
    val language = LocalLanguagePreference.current?.selected ?: AppLanguage.SPANISH
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(PawMedicSemanticColors.current.surface)
            .border(1.dp, PawMedicSemanticColors.current.separator, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(PawMedicSemanticColors.current.softAccent)
                            .border(2.dp, PawMedicColors.Teal600, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (record.petSpecies.contains("Gato", true)) "🐱" else "🐶",
                            fontSize = 30.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = record.petName.ifBlank { language.text("Mascota", "Pet") },
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = PawMedicSemanticColors.current.text
                        )
                        Text(
                            text = "${record.petSpecies} • ${record.petBreed} • ${record.petSex}",
                            fontSize = 13.sp,
                            color = PawMedicSemanticColors.current.secondaryText
                        )
                        Text(
                            text = language.text("Edad: ${record.petAgeCalculated} • Peso: ${(record.petWeightGrams ?: 4200) / 1000.0} kg", "Age: ${record.petAgeCalculated} • Weight: ${(record.petWeightGrams ?: 4200) / 1000.0} kg"),
                            fontSize = 12.sp,
                            color = PawMedicSemanticColors.current.hint
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(PawMedicSemanticColors.current.softAccent)
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = record.recordNumber,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = PawMedicSemanticColors.current.accent
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = PawMedicSemanticColors.current.separator)
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = language.text("Propietario: ${record.ownerName}", "Owner: ${record.ownerName}"), fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = PawMedicSemanticColors.current.text)
                Text(text = language.text("Estado: ${record.petReproductiveStatus}", "Status: ${record.petReproductiveStatus}"), fontSize = 12.sp, color = PawMedicSemanticColors.current.secondaryText)
            }
        }
    }
}

// ---------------- TAB 0: RESUMEN GENERAL ----------------
@Composable
private fun GeneralSummaryTab(record: PetMedicalRecord) {
    val language = LocalLanguagePreference.current?.selected ?: AppLanguage.SPANISH
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        DetailSectionCard(title = language.text("📌 Datos Principales de la Mascota", "📌 Pet details")) {
            InfoRow("Número de Expediente", record.recordNumber)
            InfoRow("Nombre de Mascota", record.petName)
            InfoRow("Especie", record.petSpecies)
            InfoRow("Raza", record.petBreed)
            InfoRow("Sexo", record.petSex)
            InfoRow("Fecha de Nacimiento", record.petDateOfBirth ?: language.text("No registrada", "Not recorded"))
            InfoRow("Edad Calculada", record.petAgeCalculated)
            InfoRow("Color / Señas", "${record.petColor} • ${record.petSpecialMarks}")
            InfoRow("Estado Reproductivo", record.petReproductiveStatus)
            InfoRow("Propietario", "${record.ownerName} (${record.ownerPhone})")
            InfoRow("Fecha Registro Expediente", record.createdAt)
        }

        val latestConsultation = record.consultations.firstOrNull()
        if (latestConsultation != null) {
            DetailSectionCard(title = language.text("🩺 Últimas Constantes Fisiológicas (${latestConsultation.occurredAt})", "🩺 Latest vital signs (${latestConsultation.occurredAt})")) {
                val v = latestConsultation.vitalSigns
                InfoRow("Temperatura", "${v.temperatureCelsius ?: 38.5} °C")
                InfoRow("Frecuencia Cardíaca", "${v.heartRateBpm ?: 120} bpm")
                InfoRow("Frecuencia Respiratoria", "${v.respiratoryRateRpm ?: 24} rpm")
                InfoRow("Pulso", v.pulseQuality.ifBlank { language.text("Fuerte y regular", "Strong and regular") })
                InfoRow("Tiempo Llenado Capilar (TLLC)", v.tllcSeconds.ifBlank { language.text("< 2 seg", "< 2 sec") })
                InfoRow("Estado de Hidratación", v.hydrationStatus.ifBlank { "Normal (0-5%)" })
                InfoRow("Condición Corporal", v.bodyConditionScore.ifBlank { "3/5 (Ideal)" })
                InfoRow("Actitud / Conciencia", "${v.attitude} • ${v.consciousnessState}")
            }
        }
    }
}

// ---------------- TAB 1: ANTECEDENTES MEDICOS ----------------
@Composable
private fun BackgroundTab(
    record: PetMedicalRecord,
    isVet: Boolean,
    onEditBackground: () -> Unit
) {
    val language = LocalLanguagePreference.current?.selected ?: AppLanguage.SPANISH
    val bg = record.background

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (isVet) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                OutlinedButton(
                    onClick = onEditBackground,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(language.text("✏️ Editar Antecedentes Médicos", "✏️ Edit medical background"), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PawMedicSemanticColors.current.accent)
                }
            }
        }

        DetailSectionCard(title = language.text("💉 Historial de Vacunación", "💉 Vaccination history")) {
            if (record.vaccinations.isEmpty()) {
                Text(language.text("Sin registros de vacunación.", "No vaccination records."), fontSize = 13.sp, color = PawMedicSemanticColors.current.hint)
            } else {
                record.vaccinations.forEach { vac ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(vac.vaccineName, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = PawMedicSemanticColors.current.text)
                            Text(language.text("Aplicada por: ${vac.appliedByName} • Lote: ${vac.batchNumber.ifBlank { "N/A" }}", "Administered by: ${vac.appliedByName} • Batch: ${vac.batchNumber.ifBlank { "N/A" }}"), fontSize = 11.sp, color = PawMedicSemanticColors.current.hint)
                        }
                        Text(vac.appliedDate, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PawMedicSemanticColors.current.accent)
                    }
                    HorizontalDivider(color = PawMedicSemanticColors.current.mutedSurface)
                }
            }
        }

        DetailSectionCard(title = language.text("🐛 Desparasitación", "🐛 Deworming")) {
            InfoRow("Última Desparasitación", bg.lastDewormingDate ?: language.text("No registrada", "Not recorded"))
            InfoRow("Producto Utilizado", bg.dewormingProduct.ifBlank { language.text("No especificado", "Not specified") })
        }

        DetailSectionCard(title = language.text("🥗 Alimentación & Hábitat", "🥗 Diet & habitat")) {
            InfoRow("Alimentación", bg.diet.ifBlank { language.text("Croquetas secas comerciales", "Commercial dry food") })
            InfoRow("Alergias Conocidas", bg.allergies.ifBlank { language.text("Ninguna reportada", "None reported") }, isAlert = bg.allergies.isNotBlank() && !bg.allergies.contains("Ninguna", true))
            InfoRow("Hábitat y Condiciones", bg.habitat.ifBlank { language.text("Interior de casa sin salidas a la calle", "Indoors without outdoor access") })
        }

        DetailSectionCard(title = language.text("🏥 Historial Clínico Previo", "🏥 Previous medical history")) {
            InfoRow("Enfermedades Anteriores", bg.pastDiseases.ifBlank { language.text("Ninguna", "None") })
            InfoRow("Cirugías Anteriores", bg.pastSurgeries.ifBlank { language.text("Ninguna", "None") })
            InfoRow("Antecedentes Familiares", bg.familyHistory.ifBlank { language.text("Sin datos relevantes", "No relevant information") })
            InfoRow("Estado Reproductivo", bg.reproductiveStatus.ifBlank { record.petReproductiveStatus })
        }
    }
}

// ---------------- TAB 2: CONSULTAS (SOAP) ----------------
@Composable
private fun ConsultationsTab(
    record: PetMedicalRecord,
    isVet: Boolean,
    onNewConsultation: () -> Unit
) {
    val language = LocalLanguagePreference.current?.selected ?: AppLanguage.SPANISH
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (isVet) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(language.text("Historial de Consultas (${record.consultations.size})", "Consultation history (${record.consultations.size})"), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = PawMedicSemanticColors.current.text)
                Button(
                    onClick = onNewConsultation,
                    colors = ButtonDefaults.buttonColors(containerColor = PawMedicColors.Teal600)
                ) {
                    Text(language.text("+ Registrar Consulta", "+ Add consultation"), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        if (record.consultations.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                Text(language.text("No hay consultas clínicas registradas.", "No consultations recorded."), color = PawMedicSemanticColors.current.hint)
            }
        } else {
            record.consultations.forEach { c ->
                ConsultationCardItem(consultation = c)
            }
        }
    }
}

@Composable
private fun ConsultationCardItem(consultation: ClinicalConsultation) {
    val language = LocalLanguagePreference.current?.selected ?: AppLanguage.SPANISH
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(PawMedicSemanticColors.current.surface)
            .border(1.dp, PawMedicSemanticColors.current.separator, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "📅 ${consultation.occurredAt}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = PawMedicSemanticColors.current.accent
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(PawMedicSemanticColors.current.softAccent)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = consultation.clinicName.ifBlank { "Clinipet Central" },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = PawMedicSemanticColors.current.accent
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(text = language.text("Médico: ${consultation.veterinarianName}", "Veterinarian: ${consultation.veterinarianName}"), fontSize = 12.sp, color = PawMedicSemanticColors.current.secondaryText)

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = PawMedicSemanticColors.current.mutedSurface)
            Spacer(modifier = Modifier.height(10.dp))

            Text(language.text("MOTIVO DE CONSULTA", "REASON FOR CONSULTATION"), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = PawMedicSemanticColors.current.hint)
            Text(consultation.reason, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = PawMedicSemanticColors.current.text)

            if (consultation.anamnesis.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(language.text("ANAMNESIS", "MEDICAL HISTORY"), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = PawMedicSemanticColors.current.hint)
                Text(consultation.anamnesis, fontSize = 13.sp, color = PawMedicSemanticColors.current.text)
            }

            if (consultation.definitiveDiagnosis.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(language.text("DIAGNÓSTICO DEFINITIVO", "FINAL DIAGNOSIS"), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = PawMedicSemanticColors.current.accent)
                Text(consultation.definitiveDiagnosis, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = PawMedicSemanticColors.current.text)
            }

            if (consultation.treatmentPlan.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(language.text("PLAN DE TRATAMIENTO", "TREATMENT PLAN"), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = PawMedicSemanticColors.current.hint)
                Text(consultation.treatmentPlan, fontSize = 13.sp, color = PawMedicSemanticColors.current.text)
            }
        }
    }
}

// ---------------- TAB 3: EXAMEN FISICO ----------------
@Composable
private fun PhysicalExamTab(record: PetMedicalRecord) {
    val language = LocalLanguagePreference.current?.selected ?: AppLanguage.SPANISH
    val latest = record.consultations.firstOrNull()

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (latest == null) {
            Text(language.text("No se cuenta con registro de examen físico.", "No physical examination recorded."), color = PawMedicSemanticColors.current.hint)
        } else {
            val v = latest.vitalSigns
            val s = latest.systemsExploration

            DetailSectionCard(title = language.text("🩺 Constantes Fisiológicas", "🩺 Vital signs")) {
                InfoRow("Temperatura", "${v.temperatureCelsius ?: 38.5} °C")
                InfoRow("Frecuencia Cardíaca", "${v.heartRateBpm ?: 120} bpm")
                InfoRow("Frecuencia Respiratoria", "${v.respiratoryRateRpm ?: 24} rpm")
                InfoRow("Pulso", v.pulseQuality.ifBlank { language.text("Fuerte y regular", "Strong and regular") })
                InfoRow("Peso", "${(v.weightGrams ?: 4200) / 1000.0} kg")
                InfoRow("Tiempo Llenado Capilar (TLLC)", v.tllcSeconds.ifBlank { language.text("< 2 seg", "< 2 sec") })
                InfoRow("Estado de Hidratación", v.hydrationStatus.ifBlank { "Normal (0-5%)" })
                InfoRow("Condición Corporal", v.bodyConditionScore.ifBlank { "3/5 (Ideal)" })
                InfoRow("Actitud / Estado de Conciencia", "${v.attitude} • ${v.consciousnessState}")
            }

            DetailSectionCard(title = language.text("🔍 Exploración por 12 Sistemas Orgánicos", "🔍 Examination of 12 organ systems")) {
                InfoRow("Mucosas", s.mucosas)
                InfoRow("Ojos", s.ojos)
                InfoRow("Oídos", s.oidos)
                InfoRow("Nódulos Linfáticos", s.nodulosLinfaticos)
                InfoRow("Piel y Anexos", s.pielAnexos)
                InfoRow("Locomoción", s.locomocion)
                InfoRow("Sistema Músculo-Esquelético", s.musculoEsqueletico)
                InfoRow("Sistema Nervioso", s.sistemaNervioso)
                InfoRow("Sistema Cardiovascular", s.sistemaCardiovascular)
                InfoRow("Sistema Respiratorio", s.sistemaRespiratorio)
                InfoRow("Sistema Digestivo", s.sistemaDigestivo)
                InfoRow("Sistema Genitourinario", s.sistemaGenitourinario)
            }
        }
    }
}

// ---------------- TAB 4: DIAGNOSTICOS & ESTUDIOS ----------------
@Composable
private fun DiagnosesAndStudiesTab(record: PetMedicalRecord) {
    val language = LocalLanguagePreference.current?.selected ?: AppLanguage.SPANISH
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        val latest = record.consultations.firstOrNull()

        DetailSectionCard(title = language.text("📋 Diagnósticos y Problemas Clinicos", "📋 Diagnoses and clinical problems")) {
            InfoRow("Lista de Problemas", latest?.problemList?.joinToString(", ")?.ifBlank { language.text("Ninguno", "None") } ?: language.text("Ninguno", "None"))
            InfoRow("Diagnósticos Diferenciales", latest?.differentialDiagnoses?.ifBlank { language.text("Sin diferenciales", "No differential diagnoses") } ?: language.text("Sin diferenciales", "No differential diagnoses"))
            InfoRow("Diagnóstico Definitivo", latest?.definitiveDiagnosis?.ifBlank { language.text("En observación", "Under observation") } ?: language.text("En observación", "Under observation"), isHighlight = true)
            InfoRow("Plan Diagnóstico", latest?.diagnosticPlan?.ifBlank { language.text("Sin exámenes adicionales requeridos", "No further tests required") } ?: language.text("Sin exámen requeridos", "No tests required"))
        }

        DetailSectionCard(title = language.text("🔬 Estudios de Laboratorio e Imágenes", "🔬 Laboratory tests and imaging")) {
            if (record.studies.isEmpty()) {
                Text(language.text("Sin estudios o imágenes adjuntas.", "No tests or images attached."), fontSize = 13.sp, color = PawMedicSemanticColors.current.hint)
            } else {
                record.studies.forEach { st ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(st.name, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = PawMedicSemanticColors.current.text)
                            Text(language.text("Tipo: ${st.studyType} • Fecha: ${st.requestedAt}", "Type: ${st.studyType} • Date: ${st.requestedAt}"), fontSize = 12.sp, color = PawMedicSemanticColors.current.secondaryText)
                            if (st.resultsNotes.isNotBlank()) {
                                Text(language.text("Resultados: ${st.resultsNotes}", "Results: ${st.resultsNotes}"), fontSize = 12.sp, color = PawMedicSemanticColors.current.text)
                            }
                        }

                        if (st.attachmentUrls.isNotEmpty()) {
                            Text(language.text("📎 Ver Adjunto", "📎 View attachment"), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PawMedicSemanticColors.current.accent)
                        }
                    }
                    HorizontalDivider(color = PawMedicSemanticColors.current.mutedSurface)
                }
            }
        }
    }
}

// ---------------- TAB 5: TRATAMIENTOS & RECETAS ----------------
@Composable
private fun PrescriptionsTab(record: PetMedicalRecord) {
    val language = LocalLanguagePreference.current?.selected ?: AppLanguage.SPANISH
    val administered = record.prescriptions.filter { it.isAdministeredInConsultation }
    val prescribed = record.prescriptions.filter { !it.isAdministeredInConsultation }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        DetailSectionCard(title = language.text("💉 Medicamentos Administrados en Consulta", "💉 Medication administered during consultation")) {
            if (administered.isEmpty()) {
                Text(language.text("Sin medicamentos administrados en consulta.", "No medication administered during consultation."), fontSize = 13.sp, color = PawMedicSemanticColors.current.hint)
            } else {
                administered.forEach { p ->
                    PrescriptionRowItem(p)
                }
            }
        }

        DetailSectionCard(title = language.text("💊 Medicamentos Recetados para Administración en Casa", "💊 Medication prescribed for home use")) {
            if (prescribed.isEmpty()) {
                Text(language.text("Sin recetas para casa.", "No prescriptions for home use."), fontSize = 13.sp, color = PawMedicSemanticColors.current.hint)
            } else {
                prescribed.forEach { p ->
                    PrescriptionRowItem(p)
                }
            }
        }
    }
}

@Composable
private fun PrescriptionRowItem(p: PrescriptionRecord) {
    val language = LocalLanguagePreference.current?.selected ?: AppLanguage.SPANISH
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(p.drugName, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = PawMedicSemanticColors.current.text)
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (p.isActive) Color(0xFFE6F4EA) else PawMedicSemanticColors.current.mutedSurface)
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(
                    text = if (p.isActive) {
                        language.text("🟢 ACTIVA (Vence: ${p.endDate ?: "N/A"})", "🟢 ACTIVE (Expires: ${p.endDate ?: "N/A"})")
                    } else {
                        language.text("⚪ VENCIDA (Venció: ${p.endDate ?: "N/A"})", "⚪ EXPIRED (Expired: ${p.endDate ?: "N/A"})")
                    },
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (p.isActive) PawMedicSemanticColors.current.accent else PawMedicSemanticColors.current.secondaryText
                )
            }
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            language.text(
                "Dosis: ${p.dosage} ${p.unit} • Vía: ${p.route} • Frecuencia: ${p.frequency} • Duración: ${p.duration}",
                "Dose: ${p.dosage} ${p.unit} • Route: ${p.route} • Frequency: ${p.frequency} • Duration: ${p.duration}"
            ),
            fontSize = 12.sp,
            color = PawMedicSemanticColors.current.secondaryText
        )
        if (p.notes.isNotBlank()) {
            Text(language.text("Instrucciones: ${p.notes}", "Instructions: ${p.notes}"), fontSize = 12.sp, color = PawMedicSemanticColors.current.text)
        }
        HorizontalDivider(color = PawMedicSemanticColors.current.mutedSurface, modifier = Modifier.padding(top = 4.dp))
    }
}

// ---------------- TAB 6: BITACORA DE AUDITORIA ----------------
@Composable
private fun AuditTrailTab(record: PetMedicalRecord) {
    val language = LocalLanguagePreference.current?.selected ?: AppLanguage.SPANISH
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        DetailSectionCard(title = language.text("📜 Bitácora de Auditoría y Trazabilidad", "📜 Audit and traceability log")) {
            if (record.auditLogs.isEmpty()) {
                Text(language.text("Sin registros de auditoría.", "No audit records."), fontSize = 13.sp, color = PawMedicSemanticColors.current.hint)
            } else {
                record.auditLogs.forEach { log ->
                    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(language.text("${log.action} por ${log.actorName} (${log.actorRole})", "${log.action} by ${log.actorName} (${log.actorRole})"), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = PawMedicSemanticColors.current.text)
                            Text(log.timestamp, fontSize = 11.sp, color = PawMedicSemanticColors.current.hint)
                        }
                        Text(log.details, fontSize = 12.sp, color = PawMedicSemanticColors.current.text)
                        HorizontalDivider(color = PawMedicSemanticColors.current.mutedSurface, modifier = Modifier.padding(top = 4.dp))
                    }
                }
            }
        }
    }
}

// COMPOSABLE AUXILIAR
@Composable
private fun DetailSectionCard(title: String, content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(PawMedicSemanticColors.current.surface)
            .border(1.dp, PawMedicSemanticColors.current.separator, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Column {
            Text(title, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = PawMedicSemanticColors.current.text)
            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = PawMedicSemanticColors.current.mutedSurface)
            Spacer(modifier = Modifier.height(10.dp))
            content()
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String, isHighlight: Boolean = false, isAlert: Boolean = false) {
    val language = LocalLanguagePreference.current?.selected ?: AppLanguage.SPANISH
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(localizeMedicalLabel(label, language), fontSize = 13.sp, color = PawMedicSemanticColors.current.secondaryText)
        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = if (isHighlight || isAlert) FontWeight.Bold else FontWeight.Medium,
            color = if (isAlert) Color(0xFFDC2626) else if (isHighlight) PawMedicColors.Teal600 else PawMedicSemanticColors.current.text
        )
    }
}

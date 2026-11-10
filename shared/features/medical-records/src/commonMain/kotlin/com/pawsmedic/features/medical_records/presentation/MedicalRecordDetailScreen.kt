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

@Composable
fun MedicalRecordDetailScreen(
    viewModel: MedicalRecordViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
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
            .background(PawMedicColors.BackgroundScreen)
    ) {
        PawMedicTopBar(
            title = "Expediente Médico Digital",
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
                    .background(if (isVet) Color(0xFFE6F4EA) else Color(0xFFEFF6FF))
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
                                text = if (isVet) "Modo Médico Veterinario (Lectura y Escritura)" else "Modo Propietario (Exclusivo Lectura)",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isVet) PawMedicColors.Teal600 else Color(0xFF1D4ED8)
                            )
                            Text(
                                text = if (isVet) "Puede registrar consultas, recetas, diagnósticos y estudios" else "Puede consultar antecedentes, consultas, recetas y estudios de su mascota",
                                fontSize = 11.sp,
                                color = PawMedicColors.Gray600
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
                            Text("+ Consulta", fontSize = 12.sp, fontWeight = FontWeight.Bold)
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
                    "Resumen General",
                    "Antecedentes",
                    "Consultas (SOAP)",
                    "Examen Físico",
                    "Diagnósticos & Estudios",
                    "Recetas & Fármacos",
                    "Bitácora Auditoría"
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
                                    color = if (uiState.selectedTabIndex == idx) PawMedicColors.Teal600 else PawMedicColors.Gray600
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
            } else {
                Box(
                    modifier = Modifier.fillMaxWidth().height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "Cargando expediente médico...", color = PawMedicColors.Gray500)
                }
            }
        }
    }
}

@Composable
private fun PetSummaryCard(record: PetMedicalRecord) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(PawMedicColors.White)
            .border(1.dp, PawMedicColors.Gray200, RoundedCornerShape(16.dp))
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
                            .background(PawMedicColors.Teal50)
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
                            text = record.petName.ifBlank { "Mascota" },
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = PawMedicColors.Gray900
                        )
                        Text(
                            text = "${record.petSpecies} • ${record.petBreed} • ${record.petSex}",
                            fontSize = 13.sp,
                            color = PawMedicColors.Gray600
                        )
                        Text(
                            text = "Edad: ${record.petAgeCalculated} • Peso: ${(record.petWeightGrams ?: 4200) / 1000.0} kg",
                            fontSize = 12.sp,
                            color = PawMedicColors.Gray500
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(PawMedicColors.Teal50)
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = record.recordNumber,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = PawMedicColors.Teal600
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = PawMedicColors.Gray200)
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Propietario: ${record.ownerName}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = PawMedicColors.Gray800)
                Text(text = "Estado: ${record.petReproductiveStatus}", fontSize = 12.sp, color = PawMedicColors.Gray600)
            }
        }
    }
}

// ---------------- TAB 0: RESUMEN GENERAL ----------------
@Composable
private fun GeneralSummaryTab(record: PetMedicalRecord) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        DetailSectionCard(title = "📌 Datos Principales de la Mascota") {
            InfoRow("Número de Expediente", record.recordNumber)
            InfoRow("Nombre de Mascota", record.petName)
            InfoRow("Especie", record.petSpecies)
            InfoRow("Raza", record.petBreed)
            InfoRow("Sexo", record.petSex)
            InfoRow("Fecha de Nacimiento", record.petDateOfBirth ?: "No registrada")
            InfoRow("Edad Calculada", record.petAgeCalculated)
            InfoRow("Color / Señas", "${record.petColor} • ${record.petSpecialMarks}")
            InfoRow("Estado Reproductivo", record.petReproductiveStatus)
            InfoRow("Propietario", "${record.ownerName} (${record.ownerPhone})")
            InfoRow("Fecha Registro Expediente", record.createdAt)
        }

        val latestConsultation = record.consultations.firstOrNull()
        if (latestConsultation != null) {
            DetailSectionCard(title = "🩺 Últimas Constantes Fisiológicas (${latestConsultation.occurredAt})") {
                val v = latestConsultation.vitalSigns
                InfoRow("Temperatura", "${v.temperatureCelsius ?: 38.5} °C")
                InfoRow("Frecuencia Cardíaca", "${v.heartRateBpm ?: 120} bpm")
                InfoRow("Frecuencia Respiratoria", "${v.respiratoryRateRpm ?: 24} rpm")
                InfoRow("Pulso", v.pulseQuality.ifBlank { "Fuerte y regular" })
                InfoRow("Tiempo Llenado Capilar (TLLC)", v.tllcSeconds.ifBlank { "< 2 seg" })
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
    val bg = record.background

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (isVet) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                OutlinedButton(
                    onClick = onEditBackground,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("✏️ Editar Antecedentes Médicos", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PawMedicColors.Teal600)
                }
            }
        }

        DetailSectionCard(title = "💉 Historial de Vacunación") {
            if (record.vaccinations.isEmpty()) {
                Text("Sin registros de vacunación.", fontSize = 13.sp, color = PawMedicColors.Gray500)
            } else {
                record.vaccinations.forEach { vac ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(vac.vaccineName, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = PawMedicColors.Gray900)
                            Text("Aplicada por: ${vac.appliedByName} • Lote: ${vac.batchNumber.ifBlank { "N/A" }}", fontSize = 11.sp, color = PawMedicColors.Gray500)
                        }
                        Text(vac.appliedDate, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PawMedicColors.Teal600)
                    }
                    HorizontalDivider(color = PawMedicColors.Gray100)
                }
            }
        }

        DetailSectionCard(title = "🐛 Desparasitación") {
            InfoRow("Última Desparasitación", bg.lastDewormingDate ?: "No registrada")
            InfoRow("Producto Utilizado", bg.dewormingProduct.ifBlank { "No especificado" })
        }

        DetailSectionCard(title = "🥗 Alimentación & Hábitat") {
            InfoRow("Alimentación", bg.diet.ifBlank { "Croquetas secas comerciales" })
            InfoRow("Alergias Conocidas", bg.allergies.ifBlank { "Ninguna reportada" }, isAlert = bg.allergies.isNotBlank() && !bg.allergies.contains("Ninguna", true))
            InfoRow("Hábitat y Condiciones", bg.habitat.ifBlank { "Interior de casa sin salidas a la calle" })
        }

        DetailSectionCard(title = "🏥 Historial Clínico Previo") {
            InfoRow("Enfermedades Anteriores", bg.pastDiseases.ifBlank { "Ninguna" })
            InfoRow("Cirugías Anteriores", bg.pastSurgeries.ifBlank { "Ninguna" })
            InfoRow("Antecedentes Familiares", bg.familyHistory.ifBlank { "Sin datos relevantes" })
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
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (isVet) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Historial de Consultas (${record.consultations.size})", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = PawMedicColors.Gray900)
                Button(
                    onClick = onNewConsultation,
                    colors = ButtonDefaults.buttonColors(containerColor = PawMedicColors.Teal600)
                ) {
                    Text("+ Registrar Consulta", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        if (record.consultations.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                Text("No hay consultas clínicas registradas.", color = PawMedicColors.Gray500)
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
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(PawMedicColors.White)
            .border(1.dp, PawMedicColors.Gray200, RoundedCornerShape(16.dp))
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
                    color = PawMedicColors.Teal600
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFFE6F4EA))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = consultation.clinicName.ifBlank { "Clinipet Central" },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = PawMedicColors.Teal600
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "Médico: ${consultation.veterinarianName}", fontSize = 12.sp, color = PawMedicColors.Gray600)

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = PawMedicColors.Gray100)
            Spacer(modifier = Modifier.height(10.dp))

            Text("MOTIVO DE CONSULTA", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = PawMedicColors.Gray400)
            Text(consultation.reason, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = PawMedicColors.Gray800)

            if (consultation.anamnesis.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text("ANAMNESIS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = PawMedicColors.Gray400)
                Text(consultation.anamnesis, fontSize = 13.sp, color = PawMedicColors.Gray700)
            }

            if (consultation.definitiveDiagnosis.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text("DIAGNÓSTICO DEFINITIVO", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = PawMedicColors.Teal600)
                Text(consultation.definitiveDiagnosis, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = PawMedicColors.Gray900)
            }

            if (consultation.treatmentPlan.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text("PLAN DE TRATAMIENTO", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = PawMedicColors.Gray400)
                Text(consultation.treatmentPlan, fontSize = 13.sp, color = PawMedicColors.Gray700)
            }
        }
    }
}

// ---------------- TAB 3: EXAMEN FISICO ----------------
@Composable
private fun PhysicalExamTab(record: PetMedicalRecord) {
    val latest = record.consultations.firstOrNull()

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (latest == null) {
            Text("No se cuenta con registro de examen físico.", color = PawMedicColors.Gray500)
        } else {
            val v = latest.vitalSigns
            val s = latest.systemsExploration

            DetailSectionCard(title = "🩺 Constantes Fisiológicas") {
                InfoRow("Temperatura", "${v.temperatureCelsius ?: 38.5} °C")
                InfoRow("Frecuencia Cardíaca", "${v.heartRateBpm ?: 120} bpm")
                InfoRow("Frecuencia Respiratoria", "${v.respiratoryRateRpm ?: 24} rpm")
                InfoRow("Pulso", v.pulseQuality.ifBlank { "Fuerte y regular" })
                InfoRow("Peso", "${(v.weightGrams ?: 4200) / 1000.0} kg")
                InfoRow("Tiempo Llenado Capilar (TLLC)", v.tllcSeconds.ifBlank { "< 2 seg" })
                InfoRow("Estado de Hidratación", v.hydrationStatus.ifBlank { "Normal (0-5%)" })
                InfoRow("Condición Corporal", v.bodyConditionScore.ifBlank { "3/5 (Ideal)" })
                InfoRow("Actitud / Estado de Conciencia", "${v.attitude} • ${v.consciousnessState}")
            }

            DetailSectionCard(title = "🔍 Exploración por 12 Sistemas Orgánicos") {
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
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        val latest = record.consultations.firstOrNull()

        DetailSectionCard(title = "📋 Diagnósticos y Problemas Clinicos") {
            InfoRow("Lista de Problemas", latest?.problemList?.joinToString(", ")?.ifBlank { "Ninguno" } ?: "Ninguno")
            InfoRow("Diagnósticos Diferenciales", latest?.differentialDiagnoses?.ifBlank { "Sin diferenciales" } ?: "Sin diferenciales")
            InfoRow("Diagnóstico Definitivo", latest?.definitiveDiagnosis?.ifBlank { "En observación" } ?: "En observación", isHighlight = true)
            InfoRow("Plan Diagnóstico", latest?.diagnosticPlan?.ifBlank { "Sin exámenes adicionales requeridos" } ?: "Sin exámen requeridos")
        }

        DetailSectionCard(title = "🔬 Estudios de Laboratorio e Imágenes") {
            if (record.studies.isEmpty()) {
                Text("Sin estudios o imágenes adjuntas.", fontSize = 13.sp, color = PawMedicColors.Gray500)
            } else {
                record.studies.forEach { st ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(st.name, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = PawMedicColors.Gray900)
                            Text("Tipo: ${st.studyType} • Fecha: ${st.requestedAt}", fontSize = 12.sp, color = PawMedicColors.Gray600)
                            if (st.resultsNotes.isNotBlank()) {
                                Text("Resultados: ${st.resultsNotes}", fontSize = 12.sp, color = PawMedicColors.Gray700)
                            }
                        }

                        if (st.attachmentUrls.isNotEmpty()) {
                            Text("📎 Ver Adjunto", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PawMedicColors.Teal600)
                        }
                    }
                    HorizontalDivider(color = PawMedicColors.Gray100)
                }
            }
        }
    }
}

// ---------------- TAB 5: TRATAMIENTOS & RECETAS ----------------
@Composable
private fun PrescriptionsTab(record: PetMedicalRecord) {
    val administered = record.prescriptions.filter { it.isAdministeredInConsultation }
    val prescribed = record.prescriptions.filter { !it.isAdministeredInConsultation }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        DetailSectionCard(title = "💉 Medicamentos Administrados en Consulta") {
            if (administered.isEmpty()) {
                Text("Sin medicamentos administrados en consulta.", fontSize = 13.sp, color = PawMedicColors.Gray500)
            } else {
                administered.forEach { p ->
                    PrescriptionRowItem(p)
                }
            }
        }

        DetailSectionCard(title = "💊 Medicamentos Recetados para Administración en Casa") {
            if (prescribed.isEmpty()) {
                Text("Sin recetas para casa.", fontSize = 13.sp, color = PawMedicColors.Gray500)
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
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(p.drugName, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = PawMedicColors.Gray900)
            Text("${p.dosage} ${p.unit}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = PawMedicColors.Teal600)
        }
        Text("Vía: ${p.route} • Frecuencia: ${p.frequency} • Duración: ${p.duration}", fontSize = 12.sp, color = PawMedicColors.Gray600)
        if (p.notes.isNotBlank()) {
            Text("Instrucciones: ${p.notes}", fontSize = 12.sp, color = PawMedicColors.Gray700)
        }
        HorizontalDivider(color = PawMedicColors.Gray100, modifier = Modifier.padding(top = 4.dp))
    }
}

// ---------------- TAB 6: BITACORA DE AUDITORIA ----------------
@Composable
private fun AuditTrailTab(record: PetMedicalRecord) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        DetailSectionCard(title = "📜 Bitácora de Auditoría y Trazabilidad") {
            if (record.auditLogs.isEmpty()) {
                Text("Sin registros de auditoría.", fontSize = 13.sp, color = PawMedicColors.Gray500)
            } else {
                record.auditLogs.forEach { log ->
                    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("${log.action} por ${log.actorName} (${log.actorRole})", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = PawMedicColors.Gray900)
                            Text(log.timestamp, fontSize = 11.sp, color = PawMedicColors.Gray500)
                        }
                        Text(log.details, fontSize = 12.sp, color = PawMedicColors.Gray700)
                        HorizontalDivider(color = PawMedicColors.Gray100, modifier = Modifier.padding(top = 4.dp))
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
            .background(PawMedicColors.White)
            .border(1.dp, PawMedicColors.Gray200, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Column {
            Text(title, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = PawMedicColors.Gray900)
            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = PawMedicColors.Gray100)
            Spacer(modifier = Modifier.height(10.dp))
            content()
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String, isHighlight: Boolean = false, isAlert: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 13.sp, color = PawMedicColors.Gray600)
        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = if (isHighlight || isAlert) FontWeight.Bold else FontWeight.Medium,
            color = if (isAlert) Color(0xFFDC2626) else if (isHighlight) PawMedicColors.Teal600 else PawMedicColors.Gray900
        )
    }
}

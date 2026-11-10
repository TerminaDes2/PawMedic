package com.pawsmedic.features.medical_records.presentation.dialog

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import com.pawsmedic.features.medical_records.domain.model.ClinicalConsultation
import com.pawsmedic.features.medical_records.domain.model.ClinicalStudyRecord
import com.pawsmedic.features.medical_records.domain.model.PrescriptionRecord
import com.pawsmedic.features.medical_records.domain.model.SystemsExploration
import com.pawsmedic.features.medical_records.domain.model.VitalSigns
import com.pawsmedic.shared.core.designsystem.theme.PawMedicColors

@Composable
fun NewConsultationDialog(
    petId: String,
    vetName: String,
    onDismiss: () -> Unit,
    onConfirm: (ClinicalConsultation, List<PrescriptionRecord>, List<ClinicalStudyRecord>) -> Unit
) {
    var formSection by remember { mutableStateOf(0) } // 0: Anamnesis, 1: Examen Físico, 2: Diagnósticos, 3: Tratamientos

    // Anamnesis & Motivo
    var reason by remember { mutableStateOf("") }
    var anamnesis by remember { mutableStateOf("") }

    // Constantes Fisiológicas
    var tempInput by remember { mutableStateOf("38.5") }
    var fcInput by remember { mutableStateOf("120") }
    var frInput by remember { mutableStateOf("24") }
    var weightInput by remember { mutableStateOf("4.2") }
    var pulseInput by remember { mutableStateOf("Fuerte y regular") }
    var tllcInput by remember { mutableStateOf("< 2 seg") }
    var hydrationInput by remember { mutableStateOf("Normal") }
    var bcsInput by remember { mutableStateOf("3/5 (Ideal)") }
    var attitudeInput by remember { mutableStateOf("Alerta") }
    var consciousnessInput by remember { mutableStateOf("Consciente") }

    // Exploración por Sistemas (12)
    var mucosas by remember { mutableStateOf("Rosadas y húmedas") }
    var ojos by remember { mutableStateOf("Normales") }
    var oidos by remember { mutableStateOf("Limpios") }
    var nodulos by remember { mutableStateOf("No reactivos") }
    var piel by remember { mutableStateOf("Pelaje sano sin parásitos") }
    var locomocion by remember { mutableStateOf("Sin claudicación") }
    var musculo by remember { mutableStateOf("Sin dolor a palpación") }
    var nervioso by remember { mutableStateOf("Reflejos normales") }
    var cardio by remember { mutableStateOf("Sin soplos") }
    var resp by remember { mutableStateOf("Campos limpios") }
    var digestivo by remember { mutableStateOf("Abdomen blando") }
    var genito by remember { mutableStateOf("Vejiga normal") }

    // Diagnósticos
    var problemListText by remember { mutableStateOf("") }
    var diffDiag by remember { mutableStateOf("") }
    var defDiag by remember { mutableStateOf("") }
    var diagPlan by remember { mutableStateOf("") }
    var treatPlan by remember { mutableStateOf("") }
    var observations by remember { mutableStateOf("") }
    var nextDate by remember { mutableStateOf("") }

    // Medicamentos
    var drugName by remember { mutableStateOf("") }
    var drugDose by remember { mutableStateOf("") }
    var drugRoute by remember { mutableStateOf("Oral") }
    var drugFreq by remember { mutableStateOf("Cada 12h") }
    var drugDuration by remember { mutableStateOf("5 días") }
    var isConsultationAdmin by remember { mutableStateOf(false) }

    // Estudios
    var studyName by remember { mutableStateOf("") }
    var studyType by remember { mutableStateOf("LABORATORY") }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = {
                    if (reason.isBlank()) return@Button

                    val vitals = VitalSigns(
                        temperatureCelsius = tempInput.toDoubleOrNull(),
                        heartRateBpm = fcInput.toIntOrNull(),
                        respiratoryRateRpm = frInput.toIntOrNull(),
                        pulseQuality = pulseInput,
                        weightGrams = ((weightInput.toDoubleOrNull() ?: 4.2) * 1000).toInt(),
                        tllcSeconds = tllcInput,
                        hydrationStatus = hydrationInput,
                        bodyConditionScore = bcsInput,
                        attitude = attitudeInput,
                        consciousnessState = consciousnessInput
                    )

                    val systems = SystemsExploration(
                        mucosas = mucosas,
                        ojos = ojos,
                        oidos = oidos,
                        nodulosLinfaticos = nodulos,
                        pielAnexos = piel,
                        locomocion = locomocion,
                        musculoEsqueletico = musculo,
                        sistemaNervioso = nervioso,
                        sistemaCardiovascular = cardio,
                        sistemaRespiratorio = resp,
                        sistemaDigestivo = digestivo,
                        sistemaGenitourinario = genito
                    )

                    val pList = problemListText.split("\n", ",").map { it.trim() }.filter { it.isNotBlank() }

                    val consultation = ClinicalConsultation(
                        petId = petId,
                        veterinarianName = vetName,
                        reason = reason,
                        anamnesis = anamnesis,
                        vitalSigns = vitals,
                        systemsExploration = systems,
                        problemList = pList,
                        differentialDiagnoses = diffDiag,
                        definitiveDiagnosis = defDiag,
                        diagnosticPlan = diagPlan,
                        treatmentPlan = treatPlan,
                        observations = observations,
                        nextAppointmentDate = nextDate.ifBlank { null }
                    )

                    val prescriptions = if (drugName.isNotBlank()) {
                        listOf(
                            PrescriptionRecord(
                                petId = petId,
                                drugName = drugName,
                                dosage = drugDose,
                                route = drugRoute,
                                frequency = drugFreq,
                                duration = drugDuration,
                                isAdministeredInConsultation = isConsultationAdmin,
                                prescribingVetName = vetName
                            )
                        )
                    } else emptyList()

                    val studies = if (studyName.isNotBlank()) {
                        listOf(
                            ClinicalStudyRecord(
                                petId = petId,
                                studyType = studyType,
                                name = studyName,
                                status = "PUBLISHED"
                            )
                        )
                    } else emptyList()

                    onConfirm(consultation, prescriptions, studies)
                },
                colors = ButtonDefaults.buttonColors(containerColor = PawMedicColors.Teal600)
            ) {
                Text("Registrar Consulta", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = PawMedicColors.Gray700)
            }
        },
        title = {
            Text(
                text = "Nueva Consulta Clínica Veterinario",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = PawMedicColors.Gray900
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Selector de Sección del Formulario
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    listOf("1. Anamnesis", "2. Examen", "3. Diagnóstico", "4. Recetas").forEachIndexed { idx, label ->
                        val isSel = formSection == idx
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSel) PawMedicColors.Teal600 else PawMedicColors.Gray100)
                                .clickable { formSection = idx }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                fontSize = 11.sp,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSel) Color.White else PawMedicColors.Gray700
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                when (formSection) {
                    0 -> { // ANAMNESIS & MOTIVO
                        Text("Motivo de Consulta (*Requerido)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PawMedicColors.Teal600)
                        OutlinedTextField(
                            value = reason,
                            onValueChange = { reason = it },
                            placeholder = { Text("Ej. Consulta general, vomitos, vacuna") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Text("Anamnesis / Historia Clínica", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PawMedicColors.Gray700)
                        OutlinedTextField(
                            value = anamnesis,
                            onValueChange = { anamnesis = it },
                            placeholder = { Text("Detalle de síntomas reportados por el propietario") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }

                    1 -> { // CONSTANTES FISIOLOGICAS & 12 SISTEMAS
                        Text("CONSTANTES FISIOLÓGICAS", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = PawMedicColors.Teal600)
                        Spacer(modifier = Modifier.height(6.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Temp (°C)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                OutlinedTextField(value = tempInput, onValueChange = { tempInput = it }, singleLine = true)
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text("FC (bpm)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                OutlinedTextField(value = fcInput, onValueChange = { fcInput = it }, singleLine = true)
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text("FR (rpm)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                OutlinedTextField(value = frInput, onValueChange = { frInput = it }, singleLine = true)
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Peso (kg)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                OutlinedTextField(value = weightInput, onValueChange = { weightInput = it }, singleLine = true)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("TLLC", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                OutlinedTextField(value = tllcInput, onValueChange = { tllcInput = it }, singleLine = true)
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Hidratación", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                OutlinedTextField(value = hydrationInput, onValueChange = { hydrationInput = it }, singleLine = true)
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Condición Corp.", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                OutlinedTextField(value = bcsInput, onValueChange = { bcsInput = it }, singleLine = true)
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        Text("EXPLORACIÓN POR SISTEMAS (12 APARTADOS)", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = PawMedicColors.Teal600)
                        Spacer(modifier = Modifier.height(6.dp))

                        val systemsList = listOf(
                            "Mucosas" to mucosas, "Ojos" to ojos, "Oídos" to oidos, "Nódulos Linfáticos" to nodulos,
                            "Piel y Anexos" to piel, "Locomoción" to locomocion, "Músculo-Esquelético" to musculo,
                            "Sistema Nervioso" to nervioso, "Cardiovascular" to cardio, "Respiratorio" to resp,
                            "Digestivo" to digestivo, "Genitourinario" to genito
                        )

                        systemsList.chunked(2).forEach { pair ->
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(vertical = 4.dp)) {
                                pair.forEach { (label, valStr) ->
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        OutlinedTextField(
                                            value = valStr,
                                            onValueChange = { newV ->
                                                when (label) {
                                                    "Mucosas" -> mucosas = newV
                                                    "Ojos" -> ojos = newV
                                                    "Oídos" -> oidos = newV
                                                    "Nódulos Linfáticos" -> nodulos = newV
                                                    "Piel y Anexos" -> piel = newV
                                                    "Locomoción" -> locomocion = newV
                                                    "Músculo-Esquelético" -> musculo = newV
                                                    "Sistema Nervioso" -> nervioso = newV
                                                    "Cardiovascular" -> cardio = newV
                                                    "Respiratorio" -> resp = newV
                                                    "Digestivo" -> digestivo = newV
                                                    "Genitourinario" -> genito = newV
                                                }
                                            },
                                            singleLine = true
                                        )
                                    }
                                }
                            }
                        }
                    }

                    2 -> { // DIAGNOSTICOS
                        Text("Lista de Problemas (separados por coma)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PawMedicColors.Gray700)
                        OutlinedTextField(
                            value = problemListText,
                            onValueChange = { problemListText = it },
                            placeholder = { Text("Ej. Vomito, Deshidratación, Fiebre") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Diagnósticos Diferenciales", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PawMedicColors.Gray700)
                        OutlinedTextField(value = diffDiag, onValueChange = { diffDiag = it }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp))

                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Diagnóstico Definitivo", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PawMedicColors.Teal600)
                        OutlinedTextField(value = defDiag, onValueChange = { defDiag = it }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp))

                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Plan Diagnóstico y Tratamiento", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PawMedicColors.Gray700)
                        OutlinedTextField(value = treatPlan, onValueChange = { treatPlan = it }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp))

                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Observaciones Generales", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PawMedicColors.Gray700)
                        OutlinedTextField(value = observations, onValueChange = { observations = it }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp))

                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Próxima Cita / Seguimiento", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PawMedicColors.Gray700)
                        OutlinedTextField(value = nextDate, onValueChange = { nextDate = it }, placeholder = { Text("AAAA-MM-DD") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp))
                    }

                    3 -> { // MEDICAMENTOS & ESTUDIOS
                        Text("MEDICAMENTO Y RECETA", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = PawMedicColors.Teal600)
                        Spacer(modifier = Modifier.height(6.dp))

                        OutlinedTextField(value = drugName, onValueChange = { drugName = it }, label = { Text("Nombre del Medicamento") }, modifier = Modifier.fillMaxWidth())

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 6.dp)) {
                            Column(modifier = Modifier.weight(1f)) {
                                OutlinedTextField(value = drugDose, onValueChange = { drugDose = it }, label = { Text("Dosis (ej. 50mg)") })
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                OutlinedTextField(value = drugRoute, onValueChange = { drugRoute = it }, label = { Text("Vía (Oral, IV)") })
                            }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 6.dp)) {
                            Column(modifier = Modifier.weight(1f)) {
                                OutlinedTextField(value = drugFreq, onValueChange = { drugFreq = it }, label = { Text("Frecuencia (c/12h)") })
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                OutlinedTextField(value = drugDuration, onValueChange = { drugDuration = it }, label = { Text("Duración (7 días)") })
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 6.dp)) {
                            Checkbox(checked = isConsultationAdmin, onCheckedChange = { isConsultationAdmin = it })
                            Text("Administrado en consulta (vacuna o inyectable)", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        Text("SOLICITUD DE ESTUDIO CLÍNICO", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = PawMedicColors.Teal600)
                        Spacer(modifier = Modifier.height(6.dp))

                        OutlinedTextField(value = studyName, onValueChange = { studyName = it }, label = { Text("Nombre del Estudio (ej. Hemograma, Ecografía)") }, modifier = Modifier.fillMaxWidth())
                    }
                }
            }
        }
    )
}

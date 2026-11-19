package com.pawsmedic.desktop.veterinary.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Healing
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pawsmedic.desktop.veterinary.supabase
import com.pawsmedic.desktop.veterinary.ui.theme.*
import com.pawsmedic.features.medical_records.data.datasource.SupabaseMedicalRecordDataSource
import com.pawsmedic.features.medical_records.data.repository.DefaultMedicalRecordRepository
import com.pawsmedic.features.medical_records.domain.model.ClinicalConsultation
import com.pawsmedic.features.medical_records.domain.model.ClinicalStudyRecord
import com.pawsmedic.features.medical_records.domain.model.PrescriptionRecord
import com.pawsmedic.features.medical_records.domain.model.SystemsExploration
import com.pawsmedic.features.medical_records.domain.model.VitalSigns
import com.pawsmedic.shared.core.common.convertMillisToIsoDateString
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.UUID

@Serializable
private data class CareServiceItem(
    val id: String,
    @SerialName("tenant_id") val tenantId: String,
    val name: String,
    @SerialName("duration_minutes") val durationMinutes: Int = 30,
    @SerialName("price_cents") val priceCents: Int = 0,
    val active: Boolean = true
)

private data class DraftMedication(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val dose: String,
    val route: String,
    val frequency: String,
    val durationDays: String
)

@Serializable
private data class ClinicTenantZone(@SerialName("time_zone") val timeZone: String)

@Composable
fun VeterinaryCareScreen(
    userEmail: String = "",
    onNavigate: (String) -> Unit,
    onLogout: () -> Unit = {}
) {
    val context = VeterinaryCareContext
    val patient = context.patient
    val category = context.categoryType
    val serviceName = context.serviceName.ifBlank {
        when (category) {
            "CIRUGIA" -> "Cirugía"
            "VACUNACION" -> "Medicina Preventiva / Vacunación"
            "ESTETICA" -> "Estética y Grooming"
            "DIAGNOSTICO" -> "Diagnóstico por Imagen / Laboratorio"
            "HOSPITALIZACION" -> "Hospitalización"
            "ADMINISTRATIVA" -> "Gestión Administrativa"
            else -> "Consulta Médica General"
        }
    }

    var isSaving by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    var careStatus by remember { mutableStateOf("IN_PROGRESS") } // DRAFT, IN_PROGRESS, FINISHED

    // Campos comunes / Consulta General
    var reason by remember { mutableStateOf(context.prefilledReason.ifBlank { serviceName }) }
    var anamnesis by remember { mutableStateOf("") }

    // Constantes Fisiológicas
    var tempInput by remember { mutableStateOf("38.5") }
    var fcInput by remember { mutableStateOf("120") }
    var frInput by remember { mutableStateOf("24") }
    var weightInput by remember { mutableStateOf("4.2") }
    var pulseInput by remember { mutableStateOf("Fuerte y regular") }
    var tllcInput by remember { mutableStateOf("< 2 seg") }
    var hydrationInput by remember { mutableStateOf("Normal (0-5%)") }
    var bcsInput by remember { mutableStateOf("3/5 (Ideal)") }
    var attitudeInput by remember { mutableStateOf("Alerta") }
    var consciousnessInput by remember { mutableStateOf("Consciente") }

    // 12 Sistemas Orgánicos
    var mucosas by remember { mutableStateOf("Rosadas y húmedas") }
    var ojos by remember { mutableStateOf("Normales") }
    var oidos by remember { mutableStateOf("Limpios") }
    var nodulos by remember { mutableStateOf("No reactivos") }
    var piel by remember { mutableStateOf("Pelaje sano") }
    var locomocion by remember { mutableStateOf("Sin claudicación") }
    var musculo by remember { mutableStateOf("Sin dolor") }
    var nervioso by remember { mutableStateOf("Reflejos normales") }
    var cardio by remember { mutableStateOf("Sin soplos") }
    var resp by remember { mutableStateOf("Campos limpios") }
    var digestivo by remember { mutableStateOf("Abdomen blando") }
    var genito by remember { mutableStateOf("Vejiga normal") }

    // Diagnósticos & Plan
    var problemListText by remember { mutableStateOf("") }
    var diffDiag by remember { mutableStateOf("") }
    var defDiag by remember { mutableStateOf("") }
    var treatPlan by remember { mutableStateOf("") }
    var observations by remember { mutableStateOf("") }
    var nextDate by remember { mutableStateOf("") }

    // Medicamentos y Receta
    var prescriptionDurationDays by remember { mutableStateOf("7") }
    val addedMedications = remember { mutableStateListOf<DraftMedication>() }
    var currentDrugName by remember { mutableStateOf("") }
    var currentDrugDose by remember { mutableStateOf("") }
    var currentDrugRoute by remember { mutableStateOf("Oral") }
    var currentDrugFreq by remember { mutableStateOf("Cada 12h") }
    var currentDrugDuration by remember { mutableStateOf("7") }
    var isConsultationAdmin by remember { mutableStateOf(false) }

    // Estudios
    var studyName by remember { mutableStateOf("") }
    var studyType by remember { mutableStateOf("LABORATORY") }

    // Cirugía / Especiales
    var surgicalProcedure by remember { mutableStateOf(serviceName) }
    var preOpNotes by remember { mutableStateOf("") }
    var anesthesiaNotes by remember { mutableStateOf("Anestesia general balanceada") }
    var surgicalFindings by remember { mutableStateOf("") }
    var postOpCare by remember { mutableStateOf("") }

    // Vacunación / Preventiva
    var vaccineName by remember { mutableStateOf(serviceName) }
    var vaccineBatch by remember { mutableStateOf("") }
    var vaccineDose by remember { mutableStateOf("1 dosis") }
    var adverseReactions by remember { mutableStateOf("Ninguna") }

    // Estética / Grooming
    var initialCondition by remember { mutableStateOf("Buen estado general, pelaje sucio") }
    var finalCondition by remember { mutableStateOf("Limpio, corte realizado exitosamente") }
    var productsUsed by remember { mutableStateOf("Champú hipoalergénico, acondicionador") }
    var groomingIncidents by remember { mutableStateOf("Ninguna") }

    // Diagnóstico específico
    var studyResultsNotes by remember { mutableStateOf("") }

    // Hospitalización
    var admissionReason by remember { mutableStateOf(serviceName) }
    var dailyEvolutionNotes by remember { mutableStateOf("") }
    var dischargeSummary by remember { mutableStateOf("") }

    // Agendar Próxima Cita
    var scheduleNextAppt by remember { mutableStateOf(false) }
    var clinicServices by remember { mutableStateOf<List<CareServiceItem>>(emptyList()) }
    var nextServiceId by remember { mutableStateOf<String?>(null) }
    var nextApptDate by remember { mutableStateOf("") }
    var nextApptTime by remember { mutableStateOf("10:00") } // Formato 24h (HH:mm)
    var serviceMenuExpanded by remember { mutableStateOf(false) }
    var clinicTimeZone by remember { mutableStateOf("America/Mexico_City") }

    val coroutineScope = rememberCoroutineScope()
    val repository = remember { DefaultMedicalRecordRepository(SupabaseMedicalRecordDataSource(supabase)) }

    LaunchedEffect(context.tenantId) {
        val clinicId = context.tenantId ?: return@LaunchedEffect
        runCatching {
            val tz = supabase.postgrest["tenants"].select {
                filter { eq("id", clinicId) }
            }.decodeSingle<ClinicTenantZone>().timeZone
            if (tz.isNotBlank()) clinicTimeZone = tz
        }
        runCatching {
            supabase.postgrest["services"].select {
                filter {
                    eq("tenant_id", clinicId)
                    eq("active", true)
                }
            }.decodeList<CareServiceItem>()
        }.onSuccess { list ->
            clinicServices = list
            if (list.isNotEmpty() && nextServiceId == null) {
                nextServiceId = list.first().id
            }
        }
    }

    VetAppLayout(
        currentScreen = if (context.appointmentId != null) "Agenda" else "Consultas",
        onNavigate = onNavigate,
        onLogout = onLogout,
        title = "Editor Unificado de Atención Veterinario: $serviceName",
        userEmail = userEmail
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White)
                .border(1.dp, BorderLight, RoundedCornerShape(12.dp))
                .padding(20.dp)
        ) {
            // HEADER DE ACCIONES Y PACIENTE
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = { onNavigate(if (context.appointmentId != null) "Agenda" else "Consultas") },
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Regresar")
                    }

                    if (patient != null) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(EmeraldLightBg)
                                .border(2.dp, ClinicalEmerald, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text((patient.name ?: "M").take(2).uppercase(), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = SelectedText)
                        }
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(patient.name ?: "Mascota", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(ClinicalEmerald)
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text("ID: ${patient.medicalId ?: patient.id.take(8)}", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                            Text("Especie: ${patient.species ?: "N/A"} • Categoría: $category • Estado: $careStatus", fontSize = 12.sp, color = TextSecondary)
                        }
                    } else {
                        Text("Atención sin paciente asignado", fontWeight = FontWeight.Bold, color = Color(0xFFDC2626))
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                isSaving = true
                                statusMessage = null
                                successMessage = null
                                try {
                                    val p = patient ?: error("No hay mascota seleccionada para guardar el reporte.")
                                    val tenantId = context.tenantId ?: error("No se encontró la clínica autorizada.")
                                    val todayIso = convertMillisToIsoDateString(System.currentTimeMillis())

                                    val finalReason = when (category) {
                                        "CIRUGIA" -> surgicalProcedure
                                        "VACUNACION" -> "Vacunación: $vaccineName"
                                        "ESTETICA" -> "Estética / Grooming: $serviceName"
                                        "DIAGNOSTICO" -> "Estudio de Diagnóstico: $studyName"
                                        "HOSPITALIZACION" -> "Hospitalización: $admissionReason"
                                        else -> reason.ifBlank { serviceName }
                                    }

                                    val finalObservations = when (category) {
                                        "CIRUGIA" -> "Pre-op: $preOpNotes\nAnestesia: $anesthesiaNotes\nHallazgos: $surgicalFindings\nCuidados post-op: $postOpCare"
                                        "VACUNACION" -> "Lote: $vaccineBatch, Dosis: $vaccineDose, Reacciones: $adverseReactions"
                                        "ESTETICA" -> "Condición inicial: $initialCondition\nCondición final: $finalCondition\nProductos: $productsUsed\nIncidencias: $groomingIncidents"
                                        "DIAGNOSTICO" -> "Resultados: $studyResultsNotes"
                                        "HOSPITALIZACION" -> "Evolución: $dailyEvolutionNotes\nAlta: $dischargeSummary"
                                        else -> observations
                                    }

                                    val vitals = if (category == "CONSULTA_GENERAL") {
                                        VitalSigns(
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
                                    } else VitalSigns()

                                    val systems = if (category == "CONSULTA_GENERAL") {
                                        SystemsExploration(
                                            mucosas = mucosas, ojos = ojos, oidos = oidos, nodulosLinfaticos = nodulos,
                                            pielAnexos = piel, locomocion = locomocion, musculoEsqueletico = musculo,
                                            sistemaNervioso = nervioso, sistemaCardiovascular = cardio,
                                            sistemaRespiratorio = resp, sistemaDigestivo = digestivo, sistemaGenitourinario = genito
                                        )
                                    } else SystemsExploration()

                                    val consultation = ClinicalConsultation(
                                        petId = p.id,
                                        tenantId = tenantId,
                                        veterinarianId = context.vetUserId ?: "",
                                        veterinarianName = context.vetUserName,
                                        reason = finalReason,
                                        anamnesis = anamnesis.trim(),
                                        vitalSigns = vitals,
                                        systemsExploration = systems,
                                        problemList = problemListText.split("\n", ",").map { it.trim() }.filter { it.isNotBlank() },
                                        differentialDiagnoses = diffDiag.trim(),
                                        definitiveDiagnosis = defDiag.trim().ifBlank { finalReason },
                                        treatmentPlan = treatPlan.trim(),
                                        observations = finalObservations.trim(),
                                        nextAppointmentDate = nextDate.ifBlank { null },
                                        occurredAt = todayIso
                                    )

                                    val overallDays = prescriptionDurationDays.toIntOrNull() ?: 7
                                    val calculatedEndDate = runCatching {
                                        convertMillisToIsoDateString(System.currentTimeMillis() + overallDays.toLong() * 86400000L)
                                    }.getOrDefault(todayIso)

                                    val finalMedications = addedMedications.toMutableList()
                                    if (currentDrugName.isNotBlank()) {
                                        finalMedications.add(
                                            DraftMedication(
                                                name = currentDrugName.trim(),
                                                dose = currentDrugDose.trim(),
                                                route = currentDrugRoute.trim(),
                                                frequency = currentDrugFreq.trim(),
                                                durationDays = currentDrugDuration.trim().ifBlank { "7" }
                                            )
                                        )
                                    }

                                    val prescriptions = finalMedications.map { med ->
                                        PrescriptionRecord(
                                            petId = p.id,
                                            drugName = med.name,
                                            dosage = med.dose,
                                            route = med.route,
                                            frequency = med.frequency,
                                            duration = "${med.durationDays} días",
                                            startDate = todayIso,
                                            endDate = calculatedEndDate,
                                            isAdministeredInConsultation = isConsultationAdmin,
                                            prescribingVetName = context.vetUserName,
                                            prescribingVetId = context.vetUserId
                                        )
                                    }

                                    val studies = if (studyName.isNotBlank() || category == "DIAGNOSTICO") {
                                        listOf(
                                            ClinicalStudyRecord(
                                                petId = p.id,
                                                studyType = studyType,
                                                name = studyName.ifBlank { serviceName },
                                                resultsNotes = studyResultsNotes,
                                                status = "COMPLETED"
                                            )
                                        )
                                    } else emptyList()

                                    repository.saveConsultation(consultation, prescriptions, studies)
                                        .onSuccess {
                                            careStatus = "FINISHED"
                                            // Si venía de una cita, actualizamos el estado de la cita a COMPLETED en la base de datos
                                            context.appointmentId?.let { appId ->
                                                runCatching {
                                                    supabase.postgrest.rpc(
                                                        function = "transition_appointment_status",
                                                        parameters = buildJsonObject {
                                                            put("p_appointment_id", appId)
                                                            put("p_expected_status", "IN_PROGRESS")
                                                            put("p_next_status", "COMPLETED")
                                                        }
                                                    )
                                                }.onFailure {
                                                    runCatching {
                                                        supabase.postgrest.rpc(
                                                            function = "transition_appointment_status",
                                                            parameters = buildJsonObject {
                                                                put("p_appointment_id", appId)
                                                                put("p_expected_status", "CONFIRMED")
                                                                put("p_next_status", "COMPLETED")
                                                            }
                                                        )
                                                    }.onFailure {
                                                        supabase.postgrest["appointments"]
                                                            .update(buildJsonObject { put("status", "COMPLETED") }) {
                                                                filter { eq("id", appId) }
                                                            }
                                                    }
                                                }
                                            }

                                            // Si el veterinario indicó programar la próxima cita, la guardamos respetando la zona horaria de la clínica
                                            if (scheduleNextAppt && nextServiceId != null && nextApptDate.isNotBlank() && nextApptTime.isNotBlank()) {
                                                runCatching {
                                                    val sId = nextServiceId!!
                                                    val sObj = clinicServices.find { it.id == sId }
                                                    val durationMins = sObj?.durationMinutes ?: 30

                                                    val zoneId = ZoneId.of(clinicTimeZone)
                                                    val localDate = LocalDate.parse(nextApptDate.trim())
                                                    val localTime = LocalTime.parse(nextApptTime.trim())
                                                    val localDateTime = LocalDateTime.of(localDate, localTime)
                                                    val zonedDateTime = ZonedDateTime.of(localDateTime, zoneId)

                                                    val startsAtInstant = zonedDateTime.toInstant()
                                                    val endsAtInstant = startsAtInstant.plusSeconds(durationMins.toLong() * 60L)

                                                    val startsAtStr = startsAtInstant.toString()
                                                    val endsAtStr = endsAtInstant.toString()

                                                    val oId = p.ownerId ?: context.vetUserId ?: p.id

                                                    supabase.postgrest["appointments"].insert(
                                                        buildJsonObject {
                                                            put("pet_id", p.id)
                                                            put("owner_id", oId)
                                                            put("tenant_id", tenantId)
                                                            put("service_id", sId)
                                                            put("status", "CONFIRMED")
                                                            put("starts_at", startsAtStr)
                                                            put("ends_at", endsAtStr)
                                                            put("booked_by", context.vetUserId)
                                                            put("idempotency_key", UUID.randomUUID().toString())
                                                        }
                                                    )
                                                }
                                            }

                                            successMessage = "¡Atención finalizada y guardada en las recetas de ${p.name}!"
                                        }
                                        .onFailure { err ->
                                            statusMessage = err.message ?: "No se pudo guardar la atención."
                                        }
                                } catch (e: Exception) {
                                    statusMessage = e.message ?: "Error al guardar la atención."
                                } finally {
                                    isSaving = false
                                }
                            }
                        },
                        enabled = !isSaving && patient != null,
                        colors = ButtonDefaults.buttonColors(containerColor = ClinicalEmerald),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Healing, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (isSaving) "Guardando…" else "Finalizar y Guardar en Recetas", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }

            if (statusMessage != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(statusMessage!!, color = Color(0xFFB91C1C), fontSize = 12.sp, modifier = Modifier.fillMaxWidth().background(Color(0xFFFEF2F2), RoundedCornerShape(6.dp)).padding(8.dp))
            }

            if (successMessage != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().background(Color(0xFFECFDF5), RoundedCornerShape(6.dp)).padding(8.dp)) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = ClinicalEmerald, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(successMessage!!, color = ClinicalEmerald, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = BorderLight)
            Spacer(modifier = Modifier.height(14.dp))

            // PLANTILLAS DINÁMICAS SEGÚN CATEGORÍA
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                when (category) {
                    "CONSULTA_GENERAL" -> {
                        Text("PLANTILLA: CONSULTA MÉDICA GENERAL (SOAP)", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = ClinicalEmerald)
                        OutlinedTextField(value = reason, onValueChange = { reason = it }, label = { Text("Motivo de consulta *") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp))
                        OutlinedTextField(value = anamnesis, onValueChange = { anamnesis = it }, label = { Text("Anamnesis / Historia clínica") }, modifier = Modifier.fillMaxWidth().height(90.dp), shape = RoundedCornerShape(8.dp))

                        Text("Constantes Fisiológicas", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Column(modifier = Modifier.weight(1f)) { OutlinedTextField(value = tempInput, onValueChange = { tempInput = it }, label = { Text("Temp (°C)") }) }
                            Column(modifier = Modifier.weight(1f)) { OutlinedTextField(value = fcInput, onValueChange = { fcInput = it }, label = { Text("FC (bpm)") }) }
                            Column(modifier = Modifier.weight(1f)) { OutlinedTextField(value = frInput, onValueChange = { frInput = it }, label = { Text("FR (rpm)") }) }
                            Column(modifier = Modifier.weight(1f)) { OutlinedTextField(value = weightInput, onValueChange = { weightInput = it }, label = { Text("Peso (kg)") }) }
                        }

                        Text("Exploración por Sistemas", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        val systems = listOf(
                            "Mucosas" to mucosas, "Ojos" to ojos, "Oídos" to oidos, "Nódulos" to nodulos,
                            "Piel" to piel, "Locomoción" to locomocion, "Músculo-Esq." to musculo,
                            "Nervioso" to nervioso, "Cardiovascular" to cardio, "Respiratorio" to resp,
                            "Digestivo" to digestivo, "Genitourinario" to genito
                        )
                        systems.chunked(2).forEach { pair ->
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                                pair.forEach { (lbl, valStr) ->
                                    Column(modifier = Modifier.weight(1f)) {
                                        OutlinedTextField(
                                            value = valStr,
                                            onValueChange = { nv ->
                                                when (lbl) {
                                                    "Mucosas" -> mucosas = nv
                                                    "Ojos" -> ojos = nv
                                                    "Oídos" -> oidos = nv
                                                    "Nódulos" -> nodulos = nv
                                                    "Piel" -> piel = nv
                                                    "Locomoción" -> locomocion = nv
                                                    "Músculo-Esq." -> musculo = nv
                                                    "Nervioso" -> nervioso = nv
                                                    "Cardiovascular" -> cardio = nv
                                                    "Respiratorio" -> resp = nv
                                                    "Digestivo" -> digestivo = nv
                                                    "Genitourinario" -> genito = nv
                                                }
                                            },
                                            label = { Text(lbl) },
                                            singleLine = true
                                        )
                                    }
                                }
                            }
                        }

                        Text("Diagnóstico & Tratamiento", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        OutlinedTextField(value = problemListText, onValueChange = { problemListText = it }, label = { Text("Lista de problemas") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp))
                        OutlinedTextField(value = defDiag, onValueChange = { defDiag = it }, label = { Text("Diagnóstico definitivo *") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp))
                        OutlinedTextField(value = treatPlan, onValueChange = { treatPlan = it }, label = { Text("Plan de tratamiento *") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp))
                        OutlinedTextField(value = observations, onValueChange = { observations = it }, label = { Text("Observaciones y seguimiento") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp))

                        Text("Receta Médica y Fármacos", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        OutlinedTextField(
                            value = prescriptionDurationDays,
                            onValueChange = { prescriptionDurationDays = it },
                            label = { Text("Duración general de la receta (días) *") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        )

                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Agregar medicamento a la receta:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextSecondary)
                        OutlinedTextField(value = currentDrugName, onValueChange = { currentDrugName = it }, label = { Text("Nombre del medicamento") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Column(modifier = Modifier.weight(1f)) { OutlinedTextField(value = currentDrugDose, onValueChange = { currentDrugDose = it }, label = { Text("Dosis") }) }
                            Column(modifier = Modifier.weight(1f)) { OutlinedTextField(value = currentDrugRoute, onValueChange = { currentDrugRoute = it }, label = { Text("Vía") }) }
                            Column(modifier = Modifier.weight(1f)) { OutlinedTextField(value = currentDrugFreq, onValueChange = { currentDrugFreq = it }, label = { Text("Frecuencia") }) }
                            Column(modifier = Modifier.weight(1f)) { OutlinedTextField(value = currentDrugDuration, onValueChange = { currentDrugDuration = it }, label = { Text("Duración (días)") }, singleLine = true) }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Button(
                            onClick = {
                                if (currentDrugName.isNotBlank()) {
                                    addedMedications.add(
                                        DraftMedication(
                                            name = currentDrugName.trim(),
                                            dose = currentDrugDose.trim(),
                                            route = currentDrugRoute.trim(),
                                            frequency = currentDrugFreq.trim(),
                                            durationDays = currentDrugDuration.trim().ifBlank { "7" }
                                        )
                                    )
                                    currentDrugName = ""
                                    currentDrugDose = ""
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(36.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = ClinicalEmerald),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text("➕ Agregar Medicamento a la Receta", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }

                        if (addedMedications.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Medicamentos en esta receta (${addedMedications.size}):", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            addedMedications.forEachIndexed { index, med ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().border(1.dp, BorderLight, RoundedCornerShape(6.dp)).padding(8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("${med.name} — ${med.dose} (${med.route}, ${med.frequency})", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                        Text("Duración específica: ${med.durationDays} días", fontSize = 11.sp, color = TextSecondary)
                                    }
                                    IconButton(onClick = { addedMedications.removeAt(index) }) {
                                        Icon(Icons.Default.Close, contentDescription = "Eliminar", tint = Color.Red, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }

                        val calcDays = prescriptionDurationDays.toIntOrNull() ?: 7
                        val calcExpiry = runCatching {
                            convertMillisToIsoDateString(System.currentTimeMillis() + calcDays.toLong() * 86400000L)
                        }.getOrDefault("N/A")
                        Text("Caducidad calculada de la receta en general: $calcExpiry ($calcDays días de vigencia)", fontSize = 12.sp, color = TextSecondary)
                    }

                    "CIRUGIA" -> {
                        Text("PLANTILLA: CIRUGÍA Y PROCEDIMIENTOS QUIRÚRGICOS", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = ClinicalEmerald)
                        OutlinedTextField(value = surgicalProcedure, onValueChange = { surgicalProcedure = it }, label = { Text("Procedimiento quirúrgico realizado *") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp))
                        OutlinedTextField(value = preOpNotes, onValueChange = { preOpNotes = it }, label = { Text("Evaluación y datos preoperatorios") }, modifier = Modifier.fillMaxWidth().height(80.dp), shape = RoundedCornerShape(8.dp))
                        OutlinedTextField(value = anesthesiaNotes, onValueChange = { anesthesiaNotes = it }, label = { Text("Protocolo anestésico y monitoreo") }, modifier = Modifier.fillMaxWidth().height(80.dp), shape = RoundedCornerShape(8.dp))
                        OutlinedTextField(value = surgicalFindings, onValueChange = { surgicalFindings = it }, label = { Text("Hallazgos quirúrgicos / Incidencias") }, modifier = Modifier.fillMaxWidth().height(90.dp), shape = RoundedCornerShape(8.dp))
                        OutlinedTextField(value = postOpCare, onValueChange = { postOpCare = it }, label = { Text("Cuidados postoperatorios y medicamentos") }, modifier = Modifier.fillMaxWidth().height(90.dp), shape = RoundedCornerShape(8.dp))
                    }

                    "VACUNACION" -> {
                        Text("PLANTILLA: MEDICINA PREVENTIVA Y VACUNACIÓN", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = ClinicalEmerald)
                        OutlinedTextField(value = vaccineName, onValueChange = { vaccineName = it }, label = { Text("Vacuna aplicada *") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.weight(1f)) { OutlinedTextField(value = vaccineBatch, onValueChange = { vaccineBatch = it }, label = { Text("Número de Lote") }) }
                            Column(modifier = Modifier.weight(1f)) { OutlinedTextField(value = vaccineDose, onValueChange = { vaccineDose = it }, label = { Text("Dosis / Vía") }) }
                        }
                        OutlinedTextField(value = adverseReactions, onValueChange = { adverseReactions = it }, label = { Text("Reacciones adversas observadas") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp))
                        OutlinedTextField(value = nextDate, onValueChange = { nextDate = it }, label = { Text("Próxima aplicación / Refuerzo (AAAA-MM-DD)") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp))
                    }

                    "ESTETICA" -> {
                        Text("PLANTILLA: ESTÉTICA Y GROOMING", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = ClinicalEmerald)
                        OutlinedTextField(value = serviceName, onValueChange = {}, label = { Text("Servicio de Estética") }, enabled = false, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp))
                        OutlinedTextField(value = initialCondition, onValueChange = { initialCondition = it }, label = { Text("Condición inicial del pelaje y piel") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp))
                        OutlinedTextField(value = finalCondition, onValueChange = { finalCondition = it }, label = { Text("Condición final y acabado") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp))
                        OutlinedTextField(value = productsUsed, onValueChange = { productsUsed = it }, label = { Text("Productos y champús utilizados") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp))
                        OutlinedTextField(value = groomingIncidents, onValueChange = { groomingIncidents = it }, label = { Text("Incidencias u observaciones (ej. nudos severos)") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp))
                    }

                    "DIAGNOSTICO" -> {
                        Text("PLANTILLA: DIAGNÓSTICO E IMAGENOLOGÍA", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = ClinicalEmerald)
                        OutlinedTextField(value = studyName, onValueChange = { studyName = it }, label = { Text("Nombre del estudio (ej. Ecografía abdominal)") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp))
                        OutlinedTextField(value = studyResultsNotes, onValueChange = { studyResultsNotes = it }, label = { Text("Resultados e interpretación profesional *") }, modifier = Modifier.fillMaxWidth().height(140.dp), shape = RoundedCornerShape(8.dp))
                    }

                    "HOSPITALIZACION" -> {
                        Text("PLANTILLA: HOSPITALIZACIÓN Y MONITOREO", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = ClinicalEmerald)
                        OutlinedTextField(value = admissionReason, onValueChange = { admissionReason = it }, label = { Text("Motivo de ingreso *") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp))
                        OutlinedTextField(value = dailyEvolutionNotes, onValueChange = { dailyEvolutionNotes = it }, label = { Text("Notas de evolución diaria y constantes") }, modifier = Modifier.fillMaxWidth().height(120.dp), shape = RoundedCornerShape(8.dp))
                        OutlinedTextField(value = dischargeSummary, onValueChange = { dischargeSummary = it }, label = { Text("Resumen de alta y pronóstico") }, modifier = Modifier.fillMaxWidth().height(90.dp), shape = RoundedCornerShape(8.dp))
                    }

                    else -> { // ADMINISTRATIVA / CUSTOM
                        Text("PLANTILLA: ATENCIÓN GENERAL / ADMINISTRATIVA", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = ClinicalEmerald)
                        OutlinedTextField(value = reason, onValueChange = { reason = it }, label = { Text("Motivo o trámite realizado *") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp))
                        OutlinedTextField(value = observations, onValueChange = { observations = it }, label = { Text("Observaciones y detalles") }, modifier = Modifier.fillMaxWidth().height(120.dp), shape = RoundedCornerShape(8.dp))
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = BorderLight)
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().clickable { scheduleNextAppt = !scheduleNextAppt }
                ) {
                    Checkbox(checked = scheduleNextAppt, onCheckedChange = { scheduleNextAppt = it })
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Agendar Próxima Cita para la Mascota", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = ClinicalEmerald)
                }

                if (scheduleNextAppt) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(OffWhiteBg, RoundedCornerShape(8.dp))
                            .border(1.dp, BorderLight, RoundedCornerShape(8.dp))
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text("Selecciona el servicio y fecha/hora para la nueva cita:", fontSize = 12.sp, color = TextSecondary)
                        Box(Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = clinicServices.find { it.id == nextServiceId }?.name ?: "Seleccionar servicio",
                                onValueChange = {},
                                readOnly = true,
                                enabled = clinicServices.isNotEmpty(),
                                singleLine = true,
                                label = { Text("Servicio de la Cita") },
                                trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null) },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Box(Modifier.matchParentSize().clickable(enabled = clinicServices.isNotEmpty()) { serviceMenuExpanded = true })
                            DropdownMenu(expanded = serviceMenuExpanded, onDismissRequest = { serviceMenuExpanded = false }) {
                                clinicServices.forEach { s ->
                                    DropdownMenuItem(
                                        text = { Text("${s.name} (${s.durationMinutes} min)") },
                                        onClick = {
                                            nextServiceId = s.id
                                            serviceMenuExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.weight(1.5f)) {
                                OutlinedTextField(
                                    value = nextApptDate,
                                    onValueChange = { nextApptDate = it },
                                    label = { Text("Fecha (AAAA-MM-DD)") },
                                    placeholder = { Text("ej. 2026-12-01") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                OutlinedTextField(
                                    value = nextApptTime,
                                    onValueChange = { nextApptTime = it },
                                    label = { Text("Hora (Formato 24h, ej. 14:30)") },
                                    placeholder = { Text("14:30") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                        Text("Usa formato de 24 horas (00:00 a 23:59)", fontSize = 11.sp, color = TextSecondary)
                    }
                }
            }
        }
    }
}

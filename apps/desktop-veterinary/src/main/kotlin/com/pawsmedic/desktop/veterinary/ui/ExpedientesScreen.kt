package com.pawsmedic.desktop.veterinary.ui

import com.pawsmedic.desktop.admin.translation.translateBatchLocally
import com.pawsmedic.desktop.admin.ui.LocalDesktopAppearance
import com.pawsmedic.desktop.veterinary.supabase
import com.pawsmedic.desktop.veterinary.ui.theme.*
import com.pawsmedic.shared.core.model.ProfileRole
import com.pawsmedic.shared.core.model.UserProfile
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
import androidx.compose.ui.window.Dialog
import java.util.UUID

@Serializable
private data class ClinicalConsultation(
    val id: String,
    @SerialName("tenant_id") val tenantId: String,
    @SerialName("pet_id") val petId: String,
    @SerialName("veterinarian_id") val veterinarianId: String,
    val reason: String,
    val observations: String,
    val diagnosis: String,
    val plan: String,
    val status: String,
    @SerialName("occurred_at") val occurredAt: String
)

@Serializable
private data class MedicalRecord(
    val id: String,
    @SerialName("tenant_id") val tenantId: String,
    @SerialName("pet_id") val petId: String,
    val title: String,
    val content: String,
    @SerialName("appointment_id") val appointmentId: String? = null,
    @SerialName("veterinarian_id") val veterinarianId: String? = null,
    @SerialName("weight_grams") val weightGrams: Int? = null,
    @SerialName("record_date") val recordDate: String? = null,
    @SerialName("record_time") val recordTime: String? = null,
    @SerialName("consultation_reason") val consultationReason: String? = null,
    val symptoms: String? = null,
    val diagnosis: String? = null,
    val treatment: String? = null,
    val medication: String? = null,
    val dosage: String? = null,
    val observations: String? = null,
    @SerialName("next_review_date") val nextReviewDate: String? = null,
    @SerialName("created_at") val createdAt: String
)

@Serializable
private data class NewClinicalConsultation(
    @SerialName("tenant_id") val tenantId: String,
    @SerialName("pet_id") val petId: String,
    @SerialName("veterinarian_id") val veterinarianId: String,
    val reason: String,
    val observations: String,
    val diagnosis: String,
    val plan: String,
    @SerialName("idempotency_key") val idempotencyKey: String
)

@Composable
fun ExpedientesScreen(
    userEmail: String = "",
    onNavigate: (String) -> Unit,
    onLogout: () -> Unit = {}
) {
    val english = LocalDesktopAppearance.current.english
    fun tr(spanish: String, translated: String) = if (english) translated else spanish
    var consultations by remember { mutableStateOf<List<ClinicalConsultation>>(emptyList()) }
    var medicalRecords by remember { mutableStateOf<List<MedicalRecord>>(emptyList()) }
    var tenantId by remember { mutableStateOf<String?>(null) }
    var veterinarianId by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var isSaving by remember { mutableStateOf(false) }
    var showConsultationDialog by remember { mutableStateOf(false) }
    var selectedRecord by remember { mutableStateOf<MedicalRecord?>(null) }
    var selectedConsultation by remember { mutableStateOf<ClinicalConsultation?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var petIdInput by remember { mutableStateOf("") }
    var reason by remember { mutableStateOf("") }
    var observations by remember { mutableStateOf("") }
    var diagnosis by remember { mutableStateOf("") }
    var plan by remember { mutableStateOf("") }
    val translations = remember(userEmail) { mutableStateMapOf<String, String>() }
    var showOriginal by remember { mutableStateOf(false) }
    var translationError by remember { mutableStateOf(false) }
    var translating by remember { mutableStateOf(false) }
    var translationAttempt by remember { mutableIntStateOf(0) }
    val coroutineScope = rememberCoroutineScope()

    val textsToTranslate = remember(consultations, medicalRecords, selectedRecord, selectedConsultation) {
        consultations.flatMap { listOf(it.reason, it.observations, it.diagnosis, it.plan) } +
            medicalRecords.flatMap { listOf(it.title, it.content) } +
            listOfNotNull(selectedRecord?.consultationReason, selectedRecord?.symptoms,
                selectedRecord?.diagnosis, selectedRecord?.treatment, selectedRecord?.medication,
                selectedRecord?.dosage, selectedRecord?.observations)
    }
    LaunchedEffect(english, textsToTranslate, translationAttempt) {
        if (!english) {
            translating = false
            translationError = false
            return@LaunchedEffect
        }
        val missing = textsToTranslate.filter { it.isNotBlank() && !translations.containsKey(it) }.distinct()
        val pending = missing.filter { it.length <= 4000 }
        translationError = pending.size != missing.size
        if (pending.isNotEmpty()) {
            translating = true
            try {
                for (batch in pending.chunked(50)) {
                    val results = translateBatchLocally(batch)
                    batch.zip(results).forEach { (original, translated) -> translations[original] = translated }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                translationError = true
            } finally {
                translating = false
            }
        }
    }
    fun displayText(value: String): String =
        if (english && !showOriginal) translations[value] ?: value else value
    fun statusText(value: String): String = when (value) {
        "DRAFT" -> tr("Borrador", "Draft")
        "PUBLISHED" -> tr("Publicado", "Published")
        "AMENDED" -> tr("Modificado", "Amended")
        else -> value
    }

    suspend fun loadRecords() {
        isLoading = true
        errorMessage = null
        try {
            val userId = supabase.auth.currentSessionOrNull()?.user?.id
                ?: error(tr("No hay una sesión de Supabase activa. Inicia sesión nuevamente.", "There is no active Supabase session. Please sign in again."))
            val profile = supabase.postgrest["profiles"].select {
                filter { eq("id", userId) }
            }.decodeSingle<UserProfile>()
            if (profile.role != ProfileRole.VETERINARY_BUSINESS) error(tr("Los expedientes solo están disponibles para cuentas veterinarias.", "Records are only available to veterinary accounts."))
            val clinicId = com.pawsmedic.desktop.veterinary.model.resolveTenantId(supabase, userId, profile)
            tenantId = clinicId
            veterinarianId = userId
            consultations = supabase.postgrest["clinical_consultations"].select {
                filter { eq("tenant_id", clinicId) }
            }.decodeList<ClinicalConsultation>().sortedByDescending { it.occurredAt }
            medicalRecords = supabase.postgrest["medical_records"].select {
                filter { eq("tenant_id", clinicId) }
            }.decodeList<MedicalRecord>().sortedByDescending { it.createdAt }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            System.err.println("No se pudieron cargar los expedientes: ${e.javaClass.simpleName}")
            errorMessage = tr("No se pudieron cargar los expedientes. Comprueba tu sesión, los permisos y la conexión.", "Could not load records. Check your session, permissions, and connection.")
        } finally {
            isLoading = false
        }
    }

    suspend fun createConsultation() {
        val clinicId = tenantId ?: run { errorMessage = tr("No hay una clínica autorizada.", "No clinic is authorized."); return }
        val userId = veterinarianId ?: run { errorMessage = tr("No se encontró el veterinario de la sesión.", "No veterinarian was found for this session."); return }
        val petId = petIdInput.trim()
        if (runCatching { UUID.fromString(petId) }.isFailure) { errorMessage = tr("Ingresa un ID de mascota UUID válido.", "Enter a valid pet UUID."); return }
        if (reason.isBlank() || observations.isBlank()) { errorMessage = tr("El motivo y las observaciones son obligatorios.", "Reason and observations are required."); return }
        isSaving = true
        errorMessage = null
        try {
            val accessibleRecords = supabase.postgrest["medical_records"].select {
                filter {
                    eq("tenant_id", clinicId)
                    eq("pet_id", petId)
                }
            }.decodeList<MedicalRecord>()
            if (accessibleRecords.isEmpty()) {
                errorMessage = tr("No se puede crear la consulta: la mascota necesita un expediente existente visible para esta clínica.", "Cannot create the consultation: the pet needs an existing record visible to this clinic.")
                return
            }
            supabase.postgrest["clinical_consultations"].insert(
                NewClinicalConsultation(
                    tenantId = clinicId,
                    petId = petId,
                    veterinarianId = userId,
                    reason = reason.trim(),
                    observations = observations.trim(),
                    diagnosis = diagnosis.trim(),
                    plan = plan.trim(),
                    idempotencyKey = UUID.randomUUID().toString()
                )
            )
            showConsultationDialog = false
            petIdInput = ""
            reason = ""
            observations = ""
            diagnosis = ""
            plan = ""
            loadRecords()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            System.err.println("No se pudo guardar la consulta: ${e.javaClass.simpleName}")
            errorMessage = tr("No se pudo guardar la consulta. Comprueba los permisos y la conexión.", "Could not save the consultation. Check permissions and your connection.")
        } finally {
            isSaving = false
        }
    }

    LaunchedEffect(Unit) { loadRecords() }

    VetAppLayout(
        currentScreen = "Expedientes",
        onNavigate = onNavigate,
        onLogout = onLogout,
        title = tr("Expedientes Clínicos", "Clinical records"),
        userEmail = userEmail
    ) {
        Column(Modifier.fillMaxSize().clip(RoundedCornerShape(12.dp)).background(CardWhite).border(1.dp, BorderLight, RoundedCornerShape(12.dp)).padding(20.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(tr("Consultas y registros de la clínica", "Clinic consultations and records"), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text(tr("Información leída de clinical_consultations y medical_records según las políticas RLS vigentes.", "Data from clinical_consultations and medical_records, subject to current RLS policies."), fontSize = 12.sp, color = TextSecondary)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = { coroutineScope.launch { loadRecords() } }, enabled = !isLoading) { Text(tr("Actualizar", "Refresh")) }
                    Button(onClick = { errorMessage = null; showConsultationDialog = true }, enabled = tenantId != null && !isLoading, colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen)) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp)); Spacer(Modifier.width(5.dp)); Text(tr("Nueva consulta", "New consultation"))
                    }
                }
            }
            if (english) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Machine translation may be inaccurate. Verify the original before clinical decisions.", color = TextSecondary, fontSize = 12.sp, modifier = Modifier.weight(1f))
                    TextButton(onClick = { showOriginal = !showOriginal }) { Text(if (showOriginal) "Show translation" else "Show original") }
                }
                Box(Modifier.fillMaxWidth().height(40.dp), contentAlignment = Alignment.CenterStart) {
                    if (translationError) Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Some text could not be translated; the original is shown.", color = Color(0xFFB91C1C), fontSize = 12.sp)
                        TextButton(onClick = { translationAttempt++ }, enabled = !translating) { Text("Retry") }
                    } else if (translating) Text("Translating…", color = TextSecondary, fontSize = 12.sp)
                }
            }
            errorMessage?.let {
                Spacer(Modifier.height(10.dp))
                Text(it, color = Color(0xFFB91C1C), fontSize = 13.sp, modifier = Modifier.fillMaxWidth().background(Color(0xFFFEF2F2), RoundedCornerShape(6.dp)).padding(10.dp))
            }
            Spacer(Modifier.height(16.dp))
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxSize()) {
                if (isLoading) item { Text(tr("Cargando expedientes…", "Loading records…"), color = TextSecondary) }
                if (!isLoading && consultations.isEmpty() && medicalRecords.isEmpty() && errorMessage == null) item {
                    Text(tr("No hay registros clínicos visibles para esta clínica. La creación de consultas requiere acceso RLS válido a un expediente previo de la mascota.", "There are no clinical records visible to this clinic. Creating a consultation requires RLS access to a previous record for the pet."), color = TextSecondary, fontSize = 13.sp)
                }
                if (consultations.isNotEmpty()) item {
                    Text(tr("CONSULTAS CLÍNICAS", "CLINICAL CONSULTATIONS"), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                }
                items(consultations, key = { "consultation-${it.id}" }) { consultation ->
                    Column(Modifier.fillMaxWidth().border(1.dp, BorderLight, RoundedCornerShape(8.dp))
                        .clickable { selectedConsultation = consultation }.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(displayText(consultation.reason), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            Text("${statusText(consultation.status)} · ${consultation.occurredAt}", fontSize = 11.sp, color = TextMuted)
                        }
                        Text("${tr("Mascota ID", "Pet ID")}: ${consultation.petId}", fontSize = 11.sp, color = TextSecondary)
                        Text("${tr("Observaciones", "Observations")}: ${displayText(consultation.observations)}", fontSize = 12.sp, color = TextPrimary)
                        if (consultation.diagnosis.isNotBlank()) Text("${tr("Diagnóstico", "Diagnosis")}: ${displayText(consultation.diagnosis)}", fontSize = 12.sp, color = TextSecondary)
                        if (consultation.plan.isNotBlank()) Text("${tr("Plan", "Plan")}: ${displayText(consultation.plan)}", fontSize = 12.sp, color = TextSecondary)
                    }
                }
                if (medicalRecords.isNotEmpty()) item {
                    Spacer(Modifier.height(4.dp))
                    Text(tr("EXPEDIENTES MÉDICOS EXISTENTES (SOLO LECTURA)", "EXISTING MEDICAL RECORDS (READ ONLY)"), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                }
                items(medicalRecords, key = { "record-${it.id}" }) { record ->
                    Column(Modifier.fillMaxWidth().border(1.dp, BorderLight, RoundedCornerShape(8.dp))
                        .clickable { selectedRecord = record }.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(displayText(record.title), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text("${tr("Mascota ID", "Pet ID")}: ${record.petId} · ${record.createdAt}", fontSize = 11.sp, color = TextMuted)
                        Text(displayText(record.content), fontSize = 12.sp, color = TextSecondary)
                    }
                }
            }
        }

        selectedRecord?.let { record ->
            Dialog(onDismissRequest = { selectedRecord = null }) {
                Card(Modifier.width(640.dp).heightIn(max = 780.dp), shape = RoundedCornerShape(16.dp)) {
                    Column(Modifier.padding(24.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(tr("Expediente médico", "Medical record"), fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        if (english) {
                            Text("Machine translation may be inaccurate. Verify the original before clinical decisions.", color = TextSecondary, fontSize = 12.sp)
                            TextButton(onClick = { showOriginal = !showOriginal }) { Text(if (showOriginal) "Show translation" else "Show original") }
                            if (translating) Text("Translating…", color = TextSecondary, fontSize = 12.sp)
                            if (translationError) {
                                Text("Some text could not be translated; the original is shown.", color = Color(0xFFB91C1C), fontSize = 12.sp)
                                TextButton(onClick = { translationAttempt++ }) { Text("Retry") }
                            }
                        }
                        listOf(
                            "Servicio" to record.title, "Mascota ID" to record.petId,
                            "Cita ID" to record.appointmentId, "Veterinario ID" to record.veterinarianId,
                            "Fecha" to record.recordDate, "Hora" to record.recordTime,
                            "Peso" to record.weightGrams?.let { "$it g" },
                            "Motivo" to record.consultationReason, "Síntomas" to record.symptoms,
                            "Diagnóstico" to record.diagnosis, "Tratamiento" to record.treatment,
                            "Medicamento" to record.medication, "Dosis" to record.dosage,
                            "Observaciones" to record.observations, "Próxima revisión" to record.nextReviewDate,
                            "Detalle" to record.content
                        ).forEach { (label, value) ->
                            if (!value.isNullOrBlank()) {
                                val englishLabel = when (label) {
                                    "Servicio" -> "Service"
                                    "Mascota ID" -> "Pet ID"
                                    "Cita ID" -> "Appointment ID"
                                    "Veterinario ID" -> "Veterinarian ID"
                                    "Fecha" -> "Date"
                                    "Hora" -> "Time"
                                    "Peso" -> "Weight"
                                    "Motivo" -> "Reason"
                                    "Síntomas" -> "Symptoms"
                                    "Diagnóstico" -> "Diagnosis"
                                    "Tratamiento" -> "Treatment"
                                    "Medicamento" -> "Medication"
                                    "Dosis" -> "Dosage"
                                    "Observaciones" -> "Observations"
                                    "Próxima revisión" -> "Next review"
                                    "Detalle" -> "Details"
                                    else -> label
                                }
                                Text(tr(label, englishLabel), fontWeight = FontWeight.Bold, color = TextPrimary)
                                val translatable = label in setOf("Servicio", "Motivo", "Síntomas", "Diagnóstico",
                                    "Tratamiento", "Medicamento", "Dosis", "Observaciones", "Detalle")
                                Text(if (translatable) displayText(value) else value, color = TextSecondary)
                            }
                        }
                        TextButton(onClick = { selectedRecord = null }, modifier = Modifier.align(Alignment.End)) { Text(tr("Cerrar", "Close")) }
                    }
                }
            }
        }
        selectedConsultation?.let { consultation ->
            AlertDialog(
                onDismissRequest = { selectedConsultation = null },
                title = { Text(tr("Consulta clínica", "Clinical consultation")) },
                text = {
                    Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (english) {
                            Text("Machine translation may be inaccurate. Verify the original before clinical decisions.", color = TextSecondary, fontSize = 12.sp)
                            TextButton(onClick = { showOriginal = !showOriginal }) { Text(if (showOriginal) "Show translation" else "Show original") }
                            if (translating) Text("Translating…", color = TextSecondary, fontSize = 12.sp)
                            if (translationError) {
                                Text("Some text could not be translated; the original is shown.", color = Color(0xFFB91C1C), fontSize = 12.sp)
                                TextButton(onClick = { translationAttempt++ }) { Text("Retry") }
                            }
                        }
                        Text("${tr("Mascota ID", "Pet ID")}: ${consultation.petId}")
                        Text("${tr("Veterinario ID", "Veterinarian ID")}: ${consultation.veterinarianId}")
                        Text("${tr("Fecha", "Date")}: ${consultation.occurredAt}")
                        Text("${tr("Estado", "Status")}: ${statusText(consultation.status)}")
                        Text("${tr("Motivo", "Reason")}: ${displayText(consultation.reason)}")
                        Text("${tr("Observaciones", "Observations")}: ${displayText(consultation.observations)}")
                        Text("${tr("Diagnóstico", "Diagnosis")}: ${displayText(consultation.diagnosis)}")
                        Text("${tr("Tratamiento", "Treatment")}: ${displayText(consultation.plan)}")
                    }
                },
                confirmButton = { TextButton(onClick = { selectedConsultation = null }) { Text(tr("Cerrar", "Close")) } }
            )
        }

        if (showConsultationDialog) Dialog(onDismissRequest = { if (!isSaving) showConsultationDialog = false }) {
            Card(Modifier.width(540.dp).wrapContentHeight(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = CardWhite)) {
                Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text(tr("Registrar consulta clínica", "Create clinical consultation"), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        IconButton(onClick = { if (!isSaving) showConsultationDialog = false }) { Icon(Icons.Default.Close, contentDescription = tr("Cerrar", "Close")) }
                    }
                    Text(tr("Se guarda en clinical_consultations como borrador. Para respetar RLS, solo se permite continuar si la mascota ya tiene un expediente medical_records visible en esta clínica.", "A draft is saved in clinical_consultations. RLS requires an existing medical_records entry for the pet that is visible to this clinic."), fontSize = 12.sp, color = TextSecondary)
                    OutlinedTextField(petIdInput, { petIdInput = it }, label = { Text(tr("ID de mascota (UUID)", "Pet ID (UUID)")) }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(reason, { reason = it }, label = { Text(tr("Motivo de consulta", "Reason for consultation")) }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(observations, { observations = it }, label = { Text(tr("Observaciones clínicas", "Clinical observations")) }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(diagnosis, { diagnosis = it }, label = { Text(tr("Diagnóstico", "Diagnosis")) }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(plan, { plan = it }, label = { Text(tr("Plan de tratamiento", "Treatment plan")) }, modifier = Modifier.fillMaxWidth())
                    errorMessage?.let { Text(it, color = Color(0xFFB91C1C), fontSize = 12.sp) }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedButton(onClick = { showConsultationDialog = false }, enabled = !isSaving, modifier = Modifier.weight(1f)) { Text(tr("Cancelar", "Cancel")) }
                        Button(onClick = { coroutineScope.launch { createConsultation() } }, enabled = !isSaving, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = ClinicalEmerald)) { Text(if (isSaving) tr("Guardando…", "Saving…") else tr("Guardar borrador", "Save draft"), color = Color.White) }
                    }
                }
            }
        }
    }
}

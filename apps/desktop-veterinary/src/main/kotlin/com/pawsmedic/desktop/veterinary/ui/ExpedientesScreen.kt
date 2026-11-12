package com.pawsmedic.desktop.veterinary.ui

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
    var consultations by remember { mutableStateOf<List<ClinicalConsultation>>(emptyList()) }
    var medicalRecords by remember { mutableStateOf<List<MedicalRecord>>(emptyList()) }
    var tenantId by remember { mutableStateOf<String?>(null) }
    var veterinarianId by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var isSaving by remember { mutableStateOf(false) }
    var showConsultationDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var petIdInput by remember { mutableStateOf("") }
    var reason by remember { mutableStateOf("") }
    var observations by remember { mutableStateOf("") }
    var diagnosis by remember { mutableStateOf("") }
    var plan by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()

    suspend fun loadRecords() {
        isLoading = true
        errorMessage = null
        try {
            val userId = supabase.auth.currentSessionOrNull()?.user?.id
                ?: error("No hay una sesión de Supabase activa. Inicia sesión nuevamente.")
            val profile = supabase.postgrest["profiles"].select {
                filter { eq("id", userId) }
            }.decodeSingle<UserProfile>()
            if (profile.role != ProfileRole.VETERINARY_BUSINESS) error("Los expedientes solo están disponibles para cuentas veterinarias.")
            val clinicId = profile.tenantId ?: error("El perfil no tiene una clínica asignada.")
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
            errorMessage = e.message ?: "No se pudieron cargar los expedientes desde Supabase."
        } finally {
            isLoading = false
        }
    }

    suspend fun createConsultation() {
        val clinicId = tenantId ?: run { errorMessage = "No hay una clínica autorizada."; return }
        val userId = veterinarianId ?: run { errorMessage = "No se encontró el veterinario de la sesión."; return }
        val petId = petIdInput.trim()
        if (runCatching { UUID.fromString(petId) }.isFailure) { errorMessage = "Ingresa un ID de mascota UUID válido."; return }
        if (reason.isBlank() || observations.isBlank()) { errorMessage = "El motivo y las observaciones son obligatorios."; return }
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
                error("No se puede crear la consulta: la política RLS solo permite leer mascotas con un expediente médico existente de esta clínica. La tabla medical_records tampoco tiene una política INSERT en las migraciones actuales.")
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
            errorMessage = e.message ?: "No se pudo guardar la consulta clínica en Supabase."
        } finally {
            isSaving = false
        }
    }

    LaunchedEffect(Unit) { loadRecords() }

    VetAppLayout(
        currentScreen = "Expedientes",
        onNavigate = onNavigate,
        onLogout = onLogout,
        title = "Expedientes Clínicos",
        userEmail = userEmail
    ) {
        Column(Modifier.fillMaxSize().clip(RoundedCornerShape(12.dp)).background(Color.White).border(1.dp, BorderLight, RoundedCornerShape(12.dp)).padding(20.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text("Consultas y registros de la clínica", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text("Información leída de clinical_consultations y medical_records según las políticas RLS vigentes.", fontSize = 12.sp, color = TextSecondary)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = { coroutineScope.launch { loadRecords() } }, enabled = !isLoading) { Text("Actualizar") }
                    Button(onClick = { showConsultationDialog = true }, enabled = tenantId != null && !isLoading, colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen)) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp)); Spacer(Modifier.width(5.dp)); Text("Nueva consulta")
                    }
                }
            }
            errorMessage?.let {
                Spacer(Modifier.height(10.dp))
                Text(it, color = Color(0xFFB91C1C), fontSize = 13.sp, modifier = Modifier.fillMaxWidth().background(Color(0xFFFEF2F2), RoundedCornerShape(6.dp)).padding(10.dp))
            }
            Spacer(Modifier.height(16.dp))
            if (isLoading) Text("Cargando expedientes…", color = TextSecondary)
            if (!isLoading && consultations.isEmpty() && medicalRecords.isEmpty() && errorMessage == null) Text("No hay registros clínicos visibles para esta clínica. La creación de consultas requiere acceso RLS válido a un expediente previo de la mascota.", color = TextSecondary, fontSize = 13.sp)
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxSize()) {
                if (consultations.isNotEmpty()) item {
                    Text("CONSULTAS CLÍNICAS", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                }
                items(consultations, key = { "consultation-${it.id}" }) { consultation ->
                    Column(Modifier.fillMaxWidth().border(1.dp, BorderLight, RoundedCornerShape(8.dp)).padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(consultation.reason, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            Text("${consultation.status} · ${consultation.occurredAt}", fontSize = 11.sp, color = TextMuted)
                        }
                        Text("Mascota ID: ${consultation.petId}", fontSize = 11.sp, color = TextSecondary)
                        Text("Observaciones: ${consultation.observations}", fontSize = 12.sp, color = TextPrimary)
                        if (consultation.diagnosis.isNotBlank()) Text("Diagnóstico: ${consultation.diagnosis}", fontSize = 12.sp, color = TextSecondary)
                        if (consultation.plan.isNotBlank()) Text("Plan: ${consultation.plan}", fontSize = 12.sp, color = TextSecondary)
                    }
                }
                if (medicalRecords.isNotEmpty()) item {
                    Spacer(Modifier.height(4.dp))
                    Text("EXPEDIENTES MÉDICOS EXISTENTES (SOLO LECTURA)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                }
                items(medicalRecords, key = { "record-${it.id}" }) { record ->
                    Column(Modifier.fillMaxWidth().border(1.dp, BorderLight, RoundedCornerShape(8.dp)).padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(record.title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text("Mascota ID: ${record.petId} · ${record.createdAt}", fontSize = 11.sp, color = TextMuted)
                        Text(record.content, fontSize = 12.sp, color = TextSecondary)
                    }
                }
            }
        }

        if (showConsultationDialog) Dialog(onDismissRequest = { if (!isSaving) showConsultationDialog = false }) {
            Card(Modifier.width(540.dp).wrapContentHeight(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Registrar consulta clínica", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        IconButton(onClick = { if (!isSaving) showConsultationDialog = false }) { Icon(Icons.Default.Close, contentDescription = "Cerrar") }
                    }
                    Text("Se guarda en clinical_consultations como borrador. Para respetar RLS, solo se permite continuar si la mascota ya tiene un expediente medical_records visible en esta clínica.", fontSize = 12.sp, color = TextSecondary)
                    OutlinedTextField(petIdInput, { petIdInput = it }, label = { Text("ID de mascota (UUID)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(reason, { reason = it }, label = { Text("Motivo de consulta") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(observations, { observations = it }, label = { Text("Observaciones clínicas") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(diagnosis, { diagnosis = it }, label = { Text("Diagnóstico") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(plan, { plan = it }, label = { Text("Plan de tratamiento") }, modifier = Modifier.fillMaxWidth())
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedButton(onClick = { showConsultationDialog = false }, enabled = !isSaving, modifier = Modifier.weight(1f)) { Text("Cancelar") }
                        Button(onClick = { coroutineScope.launch { createConsultation() } }, enabled = !isSaving, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = ClinicalEmerald)) { Text(if (isSaving) "Guardando…" else "Guardar borrador", color = Color.White) }
                    }
                }
            }
        }
    }
}

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
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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

@Serializable
private data class AgendaAppointment(
    val id: String,
    @SerialName("pet_id") val petId: String,
    @SerialName("pet_name") val petName: String,
    @SerialName("service_id") val serviceId: String,
    @SerialName("service_name") val serviceName: String,
    @SerialName("starts_at") val startsAt: String,
    @SerialName("ends_at") val endsAt: String,
    @SerialName("display_time") val displayTime: String,
    val status: String
)

@Composable
fun AgendaScreen(
    userEmail: String = "",
    onNavigate: (String) -> Unit,
    onLogout: () -> Unit = {}
) {
    var appointments by remember { mutableStateOf<List<AgendaAppointment>>(emptyList()) }
    var missingRecords by remember { mutableStateOf<Set<String>>(emptySet()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var busyAppointmentId by remember { mutableStateOf<String?>(null) }
    var completing by remember { mutableStateOf<AgendaAppointment?>(null) }
    var reason by remember { mutableStateOf("") }
    var symptoms by remember { mutableStateOf("") }
    var diagnosis by remember { mutableStateOf("") }
    var treatment by remember { mutableStateOf("") }
    var observations by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }
    var medication by remember { mutableStateOf("") }
    var dosage by remember { mutableStateOf("") }
    var formError by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

    suspend fun loadAppointments() {
        isLoading = true
        errorMessage = null
        try {
            val userId = supabase.auth.currentSessionOrNull()?.user?.id
                ?: error("No hay una sesión de Supabase activa. Inicia sesión nuevamente.")
            val profile = supabase.postgrest["profiles"].select {
                filter { eq("id", userId) }
            }.decodeSingle<UserProfile>()
            if (profile.role != ProfileRole.VETERINARY_BUSINESS) {
                error("La agenda solo está disponible para cuentas veterinarias.")
            }
            val tenantId = com.pawsmedic.desktop.veterinary.model.resolveTenantId(supabase, userId, profile)
            val loadedAppointments = supabase.postgrest.rpc(
                function = "get_tenant_appointments",
                parameters = buildJsonObject {
                    put("p_tenant_id", tenantId)
                }
            ).decodeList<AgendaAppointment>()
            val missing = supabase.postgrest.rpc(
                function = "get_completed_appointments_missing_records",
                parameters = buildJsonObject { put("p_tenant_id", tenantId) }
            ).decodeList<String>()
            appointments = loadedAppointments
            missingRecords = missing.toSet()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            errorMessage = if (e is IllegalStateException) e.message else
                "No se pudo cargar la agenda. Verifica la conexión y que el script de consultas esté instalado."
        } finally {
            isLoading = false
        }
    }

    suspend fun transition(appointment: AgendaAppointment, nextStatus: String) {
        busyAppointmentId = appointment.id
        errorMessage = null
        try {
            supabase.postgrest.rpc(
                function = "transition_appointment_status",
                parameters = buildJsonObject {
                    put("p_appointment_id", appointment.id)
                    put("p_expected_status", appointment.status)
                    put("p_next_status", nextStatus)
                }
            )
            loadAppointments()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            errorMessage = "No se pudo actualizar la cita. Verifica la conexión y la configuración del flujo de consultas."
        } finally {
            busyAppointmentId = null
        }
    }

    suspend fun finish(appointment: AgendaAppointment) {
            val grams = weight.trim().toIntOrNull()
            if (listOf(reason, symptoms, diagnosis, treatment, observations).any { it.isBlank() } || grams == null || grams <= 0) {
                formError = "Completa motivo, síntomas, diagnóstico, tratamiento, observaciones y peso en gramos."
                return
            }
            busyAppointmentId = appointment.id
            formError = null
            try {
                supabase.postgrest.rpc("finish_appointment_with_record", buildJsonObject {
                    put("p_appointment_id", appointment.id)
                    put("p_reason", reason.trim())
                    put("p_symptoms", symptoms.trim())
                    put("p_diagnosis", diagnosis.trim())
                    put("p_treatment", treatment.trim())
                    put("p_observations", observations.trim())
                    put("p_weight_grams", grams)
                    put("p_medication", medication.trim())
                    put("p_dosage", dosage.trim())
                })
                completing = null
                loadAppointments()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                formError = "No se pudo guardar el expediente. Verifica la conexión y que el script de consultas esté instalado."
            } finally {
                busyAppointmentId = null
            }
    }

    fun openRecord(appointment: AgendaAppointment) {
            reason = appointment.serviceName
            symptoms = ""; diagnosis = ""; treatment = ""; observations = ""
            weight = ""; medication = ""; dosage = ""; formError = null
            completing = appointment
    }

    LaunchedEffect(Unit) { loadAppointments() }

    VetAppLayout(
        currentScreen = "Agenda",
        onNavigate = onNavigate,
        onLogout = onLogout,
        title = "Agenda y Citas Médicas",
        userEmail = userEmail
    ) {
        Column(
            modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(12.dp))
                .background(Color.White).border(1.dp, BorderLight, RoundedCornerShape(12.dp)).padding(20.dp)
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text("Citas de la clínica", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text("Citas, mascotas y servicios de esta clínica.", fontSize = 12.sp, color = TextSecondary)
                }
                OutlinedButton(onClick = { coroutineScope.launch { loadAppointments() } }, enabled = !isLoading) {
                    Text(if (isLoading) "Cargando…" else "Actualizar")
                }
            }
            Spacer(Modifier.height(12.dp))
            errorMessage?.let {
                Text(it, color = Color(0xFFB91C1C), fontSize = 13.sp, modifier = Modifier.fillMaxWidth().background(Color(0xFFFEF2F2), RoundedCornerShape(6.dp)).padding(10.dp))
                Spacer(Modifier.height(10.dp))
            }
            Row(Modifier.fillMaxWidth().background(OffWhiteBg, RoundedCornerShape(8.dp)).border(1.dp, BorderLight, RoundedCornerShape(8.dp)).padding(12.dp)) {
                Text("FECHA Y HORA", Modifier.weight(0.23f), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                Text("MASCOTA / ID", Modifier.weight(0.28f), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                Text("SERVICIO", Modifier.weight(0.22f), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                Text("ESTADO", Modifier.weight(0.12f), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                Text("ACCIONES", Modifier.weight(0.15f), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextMuted)
            }
            Spacer(Modifier.height(8.dp))
            if (!isLoading && appointments.isEmpty() && errorMessage == null) {
                Text("No hay citas para esta clínica.", color = TextSecondary, fontSize = 14.sp, modifier = Modifier.padding(16.dp))
            }
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxSize()) {
                items(appointments, key = { it.id }) { appointment ->
                    Row(
                        Modifier.fillMaxWidth().border(1.dp, BorderLight, RoundedCornerShape(8.dp)).padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(appointment.displayTime, Modifier.weight(0.23f), fontSize = 12.sp, color = TextPrimary)
                        Text("${appointment.petName} (${appointment.petId})", Modifier.weight(0.28f), fontSize = 11.sp, color = TextSecondary)
                        Text(appointment.serviceName, Modifier.weight(0.22f), fontSize = 12.sp, color = TextPrimary)
                        Text(appointment.status.toAgendaStatus(), Modifier.weight(0.12f), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Row(Modifier.weight(0.15f), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            when (appointment.status) {
                                "REQUESTED" -> {
                                    TextButton(enabled = busyAppointmentId != appointment.id, onClick = { coroutineScope.launch { transition(appointment, "CONFIRMED") } }) { Text("Confirmar", fontSize = 10.sp) }
                                    TextButton(enabled = busyAppointmentId != appointment.id, onClick = { coroutineScope.launch { transition(appointment, "CANCELLED") } }) { Text("Cancelar", fontSize = 10.sp, color = Color(0xFFB91C1C)) }
                                }
                                "CONFIRMED" -> TextButton(enabled = busyAppointmentId != appointment.id, onClick = { coroutineScope.launch { transition(appointment, "IN_PROGRESS") } }) { Text("Iniciar consulta", fontSize = 10.sp) }
                                "IN_PROGRESS" -> TextButton(enabled = busyAppointmentId != appointment.id, onClick = { openRecord(appointment) }) { Text("Terminar consulta", fontSize = 10.sp) }
                                "COMPLETED" -> if (appointment.id in missingRecords) {
                                    TextButton(enabled = busyAppointmentId != appointment.id, onClick = { openRecord(appointment) }) { Text("Registrar expediente", fontSize = 10.sp) }
                                } else {
                                    Text("En expediente", color = TextSecondary, fontSize = 10.sp)
                                }
                            }
                            completing?.let { appointment ->
                                Dialog(onDismissRequest = { if (busyAppointmentId == null) completing = null }) {
                                    Card(Modifier.width(620.dp).heightIn(max = 850.dp), shape = RoundedCornerShape(16.dp)) {
                                        Column(Modifier.padding(24.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                            Text(if (appointment.status == "COMPLETED") "Registrar expediente anterior" else "Terminar consulta",
                                                fontSize = 20.sp, fontWeight = FontWeight.Bold)
                                            Text("${appointment.petName} · ${appointment.serviceName}", color = TextSecondary)
                                            Text("El diagnóstico y tratamiento se guardarán en el expediente de la mascota.", color = TextSecondary, fontSize = 12.sp)
                                            OutlinedTextField(reason, { reason = it }, label = { Text("Motivo de consulta *") }, modifier = Modifier.fillMaxWidth())
                                            OutlinedTextField(symptoms, { symptoms = it }, label = { Text("Síntomas *") }, modifier = Modifier.fillMaxWidth())
                                            OutlinedTextField(diagnosis, { diagnosis = it }, label = { Text("Diagnóstico *") }, modifier = Modifier.fillMaxWidth())
                                            OutlinedTextField(treatment, { treatment = it }, label = { Text("Tratamiento *") }, modifier = Modifier.fillMaxWidth())
                                            OutlinedTextField(observations, { observations = it }, label = { Text("Observaciones *") }, modifier = Modifier.fillMaxWidth())
                                            OutlinedTextField(weight, { weight = it }, label = { Text("Peso en gramos *") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                                            OutlinedTextField(medication, { medication = it }, label = { Text("Medicamento (opcional)") }, modifier = Modifier.fillMaxWidth())
                                            OutlinedTextField(dosage, { dosage = it }, label = { Text("Dosis (opcional)") }, modifier = Modifier.fillMaxWidth())
                                            formError?.let { Text(it, color = Color(0xFFB91C1C), fontSize = 13.sp) }
                                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                                TextButton(onClick = { completing = null }, enabled = busyAppointmentId == null) { Text("Cancelar") }
                                                Button(onClick = { coroutineScope.launch { finish(appointment) } }, enabled = busyAppointmentId == null) {
                                                    Text(if (busyAppointmentId != null) "Guardando…" else "Guardar expediente")
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun String.toAgendaStatus(): String = when (uppercase()) {
    "REQUESTED" -> "Pendiente"
    "CONFIRMED" -> "Confirmada"
    "IN_PROGRESS" -> "En progreso"
    "CANCELLED" -> "Cancelada"
    "COMPLETED" -> "Completada"
    "NO_SHOW" -> "No asistió"
    "REJECTED" -> "Rechazada"
    else -> this
}

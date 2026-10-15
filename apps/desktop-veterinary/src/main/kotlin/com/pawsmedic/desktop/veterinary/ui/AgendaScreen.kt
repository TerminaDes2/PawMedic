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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Serializable
private data class AgendaAppointment(
    val id: String,
    @SerialName("pet_id") val petId: String,
    @SerialName("service_id") val serviceId: String,
    @SerialName("starts_at") val startsAt: String,
    @SerialName("ends_at") val endsAt: String,
    val status: String
)

@Serializable
private data class AgendaService(val id: String, val name: String)

@Composable
fun AgendaScreen(
    userEmail: String = "",
    onNavigate: (String) -> Unit,
    onLogout: () -> Unit = {}
) {
    var appointments by remember { mutableStateOf<List<AgendaAppointment>>(emptyList()) }
    var serviceNames by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var busyAppointmentId by remember { mutableStateOf<String?>(null) }
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
            val tenantId = profile.tenantId ?: error("El perfil no tiene una clínica asignada.")
            appointments = supabase.postgrest["appointments"].select {
                filter { eq("tenant_id", tenantId) }
            }.decodeList<AgendaAppointment>().sortedBy { it.startsAt }
            serviceNames = supabase.postgrest["services"].select {
                filter { eq("tenant_id", tenantId) }
            }.decodeList<AgendaService>().associate { it.id to it.name }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            errorMessage = e.message ?: "No se pudo cargar la agenda desde Supabase."
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
            errorMessage = e.message ?: "No se pudo actualizar la cita."
        } finally {
            busyAppointmentId = null
        }
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
                    Text("Datos de appointments en Supabase. Las mascotas se identifican por su ID debido a las políticas de acceso actuales.", fontSize = 12.sp, color = TextSecondary)
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
                        Text(formatAppointmentTime(appointment.startsAt, appointment.endsAt), Modifier.weight(0.23f), fontSize = 12.sp, color = TextPrimary)
                        Text(appointment.petId, Modifier.weight(0.28f), fontSize = 11.sp, color = TextSecondary)
                        Text(serviceNames[appointment.serviceId] ?: appointment.serviceId, Modifier.weight(0.22f), fontSize = 12.sp, color = TextPrimary)
                        Text(appointment.status, Modifier.weight(0.12f), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Row(Modifier.weight(0.15f), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            when (appointment.status) {
                                "REQUESTED" -> {
                                    TextButton(enabled = busyAppointmentId != appointment.id, onClick = { coroutineScope.launch { transition(appointment, "CONFIRMED") } }) { Text("Confirmar", fontSize = 10.sp) }
                                    TextButton(enabled = busyAppointmentId != appointment.id, onClick = { coroutineScope.launch { transition(appointment, "CANCELLED") } }) { Text("Cancelar", fontSize = 10.sp, color = Color(0xFFB91C1C)) }
                                }
                                "CONFIRMED" -> TextButton(enabled = busyAppointmentId != appointment.id, onClick = { coroutineScope.launch { transition(appointment, "COMPLETED") } }) { Text("Completar", fontSize = 10.sp) }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun formatAppointmentTime(startsAt: String, endsAt: String): String = try {
    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").withZone(ZoneId.systemDefault())
    "${formatter.format(Instant.parse(startsAt))} – ${DateTimeFormatter.ofPattern("HH:mm").withZone(ZoneId.systemDefault()).format(Instant.parse(endsAt))}"
} catch (_: Exception) {
    "$startsAt – $endsAt"
}

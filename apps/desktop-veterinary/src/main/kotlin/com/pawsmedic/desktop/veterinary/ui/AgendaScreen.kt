package com.pawsmedic.desktop.veterinary.ui

import com.pawsmedic.desktop.admin.translation.translateBatchLocally
import com.pawsmedic.desktop.admin.ui.LocalDesktopAppearance
import com.pawsmedic.desktop.veterinary.model.PatientRow
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
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import kotlinx.coroutines.delay

@Serializable
private data class AgendaClinicZone(@SerialName("time_zone") val timeZone: String)

internal fun appointmentDayReached(startsAt: String, zone: ZoneId, today: LocalDate): Boolean =
    !Instant.parse(startsAt).atZone(zone).toLocalDate().isAfter(today)

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
    val english = LocalDesktopAppearance.current.english
    fun tr(spanish: String, englishText: String) = if (english) englishText else spanish
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
    var clinicZone by remember { mutableStateOf<ZoneId?>(null) }
    var clinicToday by remember { mutableStateOf<LocalDate?>(null) }

    // Filtros de búsqueda y estado
    var statusFilter by remember { mutableStateOf("Todos") } // Todos, Pendientes, Confirmadas, En proceso
    var searchQuery by remember { mutableStateOf("") }

    val translations = remember(userEmail) { mutableStateMapOf<String, String>() }
    var showOriginal by remember { mutableStateOf(false) }
    var translating by remember { mutableStateOf(false) }
    var translationError by remember { mutableStateOf(false) }
    var translationAttempt by remember { mutableIntStateOf(0) }
    val coroutineScope = rememberCoroutineScope()

    val serviceNames = remember(appointments) { appointments.map { it.serviceName }.filter { it.isNotBlank() }.distinct() }
    LaunchedEffect(english, serviceNames, translationAttempt) {
        if (!english) {
            translating = false
            translationError = false
            return@LaunchedEffect
        }
        val missing = serviceNames.filterNot { translations.containsKey(it) }
        val pending = missing.filter { it.length <= 4000 }
        translationError = pending.size != missing.size
        if (pending.isNotEmpty()) {
            translating = true
            try {
                for (batch in pending.chunked(50)) {
                    batch.zip(translateBatchLocally(batch)).forEach { (original, translated) ->
                        translations[original] = translated
                    }
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
    fun displayService(value: String): String =
        if (english && !showOriginal) translations[value] ?: value else value

    suspend fun loadAppointments() {
        isLoading = true
        errorMessage = null
        try {
            val userId = supabase.auth.currentSessionOrNull()?.user?.id
                ?: error(tr("No hay una sesión de Supabase activa. Inicia sesión nuevamente.", "There is no active Supabase session. Please sign in again."))
            val profile = supabase.postgrest["profiles"].select {
                filter { eq("id", userId) }
            }.decodeSingle<UserProfile>()
            if (profile.role != ProfileRole.VETERINARY_BUSINESS) {
                error(tr("La agenda solo está disponible para cuentas veterinarias.", "Appointments are only available to veterinary accounts."))
            }
            val tenantId = com.pawsmedic.desktop.veterinary.model.resolveTenantId(supabase, userId, profile)
            val zone = ZoneId.of(supabase.postgrest["tenants"].select {
                filter { eq("id", tenantId) }
            }.decodeSingle<AgendaClinicZone>().timeZone)
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
            appointments = loadedAppointments.filter { it.status.uppercase() in setOf("REQUESTED", "CONFIRMED", "IN_PROGRESS") }
            clinicZone = zone
            clinicToday = LocalDate.now(zone)
            missingRecords = missing.toSet()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            System.err.println("No se pudo cargar la agenda: ${e.javaClass.simpleName}")
            errorMessage = tr("No se pudo cargar la agenda. Verifica tu sesión, la conexión y la configuración del flujo de consultas.",
                "Could not load appointments. Check your session, connection, and appointment workflow configuration.")
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
            errorMessage = tr("No se pudo actualizar la cita. Verifica la conexión y la configuración del flujo de consultas.",
                "Could not update the appointment. Check your connection and appointment workflow configuration.")
        } finally {
            busyAppointmentId = null
        }
    }

    suspend fun finish(appointment: AgendaAppointment) {
            val grams = weight.trim().toIntOrNull()
            if (listOf(reason, symptoms, diagnosis, treatment, observations).any { it.isBlank() } || grams == null || grams <= 0) {
                formError = tr("Completa motivo, síntomas, diagnóstico, tratamiento, observaciones y peso en gramos.",
                    "Complete the reason, symptoms, diagnosis, treatment, observations, and weight in grams.")
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
                formError = tr("No se pudo guardar el expediente. Verifica la conexión y que el script de consultas esté instalado.",
                    "Could not save the record. Check your connection and that the consultation script is installed.")
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
    LaunchedEffect(clinicZone) {
        val zone = clinicZone ?: return@LaunchedEffect
        while (true) {
            clinicToday = LocalDate.now(zone)
            delay(60_000)
        }
    }

    val filteredAppointments = appointments.filter { appt ->
        val matchesStatus = when (statusFilter) {
            "Pendientes" -> appt.status.uppercase() == "REQUESTED"
            "Confirmadas" -> appt.status.uppercase() == "CONFIRMED"
            "En proceso" -> appt.status.uppercase() == "IN_PROGRESS"
            else -> true
        }
        val matchesSearch = searchQuery.isBlank() ||
            appt.petName.contains(searchQuery, ignoreCase = true) ||
            appt.serviceName.contains(searchQuery, ignoreCase = true)
        matchesStatus && matchesSearch
    }

    VetAppLayout(
        currentScreen = "Agenda",
        onNavigate = onNavigate,
        onLogout = onLogout,
        title = tr("Agenda y Citas Médicas", "Appointments"),
        userEmail = userEmail
    ) {
        Column(
            modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(12.dp))
                .background(CardWhite).border(1.dp, BorderLight, RoundedCornerShape(12.dp)).padding(20.dp)
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(tr("Citas de la clínica", "Clinic appointments"), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text(tr("Citas, mascotas y servicios de esta clínica.", "Appointments, pets, and services for this clinic."), fontSize = 12.sp, color = TextSecondary)
                }
                OutlinedButton(onClick = { coroutineScope.launch { loadAppointments() } }, enabled = !isLoading) {
                    Text(if (isLoading) tr("Cargando…", "Loading…") else tr("Actualizar", "Refresh"))
                }
            }
            if (english) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Service names are machine translated. Verify the original before clinical decisions.", modifier = Modifier.weight(1f), color = TextSecondary, fontSize = 12.sp)
                    TextButton(onClick = { showOriginal = !showOriginal }) { Text(if (showOriginal) "Show translation" else "Show original") }
                }
                Box(Modifier.fillMaxWidth().height(40.dp), contentAlignment = Alignment.CenterStart) {
                    if (translationError) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Some services could not be translated; the original is shown.", modifier = Modifier.weight(1f), color = Color(0xFFB91C1C), fontSize = 12.sp)
                            TextButton(onClick = { translationAttempt++ }, enabled = !translating) { Text("Retry") }
                        }
                    } else if (translating) {
                        Text("Translating…", color = TextSecondary, fontSize = 12.sp)
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // BARRA DE BÚSQUEDA Y FILTROS POR ESTADO
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text(tr("Buscar por mascota o servicio...", "Search by pet or service..."), fontSize = 12.sp) },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp)
                )

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(tr("Estado:", "Status:"), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                    listOf("Todos", "Pendientes", "Confirmadas", "En proceso").forEach { statusOpt ->
                        FilterChip(
                            selected = statusFilter == statusOpt,
                            onClick = { statusFilter = statusOpt },
                            label = { Text(statusOpt, fontSize = 11.sp) }
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            errorMessage?.let {
                Text(it, color = Color(0xFFB91C1C), fontSize = 13.sp, modifier = Modifier.fillMaxWidth().background(Color(0xFFFEF2F2), RoundedCornerShape(6.dp)).padding(10.dp))
                Spacer(Modifier.height(10.dp))
            }
            Row(Modifier.fillMaxWidth().background(OffWhiteBg, RoundedCornerShape(8.dp)).border(1.dp, BorderLight, RoundedCornerShape(8.dp)).padding(12.dp)) {
                Text(tr("FECHA Y HORA", "DATE AND TIME"), Modifier.weight(0.23f), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                Text(tr("MASCOTA", "PET"), Modifier.weight(0.28f), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                Text(tr("SERVICIO / TIPO", "SERVICE / TYPE"), Modifier.weight(0.22f), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                Text(tr("ESTADO", "STATUS"), Modifier.weight(0.12f), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                Text(tr("ACCIONES", "ACTIONS"), Modifier.weight(0.15f), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextMuted)
            }
            Spacer(Modifier.height(8.dp))
            if (!isLoading && filteredAppointments.isEmpty() && errorMessage == null) {
                Text(tr("No hay citas que coincidan con los filtros.", "No appointments match the filters."), color = TextSecondary, fontSize = 14.sp, modifier = Modifier.padding(16.dp))
            }
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxSize()) {
                items(filteredAppointments, key = { it.id }) { appointment ->
                    Row(
                        Modifier.fillMaxWidth().border(1.dp, BorderLight, RoundedCornerShape(8.dp)).padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(appointment.displayTime, Modifier.weight(0.23f), fontSize = 12.sp, color = TextPrimary)
                        Text(appointment.petName, Modifier.weight(0.28f), fontSize = 12.sp, color = TextSecondary, fontWeight = FontWeight.Medium)
                        Text(displayService(appointment.serviceName), Modifier.weight(0.22f), fontSize = 12.sp, color = TextPrimary)
                        Text(agendaStatus(appointment.status, english), Modifier.weight(0.12f), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Column(Modifier.weight(0.15f)) {
                            when (appointment.status) {
                                "REQUESTED" -> {
                                    TextButton(enabled = busyAppointmentId != appointment.id, onClick = { coroutineScope.launch { transition(appointment, "CONFIRMED") } }) { Text(tr("Confirmar", "Confirm"), fontSize = 10.sp) }
                                    TextButton(enabled = busyAppointmentId != appointment.id, onClick = { coroutineScope.launch { transition(appointment, "CANCELLED") } }) { Text(tr("Cancelar", "Cancel"), fontSize = 10.sp, color = Color(0xFFB91C1C)) }
                                }
                                "CONFIRMED" -> if (clinicZone != null && clinicToday != null &&
                                    appointmentDayReached(appointment.startsAt, clinicZone!!, clinicToday!!)) {
                                    TextButton(enabled = busyAppointmentId != appointment.id, onClick = {
                                        coroutineScope.launch {
                                            busyAppointmentId = appointment.id
                                            try {
                                                supabase.postgrest.rpc(
                                                    function = "transition_appointment_status",
                                                    parameters = buildJsonObject {
                                                        put("p_appointment_id", appointment.id)
                                                        put("p_expected_status", appointment.status)
                                                        put("p_next_status", "IN_PROGRESS")
                                                    }
                                                )
                                                val userId = supabase.auth.currentSessionOrNull()?.user?.id ?: ""
                                                val profile = supabase.postgrest["profiles"].select { filter { eq("id", userId) } }.decodeSingle<UserProfile>()
                                                val tenantId = com.pawsmedic.desktop.veterinary.model.resolveTenantId(supabase, userId, profile)
                                                val pet = supabase.postgrest["pets"].select { filter { eq("id", appointment.petId) } }.decodeSingleOrNull<PatientRow>()

                                                VeterinaryCareContext.appointmentId = appointment.id
                                                VeterinaryCareContext.serviceName = appointment.serviceName
                                                VeterinaryCareContext.categoryType = VeterinaryCareContext.inferCategory(appointment.serviceName)
                                                VeterinaryCareContext.tenantId = tenantId
                                                VeterinaryCareContext.vetUserId = userId
                                                VeterinaryCareContext.vetUserName = profile.displayName.ifBlank { "Dr. Veterinario" }
                                                VeterinaryCareContext.patient = pet ?: PatientRow(id = appointment.petId, name = appointment.petName)
                                                VeterinaryCareContext.prefilledReason = appointment.serviceName

                                                onNavigate("VeterinaryCare")
                                            } catch (e: Exception) {
                                                errorMessage = tr("No se pudo iniciar la consulta.", "Could not start consultation.")
                                            } finally {
                                                busyAppointmentId = null
                                            }
                                        }
                                    }) { Text(tr("Iniciar consulta", "Start consultation"), fontSize = 10.sp) }
                                }
                                "IN_PROGRESS" -> if (clinicZone != null && clinicToday != null &&
                                    appointmentDayReached(appointment.startsAt, clinicZone!!, clinicToday!!)) {
                                    TextButton(enabled = busyAppointmentId != appointment.id, onClick = {
                                        coroutineScope.launch {
                                            try {
                                                val userId = supabase.auth.currentSessionOrNull()?.user?.id ?: ""
                                                val profile = supabase.postgrest["profiles"].select { filter { eq("id", userId) } }.decodeSingle<UserProfile>()
                                                val tenantId = com.pawsmedic.desktop.veterinary.model.resolveTenantId(supabase, userId, profile)
                                                val pet = supabase.postgrest["pets"].select { filter { eq("id", appointment.petId) } }.decodeSingleOrNull<PatientRow>()

                                                VeterinaryCareContext.appointmentId = appointment.id
                                                VeterinaryCareContext.serviceName = appointment.serviceName
                                                VeterinaryCareContext.categoryType = VeterinaryCareContext.inferCategory(appointment.serviceName)
                                                VeterinaryCareContext.tenantId = tenantId
                                                VeterinaryCareContext.vetUserId = userId
                                                VeterinaryCareContext.vetUserName = profile.displayName.ifBlank { "Dr. Veterinario" }
                                                VeterinaryCareContext.patient = pet ?: PatientRow(id = appointment.petId, name = appointment.petName)
                                                VeterinaryCareContext.prefilledReason = appointment.serviceName

                                                onNavigate("VeterinaryCare")
                                            } catch (e: Exception) {
                                                errorMessage = tr("No se pudo abrir la atención.", "Could not open care session.")
                                            }
                                        }
                                    }) { Text(tr("Terminar consulta", "Finish consultation"), fontSize = 10.sp) }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

internal fun agendaStatus(status: String, english: Boolean): String = when (status.uppercase()) {
    "REQUESTED" -> if (english) "Pending" else "Pendiente"
    "CONFIRMED" -> if (english) "Confirmed" else "Confirmada"
    "IN_PROGRESS" -> if (english) "In progress" else "En progreso"
    "CANCELLED" -> if (english) "Cancelled" else "Cancelada"
    "COMPLETED" -> if (english) "Completed" else "Completada"
    "NO_SHOW" -> if (english) "No-show" else "No asistió"
    "REJECTED" -> if (english) "Rejected" else "Rechazada"
    else -> status
}

package com.pawsmedic.desktop.veterinary.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pawsmedic.desktop.veterinary.model.resolveTenantId
import com.pawsmedic.desktop.admin.translation.translateBatchLocally
import com.pawsmedic.desktop.admin.ui.LocalDesktopAppearance
import com.pawsmedic.desktop.veterinary.supabase
import com.pawsmedic.desktop.veterinary.ui.theme.*
import com.pawsmedic.shared.core.model.ProfileRole
import com.pawsmedic.shared.core.model.UserProfile
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Serializable
private data class ReportAppointment(
    val id: String,
    @SerialName("starts_at") val startsAt: String,
    @SerialName("pet_name") val petName: String,
    @SerialName("service_name") val serviceName: String
)

@Serializable
private data class ReportService(val name: String, val total: Int)

@Serializable
private data class OperationalReport(
    @SerialName("total_appointments") val totalAppointments: Int,
    @SerialName("completed_appointments") val completedAppointments: Int,
    val appointments: List<ReportAppointment>,
    val services: List<ReportService>,
    @SerialName("time_zone") val timeZone: String
)

internal fun reportDates(start: String, end: String): Pair<LocalDate, LocalDate>? {
    val first = runCatching { LocalDate.parse(start.trim()) }.getOrNull() ?: return null
    val last = runCatching { LocalDate.parse(end.trim()) }.getOrNull() ?: return null
    return if (first.year in 1..9998 && last.year in 1..9998 && first <= last) first to last else null
}

@Composable
fun ReportsScreen(userEmail: String, onNavigate: (String) -> Unit, onLogout: () -> Unit) {
    val english = LocalDesktopAppearance.current.english
    val today = remember { LocalDate.now() }
    var start by remember { mutableStateOf(today.withDayOfMonth(1).toString()) }
    var end by remember { mutableStateOf(today.toString()) }
    var report by remember { mutableStateOf<OperationalReport?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }
    var exporting by remember { mutableStateOf(false) }
    var notice by remember { mutableStateOf<String?>(null) }
    val translations = remember(userEmail) { mutableStateMapOf<String, String>() }
    var showOriginal by remember { mutableStateOf(false) }
    var translating by remember { mutableStateOf(false) }
    var translationError by remember { mutableStateOf(false) }
    var translationAttempt by remember { mutableIntStateOf(0) }
    val scope = rememberCoroutineScope()

    val serviceNames = remember(report) {
        report?.let { result ->
            (result.appointments.map { it.serviceName } + result.services.map { it.name })
                .filter { it.isNotBlank() }.distinct()
        }.orEmpty()
    }
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

    suspend fun load() {
        val dates = reportDates(start, end)
        if (dates == null) {
            error = if (english) "Enter a valid date range in YYYY-MM-DD format."
                else "Ingresa un rango válido con formato AAAA-MM-DD."
            return
        }
        val (first, last) = dates
        loading = true
        error = null
        notice = null
        report = null
        try {
            val userId = supabase.auth.currentSessionOrNull()?.user?.id
                ?: error("No hay una sesión activa.")
            val profile = supabase.postgrest["profiles"].select {
                filter { eq("id", userId) }
            }.decodeSingle<UserProfile>()
            check(profile.role == ProfileRole.VETERINARY_BUSINESS) { "Se requiere una cuenta veterinaria." }
            resolveTenantId(supabase, userId, profile)
            report = supabase.postgrest.rpc("get_operational_report", buildJsonObject {
                put("p_start_date", first.toString())
                put("p_end_date", last.toString())
            }).decodeAs<OperationalReport>()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            error = if (english) "Could not load the report. Check your connection and that the report migration is installed."
                else "No se pudo cargar el reporte. Verifica tu conexión y que la migración de reportes esté instalada."
        } finally {
            loading = false
        }
    }

    VetAppLayout("Reports", onNavigate, onLogout, "Reportes operativos", userEmail) {
        LazyColumn(
            modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(12.dp))
                .background(CardWhite).border(1.dp, BorderLight, RoundedCornerShape(12.dp)),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Column {
                    Text(
                        if (english) "Clinic reports" else "Reportes de la clínica",
                        fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary
                    )
                    Text(
                        if (english) "Review appointments and services for a selected period."
                        else "Consulta las citas y los servicios prestados en el periodo seleccionado.",
                        fontSize = 12.sp, color = TextSecondary
                    )
                }
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().background(OffWhiteBg, RoundedCornerShape(8.dp))
                        .border(1.dp, BorderLight, RoundedCornerShape(8.dp)).padding(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        start, { start = it; report = null },
                        label = { Text(if (english) "From (YYYY-MM-DD)" else "Desde (AAAA-MM-DD)") },
                        singleLine = true, enabled = !loading, modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        end, { end = it; report = null },
                        label = { Text(if (english) "To (YYYY-MM-DD)" else "Hasta (AAAA-MM-DD)") },
                        singleLine = true, enabled = !loading, modifier = Modifier.weight(1f)
                    )
                    Button(
                        onClick = { scope.launch { load() } },
                        enabled = !loading,
                        colors = ButtonDefaults.buttonColors(containerColor = ClinicalEmerald)
                    ) {
                        Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(if (english) "Search" else "Consultar", color = Color.White)
                    }
                    OutlinedButton(
                        enabled = report != null && !loading && !exporting,
                        onClick = {
                            val current = report
                            if (current != null) {
                                val destination = choosePdfFile(english)
                                if (destination != null) {
                                    val pdf = PdfReport(start, end, current.timeZone, current.totalAppointments,
                                        current.completedAppointments,
                                        current.appointments.map { PdfReportAppointment(it.startsAt, it.petName, it.serviceName) },
                                        current.services.map { PdfReportService(it.name, it.total) })
                                    scope.launch {
                                        exporting = true
                                        error = null
                                        notice = null
                                        try {
                                            withContext(Dispatchers.IO) { writeReportPdf(destination, pdf, english) }
                                            notice = if (english) "PDF saved successfully." else "PDF guardado correctamente."
                                        } catch (e: CancellationException) {
                                            throw e
                                        } catch (e: Exception) {
                                            System.err.println("No se pudo guardar el PDF: ${e.javaClass.simpleName}")
                                            error = if (english) "Could not save the PDF. Check the destination and try again." else "No se pudo guardar el PDF. Verifica el destino e inténtalo de nuevo."
                                        } finally {
                                            exporting = false
                                        }
                                    }
                                }
                            }
                        }
                    ) {
                        Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(if (exporting) (if (english) "Saving…" else "Guardando…") else "PDF")
                    }
                }
            }
            notice?.let { message -> item { Text(message, color = MaterialTheme.colorScheme.primary, fontSize = 13.sp) } }
            error?.let { message ->
                item {
                    Text(
                        message,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        fontSize = 13.sp,
                        modifier = Modifier.fillMaxWidth()
                            .background(MaterialTheme.colorScheme.errorContainer, RoundedCornerShape(6.dp))
                            .padding(10.dp)
                    )
                }
            }
            when {
                loading -> item {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 3.dp)
                        Text(if (english) "Loading report…" else "Cargando reporte…", color = TextSecondary)
                    }
                }
                report == null && error == null -> item {
                    Text(
                        if (english) "Choose a date range and select Search to view the report."
                        else "Selecciona un rango de fechas y pulsa Consultar para ver el reporte.",
                        color = TextSecondary, fontSize = 13.sp, modifier = Modifier.padding(vertical = 12.dp)
                    )
                }
            }
            report?.let { result ->
                item {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        ReportStatCard(
                            title = if (english) "Total appointments" else "Total de citas",
                            value = result.totalAppointments,
                            icon = Icons.Default.CalendarMonth,
                            modifier = Modifier.weight(1f)
                        )
                        ReportStatCard(
                            title = if (english) "Completed appointments" else "Citas atendidas",
                            value = result.completedAppointments,
                            icon = Icons.Default.EventAvailable,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                item {
                    ReportSectionTitle(
                        if (english) "Completed appointments" else "Citas atendidas",
                        if (english) "Appointments completed within the selected period."
                        else "Citas completadas durante el periodo seleccionado."
                    )
                    Row(Modifier.fillMaxWidth().background(OffWhiteBg, RoundedCornerShape(8.dp))
                        .border(1.dp, BorderLight, RoundedCornerShape(8.dp)).padding(12.dp)) {
                        ReportColumnTitle(if (english) "DATE AND TIME" else "FECHA Y HORA", Modifier.weight(0.30f))
                        ReportColumnTitle(if (english) "PET" else "MASCOTA", Modifier.weight(0.30f))
                        ReportColumnTitle(if (english) "SERVICE" else "SERVICIO", Modifier.weight(0.40f))
                    }
                }
                if (result.appointments.isEmpty()) item {
                    Text(
                        if (english) "No completed appointments in this range." else "No hay citas atendidas en el rango.",
                        color = TextSecondary, fontSize = 13.sp, modifier = Modifier.padding(12.dp)
                    )
                }
                items(result.appointments, key = { "appointment-${it.id}" }) { appointment ->
                    val date = Instant.parse(appointment.startsAt).atZone(ZoneId.of(result.timeZone))
                        .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
                    Row(
                        Modifier.fillMaxWidth().border(1.dp, BorderLight, RoundedCornerShape(8.dp)).padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(date, Modifier.weight(0.30f), fontSize = 12.sp, color = TextPrimary)
                        Text(appointment.petName, Modifier.weight(0.30f), fontSize = 12.sp, color = TextSecondary)
                        Text(displayService(appointment.serviceName), Modifier.weight(0.40f), fontSize = 12.sp, color = TextPrimary)
                    }
                }
                item {
                    ReportSectionTitle(
                        if (english) "Services provided" else "Servicios prestados",
                        if (english) "Services associated with completed appointments."
                        else "Servicios asociados a las citas atendidas."
                    )
                    Row(Modifier.fillMaxWidth().background(OffWhiteBg, RoundedCornerShape(8.dp))
                        .border(1.dp, BorderLight, RoundedCornerShape(8.dp)).padding(12.dp)) {
                        ReportColumnTitle(if (english) "SERVICE" else "SERVICIO", Modifier.weight(0.75f))
                        ReportColumnTitle(if (english) "TOTAL" else "TOTAL", Modifier.weight(0.25f))
                    }
                }
                if (result.services.isEmpty()) item {
                    Text(
                        if (english) "No services provided in this range." else "No hay servicios prestados en el rango.",
                        color = TextSecondary, fontSize = 13.sp, modifier = Modifier.padding(12.dp)
                    )
                }
                items(result.services) { service ->
                    Row(
                        Modifier.fillMaxWidth().border(1.dp, BorderLight, RoundedCornerShape(8.dp)).padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(displayService(service.name), Modifier.weight(0.75f), fontSize = 12.sp, color = TextPrimary)
                        Text(service.total.toString(), Modifier.weight(0.25f), fontSize = 12.sp,
                            fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }
}

@Composable
private fun ReportStatCard(title: String, value: Int, icon: ImageVector, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.background(OffWhiteBg, RoundedCornerShape(8.dp))
            .border(1.dp, BorderLight, RoundedCornerShape(8.dp)).padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(40.dp).background(EmeraldLightBg, RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(21.dp))
        }
        Column {
            Text(title, fontSize = 12.sp, color = TextSecondary)
            Text(value.toString(), fontSize = 22.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        }
    }
}

@Composable
private fun ReportSectionTitle(title: String, description: String) {
    Column(Modifier.padding(top = 8.dp, bottom = 10.dp)) {
        Text(title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        Text(description, fontSize = 12.sp, color = TextSecondary)
    }
}

@Composable
private fun ReportColumnTitle(text: String, modifier: Modifier = Modifier) {
    Text(text, modifier, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextMuted)
}

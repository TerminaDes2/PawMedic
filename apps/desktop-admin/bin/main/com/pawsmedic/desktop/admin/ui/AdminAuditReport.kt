package com.pawsmedic.desktop.admin.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Serializable
private data class AdminAuditRow(
    @SerialName("actor_id") val actorId: String? = null,
    val action: String,
    @SerialName("entity_id") val entityId: String? = null,
    @SerialName("created_at") val createdAt: String,
    val metadata: JsonObject? = null
)

private enum class AdminEventKind { RECEIVED, APPROVED, REJECTED, SUSPENDED, REACTIVATED }

private data class AdminEvent(val at: OffsetDateTime, val kind: AdminEventKind, val clinic: String, val actor: String)

private fun AdminAuditRow.kind(): AdminEventKind? = when (action) {
    "SUBMIT_BUSINESS_APPLICATION" -> AdminEventKind.RECEIVED
    "APPROVE_BUSINESS" -> AdminEventKind.APPROVED
    "REJECT_BUSINESS" -> AdminEventKind.REJECTED
    "CHANGE_BUSINESS_STATUS" -> when ((metadata?.get("status") as? JsonPrimitive)?.content) {
        "SUSPENDED" -> AdminEventKind.SUSPENDED
        "APPROVED" -> AdminEventKind.REACTIVATED
        else -> null
    }
    else -> null
}

private fun AdminEventKind.label(english: Boolean) = when (this) {
    AdminEventKind.RECEIVED -> if (english) "Application received" else "Solicitud recibida"
    AdminEventKind.APPROVED -> if (english) "Application approved" else "Solicitud aprobada"
    AdminEventKind.REJECTED -> if (english) "Application rejected" else "Solicitud rechazada"
    AdminEventKind.SUSPENDED -> if (english) "Clinic suspended" else "Veterinaria suspendida"
    AdminEventKind.REACTIVATED -> if (english) "Clinic reactivated" else "Veterinaria reactivada"
}

private val eventFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")

@Composable
fun AuditoriaModule(supabaseClient: SupabaseClient, userEmail: String) {
    val english = LocalDesktopAppearance.current.english
    fun tr(spanish: String, translated: String) = if (english) translated else spanish
    val today = remember { LocalDate.now() }
    var start by remember { mutableStateOf(today.withDayOfMonth(1).toString()) }
    var end by remember { mutableStateOf(today.toString()) }
    var events by remember { mutableStateOf<List<AdminEvent>?>(null) }
    var loading by remember { mutableStateOf(false) }
    var exporting by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var notice by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val zone = remember { ZoneId.systemDefault() }

    suspend fun load() {
        val first = runCatching { LocalDate.parse(start.trim()) }.getOrNull()
        val last = runCatching { LocalDate.parse(end.trim()) }.getOrNull()
        if (first == null || last == null || first > last) {
            error = tr("Ingresa un rango válido con formato AAAA-MM-DD.", "Enter a valid date range in YYYY-MM-DD format.")
            return
        }
        loading = true
        error = null
        notice = null
        events = null
        try {
            val clinics = try {
                fetchApplicationsFromDatabase(supabaseClient).associateBy { it.id }
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                emptyMap()
            }
            val from = first.atStartOfDay(zone).toInstant().toString()
            val until = last.plusDays(1).atStartOfDay(zone).toInstant().toString()
            val rows = supabaseClient.postgrest["audit_logs"].select {
                filter {
                    gte("created_at", from)
                    lt("created_at", until)
                }
                order("created_at", Order.DESCENDING)
            }.decodeList<AdminAuditRow>()
            events = rows.mapNotNull { row ->
                val kind = row.kind() ?: return@mapNotNull null
                val clinic = clinics[row.entityId]
                AdminEvent(
                    at = OffsetDateTime.parse(row.createdAt).atZoneSameInstant(zone).toOffsetDateTime(),
                    kind = kind,
                    clinic = clinic?.publicName?.ifBlank { null } ?: "—",
                    actor = if (kind == AdminEventKind.RECEIVED) clinic?.applicantName?.ifBlank { null } ?: tr("Solicitante", "Applicant")
                        else "Superadmin"
                )
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            error = tr("No se pudo cargar el reporte de auditoría. Verifica tu conexión y permisos.", "Could not load the audit report. Check your connection and permissions.")
        } finally {
            loading = false
        }
    }

    LaunchedEffect(Unit) { load() }

    fun count(kind: AdminEventKind) = events.orEmpty().count { it.kind == kind }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, AdminBorderLight, RoundedCornerShape(12.dp)),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Column {
                Text(tr("Reporte administrativo", "Administrative report"), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = AdminTextPrimary)
                Text(
                    tr("Solicitudes recibidas, aprobadas y rechazadas, y veterinarias suspendidas o reactivadas en el periodo.",
                        "Applications received, approved and rejected, and clinics suspended or reactivated in the period."),
                    fontSize = 12.sp, color = AdminTextSecondary
                )
            }
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth().background(AdminOffWhiteBg, RoundedCornerShape(8.dp))
                    .border(1.dp, AdminBorderLight, RoundedCornerShape(8.dp)).padding(14.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    start, { start = it; events = null },
                    label = { Text(tr("Desde (AAAA-MM-DD)", "From (YYYY-MM-DD)")) },
                    singleLine = true, enabled = !loading, modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    end, { end = it; events = null },
                    label = { Text(tr("Hasta (AAAA-MM-DD)", "To (YYYY-MM-DD)")) },
                    singleLine = true, enabled = !loading, modifier = Modifier.weight(1f)
                )
                Button(
                    onClick = { scope.launch { load() } },
                    enabled = !loading,
                    colors = ButtonDefaults.buttonColors(containerColor = AdminEmeraldGreen)
                ) {
                    Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(tr("Consultar", "Search"), color = Color.White)
                }
                OutlinedButton(
                    enabled = events != null && !loading && !exporting,
                    onClick = {
                        val current = events ?: return@OutlinedButton
                        val destination = chooseAdminPdfFile(english) ?: return@OutlinedButton
                        val pdf = AdminPdfReport(
                            start = start.trim(),
                            end = end.trim(),
                            generatedBy = userEmail.ifBlank { "Superadmin" },
                            counters = AdminEventKind.entries.map { it.label(english) to count(it) },
                            events = current.map {
                                AdminPdfEvent(it.at.format(eventFormatter), it.kind.label(english), it.clinic, it.actor)
                            }
                        )
                        scope.launch {
                            exporting = true
                            error = null
                            notice = null
                            try {
                                withContext(Dispatchers.IO) { writeAdminReportPdf(destination, pdf, english) }
                                notice = tr("PDF guardado correctamente.", "PDF saved successfully.")
                            } catch (e: CancellationException) {
                                throw e
                            } catch (e: Exception) {
                                System.err.println("No se pudo guardar el PDF: ${e.javaClass.simpleName}: ${e.message}")
                                error = tr("No se pudo guardar el PDF. Verifica el destino e inténtalo de nuevo.",
                                    "Could not save the PDF. Check the destination and try again.")
                            } finally {
                                exporting = false
                            }
                        }
                    }
                ) {
                    Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(if (exporting) tr("Guardando…", "Saving…") else "PDF")
                }
            }
        }
        notice?.let { message -> item { Text(message, color = AdminEmeraldGreen, fontSize = 13.sp) } }
        error?.let { message ->
            item {
                Text(
                    message, color = Color(0xFF991B1B), fontSize = 13.sp,
                    modifier = Modifier.fillMaxWidth().background(Color(0xFFFEE2E2), RoundedCornerShape(6.dp)).padding(10.dp)
                )
            }
        }
        if (loading) item {
            Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 3.dp, color = AdminEmeraldGreen)
                Text(tr("Cargando reporte…", "Loading report…"), color = AdminTextSecondary)
            }
        }
        events?.let { list ->
            item {
                val cards = listOf(
                    AdminEventKind.RECEIVED to Icons.Default.Inbox,
                    AdminEventKind.APPROVED to Icons.Default.CheckCircle,
                    AdminEventKind.REJECTED to Icons.Default.Cancel,
                    AdminEventKind.SUSPENDED to Icons.Default.PauseCircle,
                    AdminEventKind.REACTIVATED to Icons.Default.PlayCircle
                )
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    cards.forEach { (kind, icon) ->
                        AdminStatCard(kind.label(english), count(kind), icon, Modifier.weight(1f))
                    }
                }
            }
            item {
                Column(Modifier.padding(top = 8.dp, bottom = 4.dp)) {
                    Text(tr("Detalle de actividad", "Activity detail"), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = AdminTextPrimary)
                    Text(tr("Eventos registrados en el periodo seleccionado.", "Events recorded in the selected period."), fontSize = 12.sp, color = AdminTextSecondary)
                }
                Row(
                    Modifier.fillMaxWidth().background(AdminOffWhiteBg, RoundedCornerShape(8.dp))
                        .border(1.dp, AdminBorderLight, RoundedCornerShape(8.dp)).padding(12.dp)
                ) {
                    AdminColumnTitle(tr("FECHA Y HORA", "DATE AND TIME"), Modifier.weight(0.22f))
                    AdminColumnTitle(tr("EVENTO", "EVENT"), Modifier.weight(0.26f))
                    AdminColumnTitle(tr("VETERINARIA", "CLINIC"), Modifier.weight(0.32f))
                    AdminColumnTitle(tr("POR", "BY"), Modifier.weight(0.20f))
                }
            }
            if (list.isEmpty()) item {
                Text(tr("No hay actividad en el periodo.", "No activity in this period."), color = AdminTextSecondary, fontSize = 13.sp, modifier = Modifier.padding(12.dp))
            }
            itemsIndexed(list) { _, event ->
                Row(
                    Modifier.fillMaxWidth().border(1.dp, AdminBorderLight, RoundedCornerShape(8.dp)).padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(event.at.format(eventFormatter), Modifier.weight(0.22f), fontSize = 12.sp, color = AdminTextPrimary)
                    Text(event.kind.label(english), Modifier.weight(0.26f), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AdminTextPrimary)
                    Text(event.clinic, Modifier.weight(0.32f), fontSize = 12.sp, color = AdminTextSecondary)
                    Text(event.actor, Modifier.weight(0.20f), fontSize = 12.sp, color = AdminTextSecondary)
                }
            }
        }
    }
}

@Composable
private fun AdminStatCard(title: String, value: Int, icon: ImageVector, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.background(AdminOffWhiteBg, RoundedCornerShape(8.dp))
            .border(1.dp, AdminBorderLight, RoundedCornerShape(8.dp)).padding(14.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(36.dp).background(AdminSelectedBg, RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = AdminSelectedText, modifier = Modifier.size(20.dp))
        }
        Column {
            Text(title, fontSize = 11.sp, color = AdminTextSecondary)
            Text(value.toString(), fontSize = 20.sp, fontWeight = FontWeight.Bold, color = AdminTextPrimary)
        }
    }
}

@Composable
private fun AdminColumnTitle(text: String, modifier: Modifier = Modifier) {
    Text(text, modifier, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AdminTextMuted)
}

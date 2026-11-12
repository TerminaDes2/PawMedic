package com.pawsmedic.desktop.veterinary.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.pawsmedic.desktop.veterinary.supabase
import com.pawsmedic.desktop.veterinary.ui.theme.*
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Serializable
internal data class DateAvailability(
    @SerialName("tenant_id") val tenantId: String,
    @SerialName("schedule_date") val date: String,
    val hours: List<Int>
)

internal fun calendarDays(month: YearMonth): List<LocalDate?> {
    val first = month.atDay(1)
    val offset = first.dayOfWeek.value - 1
    val count = ((offset + month.lengthOfMonth() + 6) / 7) * 7
    return (0 until count).map { index ->
        val day = index - offset + 1
        if (day in 1..month.lengthOfMonth()) month.atDay(day) else null
    }
}

@Composable
internal fun DateAvailabilityDialog(tenantId: String, timeZone: String, onClose: () -> Unit) {
    val zone = remember(timeZone) { runCatching { ZoneId.of(timeZone) }.getOrNull() }
    val today = remember(zone) { zone?.let { LocalDate.now(it) } }
    val maxDate = remember(today) { today?.plusYears(2) }
    var month by remember(today) { mutableStateOf(today?.let { YearMonth.from(it) }) }
    var selectedDates by remember { mutableStateOf<Set<LocalDate>>(emptySet()) }
    var selectedHours by remember { mutableStateOf<Set<Int>>(emptySet()) }
    var overrides by remember { mutableStateOf<List<DateAvailability>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var loaded by remember { mutableStateOf(false) }
    var saving by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var notice by remember { mutableStateOf<String?>(null) }
    var confirmReset by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    suspend fun load() {
        loading = true
        loaded = false
        errorMessage = null
        try {
            check(today != null && maxDate != null) { "La zona horaria de la clínica no es válida." }
            overrides = supabase.postgrest["business_schedule_overrides"]
                .select {
                    filter {
                        eq("tenant_id", tenantId)
                        gte("schedule_date", today.toString())
                        lte("schedule_date", maxDate.toString())
                    }
                }
                .decodeList<DateAvailability>()
            loaded = true
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            errorMessage = "No se pudieron cargar los horarios por fecha. Verifica que el script de disponibilidad esté instalado."
        } finally {
            loading = false
        }
    }

    suspend fun save() {
        if (selectedDates.isEmpty()) {
            errorMessage = "Selecciona al menos una fecha."
            return
        }
        saving = true
        errorMessage = null
        try {
            val rows = selectedDates.sorted().map { DateAvailability(tenantId, it.toString(), selectedHours.sorted()) }
            val saved = supabase.postgrest["business_schedule_overrides"].upsert(rows) {
                onConflict = "tenant_id,schedule_date"
                select()
            }.decodeList<DateAvailability>()
            check(saved.size == rows.size) { "No se pudieron confirmar todas las fechas. Actualiza antes de volver a guardar." }
            selectedDates = emptySet()
            notice = "Disponibilidad guardada para ${saved.size} fecha(s)."
            load()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            errorMessage = if (e is IllegalStateException) e.message else "No se pudieron guardar las fechas. Revisa la conexión y los permisos."
        } finally {
            saving = false
        }
    }

    suspend fun reset() {
        val dates = selectedDates.map { it.toString() }
        if (dates.isEmpty()) return
        saving = true
        errorMessage = null
        try {
            val removed = supabase.postgrest["business_schedule_overrides"].delete {
                filter { eq("tenant_id", tenantId); isIn("schedule_date", dates) }
                select()
            }.decodeList<DateAvailability>()
            val expected = overrides.count { it.date in dates }
            check(removed.size == expected) { "No se pudieron restablecer todas las fechas. Actualiza e inténtalo de nuevo." }
            selectedDates = emptySet()
            confirmReset = false
            notice = "Se restableció el horario semanal para las fechas seleccionadas."
            load()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            confirmReset = false
            errorMessage = if (e is IllegalStateException) e.message else "No se pudieron restablecer las fechas seleccionadas."
        } finally {
            saving = false
        }
    }

    LaunchedEffect(tenantId) { load() }

    Dialog(onDismissRequest = { if (!saving) onClose() }) {
        Card(Modifier.width(760.dp).heightIn(max = 850.dp), shape = RoundedCornerShape(16.dp)) {
            Column(Modifier.padding(20.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Horarios por fecha", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    TextButton(onClick = onClose, enabled = !saving) { Text("Cerrar") }
                }
                Text("Selecciona fechas y las franjas de una hora. En esas fechas se sustituye el horario semanal.", color = TextSecondary, fontSize = 13.sp)
                Text("Zona horaria: $timeZone. Las citas existentes no se cancelan.", color = TextSecondary, fontSize = 12.sp)
                if (zone == null || today == null || maxDate == null || month == null) {
                    Text("La zona horaria de la clínica no es válida; no se pueden configurar fechas.", color = Color(0xFFB91C1C))
                } else {
                    val currentMonth = month!!
                    val firstMonth = YearMonth.from(today)
                    val lastMonth = YearMonth.from(maxDate)
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedButton(onClick = { month = currentMonth.minusMonths(1) }, enabled = currentMonth > firstMonth && !saving) { Text("‹") }
                        MonthPicker(currentMonth, firstMonth, lastMonth, !saving) { month = it }
                        OutlinedButton(onClick = { month = currentMonth.plusMonths(1) }, enabled = currentMonth < lastMonth && !saving) { Text("›") }
                        Spacer(Modifier.weight(1f))
                        TextButton(onClick = {
                            selectedDates = selectedDates + (1..currentMonth.lengthOfMonth()).map { currentMonth.atDay(it) }
                                .filter { !it.isBefore(today) && !it.isAfter(maxDate) }
                        }, enabled = !saving && !loading && loaded) { Text("Seleccionar mes") }
                    }
                    Row(Modifier.fillMaxWidth()) {
                        listOf("Lun", "Mar", "Mié", "Jue", "Vie", "Sáb", "Dom").forEach {
                            Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                                Text(it, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                            }
                        }
                    }
                    calendarDays(currentMonth).chunked(7).forEach { week ->
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            week.forEach { date ->
                                val available = date != null && !date.isBefore(today) && !date.isAfter(maxDate)
                                val selected = date in selectedDates
                                val hasOverride = date != null && overrides.any { it.date == date.toString() }
                                Box(
                                    Modifier.weight(1f).height(44.dp)
                                        .background(if (selected) EmeraldLightBg else OffWhiteBg, RoundedCornerShape(6.dp))
                                        .border(1.dp, if (selected) ClinicalEmerald else BorderLight, RoundedCornerShape(6.dp))
                                        .clickable(enabled = available && !saving && !loading && loaded) {
                                            if (date != null) {
                                                if (selectedDates.isEmpty()) {
                                                    selectedHours = overrides.firstOrNull { it.date == date.toString() }?.hours?.toSet() ?: emptySet()
                                                }
                                                selectedDates = if (selected) selectedDates - date else selectedDates + date
                                            }
                                        }, contentAlignment = Alignment.Center
                                ) {
                                    if (date != null) Text("${date.dayOfMonth}${if (hasOverride) " •" else ""}", color = if (available) TextPrimary else TextMuted)
                                }
                            }
                        }
                    }
                    Text("${selectedDates.size} fecha(s) seleccionada(s). • indica una fecha con horario especial.", color = TextSecondary, fontSize = 12.sp)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = selectedHours.size == 24,
                            onCheckedChange = { selectedHours = if (it) (0..23).toSet() else emptySet() },
                            enabled = !saving && !loading && loaded
                        )
                        Text("Seleccionar todas las horas", fontWeight = FontWeight.Medium)
                    }
                    (0..23).toList().chunked(6).forEach { group ->
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            group.forEach { hour ->
                                val selected = hour in selectedHours
                                Row(
                                    Modifier.weight(1f).background(if (selected) EmeraldLightBg else OffWhiteBg, RoundedCornerShape(6.dp))
                                        .clickable(enabled = !saving && !loading && loaded) {
                                            selectedHours = if (selected) selectedHours - hour else selectedHours + hour
                                        }.padding(vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Text("${hour.toString().padStart(2, '0')}:00", fontSize = 12.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal)
                                    if (selected) Text(" ✓", color = ClinicalEmerald, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                    Text("Cada selección cubre una hora. Sin horas seleccionadas, la fecha queda cerrada. Guardar reemplaza cualquier horario especial anterior.", color = TextSecondary, fontSize = 12.sp)
                    if (loading) Text("Cargando horarios…", color = TextSecondary)
                    notice?.let { Text(it, color = Color(0xFF166534)) }
                    errorMessage?.let { Text(it, color = Color(0xFFB91C1C)) }
                    if (!loaded && !loading) TextButton(onClick = { scope.launch { load() } }) { Text("Reintentar carga") }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { confirmReset = true }, enabled = selectedDates.isNotEmpty() && !saving && !loading && loaded) { Text("Usar horario semanal") }
                        Spacer(Modifier.width(8.dp))
                        Button(onClick = { scope.launch { save() } }, enabled = selectedDates.isNotEmpty() && !saving && !loading && loaded) {
                            Text(if (saving) "Guardando…" else "Guardar fechas")
                        }
                    }
                }
            }
        }
    }
    if (confirmReset) {
        AlertDialog(
            onDismissRequest = { if (!saving) confirmReset = false },
            title = { Text("Restablecer horario semanal") },
            text = { Text("Se eliminarán los horarios especiales de las fechas seleccionadas. Las citas existentes no se cancelarán.") },
            confirmButton = { TextButton(onClick = { scope.launch { reset() } }, enabled = !saving) { Text("Restablecer") } },
            dismissButton = { TextButton(onClick = { confirmReset = false }, enabled = !saving) { Text("Cancelar") } }
        )
    }
}

@Composable
private fun MonthPicker(
    month: YearMonth,
    first: YearMonth,
    last: YearMonth,
    enabled: Boolean,
    onChange: (YearMonth) -> Unit
) {
    var showMonths by remember { mutableStateOf(false) }
    var showYears by remember { mutableStateOf(false) }
    val locale = remember { Locale("es", "MX") }
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Box {
            OutlinedButton(onClick = { showMonths = true }, enabled = enabled) {
                Text(month.format(DateTimeFormatter.ofPattern("MMMM", locale)).replaceFirstChar { it.uppercase() })
            }
            DropdownMenu(expanded = showMonths, onDismissRequest = { showMonths = false }, modifier = Modifier.heightIn(max = 300.dp)) {
                (1..12).forEach { value ->
                    val candidate = YearMonth.of(month.year, value)
                    DropdownMenuItem(
                        text = { Text(candidate.format(DateTimeFormatter.ofPattern("MMMM", locale)).replaceFirstChar { it.uppercase() }) },
                        enabled = candidate >= first && candidate <= last,
                        onClick = { onChange(candidate); showMonths = false }
                    )
                }
            }
        }
        Box {
            OutlinedButton(onClick = { showYears = true }, enabled = enabled) { Text(month.year.toString()) }
            DropdownMenu(expanded = showYears, onDismissRequest = { showYears = false }) {
                (first.year..last.year).forEach { value ->
                    DropdownMenuItem(text = { Text(value.toString()) }, onClick = {
                        onChange(YearMonth.of(value, month.monthValue).coerceIn(first, last))
                        showYears = false
                    })
                }
            }
        }
    }
}

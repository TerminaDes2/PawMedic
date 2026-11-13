package com.pawsmedic.desktop.veterinary.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.pawsmedic.desktop.veterinary.model.resolveTenantId
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
import kotlinx.serialization.json.JsonObject

private val weekdays = listOf("Domingo", "Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado")

@Serializable
internal data class ClinicShift(
    val id: String,
    @SerialName("day_of_week") val day: Int,
    @SerialName("open_time") val openTime: String,
    @SerialName("close_time") val closeTime: String,
    @SerialName("is_active") val active: Boolean
)

@Serializable
private data class ClinicTimeZone(@SerialName("time_zone") val timeZone: String)

@Serializable
private data class NewClinicShift(
    @SerialName("tenant_id") val tenantId: String,
    @SerialName("day_of_week") val day: Int,
    @SerialName("open_time") val openTime: String,
    @SerialName("close_time") val closeTime: String,
    @SerialName("is_active") val active: Boolean = true
)

internal fun shiftValidationError(
    day: Int,
    open: String,
    close: String,
    existing: List<ClinicShift>,
    editingId: String? = null
): String? {
    val format = Regex("^([01]\\d|2[0-3]):00$")
    if (day !in 0..6 || !format.matches(open) || (!format.matches(close) && close != "24:00")) {
        return "Selecciona horas en punto, por ejemplo 09:00."
    }
    if (open >= close) return "La hora de cierre debe ser posterior a la apertura en el mismo día."
    if (existing.any {
            it.id != editingId && it.day == day && it.active &&
                open < it.closeTime.take(5) && close > it.openTime.take(5)
        }) {
        return "Este turno se superpone con otro horario activo del mismo día."
    }
    return null
}

internal fun shiftsValidationError(
    days: Set<Int>,
    open: String,
    close: String,
    existing: List<ClinicShift>,
    editingId: String? = null
): String? {
    if (days.isEmpty()) return "Selecciona al menos un día."
    for (day in days.sorted()) {
        val error = shiftValidationError(day, open, close, existing, editingId)
        if (error != null) return "${weekdays.getOrElse(day) { "Día $day" }}: $error"
    }
    return null
}

internal fun selectedHourRanges(hours: Set<Int>): List<Pair<String, String>> {
    require(hours.all { it in 0..23 })
    val sorted = hours.sorted()
    if (sorted.isEmpty()) return emptyList()
    val ranges = mutableListOf<Pair<String, String>>()
    var start = sorted.first()
    var last = start
    for (hour in sorted.drop(1)) {
        if (hour != last + 1) {
            ranges.add("%02d:00".format(start) to "%02d:00".format(last + 1))
            start = hour
        }
        last = hour
    }
    ranges.add("%02d:00".format(start) to "%02d:00".format(last + 1))
    return ranges
}

@Composable
private fun TimeChoice(label: String, value: String, enabled: Boolean, allowMidnight: Boolean = false, onChange: (String) -> Unit) {
    val parts = value.split(":")
    val hour = parts.getOrNull(0)?.toIntOrNull()?.coerceIn(0, if (allowMidnight) 24 else 23) ?: 0
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label, color = TextSecondary, fontSize = 12.sp)
        Row(verticalAlignment = Alignment.CenterVertically) {
            NumberChoice(hour, if (allowMidnight) 24 else 23, enabled) { onChange("${it.toString().padStart(2, '0')}:00") }
            Text(":00", Modifier.padding(start = 4.dp), fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun NumberChoice(value: Int, maximum: Int, enabled: Boolean, onChange: (Int) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        OutlinedButton(onClick = { expanded = true }, enabled = enabled, contentPadding = PaddingValues(horizontal = 12.dp)) {
            Text(value.toString().padStart(2, '0'))
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }, modifier = Modifier.heightIn(max = 300.dp)) {
            (0..maximum).forEach { number ->
                DropdownMenuItem(
                    text = { Text(number.toString().padStart(2, '0')) },
                    onClick = { onChange(number); expanded = false }
                )
            }
        }
    }
}

@Composable
fun ScheduleScreen(
    userEmail: String = "",
    onNavigate: (String) -> Unit,
    onLogout: () -> Unit = {}
) {
    var tenantId by remember { mutableStateOf<String?>(null) }
    var timeZone by remember { mutableStateOf("") }
    var shifts by remember { mutableStateOf<List<ClinicShift>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var saving by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var notice by remember { mutableStateOf<String?>(null) }
    var editing by remember { mutableStateOf<ClinicShift?>(null) }
    var showEditor by remember { mutableStateOf(false) }
    var deleting by remember { mutableStateOf<ClinicShift?>(null) }
    var selectedDays by remember { mutableStateOf(setOf(1)) }
    var selectedHours by remember { mutableStateOf<Set<Int>>(emptySet()) }
    var open by remember { mutableStateOf("09:00") }
    var close by remember { mutableStateOf("18:00") }
    val scope = rememberCoroutineScope()

    suspend fun load() {
        loading = true
        errorMessage = null
        try {
            val userId = supabase.auth.currentSessionOrNull()?.user?.id
                ?: error("Inicia sesión nuevamente.")
            val profile = supabase.postgrest["profiles"]
                .select { filter { eq("id", userId) } }.decodeSingle<UserProfile>()
            check(profile.role == ProfileRole.VETERINARY_BUSINESS) { "Esta pantalla es exclusiva para clínicas veterinarias." }
            val clinicId = resolveTenantId(supabase, userId, profile)
            tenantId = clinicId
            timeZone = supabase.postgrest["tenants"].select { filter { eq("id", clinicId) } }
                .decodeSingle<ClinicTimeZone>().timeZone
            shifts = supabase.postgrest["business_schedules"]
                .select { filter { eq("tenant_id", clinicId) } }
                .decodeList<ClinicShift>()
                .sortedWith(compareBy<ClinicShift> { (it.day + 6) % 7 }.thenBy { it.openTime })
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            errorMessage = if (e is IllegalStateException) e.message else
                "No se pudieron cargar los horarios. Comprueba la conexión y la configuración de Supabase."
        } finally {
            loading = false
        }
    }

    suspend fun save() {
        val clinicId = tenantId ?: return
        val start = open.trim()
        val end = close.trim()
        val ranges = if (editing == null) selectedHourRanges(selectedHours) else listOf(start to end)
        val validation = when {
            selectedDays.isEmpty() -> "Selecciona al menos un día."
            ranges.isEmpty() -> "Selecciona al menos una franja horaria."
            else -> ranges.firstNotNullOfOrNull { (from, to) ->
                shiftsValidationError(selectedDays, from, to, shifts, editing?.id)
            }
        }
        if (validation != null) {
            errorMessage = validation
            return
        }
        saving = true
        errorMessage = null
        try {
            val current = editing
            if (current == null) {
                val inserted = supabase.postgrest["business_schedules"]
                    .insert(selectedDays.sorted().flatMap { day ->
                        ranges.map { (from, to) -> NewClinicShift(clinicId, day, from, to) }
                    }) { select() }
                    .decodeList<ClinicShift>()
                check(inserted.size == selectedDays.size * ranges.size) { "No se pudieron confirmar todos los turnos. Actualiza la pantalla antes de volver a intentarlo." }
            } else {
                val updated = supabase.postgrest["business_schedules"].update({
                    set("day_of_week", selectedDays.single())
                    set("open_time", start)
                    set("close_time", end)
                    set("is_active", true)
                }) { filter { eq("id", current.id); eq("tenant_id", clinicId) }; select() }
                    .decodeList<JsonObject>()
                check(updated.size == 1) { "No se actualizó el turno. Actualiza la pantalla e inténtalo de nuevo." }
            }
            showEditor = false
            notice = if (current == null && selectedDays.size > 1) "Horarios guardados." else "Horario guardado."
            load()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            errorMessage = if (e is IllegalStateException) e.message else "No se pudo guardar el horario. Comprueba los permisos y vuelve a intentarlo."
        } finally {
            saving = false
        }
    }

    suspend fun delete(shift: ClinicShift) {
        val clinicId = tenantId ?: return
        saving = true
        errorMessage = null
        try {
            val removed = supabase.postgrest["business_schedules"].delete {
                filter { eq("id", shift.id); eq("tenant_id", clinicId) }
                select()
            }.decodeList<JsonObject>()
            check(removed.size == 1) { "No se eliminó el turno. Actualiza la pantalla e inténtalo de nuevo." }
            deleting = null
            notice = "Horario eliminado."
            load()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            deleting = null
            errorMessage = if (e is IllegalStateException) e.message else "No se pudo eliminar el horario. Comprueba los permisos y vuelve a intentarlo."
        } finally {
            saving = false
        }
    }

    LaunchedEffect(Unit) { load() }

    VetAppLayout("Horarios", onNavigate, onLogout, "Horarios de atención", userEmail) {
        Column(
            Modifier.fillMaxSize().background(Color.White, RoundedCornerShape(12.dp))
                .border(1.dp, BorderLight, RoundedCornerShape(12.dp)).padding(20.dp)
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text("Horarios de la clínica", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text("Agrega los turnos semanales en los que aceptas citas.", fontSize = 12.sp, color = TextSecondary)
                    if (timeZone.isNotBlank()) Text("Zona horaria: $timeZone", fontSize = 12.sp, color = TextSecondary)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = { scope.launch { load() } }, enabled = !loading && !saving) { Text("Actualizar") }
                    Button(onClick = {
                        editing = null; selectedDays = setOf(1); selectedHours = emptySet(); errorMessage = null; showEditor = true
                    }, enabled = tenantId != null && !loading && !saving) { Text("Agregar horario") }
                }
            }
            Spacer(Modifier.height(16.dp))
            notice?.let { Text(it, color = Color(0xFF166534), fontSize = 13.sp) }
            if (!showEditor) errorMessage?.let { Text(it, color = Color(0xFFB91C1C), fontSize = 13.sp) }
            if (loading) Text("Cargando horarios…", color = TextSecondary)
            else if (shifts.isEmpty()) Text("No hay horarios configurados. Agrega un turno para que tus clientes puedan reservar.", color = TextSecondary)
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxSize()) {
                items(shifts, key = { it.id }) { shift ->
                    Row(
                        Modifier.fillMaxWidth().border(1.dp, BorderLight, RoundedCornerShape(7.dp)).padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(weekdays.getOrElse(shift.day) { "Día ${shift.day}" }, Modifier.weight(1f), fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text("${shift.openTime.take(5)} – ${shift.closeTime.take(5)}", Modifier.weight(1f), color = TextSecondary)
                        Text(if (shift.active) "Activo" else "Inactivo", Modifier.weight(0.7f), color = TextSecondary)
                        TextButton(onClick = {
                            editing = shift; selectedDays = setOf(shift.day); open = shift.openTime.take(5); close = shift.closeTime.take(5)
                            errorMessage = null; showEditor = true
                        }, enabled = !saving) { Text("Editar") }
                        TextButton(onClick = { deleting = shift }, enabled = !saving) { Text("Eliminar", color = Color(0xFFB91C1C)) }
                    }
                }
            }
        }

        if (showEditor) Dialog(onDismissRequest = { if (!saving) showEditor = false }) {
            Card(Modifier.width(600.dp), shape = RoundedCornerShape(16.dp)) {
                Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(if (editing == null) "Agregar horario" else "Editar horario", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text(
                        if (editing == null) "Selecciona los días en los que se repetirá este horario cada semana."
                        else "Selecciona el día para este turno semanal.",
                        color = TextSecondary
                    )
                    if (editing == null) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = selectedDays.size == 7,
                                onCheckedChange = { selectedDays = if (it) (0..6).toSet() else emptySet() },
                                enabled = !saving
                            )
                            Text("Seleccionar todos los días", color = TextPrimary)
                        }
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(1, 2, 3, 4, 5, 6, 0).forEach { value ->
                            val selected = value in selectedDays
                            Column(
                                modifier = Modifier.weight(1f)
                                    .background(if (selected) EmeraldLightBg else OffWhiteBg, RoundedCornerShape(8.dp))
                                    .border(1.dp, if (selected) ClinicalEmerald else BorderLight, RoundedCornerShape(8.dp))
                                    .clickable(enabled = !saving) {
                                        selectedDays = if (editing != null) setOf(value)
                                        else if (selected) selectedDays - value else selectedDays + value
                                    }
                                    .padding(vertical = 10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(weekdays[value].take(3), fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 12.sp)
                                Text(if (selected) "✓" else "–", color = if (selected) ClinicalEmerald else TextMuted)
                            }
                        }
                    }
                    Text("Horario de 24 horas · zona: $timeZone", color = TextSecondary, fontSize = 12.sp)
                    if (editing == null) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = selectedHours.size == 24, onCheckedChange = {
                                selectedHours = if (it) (0..23).toSet() else emptySet()
                            }, enabled = !saving)
                            Text("Seleccionar todas las horas")
                        }
                        (0..23).toList().chunked(6).forEach { group ->
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                                group.forEach { hour ->
                                    val selected = hour in selectedHours
                                    Box(
                                        Modifier.weight(1f)
                                            .background(if (selected) EmeraldLightBg else OffWhiteBg, RoundedCornerShape(6.dp))
                                            .border(1.dp, if (selected) ClinicalEmerald else BorderLight, RoundedCornerShape(6.dp))
                                            .clickable(enabled = !saving) {
                                                selectedHours = if (selected) selectedHours - hour else selectedHours + hour
                                            }.padding(vertical = 10.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("${hour.toString().padStart(2, '0')}:00", color = if (selected) ClinicalEmerald else TextPrimary)
                                    }
                                }
                            }
                        }
                        Text("Cada franja dura una hora y se repite semanalmente. Las horas consecutivas se agrupan.", color = TextSecondary, fontSize = 12.sp)
                    } else {
                        Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                            TimeChoice("Apertura", open, !saving) { open = it }
                            TimeChoice("Cierre", close, !saving, allowMidnight = true) { close = it }
                        }
                    }
                    if (editing?.active == false) Text("Al guardar, este turno se activará.", color = TextSecondary)
                    errorMessage?.let { Text(it, color = Color(0xFFB91C1C), fontSize = 13.sp) }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { showEditor = false }, enabled = !saving) { Text("Cancelar") }
                        Button(onClick = { scope.launch { save() } }, enabled = !saving) { Text(if (saving) "Guardando…" else "Guardar") }
                    }
                }
            }
        }
        deleting?.let { shift ->
            AlertDialog(
                onDismissRequest = { if (!saving) deleting = null },
                title = { Text("Eliminar horario") },
                text = { Text("¿Eliminar el turno del ${weekdays.getOrElse(shift.day) { "día ${shift.day}" }} de ${shift.openTime.take(5)} a ${shift.closeTime.take(5)}? Las citas existentes no se cancelarán.") },
                confirmButton = { TextButton(onClick = { scope.launch { delete(shift) } }, enabled = !saving) { Text("Eliminar") } },
                dismissButton = { TextButton(onClick = { deleting = null }, enabled = !saving) { Text("Cancelar") } }
            )
        }
    }
}

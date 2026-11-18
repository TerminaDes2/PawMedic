package com.pawsmedic.features.businesses.presentation.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pawsmedic.shared.core.designsystem.theme.PawMedicColors
import com.pawsmedic.features.businesses.domain.model.BusinessSchedule

internal data class LocalTime(val dayOfWeek: Int, val hour: Int, val minute: Int)
private data class OpeningStatus(val isOpen: Boolean, val message: String)
private data class ScheduleGroup(val days: String, val hours: String)

@Composable
fun OpeningHoursCard(schedules: List<BusinessSchedule>) {
    val now = currentLocalTime()
    val status = openingStatus(schedules, now)
    val weekdayGroups = groupSchedules(schedules.filter { it.dayOfWeek in 1..5 })
    val hasWeekendHours = schedules.any { it.isActive && it.dayOfWeek in setOf(0, 6) }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, PawMedicColors.Gray200, RoundedCornerShape(16.dp))
            .background(PawMedicColors.White, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Schedule, contentDescription = null, tint = PawMedicColors.Teal600, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("Horarios de atención", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = PawMedicColors.Gray900)
                }
                Text(
                    text = status.message,
                    modifier = Modifier.weight(1f),
                    color = if (status.isOpen) PawMedicColors.Teal700 else PawMedicColors.Gray600,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.End
                )
            }
            weekdayGroups.forEach { group ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(group.days, color = PawMedicColors.Gray600, fontSize = 16.sp)
                    Text(group.hours, color = PawMedicColors.Gray900, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
            if (weekdayGroups.isEmpty()) {
                Text("Horario entre semana no registrado", color = PawMedicColors.Gray600, fontSize = 14.sp)
            }
            Text(
                text = if (hasWeekendHours) "Fin de semana · horario especial" else "Sábado y domingo cerrado",
                color = PawMedicColors.Gray500,
                fontSize = 14.sp
            )
        }
    }
}

private fun groupSchedules(schedules: List<BusinessSchedule>): List<ScheduleGroup> {
    val byDay = schedules
        .filter { it.isActive }
        .groupBy { it.dayOfWeek }
        .mapValues { (_, entries) ->
            entries.sortedBy { it.openTime }
                .joinToString("\n") { "${it.openTime.take(5)} – ${it.closeTime.take(5)}" }
        }
    return byDay.entries
        .groupBy { it.value }
        .map { (hours, entries) ->
            ScheduleGroup(dayRange(entries.map { it.key }.sorted()), hours)
        }
        .sortedBy { it.days }
}

private fun dayRange(days: List<Int>): String {
    if (days.size == 5 && days == (1..5).toList()) return "Lunes a viernes"
    return days.joinToString(", ") { dayName(it) }
}

private fun dayName(dayOfWeek: Int) = when (dayOfWeek) {
    0 -> "Domingo"
    1 -> "Lunes"
    2 -> "Martes"
    3 -> "Miércoles"
    4 -> "Jueves"
    5 -> "Viernes"
    6 -> "Sábado"
    else -> "Día"
}

private fun openingStatus(schedules: List<BusinessSchedule>, now: LocalTime): OpeningStatus {
    val today = if (now.dayOfWeek == 6) 0 else now.dayOfWeek + 1
    val todaySchedules = schedules.filter { it.isActive && it.dayOfWeek == today }
    val minutes = now.hour * 60 + now.minute
    todaySchedules.forEach { schedule ->
        val start = parseMinutes(schedule.openTime)
        val end = parseMinutes(schedule.closeTime)
        if (start != null && end != null && minutes in start until end) {
            return OpeningStatus(true, "Abierto ahora · cierra ${schedule.closeTime.take(5)}")
        }
    }
    val nextOpening = todaySchedules
        .mapNotNull { parseMinutes(it.openTime)?.let { minutes -> minutes to it.openTime.take(5) } }
        .filter { it.first > minutes }
        .minByOrNull { it.first }
        ?.second
    return OpeningStatus(false, "Cerrado ahora" + (nextOpening?.let { " · abre $it" } ?: ""))
}

private fun parseMinutes(value: String): Int? {
    val parts = value.trim().split(":")
    val hour = parts.getOrNull(0)?.toIntOrNull() ?: return null
    val minute = parts.getOrNull(1)?.toIntOrNull() ?: 0
    return hour * 60 + minute
}
internal expect fun currentLocalTime(): LocalTime

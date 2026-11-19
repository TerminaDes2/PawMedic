package com.pawsmedic.features.businesses.presentation.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
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
import com.pawsmedic.features.businesses.domain.model.BusinessSchedule
import com.pawsmedic.shared.core.designsystem.language.AppLanguage
import com.pawsmedic.shared.core.designsystem.language.LocalLanguagePreference
import com.pawsmedic.shared.core.designsystem.theme.PawMedicSemanticColors

internal data class LocalTime(val dayOfWeek: Int, val hour: Int, val minute: Int)
private data class OpeningStatus(val isOpen: Boolean, val message: String)
private data class ScheduleGroup(val days: String, val hours: String)

@Composable
fun OpeningHoursCard(schedules: List<BusinessSchedule>, timeZone: String) {
    val language = LocalLanguagePreference.current?.selected ?: AppLanguage.SPANISH
    val now = currentLocalTime(timeZone)
    val status = openingStatus(schedules, now, language)
    val weekdayGroups = groupSchedules(schedules.filter { it.isActive && it.dayOfWeek in 1..5 }, language)
    val weekendSchedules = schedules.filter { it.isActive && it.dayOfWeek in setOf(0, 6) }

    Box(
        modifier = Modifier.fillMaxWidth()
            .border(1.dp, PawMedicSemanticColors.current.separator, RoundedCornerShape(16.dp))
            .background(PawMedicSemanticColors.current.surface, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Schedule, null, tint = PawMedicSemanticColors.current.accent, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(language.text("Horarios de atención", "Opening hours"), fontSize = 17.sp, fontWeight = FontWeight.Bold, color = PawMedicSemanticColors.current.text)
                }
                Text(
                    status.message,
                    modifier = Modifier.weight(1f),
                    color = if (status.isOpen) PawMedicSemanticColors.current.accent else PawMedicSemanticColors.current.secondaryText,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.End
                )
            }

            if (weekdayGroups.isEmpty()) {
                Text(language.text("Horario entre semana no registrado", "No weekday schedule recorded"), color = PawMedicSemanticColors.current.secondaryText, fontSize = 13.sp)
            } else {
                weekdayGroups.forEach { group ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(group.days, color = PawMedicSemanticColors.current.secondaryText, fontSize = 14.sp)
                        Text(group.hours, color = PawMedicSemanticColors.current.text, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            if (weekendSchedules.isEmpty()) {
                Text(language.text("Fin de semana cerrado", "Closed on weekends"), color = PawMedicSemanticColors.current.secondaryText, fontSize = 13.sp)
            } else {
                val weekendText = weekendSchedules
                    .sortedWith(compareBy<BusinessSchedule> { it.dayOfWeek }.thenBy { it.openTime })
                    .joinToString(" · ") { "${dayName(it.dayOfWeek, language)} ${it.openTime.take(5)}–${it.closeTime.take(5)}" }
                Text(weekendText, color = PawMedicSemanticColors.current.secondaryText, fontSize = 13.sp)
            }
        }
    }
}

private fun groupSchedules(schedules: List<BusinessSchedule>, language: AppLanguage): List<ScheduleGroup> {
    val byDay = schedules.groupBy { it.dayOfWeek }.mapValues { (_, entries) ->
        entries.sortedBy { it.openTime }.joinToString("\n") { "${it.openTime.take(5)}–${it.closeTime.take(5)}" }
    }
    return byDay.entries.groupBy { it.value }.map { (hours, entries) ->
        ScheduleGroup(dayRange(entries.flatMap { entry -> listOf(entry.key) }.sorted(), language), hours)
    }.sortedBy { it.days }
}

private fun dayRange(days: List<Int>, language: AppLanguage): String {
    if (days.size == 5 && days == listOf(1, 2, 3, 4, 5)) return language.text("Lunes a viernes", "Monday to Friday")
    return days.joinToString(", ") { dayName(it, language) }
}

private fun dayName(dayOfWeek: Int, language: AppLanguage): String = when (dayOfWeek) {
    0 -> language.text("Domingo", "Sunday")
    1 -> language.text("Lunes", "Monday")
    2 -> language.text("Martes", "Tuesday")
    3 -> language.text("Miércoles", "Wednesday")
    4 -> language.text("Jueves", "Thursday")
    5 -> language.text("Viernes", "Friday")
    6 -> language.text("Sábado", "Saturday")
    else -> language.text("Día", "Day")
}

private fun openingStatus(schedules: List<BusinessSchedule>, now: LocalTime, language: AppLanguage): OpeningStatus {
    val todaySchedules = schedules.filter { it.isActive && it.dayOfWeek == now.dayOfWeek }
    val minutes = now.hour * 60 + now.minute

    todaySchedules.forEach { schedule ->
        val start = parseMinutes(schedule.openTime)
        val end = parseMinutes(schedule.closeTime)
        if (start != null && end != null) {
            val openNow = if (end > start) minutes in start until end else minutes >= start
            if (openNow) {
                val closeLabel = schedule.closeTime.take(5)
                return OpeningStatus(true, language.text("Abierto ahora · cierra $closeLabel", "Open now · closes $closeLabel"))
            }
        }
    }

    val previousDay = (now.dayOfWeek + 6) % 7
    schedules.filter { it.isActive && it.dayOfWeek == previousDay }.forEach { schedule ->
        val start = parseMinutes(schedule.openTime)
        val end = parseMinutes(schedule.closeTime)
        if (start != null && end != null && end <= start && minutes < end) {
            val closeLabel = schedule.closeTime.take(5)
            return OpeningStatus(true, language.text("Abierto ahora · cierra $closeLabel", "Open now · closes $closeLabel"))
        }
    }

    val nextOpening = todaySchedules
        .mapNotNull { schedule -> parseMinutes(schedule.openTime)?.let { it to schedule.openTime.take(5) } }
        .filter { it.first > minutes }
        .minByOrNull { it.first }
        ?.second


    return OpeningStatus(false, language.text(
        "Cerrado ahora" + (nextOpening?.let { " · abre $it" } ?: ""),
        "Closed now" + (nextOpening?.let { " · opens $it" } ?: "")
    ))
}

private fun parseMinutes(value: String): Int? {
    val normalized = value.trim()
    if (normalized == "24:00") return 24 * 60
    val parts = normalized.split(':')
    val hour = parts.getOrNull(0)?.toIntOrNull() ?: return null
    val minute = parts.getOrNull(1)?.toIntOrNull() ?: 0
    return hour * 60 + minute
}

internal expect fun currentLocalTime(timeZone: String): LocalTime

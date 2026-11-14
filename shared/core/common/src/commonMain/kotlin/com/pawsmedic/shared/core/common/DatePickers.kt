package com.pawsmedic.shared.core.common

fun convertMillisToIsoDateString(millis: Long): String {
    // Material 3 DatePickerMillis returns UTC Midnight.
    // Adding 12 hours (43200000 ms) ensures the calculation falls squarely in the middle of the selected day.
    val adjustedMillis = millis + 43200000L
    val totalSeconds = adjustedMillis / 1000
    val totalDays = (totalSeconds / 86400).toInt()

    var days = totalDays + 719468
    val era = (if (days >= 0) days else days - 146096) / 146097
    val doe = days - era * 146097
    val yoe = (doe - doe / 1460 + doe / 36524 - doe / 146096) / 365
    val y = yoe + era * 400
    val doy = doe - (365 * yoe + yoe / 4 - yoe / 100)
    val mp = (5 * doy + 2) / 153
    val d = doy - (153 * mp + 2) / 5 + 1
    val m = mp + if (mp < 10) 3 else -9
    val year = y + if (m <= 2) 1 else 0

    val dayStr = d.toString().padStart(2, '0')
    val monthStr = m.toString().padStart(2, '0')
    return "$year-$monthStr-$dayStr"
}

fun formatIsoToDisplayDate(isoDateStr: String): String {
    val clean = isoDateStr.trim()
    val parts = clean.split("-")
    if (parts.size != 3) return isoDateStr
    val year = parts[0]
    val month = parts[1]
    val day = parts[2]
    val shortYear = if (year.length >= 4) year.takeLast(2) else year
    return "$day-$month-$shortYear" // dd-MM-yy
}

fun calculateAgeFromIsoDate(dateOfBirth: String?): String? {
    if (dateOfBirth.isNullOrBlank()) return null
    val cleanStr = dateOfBirth.trim()
    val parts = cleanStr.split("-")
    if (parts.size != 3) return null
    val bYear = parts[0].toIntOrNull() ?: return null
    val bMonth = parts[1].toIntOrNull() ?: return null
    val bDay = parts[2].toIntOrNull() ?: return null

    val todayIso = convertMillisToIsoDateString(System.currentTimeMillis())
    val todayParts = todayIso.split("-")
    val cYear = todayParts[0].toInt()
    val cMonth = todayParts[1].toInt()
    val cDay = todayParts[2].toInt()

    var years = cYear - bYear
    var months = cMonth - bMonth
    var days = cDay - bDay

    if (days < 0) {
        months -= 1
    }
    if (months < 0) {
        years -= 1
        months += 12
    }

    val totalMonths = years * 12 + maxOf(0, months)
    val totalDays = (cYear * 365 + cMonth * 30 + cDay) - (bYear * 365 + bMonth * 30 + bDay)

    if (totalDays <= 0) return "1 semana"

    return when {
        years >= 1 -> {
            if (months > 0) "$years años $months meses" else if (years == 1) "1 año" else "$years años"
        }
        totalMonths >= 1 -> {
            if (totalMonths == 1) "1 mes" else "$totalMonths meses"
        }
        else -> {
            val weeks = maxOf(1, totalDays / 7)
            if (weeks == 1) "1 semana" else "$weeks semanas"
        }
    }
}

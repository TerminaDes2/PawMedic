package com.pawsmedic.shared.core.common

fun convertMillisToIsoDateString(millis: Long): String {
    val totalSeconds = millis / 1000
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

package com.pawsmedic.features.appointments.presentation.booking

internal expect fun bookingToday(): String

internal fun bookingDateLimit(today: String): String {
    val year = today.substring(0, 4).toInt() + 2
    val month = today.substring(5, 7).toInt()
    val day = today.substring(8, 10).toInt()
    val lastDay = when (month) {
        2 -> if (year % 4 == 0 && (year % 100 != 0 || year % 400 == 0)) 29 else 28
        4, 6, 9, 11 -> 30
        else -> 31
    }
    return "$year-${month.toString().padStart(2, '0')}-${minOf(day, lastDay).toString().padStart(2, '0')}"
}

internal fun isBookableDate(date: String, today: String): Boolean {
    if (!Regex("""\d{4}-\d{2}-\d{2}""").matches(date)) return false
    val year = date.substring(0, 4).toInt()
    val month = date.substring(5, 7).toInt()
    val day = date.substring(8, 10).toInt()
    if (month !in 1..12) return false
    val maxDay = when (month) {
        2 -> if (year % 4 == 0 && (year % 100 != 0 || year % 400 == 0)) 29 else 28
        4, 6, 9, 11 -> 30
        else -> 31
    }
    return day in 1..maxDay && date > today && date <= bookingDateLimit(today)
}

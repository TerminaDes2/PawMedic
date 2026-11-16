package com.pawsmedic.features.businesses.presentation.detail

import java.util.Calendar

internal actual fun currentLocalTime(): LocalTime {
    val now = Calendar.getInstance()
    return LocalTime(
        dayOfWeek = (now.get(Calendar.DAY_OF_WEEK) + 5) % 7,
        hour = now.get(Calendar.HOUR_OF_DAY),
        minute = now.get(Calendar.MINUTE)
    )
}

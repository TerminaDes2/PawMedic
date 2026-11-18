package com.pawsmedic.features.businesses.presentation.detail

import java.time.LocalDateTime

internal actual fun currentLocalTime(): LocalTime {
    val now = LocalDateTime.now()
    return LocalTime(
        dayOfWeek = now.dayOfWeek.value - 1,
        hour = now.hour,
        minute = now.minute
    )
}

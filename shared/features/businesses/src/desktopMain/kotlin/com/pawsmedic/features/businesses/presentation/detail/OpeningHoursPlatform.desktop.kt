package com.pawsmedic.features.businesses.presentation.detail

import java.time.ZoneId
import java.time.ZonedDateTime

internal actual fun currentLocalTime(timeZone: String): LocalTime {
    val zone = runCatching { ZoneId.of(timeZone) }.getOrDefault(ZoneId.of("America/Mexico_City"))
    val now = ZonedDateTime.now(zone)
    val postgresDay = now.dayOfWeek.value % 7
    return LocalTime(postgresDay, now.hour, now.minute)
}

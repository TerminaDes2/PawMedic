package com.pawsmedic.features.appointments.presentation.booking

import java.time.LocalDate

internal actual fun bookingToday(): String = LocalDate.now().toString()

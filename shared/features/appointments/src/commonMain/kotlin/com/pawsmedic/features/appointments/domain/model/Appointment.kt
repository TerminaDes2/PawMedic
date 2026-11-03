package com.pawsmedic.features.appointments.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Appointment(
    val id: String,
    val petId: String,
    val petName: String,
    val businessId: String,
    val businessName: String,
    val serviceId: String,
    val serviceName: String,
    val date: String,
    val time: String,
    val notes: String? = null,
    val status: String = "Pendiente de aprobación",
    val startsAt: String? = null,
    val endsAt: String? = null,
    val idempotencyKey: String? = null
)

@Serializable
data class VeterinaryService(
    val id: String,
    val title: String,
    val description: String,
    val durationMinutes: Int,
    val priceCents: Int? = null
)

data class AvailableAppointmentSlot(
    val startsAt: String,
    val endsAt: String,
    val label: String
)

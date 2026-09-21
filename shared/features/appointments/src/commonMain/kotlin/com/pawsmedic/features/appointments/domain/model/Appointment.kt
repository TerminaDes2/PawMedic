package com.pawsmedic.features.appointments.domain.model

data class Appointment(
    val id: String,
    val petId: String,
    val businessId: String,
    val serviceId: String,
    val requestedAt: String,
    val notes: String? = null
)

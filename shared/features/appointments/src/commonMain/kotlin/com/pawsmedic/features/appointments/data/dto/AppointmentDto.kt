package com.pawsmedic.features.appointments.data.dto

import com.pawsmedic.features.appointments.domain.model.Appointment
import kotlinx.serialization.Serializable

@Serializable
data class AppointmentDto(
    val id: String,
    val petId: String,
    val businessId: String,
    val serviceId: String,
    val requestedAt: String,
    val notes: String? = null
) {
    fun toDomain() = Appointment(id, petId, businessId, serviceId, requestedAt, notes)
}

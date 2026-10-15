package com.pawsmedic.features.appointments.data.dto

import com.pawsmedic.features.appointments.domain.model.Appointment
import kotlinx.serialization.Serializable

@Serializable
data class AppointmentDto(
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
) {
    fun toDomain() = Appointment(
        id = id,
        petId = petId,
        petName = petName,
        businessId = businessId,
        businessName = businessName,
        serviceId = serviceId,
        serviceName = serviceName,
        date = date,
        time = time,
        notes = notes,
        status = status,
        startsAt = startsAt,
        endsAt = endsAt,
        idempotencyKey = idempotencyKey
    )

    companion object {
        fun fromDomain(app: Appointment) = AppointmentDto(
            id = app.id,
            petId = app.petId,
            petName = app.petName,
            businessId = app.businessId,
            businessName = app.businessName,
            serviceId = app.serviceId,
            serviceName = app.serviceName,
            date = app.date,
            time = app.time,
            notes = app.notes,
            status = app.status,
            startsAt = app.startsAt,
            endsAt = app.endsAt,
            idempotencyKey = app.idempotencyKey
        )
    }
}

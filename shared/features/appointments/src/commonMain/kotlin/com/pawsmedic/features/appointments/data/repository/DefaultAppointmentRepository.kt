package com.pawsmedic.features.appointments.data.repository

import com.pawsmedic.features.appointments.data.datasource.AppointmentDataSource
import com.pawsmedic.features.appointments.data.dto.AppointmentDto
import com.pawsmedic.features.appointments.domain.model.Appointment
import com.pawsmedic.features.appointments.domain.repository.AppointmentRepository

class DefaultAppointmentRepository(
    private val dataSource: AppointmentDataSource
) : AppointmentRepository {
    override suspend fun requestAppointment(appointment: Appointment): Result<Appointment> = try {
        val dto = AppointmentDto(
            id = appointment.id,
            petId = appointment.petId,
            businessId = appointment.businessId,
            serviceId = appointment.serviceId,
            requestedAt = appointment.requestedAt,
            notes = appointment.notes
        )
        Result.success(dataSource.requestAppointment(dto).toDomain())
    } catch (error: Throwable) {
        Result.failure(error)
    }
}

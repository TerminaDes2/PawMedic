package com.pawsmedic.features.appointments.data.datasource

import com.pawsmedic.features.appointments.data.dto.AppointmentDto

interface AppointmentDataSource {
    suspend fun requestAppointment(appointment: AppointmentDto): AppointmentDto
}

class EmptyAppointmentDataSource : AppointmentDataSource {
    override suspend fun requestAppointment(appointment: AppointmentDto): AppointmentDto =
        throw UnsupportedOperationException("Appointment data source is not configured")
}

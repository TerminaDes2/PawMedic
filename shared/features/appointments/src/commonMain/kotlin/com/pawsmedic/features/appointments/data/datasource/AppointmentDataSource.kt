package com.pawsmedic.features.appointments.data.datasource

import com.pawsmedic.features.appointments.data.dto.AppointmentDto

interface AppointmentDataSource {
    suspend fun getAppointments(): List<AppointmentDto>
    suspend fun requestAppointment(appointment: AppointmentDto): AppointmentDto
}

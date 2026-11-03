package com.pawsmedic.features.appointments.data.datasource

import com.pawsmedic.features.appointments.data.dto.AppointmentDto
import com.pawsmedic.features.appointments.domain.model.AvailableAppointmentSlot
import com.pawsmedic.features.appointments.domain.model.VeterinaryService

interface AppointmentDataSource {
    suspend fun getAppointments(): List<AppointmentDto>
    suspend fun getServices(tenantId: String): List<VeterinaryService>
    suspend fun getAvailableSlots(
        tenantId: String,
        serviceId: String,
        date: String
    ): List<AvailableAppointmentSlot>
    suspend fun requestAppointment(appointment: AppointmentDto): AppointmentDto
}

package com.pawsmedic.features.appointments.data.repository

import com.pawsmedic.features.appointments.data.datasource.AppointmentDataSource
import com.pawsmedic.features.appointments.data.dto.AppointmentDto
import com.pawsmedic.features.appointments.domain.model.Appointment
import com.pawsmedic.features.appointments.domain.model.AvailableAppointmentSlot
import com.pawsmedic.features.appointments.domain.model.VeterinaryService
import com.pawsmedic.features.appointments.domain.repository.AppointmentRepository

class DefaultAppointmentRepository(
    private val dataSource: AppointmentDataSource
) : AppointmentRepository {
    override suspend fun getAppointments(): Result<List<Appointment>> = try {
        Result.success(dataSource.getAppointments().map { it.toDomain() })
    } catch (error: Throwable) {
        Result.failure(error)
    }

    override suspend fun getServices(tenantId: String): Result<List<VeterinaryService>> = try {
        Result.success(dataSource.getServices(tenantId))
    } catch (error: Throwable) {
        Result.failure(error)
    }

    override suspend fun getAvailableSlots(
        tenantId: String,
        serviceId: String,
        date: String
    ): Result<List<AvailableAppointmentSlot>> = try {
        Result.success(dataSource.getAvailableSlots(tenantId, serviceId, date))
    } catch (error: Throwable) {
        Result.failure(error)
    }

    override suspend fun requestAppointment(appointment: Appointment): Result<Appointment> = try {
        val dto = AppointmentDto.fromDomain(appointment)
        Result.success(dataSource.requestAppointment(dto).toDomain())
    } catch (error: Throwable) {
        Result.failure(error)
    }
}

package com.pawsmedic.features.businesses.data.dto

import com.pawsmedic.features.businesses.domain.model.Business
import com.pawsmedic.features.businesses.domain.model.BusinessSchedule
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BusinessDto(
    val id: String,
    val name: String,
    val city: String? = null,
    val address: String? = null,
    val phone: String? = null,
    val description: String? = null,
    val rating: Double = 0.0,
    val reviewCount: String = "Sin reseñas",
    val specialties: List<String> = emptyList(),
    val hours: List<String> = emptyList(),
    val schedules: List<BusinessScheduleDto> = emptyList(),
    @SerialName("time_zone") val timeZone: String = "America/Mexico_City",
    @SerialName("photo_url")
    val photoUrl: String? = null
) {
    fun toDomain() = Business(
        id = id,
        name = name,
        city = city,
        address = address,
        phone = phone,
        description = description,
        rating = rating,
        reviewCount = reviewCount,
        specialties = specialties,
        hours = hours,
        schedules = schedules.map(BusinessScheduleDto::toDomain),
        timeZone = timeZone,
        imageUrl = photoUrl
    )
}

@Serializable
data class BusinessScheduleDto(
    @SerialName("day_of_week") val dayOfWeek: Int,
    @SerialName("open_time") val openTime: String,
    @SerialName("close_time") val closeTime: String,
    @SerialName("is_active") val isActive: Boolean = true
) {
    fun toDomain() = BusinessSchedule(dayOfWeek, openTime, closeTime, isActive)
}

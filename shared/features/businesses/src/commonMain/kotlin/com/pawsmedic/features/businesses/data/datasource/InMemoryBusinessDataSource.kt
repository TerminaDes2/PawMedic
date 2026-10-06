package com.pawsmedic.features.businesses.data.datasource

import com.pawsmedic.features.businesses.data.dto.BusinessDto

class InMemoryBusinessDataSource : BusinessDataSource {
    private val clinics = listOf(
        BusinessDto(
            id = "biz-1",
            name = "Clinipet Central",
            city = "Bogotá",
            address = "Calle 100 #15-45, Bogotá, Colombia",
            phone = "+57 (601) 555-0199",
            description = "Ofrecemos atención integral de la más alta calidad para tus mejores amigos. Contamos con especialistas en medicina preventiva, urgencias las 24 horas y cirugías complejas.",
            rating = 4.9,
            reviewCount = "120+ opiniones",
            specialties = listOf("Urgencias 24/7", "Medicina General", "Cirugía General", "Vacunación", "Odontología", "Peluquería", "Urgencias", "Cirugía", "Laboratorio"),
            hours = listOf("Lunes a Viernes: 24 Horas Abierto", "Sábados y Domingos: 8:00 AM - 10:00 PM"),
            imageUrl = "https://images.unsplash.com/photo-1584820927498-cfe5211fd8bf"
        ),
        BusinessDto(
            id = "biz-2",
            name = "Hospital Vet Norte",
            city = "Bogotá",
            address = "Avenida Suba #122-10, Bogotá, Colombia",
            phone = "+57 (601) 555-0288",
            description = "Centro hospitalario veterinario especializado en cardiología, diagnóstico por imagen y ecografía avanzada.",
            rating = 4.8,
            reviewCount = "95+ opiniones",
            specialties = listOf("Cardiología", "Ecografía", "Radiología", "Cuidados Intensivos"),
            hours = listOf("Lunes a Domingo: 24 Horas Abierto"),
            imageUrl = "https://images.unsplash.com/photo-1516549655169-df83a0774514"
        )
    )

    override suspend fun getBusinesses(): List<BusinessDto> {
        return clinics
    }

    override suspend fun getBusinessById(id: String): BusinessDto? {
        return clinics.find { it.id == id }
    }
}

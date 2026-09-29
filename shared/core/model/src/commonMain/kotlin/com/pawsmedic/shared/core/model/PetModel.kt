package com.pawsmedic.shared.core.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Modelo de datos para las Mascotas registradas por los clientes.
 * Mapeado desde la tabla 'pets' en Supabase PostgreSQL (Fase 2).
 *
 * @property id UUID único de la mascota.
 * @property ownerId UUID del cliente dueño de la mascota (FK -> profiles.id).
 * @property nombre Nombre de la mascota.
 * @property especie Especie (ej. 'Perro', 'Gato').
 * @property raza Raza de la mascota (opcional).
 * @property edad Edad en años.
 * @property alergias Alergias conocidas ('Ninguna' por defecto).
 * @property sexo Sexo ('Macho' / 'Hembra').
 * @property peso Peso actual en kilogramos.
 * @property fotoUrl URL de la fotografía de la mascota.
 * @property createdAt Fecha de registro en el sistema.
 */
@Serializable
data class PetModel(
    @SerialName("id") val id: String? = null,
    @SerialName("owner_id") val ownerId: String,
    @SerialName("nombre") val nombre: String,
    @SerialName("especie") val especie: String,
    @SerialName("raza") val raza: String? = null,
    @SerialName("edad") val edad: Int,
    @SerialName("alergias") val alergias: String = "Ninguna",
    @SerialName("sexo") val sexo: String,
    @SerialName("peso") val peso: Double? = null,
    @SerialName("foto_url") val fotoUrl: String? = null,
    @SerialName("created_at") val createdAt: String? = null
)

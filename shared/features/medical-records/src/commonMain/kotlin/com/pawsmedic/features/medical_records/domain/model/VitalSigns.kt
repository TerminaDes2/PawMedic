package com.pawsmedic.features.medical_records.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class VitalSigns(
    val temperatureCelsius: Double? = null,
    val heartRateBpm: Int? = null,
    val respiratoryRateRpm: Int? = null,
    val pulseQuality: String = "",
    val weightGrams: Int? = null,
    val tllcSeconds: String = "", // Tiempo de llenado capilar
    val hydrationStatus: String = "", // p.ej. "Normal (0-5%)", "Deshidratado (6-8%)"
    val bodyConditionScore: String = "", // p.ej. "3/5", "Ideal"
    val attitude: String = "", // p.ej. "Alerta", "Letárgico"
    val consciousnessState: String = "" // p.ej. "Consciente", "Deprimido"
)

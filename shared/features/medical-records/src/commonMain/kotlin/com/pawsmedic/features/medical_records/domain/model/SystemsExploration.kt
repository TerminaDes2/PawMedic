package com.pawsmedic.features.medical_records.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class SystemsExploration(
    val mucosas: String = "Sin hallazgos patológicos",
    val ojos: String = "Sin hallazgos patológicos",
    val oidos: String = "Sin hallazgos patológicos",
    val nodulosLinfaticos: String = "Sin hallazgos patológicos",
    val pielAnexos: String = "Sin hallazgos patológicos",
    val locomocion: String = "Sin hallazgos patológicos",
    val musculoEsqueletico: String = "Sin hallazgos patológicos",
    val sistemaNervioso: String = "Sin hallazgos patológicos",
    val sistemaCardiovascular: String = "Sin hallazgos patológicos",
    val sistemaRespiratorio: String = "Sin hallazgos patológicos",
    val sistemaDigestivo: String = "Sin hallazgos patológicos",
    val sistemaGenitourinario: String = "Sin hallazgos patológicos"
)

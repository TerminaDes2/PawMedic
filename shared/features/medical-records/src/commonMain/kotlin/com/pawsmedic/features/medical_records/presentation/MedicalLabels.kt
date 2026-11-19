package com.pawsmedic.features.medical_records.presentation

import com.pawsmedic.shared.core.designsystem.language.AppLanguage

internal fun localizeMedicalLabel(label: String, language: AppLanguage): String {
    if (language == AppLanguage.SPANISH) return label
    return when (label) {
        "Número de Expediente" -> "Record number"
        "Nombre de Mascota" -> "Pet name"
        "Especie" -> "Species"
        "Raza" -> "Breed"
        "Sexo" -> "Sex"
        "Fecha de Nacimiento" -> "Date of birth"
        "Edad Calculada" -> "Calculated age"
        "Color / Señas" -> "Color / markings"
        "Estado Reproductivo" -> "Reproductive status"
        "Propietario" -> "Owner"
        "Fecha Registro Expediente" -> "Record creation date"
        "Temperatura" -> "Temperature"
        "Frecuencia Cardíaca" -> "Heart rate"
        "Frecuencia Respiratoria" -> "Respiratory rate"
        "Pulso" -> "Pulse"
        "Peso" -> "Weight"
        "Tiempo Llenado Capilar (TLLC)" -> "Capillary refill time"
        "Estado de Hidratación" -> "Hydration status"
        "Condición Corporal" -> "Body condition"
        "Actitud / Conciencia", "Actitud / Estado de Conciencia" -> "Attitude / consciousness"
        "Última Desparasitación" -> "Last deworming"
        "Producto Utilizado" -> "Product used"
        "Alimentación" -> "Diet"
        "Alergias Conocidas" -> "Known allergies"
        "Hábitat y Condiciones" -> "Habitat and conditions"
        "Enfermedades Anteriores" -> "Previous diseases"
        "Cirugías Anteriores" -> "Previous surgeries"
        "Antecedentes Familiares" -> "Family history"
        "Mucosas" -> "Mucous membranes"
        "Ojos" -> "Eyes"
        "Oídos" -> "Ears"
        "Nódulos Linfáticos" -> "Lymph nodes"
        "Piel y Anexos" -> "Skin and coat"
        "Locomoción" -> "Locomotion"
        "Sistema Músculo-Esquelético" -> "Musculoskeletal system"
        "Sistema Nervioso" -> "Nervous system"
        "Sistema Cardiovascular" -> "Cardiovascular system"
        "Sistema Respiratorio" -> "Respiratory system"
        "Sistema Digestivo" -> "Digestive system"
        "Sistema Genitourinario" -> "Genitourinary system"
        "Lista de Problemas" -> "Problem list"
        "Diagnósticos Diferenciales" -> "Differential diagnoses"
        "Diagnóstico Definitivo" -> "Final diagnosis"
        "Plan Diagnóstico" -> "Diagnostic plan"
        else -> label
    }
}

internal fun localizeReproductiveStatus(value: String, language: AppLanguage): String =
    language.text(value, when (value) {
        "Intacto" -> "Intact"
        "Intacta" -> "Intact"
        "Castrado" -> "Neutered"
        "Castrada", "Esterilizada" -> "Spayed"
        "Esterilizado" -> "Neutered"
        "No especificado", "No especificada" -> "Not specified"
        else -> value
    })

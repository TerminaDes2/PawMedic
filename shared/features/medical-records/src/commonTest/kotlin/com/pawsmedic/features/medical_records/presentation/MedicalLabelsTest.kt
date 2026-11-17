package com.pawsmedic.features.medical_records.presentation

import com.pawsmedic.shared.core.designsystem.language.AppLanguage
import kotlin.test.Test
import kotlin.test.assertEquals

class MedicalLabelsTest {
    @Test
    fun translatesLabelsWithoutChangingUnknownValues() {
        assertEquals("Heart rate", localizeMedicalLabel("Frecuencia Cardíaca", AppLanguage.ENGLISH))
        assertEquals("Frecuencia Cardíaca", localizeMedicalLabel("Frecuencia Cardíaca", AppLanguage.SPANISH))
        assertEquals("Other", localizeMedicalLabel("Other", AppLanguage.ENGLISH))
    }
}

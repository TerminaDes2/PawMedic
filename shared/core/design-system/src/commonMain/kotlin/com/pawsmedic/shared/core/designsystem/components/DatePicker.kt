package com.pawsmedic.shared.core.designsystem.components

import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import com.pawsmedic.shared.core.designsystem.language.AppLanguage
import com.pawsmedic.shared.core.designsystem.language.LocalLanguagePreference

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerModal(
    onDateSelected: (Long?) -> Unit,
    onDismiss: () -> Unit
) {
    DatePickerModal(onDateSelected, onDismiss, DatePickerDefaults.YearRange, DatePickerDefaults.AllDates)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerModal(
    onDateSelected: (Long?) -> Unit,
    onDismiss: () -> Unit,
    yearRange: IntRange,
    selectableDates: SelectableDates
) {
    LocalizedDatePickerContent {
        DatePickerModalContent(onDateSelected, onDismiss, yearRange, selectableDates)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DatePickerModalContent(
    onDateSelected: (Long?) -> Unit,
    onDismiss: () -> Unit,
    yearRange: IntRange,
    selectableDates: SelectableDates
) {
    val datePickerState = rememberDatePickerState(yearRange = yearRange, selectableDates = selectableDates)

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                onDateSelected(datePickerState.selectedDateMillis)
                onDismiss()
            }) {
                Text((LocalLanguagePreference.current?.selected ?: AppLanguage.SPANISH).text("Aceptar", "OK"))
            }

        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text((LocalLanguagePreference.current?.selected ?: AppLanguage.SPANISH).text("Cancelar", "Cancel"))
            }

        }
    ) {
        DatePicker(state = datePickerState)
    }
}

@Composable
internal expect fun LocalizedDatePickerContent(content: @Composable () -> Unit)

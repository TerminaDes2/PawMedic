package com.pawsmedic.shared.core.designsystem.components

import androidx.compose.runtime.Composable

@Composable
expect fun TimePickerModal(
    initialHour: Int = 11,
    initialMinute: Int = 0,
    onTimeSelected: (hour: Int, minute: Int, formattedTime: String) -> Unit,
    onDismiss: () -> Unit
)

package com.pawsmedic.features.pets.presentation.settings

import androidx.compose.runtime.staticCompositionLocalOf

class NotificationSettingsControl(
    val enabled: Boolean,
    val busy: Boolean,
    val error: String?,
    val onChange: (Boolean) -> Unit
)

val LocalNotificationSettings = staticCompositionLocalOf<NotificationSettingsControl?> { null }

package com.pawsmedic.features.auth.presentation.recovery

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable

@Composable
internal actual fun RecoveryBackHandler(onBack: () -> Unit) {
    BackHandler(onBack = onBack)
}

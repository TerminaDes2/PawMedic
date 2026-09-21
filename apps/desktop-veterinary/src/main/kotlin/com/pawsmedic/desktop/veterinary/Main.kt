package com.pawsmedic.desktop.veterinary

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.pawsmedic.features.auth.domain.model.PawMedicRole
import com.pawsmedic.features.auth.presentation.AuthUiState
import com.pawsmedic.features.auth.presentation_desktop.AuthDesktopScreen

fun main() = application {
    Window(onCloseRequest = ::exitApplication, title = "PawMedic Veterinary") {
        AuthDesktopScreen(AuthUiState.SignedOut)
    }
}

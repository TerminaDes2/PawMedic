package com.pawsmedic.features.auth.presentation_desktop

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.pawsmedic.features.auth.presentation.AuthUiState

@Composable
fun AuthDesktopScreen(state: AuthUiState) {
    Text("PawMedic sign in (Desktop): ${state::class.simpleName}")
}

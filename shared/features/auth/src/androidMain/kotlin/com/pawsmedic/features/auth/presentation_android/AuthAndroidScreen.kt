package com.pawsmedic.features.auth.presentation_android

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.pawsmedic.features.auth.presentation.AuthUiState

@Composable
fun AuthAndroidScreen(state: AuthUiState) {
    Text("PawMedic sign in (Android): ${state::class.simpleName}")
}

package com.pawsmedic.mobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.pawsmedic.features.auth.domain.model.PawMedicRole
import com.pawsmedic.features.auth.presentation.AuthUiState
import com.pawsmedic.features.auth.presentation_android.AuthAndroidScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { AuthAndroidScreen(AuthUiState.SignedOut) }
    }
}

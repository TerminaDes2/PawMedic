package com.pawsmedic.features.auth.presentation.splash

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pawsmedic.shared.core.designsystem.components.PawMedicLogoIcon
import com.pawsmedic.shared.core.designsystem.theme.PawMedicColors

@Composable
fun SplashScreen(
    viewModel: SplashViewModel,
    onNavigateToWelcome: () -> Unit,
    onNavigateToHome: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is SplashUiState.NavigateToWelcome -> onNavigateToWelcome()
            is SplashUiState.NavigateToHome -> onNavigateToHome(state.userId)
            SplashUiState.Loading -> Unit
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(PawMedicColors.Teal50),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
        ) {
            PawMedicLogoIcon(
                size = 96.dp,
                containerColor = PawMedicColors.Teal600,
                pawColor = PawMedicColors.White
            )

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = "PawsMedic",
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = PawMedicColors.Teal600
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Atención veterinaria para tu mascota",
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = PawMedicColors.Teal700,
                textAlign = TextAlign.Center
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(32.dp),
                color = PawMedicColors.Teal600,
                strokeWidth = 3.dp
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "v1.0.0",
                fontSize = 12.sp,
                color = PawMedicColors.Teal700,
                fontWeight = FontWeight.Normal
            )
        }
    }
}

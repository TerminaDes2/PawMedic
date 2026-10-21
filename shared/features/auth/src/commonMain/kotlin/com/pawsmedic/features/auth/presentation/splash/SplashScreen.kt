package com.pawsmedic.features.auth.presentation.splash

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
    onNavigateToHome: (String, String?) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    var isAnimationFinished by remember { mutableStateOf(false) }

    LaunchedEffect(uiState, isAnimationFinished) {
        if (isAnimationFinished) {
            when (val state = uiState) {
                is SplashUiState.NavigateToWelcome -> onNavigateToWelcome()
                is SplashUiState.NavigateToHome -> onNavigateToHome(state.userId, state.email)
                SplashUiState.Loading -> Unit
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .systemBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp)
        ) {
            Spacer(modifier = Modifier.weight(1f))

            PawMedicLogoIcon(
                size = 150.dp,
                animated = true,
                onAnimationEnd = {
                    isAnimationFinished = true
                }
            )

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = "PawsMedic",
                fontSize = 38.sp,
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

            Spacer(modifier = Modifier.weight(1f))

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(bottom = 24.dp)
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(28.dp),
                    color = PawMedicColors.Teal600,
                    strokeWidth = 3.dp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "v1.0.0",
                    fontSize = 12.sp,
                    color = PawMedicColors.Teal700,
                    fontWeight = FontWeight.Normal
                )
            }
        }
    }
}
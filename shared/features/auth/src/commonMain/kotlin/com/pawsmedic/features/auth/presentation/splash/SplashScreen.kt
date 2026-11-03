package com.pawsmedic.features.auth.presentation.splash

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.pawsmedic.features.auth.presentation.components.AuthLogo

@Composable
fun SplashScreen(
    viewModel: SplashViewModel,
    onNavigateToWelcome: () -> Unit,
    onNavigateToHome: (String, String?) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is SplashUiState.NavigateToWelcome -> onNavigateToWelcome()
            is SplashUiState.NavigateToHome -> onNavigateToHome(state.userId, state.email)
            SplashUiState.Loading -> Unit
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .systemBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        AuthLogo(modifier = Modifier.size(150.dp))
    }
}
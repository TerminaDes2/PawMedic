package com.pawsmedic.features.auth.presentation.welcome

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pawsmedic.shared.core.designsystem.components.PawMedicLogoIcon
import com.pawsmedic.shared.core.designsystem.components.PawMedicPrimaryButton
import com.pawsmedic.shared.core.designsystem.components.PawMedicSecondaryButton
import com.pawsmedic.shared.core.designsystem.theme.PawMedicColors

@Composable
fun WelcomeScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToRegister: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(PawMedicColors.BackgroundScreen)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            PawMedicLogoIcon(
                size = 140.dp,
                containerColor = PawMedicColors.Teal600,
                pawColor = PawMedicColors.White
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Brand Header
            Text(
                text = "PawMedic",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = PawMedicColors.Teal600
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Title & Description
            Text(
                text = "Cuidamos a quienes más amas",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = PawMedicColors.Gray900,
                textAlign = TextAlign.Center,
                lineHeight = 34.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Encuentra veterinarias, gestiona citas y lleva el historial médico de tus mascotas en un solo lugar.",
                fontSize = 15.sp,
                fontWeight = FontWeight.Normal,
                color = PawMedicColors.Gray600,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp,
                modifier = Modifier.padding(horizontal = 12.dp)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Actions
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            PawMedicPrimaryButton(
                text = "Iniciar sesión",
                onClick = onNavigateToLogin
            )

            Spacer(modifier = Modifier.height(12.dp))

            PawMedicSecondaryButton(
                text = "Registrarse",
                onClick = onNavigateToRegister
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Al continuar, aceptas nuestros Términos y Políticas de Privacidad",
                fontSize = 12.sp,
                color = PawMedicColors.Gray400,
                textAlign = TextAlign.Center
            )
        }
    }
}

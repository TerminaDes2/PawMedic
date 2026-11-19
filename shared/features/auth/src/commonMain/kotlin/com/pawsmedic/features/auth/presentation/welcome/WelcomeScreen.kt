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
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pawsmedic.features.auth.presentation.components.AuthLogo
import com.pawsmedic.features.auth.presentation.terms.LegalSection
import com.pawsmedic.features.auth.presentation.terms.TermsAndConditionsDialog
import com.pawsmedic.shared.core.designsystem.components.PawMedicPrimaryButton
import com.pawsmedic.shared.core.designsystem.components.PawMedicSecondaryButton
import com.pawsmedic.shared.core.designsystem.theme.PawMedicColors
import com.pawsmedic.shared.core.designsystem.theme.PawMedicSemanticColors
import com.pawsmedic.shared.core.designsystem.language.AppLanguage
import com.pawsmedic.shared.core.designsystem.language.LocalLanguagePreference

@Composable
fun WelcomeScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToRegister: () -> Unit,
    modifier: Modifier = Modifier
) {
    val language = LocalLanguagePreference.current?.selected ?: AppLanguage.SPANISH
    var legalSection by remember { mutableStateOf<LegalSection?>(null) }
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(PawMedicSemanticColors.current.background)
            .systemBarsPadding()
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

            AuthLogo(
                modifier = Modifier.size(280.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Brand Header
            Text(
                text = "PawsMedic",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = PawMedicSemanticColors.current.accent
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Title & Description
            Text(
                text = language.text("Cuidamos a quienes más amas", "We care for those you love most"),
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = PawMedicSemanticColors.current.text,
                textAlign = TextAlign.Center,
                lineHeight = 34.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = language.text("Encuentra veterinarias, gestiona citas y lleva las recetas médicas de tus mascotas en un solo lugar.", "Find veterinary clinics, manage appointments and keep your pets' prescriptions in one place."),
                fontSize = 15.sp,
                fontWeight = FontWeight.Normal,
                color = PawMedicSemanticColors.current.secondaryText,
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
                text = language.text("Iniciar sesión", "Sign in"),
                onClick = onNavigateToLogin
            )

            Spacer(modifier = Modifier.height(12.dp))

            PawMedicSecondaryButton(
                text = language.text("Registrarse", "Sign up"),
                onClick = onNavigateToRegister
            )

            Spacer(modifier = Modifier.height(20.dp))

            androidx.compose.foundation.layout.Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = language.text("Términos", "Terms"),
                    fontSize = 12.sp,
                    color = PawMedicSemanticColors.current.accent,
                    modifier = Modifier.clickable { legalSection = LegalSection.TERMS }
                )
                Text(" · ", fontSize = 12.sp, color = PawMedicSemanticColors.current.hint)
                Text(
                    text = language.text("Política de privacidad", "Privacy policy"),
                    fontSize = 12.sp,
                    color = PawMedicSemanticColors.current.accent,
                    modifier = Modifier.clickable { legalSection = LegalSection.PRIVACY }
                )
            }
        }
    }

    legalSection?.let { section ->
        TermsAndConditionsDialog(section = section, onDismiss = { legalSection = null })
    }
}

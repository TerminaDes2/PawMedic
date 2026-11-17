package com.pawsmedic.features.auth.presentation.otp

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pawsmedic.features.auth.presentation.register.RegisterViewModel
import com.pawsmedic.features.auth.presentation.localizeAuthMessage
import com.pawsmedic.shared.core.designsystem.components.PawMedicPrimaryButton
import com.pawsmedic.shared.core.designsystem.components.PawMedicTopBar
import com.pawsmedic.shared.core.designsystem.theme.PawMedicColors
import com.pawsmedic.shared.core.designsystem.theme.PawMedicSemanticColors
import com.pawsmedic.shared.core.designsystem.language.AppLanguage
import com.pawsmedic.shared.core.designsystem.language.LocalLanguagePreference

@Composable
fun OtpVerificationScreen(
    viewModel: RegisterViewModel,
    onBackClick: () -> Unit,
    onVerificationSuccess: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val language = LocalLanguagePreference.current?.selected ?: AppLanguage.SPANISH
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess && uiState.userId != null) {
            onVerificationSuccess(uiState.userId!!)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(PawMedicSemanticColors.current.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            PawMedicTopBar(
                title = language.text("Verifica tu correo", "Verify your email"),
                subtitle = language.text("Hemos enviado un código de 6 dígitos a:\n${uiState.email}", "We sent a 6-digit code to:\n${uiState.email}"),
                onBackClick = onBackClick
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = language.text("Código de confirmación", "Confirmation code"),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = PawMedicSemanticColors.current.text,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            BasicTextField(
                value = uiState.otpCode,
                onValueChange = viewModel::onOtpCodeChanged,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(onDone = { viewModel.verifyOtp() }),
                decorationBox = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        repeat(6) { index ->
                            val char = uiState.otpCode.getOrNull(index)?.toString() ?: ""
                            val isFocused = uiState.otpCode.length == index
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(PawMedicSemanticColors.current.surface)
                                    .border(
                                        width = if (isFocused) 2.dp else 1.dp,
                                        color = if (isFocused) PawMedicColors.Teal600 else PawMedicSemanticColors.current.border,
                                        shape = RoundedCornerShape(12.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = char,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PawMedicSemanticColors.current.text,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            )

            if (uiState.errorMessage != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = localizeAuthMessage(uiState.errorMessage!!, language),
                    fontSize = 13.sp,
                    color = PawMedicColors.Error,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            if (uiState.resendSuccessMessage != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = localizeAuthMessage(uiState.resendSuccessMessage!!, language),
                    fontSize = 13.sp,
                    color = PawMedicSemanticColors.current.accent,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            PawMedicPrimaryButton(
                text = language.text("Verificar y continuar", "Verify and continue"),
                onClick = viewModel::verifyOtp,
                isLoading = uiState.isLoading,
                enabled = uiState.otpCode.length == 6
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = language.text("¿No recibiste el código? ", "Didn't receive the code? "),
                fontSize = 14.sp,
                color = PawMedicSemanticColors.current.secondaryText
            )
            Text(
                text = language.text("Reenviar", "Resend"),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = PawMedicSemanticColors.current.accent,
                modifier = Modifier.clickable { viewModel.resendOtp() }
            )
        }
    }
}

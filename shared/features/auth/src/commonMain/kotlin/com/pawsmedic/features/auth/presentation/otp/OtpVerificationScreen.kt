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
import com.pawsmedic.shared.core.designsystem.components.PawMedicPrimaryButton
import com.pawsmedic.shared.core.designsystem.components.PawMedicTopBar
import com.pawsmedic.shared.core.designsystem.theme.PawMedicColors

@Composable
fun OtpVerificationScreen(
    viewModel: RegisterViewModel,
    onBackClick: () -> Unit,
    onVerificationSuccess: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess && uiState.userId != null) {
            onVerificationSuccess(uiState.userId!!)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(PawMedicColors.BackgroundScreen)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            PawMedicTopBar(
                title = "Verifica tu correo",
                subtitle = "Hemos enviado un código de 6 dígitos a:\n${uiState.email}",
                onBackClick = onBackClick
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Código de confirmación",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = PawMedicColors.Gray800,
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
                                    .background(PawMedicColors.White)
                                    .border(
                                        width = if (isFocused) 2.dp else 1.dp,
                                        color = if (isFocused) PawMedicColors.Teal600 else PawMedicColors.Gray300,
                                        shape = RoundedCornerShape(12.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = char,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PawMedicColors.Gray900,
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
                    text = uiState.errorMessage!!,
                    fontSize = 13.sp,
                    color = PawMedicColors.Error,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            if (uiState.resendSuccessMessage != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = uiState.resendSuccessMessage!!,
                    fontSize = 13.sp,
                    color = PawMedicColors.Teal600,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            PawMedicPrimaryButton(
                text = "Verificar y continuar",
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
                text = "¿No recibiste el código? ",
                fontSize = 14.sp,
                color = PawMedicColors.Gray600
            )
            Text(
                text = "Reenviar",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = PawMedicColors.Teal600,
                modifier = Modifier.clickable { viewModel.resendOtp() }
            )
        }
    }
}

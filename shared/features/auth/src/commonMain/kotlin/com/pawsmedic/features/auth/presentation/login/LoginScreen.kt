package com.pawsmedic.features.auth.presentation.login

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pawsmedic.shared.core.designsystem.components.PawMedicPrimaryButton
import com.pawsmedic.shared.core.designsystem.components.PawMedicTextField
import com.pawsmedic.shared.core.designsystem.components.PawMedicTopBar
import com.pawsmedic.shared.core.designsystem.theme.PawMedicColors

@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    onBackClick: () -> Unit,
    onNavigateToRegister: () -> Unit,
    onLoginSuccess: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess && uiState.userId != null) {
            onLoginSuccess(uiState.userId!!)
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
                title = "Iniciar sesión",
                subtitle = "Bienvenido de nuevo a PawMedic",
                onBackClick = onBackClick
            )

            Spacer(modifier = Modifier.height(24.dp))

            PawMedicTextField(
                value = uiState.email,
                onValueChange = viewModel::onEmailChanged,
                label = "Correo electrónico",
                placeholder = "tu@email.com",
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                ),
                leadingIcon = {
                    Text(
                        text = "@",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = PawMedicColors.Teal600
                    )
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            PawMedicTextField(
                value = uiState.password,
                onValueChange = viewModel::onPasswordChanged,
                label = "Contraseña",
                placeholder = "••••••••",
                visualTransformation = if (uiState.isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(onDone = { viewModel.login() }),
                leadingIcon = {
                    Text(
                        text = "🔒",
                        fontSize = 16.sp,
                        color = PawMedicColors.Teal600
                    )
                },
                trailingIcon = {
                    Text(
                        text = if (uiState.isPasswordVisible) "👁" else "🙈",
                        fontSize = 18.sp,
                        modifier = Modifier
                            .clickable { viewModel.togglePasswordVisibility() }
                            .padding(8.dp)
                    )
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "¿Olvidaste tu contraseña?",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = PawMedicColors.Teal600,
                textAlign = TextAlign.End,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { /* Handle forgot password */ }
                    .padding(vertical = 4.dp)
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

            Spacer(modifier = Modifier.height(28.dp))

            PawMedicPrimaryButton(
                text = "Iniciar sesión",
                onClick = viewModel::login,
                isLoading = uiState.isLoading
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
                text = "¿No tienes una cuenta? ",
                fontSize = 14.sp,
                color = PawMedicColors.Gray600
            )
            Text(
                text = "Registrarse",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = PawMedicColors.Teal600,
                modifier = Modifier.clickable { onNavigateToRegister() }
            )
        }
    }
}

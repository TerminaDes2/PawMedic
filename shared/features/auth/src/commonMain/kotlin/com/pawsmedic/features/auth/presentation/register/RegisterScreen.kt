package com.pawsmedic.features.auth.presentation.register

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.ContentType
import androidx.compose.ui.semantics.contentType
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pawsmedic.shared.core.designsystem.components.GoogleSocialButton
import com.pawsmedic.shared.core.designsystem.components.PawMedicPrimaryButton
import com.pawsmedic.shared.core.designsystem.components.PawMedicTextField
import com.pawsmedic.shared.core.designsystem.components.PawMedicTopBar
import com.pawsmedic.shared.core.designsystem.theme.PawMedicColors
import com.pawsmedic.shared.core.designsystem.theme.PawMedicSemanticColors

@Composable
fun RegisterScreen(
    viewModel: RegisterViewModel,
    onBackClick: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onRegisterSuccess: (String) -> Unit,
    onGoogleSignInClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.isSuccess, uiState.userId) {
        if (uiState.isSuccess && uiState.userId != null) onRegisterSuccess(uiState.userId!!)
    }

    val stepOneValid = uiState.firstName.trim().length >= 2 && uiState.lastName.trim().length >= 2
    val stepTwoValid = uiState.username.matches(Regex("^[A-Za-z0-9._-]{3,30}$")) &&
        uiState.email.matches(Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) &&
        uiState.phone.length == 10 && uiState.phone.all(Char::isDigit)
    val securityValid = uiState.password.length >= 6 && uiState.termsAccepted

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(PawMedicSemanticColors.current.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        PawMedicTopBar(
            title = "Crear cuenta",
            subtitle = "Regístrate para comenzar con PawMedic",
            onBackClick = if (uiState.step == 1) onBackClick else viewModel::backToIdentity
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Paso ${uiState.step} de 3",
            color = PawMedicSemanticColors.current.accent,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            repeat(3) { index ->
                HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    color = if (index < uiState.step) PawMedicColors.Teal600 else PawMedicSemanticColors.current.border,
                    thickness = 3.dp
                )
            }
        }
        Spacer(modifier = Modifier.height(28.dp))

        when (uiState.step) {
            1 -> {
                if (onGoogleSignInClick != null) {
                    GoogleSocialButton(
                        onClick = onGoogleSignInClick,
                        isLoading = uiState.isLoading,
                        text = "Registrarse con Google"
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        HorizontalDivider(modifier = Modifier.weight(1f), color = PawMedicSemanticColors.current.border)
                        Text("  o regístrate con correo  ", fontSize = 12.sp, color = PawMedicSemanticColors.current.hint)
                        HorizontalDivider(modifier = Modifier.weight(1f), color = PawMedicSemanticColors.current.border)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
                PawMedicTextField(
                    value = uiState.firstName,
                    onValueChange = viewModel::onFirstNameChanged,
                    label = "Nombres",
                    placeholder = "Ej. Juan Carlos",
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    leadingIcon = { Icon(Icons.Default.Person, null, tint = PawMedicSemanticColors.current.accent, modifier = Modifier.size(20.dp)) }
                )
                Spacer(modifier = Modifier.height(14.dp))
                PawMedicTextField(
                    value = uiState.lastName,
                    onValueChange = viewModel::onLastNameChanged,
                    label = "Apellidos",
                    placeholder = "Ej. Pérez López",
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    leadingIcon = { Icon(Icons.Default.Person, null, tint = PawMedicSemanticColors.current.accent, modifier = Modifier.size(20.dp)) }
                )
                StepError(uiState.errorMessage)
                Spacer(modifier = Modifier.height(24.dp))
                PawMedicPrimaryButton(
                    text = "Continuar",
                    onClick = viewModel::continueToAccount,
                    enabled = stepOneValid
                )
            }
            2 -> {
                PawMedicTextField(
                    value = uiState.username,
                    onValueChange = viewModel::onUsernameChanged,
                    label = "Nombre de usuario",
                    placeholder = "juansito",
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    leadingIcon = { Icon(Icons.Default.Person, null, tint = PawMedicSemanticColors.current.accent, modifier = Modifier.size(20.dp)) }
                )
                Spacer(modifier = Modifier.height(14.dp))
                PawMedicTextField(
                    value = uiState.email,
                    onValueChange = viewModel::onEmailChanged,
                    label = "Correo electrónico",
                    placeholder = "juanperez@correo.com",
                    inputModifier = Modifier.semantics { contentType = ContentType.EmailAddress },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                    leadingIcon = { Icon(Icons.Default.Email, null, tint = PawMedicSemanticColors.current.accent, modifier = Modifier.size(20.dp)) }
                )
                Spacer(modifier = Modifier.height(14.dp))
                PawMedicTextField(
                    value = uiState.phone,
                    onValueChange = viewModel::onPhoneChanged,
                    label = "Teléfono",
                    placeholder = "111-222-3333",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Done),
                    leadingIcon = { Icon(Icons.Default.Phone, null, tint = PawMedicSemanticColors.current.accent, modifier = Modifier.size(20.dp)) }
                )
                StepError(uiState.errorMessage)
                Spacer(modifier = Modifier.height(24.dp))
                PawMedicPrimaryButton(
                    text = "Continuar",
                    onClick = viewModel::continueToSecurity,
                    enabled = stepTwoValid
                )
            }
            else -> {
                PawMedicTextField(
                    value = uiState.password,
                    onValueChange = viewModel::onPasswordChanged,
                    label = "Contraseña",
                    placeholder = "Mínimo 6 caracteres",
                    inputModifier = Modifier.semantics { contentType = ContentType.NewPassword },
                    visualTransformation = if (uiState.isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                    leadingIcon = { Icon(Icons.Default.Lock, null, tint = PawMedicSemanticColors.current.accent, modifier = Modifier.size(20.dp)) },
                    trailingIcon = {
                        Icon(
                            imageVector = if (uiState.isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = if (uiState.isPasswordVisible) "Ocultar contraseña" else "Mostrar contraseña",
                            tint = PawMedicSemanticColors.current.hint,
                            modifier = Modifier.size(20.dp).clickable { viewModel.togglePasswordVisibility() }
                        )
                    }
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().clickable { viewModel.toggleTermsAccepted() }
                ) {
                    Checkbox(
                        checked = uiState.termsAccepted,
                        onCheckedChange = { viewModel.toggleTermsAccepted() },
                        colors = CheckboxDefaults.colors(checkedColor = PawMedicColors.Teal600, uncheckedColor = PawMedicSemanticColors.current.hint)
                    )
                    Text(
                        text = "Acepto los Términos y Condiciones y la Política de Privacidad",
                        fontSize = 13.sp,
                        color = PawMedicSemanticColors.current.text,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
                StepError(uiState.errorMessage)
                Spacer(modifier = Modifier.height(24.dp))
                PawMedicPrimaryButton(
                    text = "Crear cuenta",
                    onClick = viewModel::register,
                    enabled = securityValid,
                    isLoading = uiState.isLoading
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("¿Ya tienes una cuenta? ", fontSize = 14.sp, color = PawMedicSemanticColors.current.secondaryText)
            Text(
                "Iniciar sesión",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = PawMedicSemanticColors.current.accent,
                modifier = Modifier.clickable { onNavigateToLogin() }
            )
        }
    }
}

@Composable
private fun StepError(message: String?) {
    if (message != null) {
        Spacer(modifier = Modifier.height(12.dp))
        Text(message, fontSize = 13.sp, color = PawMedicColors.Error)
    }
}

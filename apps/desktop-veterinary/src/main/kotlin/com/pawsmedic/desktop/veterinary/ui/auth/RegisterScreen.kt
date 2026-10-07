package com.pawsmedic.desktop.veterinary.ui.auth

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.singleWindowApplication
import com.pawsmedic.desktop.veterinary.model.AccountApplicationDto
import com.pawsmedic.desktop.veterinary.model.AccountType
import com.pawsmedic.desktop.veterinary.supabase
import com.pawsmedic.desktop.veterinary.ui.theme.*
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun RegisterScreen(
    onNavigateToVetMain: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    var clinicName by remember { mutableStateOf("") }
    var rucTaxId by remember { mutableStateOf("") }
    var adminEmail by remember { mutableStateOf("") }
    var contactPhone by remember { mutableStateOf("") }

    var clinicAddress by remember { mutableStateOf("") }
    var vetManagerName by remember { mutableStateOf("") }
    var licenseNumber by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    var acceptTerms by remember { mutableStateOf(false) }

    var phoneWarning by remember { mutableStateOf(false) }
    var rucWarning by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    fun validatePassword(pwd: String): String? {
        if (pwd.length < 5) {
            return "La contraseña debe tener al menos 5 caracteres."
        }
        if (!pwd.any { it.isUpperCase() }) {
            return "La contraseña debe incluir al menos una letra mayúscula (A-Z)."
        }
        if (!pwd.any { it.isLowerCase() }) {
            return "La contraseña debe incluir al menos una letra minúscula (a-z)."
        }
        if (!pwd.any { it.isDigit() }) {
            return "La contraseña debe incluir al menos un número (0-9)."
        }
        val specialChars = "!@#$%^&*()_+-=[]{}|;:,.<>/?"
        if (!pwd.any { it in specialChars }) {
            return "La contraseña debe incluir al menos un carácter especial (ej. !@#$)."
        }
        return null
    }

    val passwordError = remember(password) {
        if (password.isNotEmpty()) validatePassword(password) else null
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(OffWhiteBg)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .width(880.dp)
                .wrapContentHeight(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = CardWhite),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(36.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // ENCABEZADO DE REGISTRO
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(EmeraldLightBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AppRegistration,
                            contentDescription = null,
                            tint = ClinicalEmerald,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "Registro de Clínica Veterinaria",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Solicita el alta de tu establecimiento para comenzar a utilizar PawsMedic",
                            fontSize = 13.sp,
                            color = TextSecondary
                        )
                    }
                }

                if (errorMessage != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFFEE2E2))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = errorMessage ?: "",
                            fontSize = 12.sp,
                            color = Color(0xFF991B1B),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                if (successMessage != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFDCFCE7))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = successMessage ?: "",
                            fontSize = 12.sp,
                            color = Color(0xFF166534),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                HorizontalDivider(color = BorderLight)

                // FORMULARIO DE VETERINARIA (2 COLUMNAS)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    // COLUMNA IZQUIERDA
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        OutlinedTextField(
                            value = clinicName,
                            onValueChange = { clinicName = it },
                            label = { Text("Nombre de la Clínica *", color = TextSecondary) },
                            placeholder = { Text("ej. Veterinaria San Miguel", color = TextMuted) },
                            leadingIcon = { Icon(Icons.Default.Store, contentDescription = null, tint = TextMuted) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ClinicalEmerald, unfocusedBorderColor = BorderLight)
                        )

                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            OutlinedTextField(
                                value = rucTaxId,
                                onValueChange = { input ->
                                    if (input.all { it.isLetterOrDigit() || it == '-' }) {
                                        rucTaxId = input
                                        rucWarning = false
                                    } else {
                                        rucWarning = true
                                    }
                                },
                                label = { Text("RUC / ID Fiscal *", color = TextSecondary) },
                                placeholder = { Text("ej. 20123456789", color = TextMuted) },
                                leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null, tint = TextMuted) },
                                singleLine = true,
                                isError = rucWarning,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ClinicalEmerald, unfocusedBorderColor = BorderLight)
                            )
                            if (rucWarning) {
                                Text("⚠️ Solo se permiten letras, números y guiones.", fontSize = 11.sp, color = Color(0xFFDC2626), fontWeight = FontWeight.Bold)
                            }
                        }

                        OutlinedTextField(
                            value = adminEmail,
                            onValueChange = { adminEmail = it },
                            label = { Text("Correo Electrónico Administrador *", color = TextSecondary) },
                            placeholder = { Text("admin@veterinaria.com", color = TextMuted) },
                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = TextMuted) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ClinicalEmerald, unfocusedBorderColor = BorderLight)
                        )

                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            OutlinedTextField(
                                value = contactPhone,
                                onValueChange = { input ->
                                    if (input.all { it.isDigit() || it == '+' || it == ' ' || it == '-' }) {
                                        contactPhone = input
                                        phoneWarning = false
                                    } else {
                                        phoneWarning = true
                                    }
                                },
                                label = { Text("Teléfono de Contacto *", color = TextSecondary) },
                                placeholder = { Text("+52 55 1234 5678", color = TextMuted) },
                                leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = TextMuted) },
                                singleLine = true,
                                isError = phoneWarning,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ClinicalEmerald, unfocusedBorderColor = BorderLight)
                            )
                            if (phoneWarning) {
                                Text("⚠️ En este campo solo se permiten números, espacio y '+'.", fontSize = 11.sp, color = Color(0xFFDC2626), fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // COLUMNA DERECHA
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        OutlinedTextField(
                            value = clinicAddress,
                            onValueChange = { clinicAddress = it },
                            label = { Text("Dirección de la Clínica *", color = TextSecondary) },
                            placeholder = { Text("Av. Principal 123, Ciudad", color = TextMuted) },
                            leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = TextMuted) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ClinicalEmerald, unfocusedBorderColor = BorderLight)
                        )

                        OutlinedTextField(
                            value = vetManagerName,
                            onValueChange = { vetManagerName = it },
                            label = { Text("Nombre del Veterinario Responsable *", color = TextSecondary) },
                            placeholder = { Text("MVZ Dr. Carlos Ramos", color = TextMuted) },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = TextMuted) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ClinicalEmerald, unfocusedBorderColor = BorderLight)
                        )

                        OutlinedTextField(
                            value = licenseNumber,
                            onValueChange = { licenseNumber = it },
                            label = { Text("Cédula Profesional / Licencia *", color = TextSecondary) },
                            placeholder = { Text("CÉD-981204", color = TextMuted) },
                            leadingIcon = { Icon(Icons.Default.Verified, contentDescription = null, tint = TextMuted) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ClinicalEmerald, unfocusedBorderColor = BorderLight)
                        )

                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            OutlinedTextField(
                                value = password,
                                onValueChange = { password = it },
                                label = { Text("Contraseña (Seguridad Alta) *", color = TextSecondary) },
                                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = TextMuted) },
                                trailingIcon = {
                                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                        Icon(imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff, contentDescription = null, tint = TextMuted)
                                    }
                                },
                                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                singleLine = true,
                                isError = passwordError != null,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ClinicalEmerald, unfocusedBorderColor = BorderLight)
                            )

                            if (passwordError != null) {
                                Text("⚠️ $passwordError", fontSize = 11.sp, color = Color(0xFFDC2626), fontWeight = FontWeight.Medium)
                            } else if (password.isNotEmpty()) {
                                Text("✓ Contraseña segura con formato correcto.", fontSize = 11.sp, color = Color(0xFF166534), fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                HorizontalDivider(color = BorderLight)

                // PARTE INFERIOR: Checkbox & Botones
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { acceptTerms = !acceptTerms }
                    ) {
                        Checkbox(
                            checked = acceptTerms,
                            onCheckedChange = { acceptTerms = it },
                            colors = CheckboxDefaults.colors(checkedColor = ClinicalEmerald)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Acepto los Términos de Servicio y la Política de Privacidad de PawsMedic.",
                            fontSize = 12.sp,
                            color = TextPrimary
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = onNavigateToLogin,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(44.dp)
                        ) {
                            Text("Cancelar", fontSize = 14.sp, color = TextPrimary)
                        }

                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    val pwdCheck = validatePassword(password)
                                    if (clinicName.isBlank() || adminEmail.isBlank() || rucTaxId.isBlank() || contactPhone.isBlank() || clinicAddress.isBlank() || vetManagerName.isBlank()) {
                                        errorMessage = "Por favor complete todos los campos obligatorios (*)."
                                        return@launch
                                    }

                                    if (!adminEmail.contains("@") || !adminEmail.contains(".")) {
                                        errorMessage = "El correo electrónico no tiene un formato válido."
                                        return@launch
                                    }
                                    if (pwdCheck != null) {
                                        errorMessage = pwdCheck
                                        return@launch
                                    }
                                    if (!acceptTerms) {
                                        errorMessage = "Debe aceptar los Términos de Servicio para continuar."
                                        return@launch
                                    }

                                    isLoading = true
                                    errorMessage = null
                                    try {
                                        val application = AccountApplicationDto(
                                            type = AccountType.VETERINARIA,
                                            applicantName = vetManagerName.trim(),
                                            businessName = clinicName.trim(),
                                            taxId = rucTaxId.trim(),
                                            email = adminEmail.trim(),
                                            phone = contactPhone.trim(),
                                            address = clinicAddress.trim(),
                                            licenseNumber = licenseNumber.trim(),
                                            status = "PENDIENTE"
                                        )

                                        try {
                                            supabase.postgrest["business_applications"].insert(application)
                                        } catch (e: Exception) {
                                            // Handled
                                        }

                                        successMessage = "¡Solicitud enviada al Superadmin! Su cuenta será habilitada una vez aprobada."
                                        delay(1800)
                                        onNavigateToLogin()
                                    } catch (e: Exception) {
                                        errorMessage = e.message ?: "Error al procesar la solicitud."
                                    } finally {
                                        isLoading = false
                                    }
                                }
                            },
                            enabled = !isLoading,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = ClinicalEmerald),
                            modifier = Modifier.height(44.dp)
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text(
                                    text = "Enviar Solicitud de Registro",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun RegisterScreenPreview() {
    RegisterScreen(
        onNavigateToVetMain = {},
        onNavigateToLogin = {}
    )
}

fun main() = singleWindowApplication(title = "Preview - Register Screen") {
    RegisterScreen(
        onNavigateToVetMain = {},
        onNavigateToLogin = {}
    )
}

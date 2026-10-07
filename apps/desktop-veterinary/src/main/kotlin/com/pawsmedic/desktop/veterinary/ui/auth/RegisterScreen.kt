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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.singleWindowApplication
import com.pawsmedic.desktop.veterinary.BusinessRegistration
import com.pawsmedic.desktop.veterinary.DesktopAccess
import com.pawsmedic.desktop.veterinary.ui.theme.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

@Composable
fun RegisterScreen(
    onSubmit: suspend (BusinessRegistration) -> DesktopAccess?,
    onNavigateToLogin: () -> Unit
) {
    var clinicName by remember { mutableStateOf("") }
    var rucTaxId by remember { mutableStateOf("") }
    var adminEmail by remember { mutableStateOf("") }
    var contactPhone by remember { mutableStateOf("") }

    var clinicAddress by remember { mutableStateOf("") }
    var municipality by remember { mutableStateOf("") }
    var vetManagerName by remember { mutableStateOf("") }
    var licenseNumber by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    var acceptTerms by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

    fun submitRegistration() {
        val requiredFields = listOf(
            clinicName,
            rucTaxId,
            adminEmail,
            contactPhone,
            clinicAddress,
            municipality,
            vetManagerName,
            licenseNumber,
            password
        )
        if (requiredFields.any { it.isBlank() }) {
            errorMessage = "Completa todos los campos para enviar la solicitud."
            return
        }
        if (!adminEmail.trim().matches(Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$"))) {
            errorMessage = "Ingresa un correo electrónico válido."
            return
        }
        if (contactPhone.count(Char::isDigit) < 7) {
            errorMessage = "Ingresa un teléfono válido con al menos 7 dígitos."
            return
        }
        if (password.length < 8) {
            errorMessage = "La contraseña debe tener al menos 8 caracteres."
            return
        }
        if (!acceptTerms) {
            errorMessage = "Debes aceptar los Términos de Servicio."
            return
        }

        coroutineScope.launch {
            isLoading = true
            errorMessage = null
            successMessage = null
            try {
                val access = onSubmit(
                    BusinessRegistration(
                        clinicName = clinicName.trim(),
                        taxId = rucTaxId.trim(),
                        email = adminEmail.trim(),
                        phone = contactPhone.trim(),
                        address = clinicAddress.trim(),
                        municipality = municipality.trim(),
                        veterinarianName = vetManagerName.trim(),
                        licenseNumber = licenseNumber.trim(),
                        password = password
                    )
                )
                if (access == null) {
                    successMessage =
                        "Solicitud enviada. Confirma tu correo si recibes el mensaje y luego inicia sesión. El acceso quedará pendiente hasta la aprobación del Superadmin."
                }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                errorMessage = error.message ?: "No se pudo enviar la solicitud."
            } finally {
                isLoading = false
            }
        }
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
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // ENCABEZADO
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

                HorizontalDivider(color = BorderLight)

                // FORMULARIO DE 2 COLUMNAS
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    // COLUMNA IZQUIERDA
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        OutlinedTextField(
                            value = clinicName,
                            onValueChange = { clinicName = it },
                            label = { Text("Nombre de la Clínica", color = TextSecondary) },
                            placeholder = { Text("ej. Veterinaria San Miguel", color = TextMuted) },
                            leadingIcon = { Icon(Icons.Default.Store, contentDescription = null, tint = TextMuted) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ClinicalEmerald,
                                unfocusedBorderColor = BorderLight
                            )
                        )

                        OutlinedTextField(
                            value = rucTaxId,
                            onValueChange = { rucTaxId = it },
                            label = { Text("RUC / ID Fiscal", color = TextSecondary) },
                            placeholder = { Text("ej. 20123456789", color = TextMuted) },
                            leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null, tint = TextMuted) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ClinicalEmerald,
                                unfocusedBorderColor = BorderLight
                            )
                        )

                        OutlinedTextField(
                            value = adminEmail,
                            onValueChange = { adminEmail = it },
                            label = { Text("Correo Electrónico Administrador", color = TextSecondary) },
                            placeholder = { Text("admin@veterinaria.com", color = TextMuted) },
                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = TextMuted) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ClinicalEmerald,
                                unfocusedBorderColor = BorderLight
                            )
                        )

                        OutlinedTextField(
                            value = contactPhone,
                            onValueChange = { value ->
                                contactPhone = value.filter { it.isDigit() || it in "+- ()" }
                            },
                            label = { Text("Teléfono de Contacto", color = TextSecondary) },
                            placeholder = { Text("+52 55 1234 5678", color = TextMuted) },
                            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = TextMuted) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ClinicalEmerald,
                                unfocusedBorderColor = BorderLight
                            )
                        )
                    }

                    // COLUMNA DERECHA
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        OutlinedTextField(
                            value = clinicAddress,
                            onValueChange = { clinicAddress = it },
                            label = { Text("Dirección de la Clínica", color = TextSecondary) },
                            placeholder = { Text("Av. Principal 123, Ciudad", color = TextMuted) },
                            leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = TextMuted) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ClinicalEmerald,
                                unfocusedBorderColor = BorderLight
                            )
                        )

                        OutlinedTextField(
                            value = municipality,
                            onValueChange = { municipality = it },
                            label = { Text("Municipio / Ciudad", color = TextSecondary) },
                            placeholder = { Text("ej. Ciudad de México", color = TextMuted) },
                            leadingIcon = { Icon(Icons.Default.LocationCity, contentDescription = null, tint = TextMuted) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ClinicalEmerald,
                                unfocusedBorderColor = BorderLight
                            )
                        )

                        OutlinedTextField(
                            value = vetManagerName,
                            onValueChange = { vetManagerName = it },
                            label = { Text("Nombre del Veterinario Responsable", color = TextSecondary) },
                            placeholder = { Text("MVZ Dr. Carlos Ramos", color = TextMuted) },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = TextMuted) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ClinicalEmerald,
                                unfocusedBorderColor = BorderLight
                            )
                        )

                        OutlinedTextField(
                            value = licenseNumber,
                            onValueChange = { licenseNumber = it },
                            label = { Text("Cédula Profesional / Licencia", color = TextSecondary) },
                            placeholder = { Text("CÉD-981204", color = TextMuted) },
                            leadingIcon = { Icon(Icons.Default.Verified, contentDescription = null, tint = TextMuted) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ClinicalEmerald,
                                unfocusedBorderColor = BorderLight
                            )
                        )

                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("Contraseña", color = TextSecondary) },
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                keyboardType = KeyboardType.Password
                            ),
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = TextMuted) },
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = null,
                                        tint = TextMuted
                                    )
                                }
                            },
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ClinicalEmerald,
                                unfocusedBorderColor = BorderLight
                            )
                        )
                    }
                }

                HorizontalDivider(color = BorderLight)

                if (errorMessage != null || successMessage != null) {
                    Text(
                        text = errorMessage ?: successMessage.orEmpty(),
                        color = if (errorMessage != null) Color(0xFFB91C1C) else ClinicalEmerald,
                        fontSize = 13.sp,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

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
                            enabled = !isLoading,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(44.dp)
                        ) {
                            Text("Cancelar", fontSize = 14.sp, color = TextPrimary)
                        }

                        Button(
                            onClick = ::submitRegistration,
                            enabled = !isLoading && successMessage == null,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = ClinicalEmerald),
                            modifier = Modifier.height(44.dp)
                        ) {
                            Text(
                                text = when {
                                    isLoading -> "Enviando..."
                                    successMessage != null -> "Solicitud Enviada"
                                    else -> "Enviar Solicitud de Registro"
                                },
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

@Preview
@Composable
fun RegisterScreenPreview() {
    RegisterScreen(
        onSubmit = { null },
        onNavigateToLogin = {}
    )
}

fun main() = singleWindowApplication(title = "Preview - Register Screen") {
    RegisterScreen(
        onSubmit = { null },
        onNavigateToLogin = {}
    )
}

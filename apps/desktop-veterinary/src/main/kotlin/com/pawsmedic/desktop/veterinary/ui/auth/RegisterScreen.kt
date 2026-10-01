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

    val darkSlate = Color(0xFF0F172A)
    val textPrimary = Color(0xFF0F172A)
    val textMuted = Color(0xFF94A3B8)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .width(880.dp)
                .wrapContentHeight(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
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
                            .background(Color(0xFFF1F5F9)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AppRegistration,
                            contentDescription = null,
                            tint = darkSlate,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "Registro de Clínica Veterinaria",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = textPrimary
                        )
                        Text(
                            text = "Solicita el alta de tu establecimiento para comenzar a utilizar PawsMedic",
                            fontSize = 13.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                }

                HorizontalDivider(color = Color(0xFFE2E8F0))

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
                            label = { Text("Nombre de la Clínica") },
                            placeholder = { Text("ej. Veterinaria San Miguel") },
                            leadingIcon = { Icon(Icons.Default.Store, contentDescription = null, tint = textMuted) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        )

                        OutlinedTextField(
                            value = rucTaxId,
                            onValueChange = { rucTaxId = it },
                            label = { Text("RUC / ID Fiscal") },
                            placeholder = { Text("ej. 20123456789") },
                            leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null, tint = textMuted) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        )

                        OutlinedTextField(
                            value = adminEmail,
                            onValueChange = { adminEmail = it },
                            label = { Text("Correo Electrónico Administrador") },
                            placeholder = { Text("admin@veterinaria.com") },
                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = textMuted) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        )

                        OutlinedTextField(
                            value = contactPhone,
                            onValueChange = { contactPhone = it },
                            label = { Text("Teléfono de Contacto") },
                            placeholder = { Text("+52 55 1234 5678") },
                            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = textMuted) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
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
                            label = { Text("Dirección de la Clínica") },
                            placeholder = { Text("Av. Principal 123, Ciudad") },
                            leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = textMuted) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        )

                        OutlinedTextField(
                            value = vetManagerName,
                            onValueChange = { vetManagerName = it },
                            label = { Text("Nombre del Veterinario Responsable") },
                            placeholder = { Text("MVZ Dr. Carlos Ramos") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = textMuted) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        )

                        OutlinedTextField(
                            value = licenseNumber,
                            onValueChange = { licenseNumber = it },
                            label = { Text("Cédula Profesional / Licencia") },
                            placeholder = { Text("CÉD-981204") },
                            leadingIcon = { Icon(Icons.Default.Verified, contentDescription = null, tint = textMuted) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        )

                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("Contraseña") },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = textMuted) },
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = null,
                                        tint = textMuted
                                    )
                                }
                            },
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                }

                HorizontalDivider(color = Color(0xFFE2E8F0))

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
                            onCheckedChange = { acceptTerms = it }
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Acepto los Términos de Servicio y la Política de Privacidad de PawsMedic.",
                            fontSize = 12.sp,
                            color = textPrimary
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
                            Text("Cancelar", fontSize = 14.sp, color = textPrimary)
                        }

                        Button(
                            onClick = onNavigateToVetMain,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = darkSlate),
                            modifier = Modifier.height(44.dp)
                        ) {
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

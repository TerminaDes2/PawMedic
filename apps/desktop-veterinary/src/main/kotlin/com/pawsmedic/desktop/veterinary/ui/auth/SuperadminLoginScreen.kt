package com.pawsmedic.desktop.veterinary.ui.auth

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
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
import com.pawsmedic.desktop.veterinary.SessionManager
import com.pawsmedic.desktop.veterinary.supabase
import com.pawsmedic.desktop.veterinary.ui.theme.*
import com.pawsmedic.shared.core.model.ProfileRole
import com.pawsmedic.shared.core.model.UserProfile
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email as SupabaseEmail
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.launch

@Composable
fun SuperadminLoginScreen(
    onNavigateToAdminMain: (String) -> Unit,
    onBackToLogin: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var rememberMe by remember { mutableStateOf(true) }

    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val coroutineScope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(OffWhiteBg)
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            // LADO IZQUIERDO: Branding Superadmin
            Column(
                modifier = Modifier
                    .weight(1.35f)
                    .fillMaxHeight()
                    .background(DarkSlate)
                    .padding(48.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(ClinicalEmerald),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AdminPanelSettings,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "Paws", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Text(text = "Admin", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = ClinicalEmerald)
                    }
                }

                Column(
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    Text(
                        text = "Control Central de Plataforma",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        lineHeight = 40.sp
                    )
                    Text(
                        text = "Supervisión general, aprobación de solicitudes de nuevas clínicas y auditoría central de operaciones veterinarias.",
                        fontSize = 15.sp,
                        color = Color(0xFF94A3B8),
                        lineHeight = 22.sp
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "© 2026 PawsMedic. Superadmin Suite.", fontSize = 12.sp, color = TextMuted)
                    TextButton(onClick = onBackToLogin) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = ClinicalEmerald, modifier = Modifier.size(16.dp))
                            Text("Volver al login de veterinaria", fontSize = 12.sp, color = ClinicalEmerald, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // LADO DERECHO: Formulario de Login Superadmin
            Box(
                modifier = Modifier
                    .weight(0.85f)
                    .fillMaxHeight()
                    .padding(40.dp),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier.width(440.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = CardWhite),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(36.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(18.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(EmeraldLightBg),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                tint = ClinicalEmerald,
                                modifier = Modifier.size(26.dp)
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Acceso Superadmin",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Credenciales de control central requeridas",
                                fontSize = 13.sp,
                                color = TextSecondary
                            )
                        }

                        if (errorMessage != null) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFFFEE2E2))
                                    .padding(10.dp)
                            ) {
                                Text(
                                    text = errorMessage ?: "",
                                    fontSize = 12.sp,
                                    color = Color(0xFF991B1B),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        Column(
                            verticalArrangement = Arrangement.spacedBy(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("Correo de Superadmin", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                                OutlinedTextField(
                                    value = email,
                                    onValueChange = { email = it; errorMessage = null },
                                    placeholder = { Text("admin@pawsmedic.com", fontSize = 13.sp, color = TextMuted) },
                                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = TextMuted, modifier = Modifier.size(18.dp)) },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = ClinicalEmerald,
                                        unfocusedBorderColor = BorderLight
                                    )
                                )
                            }

                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("Contraseña", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                                OutlinedTextField(
                                    value = password,
                                    onValueChange = { password = it; errorMessage = null },
                                    placeholder = { Text("••••••••••••", fontSize = 13.sp, color = TextMuted) },
                                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = TextMuted, modifier = Modifier.size(18.dp)) },
                                    trailingIcon = {
                                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                            Icon(
                                                imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                                contentDescription = null,
                                                tint = TextMuted,
                                                modifier = Modifier.size(18.dp)
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

                        // Recordarme
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.clickable { rememberMe = !rememberMe }
                            ) {
                                Checkbox(
                                    checked = rememberMe,
                                    onCheckedChange = { rememberMe = it },
                                    colors = CheckboxDefaults.colors(checkedColor = ClinicalEmerald)
                                )
                                Text("Recordarme", fontSize = 12.sp, color = TextSecondary)
                            }
                        }

                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    isLoading = true
                                    errorMessage = null
                                    try {
                                        val cleanEmail = email.trim()
                                        val submittedPassword = password

                                        if (cleanEmail.isBlank() || submittedPassword.isBlank()) {
                                            throw IllegalArgumentException("Por favor ingrese correo y contraseña.")
                                        }

                                        supabase.auth.signInWith(SupabaseEmail) {
                                            this.email = cleanEmail
                                            this.password = submittedPassword
                                        }
                                        val session = supabase.auth.currentSessionOrNull() ?: error("No se pudo iniciar sesión.")
                                        val profile = supabase.postgrest["profiles"]
                                            .select { filter { eq("id", session.user!!.id) } }
                                            .decodeSingle<UserProfile>()
                                        if (profile.role != ProfileRole.SUPERADMIN) {
                                            supabase.auth.signOut()
                                            error("Esta cuenta no tiene permisos de Superadmin.")
                                        }

                                        SessionManager.saveSession(email = cleanEmail, rememberMe = rememberMe)
                                        onNavigateToAdminMain(cleanEmail)
                                    } catch (e: Exception) {
                                        val rawMsg = e.message.orEmpty()
                                        errorMessage = when {
                                            rawMsg.contains("missing email or phone", ignoreCase = true) ||
                                            rawMsg.contains("invalid_credentials", ignoreCase = true) ||
                                            rawMsg.contains("invalid login", ignoreCase = true) ->
                                                "Correo o contraseña incorrectos, o cuenta no verificada."
                                            else -> rawMsg.ifBlank { "Error de acceso. Verifique sus credenciales." }
                                        }
                                    } finally {
                                        isLoading = false
                                    }
                                }
                            },
                            enabled = !isLoading,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = ClinicalEmerald)
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                                    Text(
                                        text = "Ingresar como Superadmin",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                        }

                        OutlinedButton(
                            onClick = onBackToLogin,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(42.dp),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Volver al inicio de sesión veterinario", fontSize = 12.sp, color = TextPrimary)
                        }
                    }
                }
            }
        }
    }
}

package com.pawsmedic.desktop.veterinary.ui.auth

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.coroutines.launch

@Serializable
private data class BusinessLoginStatus(
    @SerialName("status") val status: String = "",
    @SerialName("decision_reason") val decisionReason: String? = null
)

@Serializable
private data class TenantLoginStatus(
    @SerialName("status") val status: String = ""
)

@Composable
fun LoginScreen(
    onNavigateToVetMain: (String) -> Unit = {},
    onNavigateToAdminLogin: () -> Unit = {},
    onNavigateToRegister: () -> Unit = {}
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var rememberMe by remember { mutableStateOf(true) }

    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val coroutineScope = rememberCoroutineScope()
    val cardDarkIllustration = Color(0xFF1C2A3A)
    val borderDark = Color(0xFF2A3C4E)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(OffWhiteBg)
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            // LADO IZQUIERDO: Branding, Textos e Ilustración Cuadrada en Row
            Column(
                modifier = Modifier
                    .weight(1.35f)
                    .fillMaxHeight()
                    .background(DarkSlate)
                    .padding(48.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // 1. Header: Logo PawsMedic
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
                            imageVector = Icons.Default.Pets,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Paws",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Medic",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = ClinicalEmerald
                        )
                    }
                }

                // 2. Contenido Central: Row con Textos a la izquierda e Ilustración Cuadrada a la derecha
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(32.dp)
                ) {
                    // Columna Izquierda: Textos y Bullets
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        // Badge "TECNOLOGÍA QUE CUIDA"
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(Color(0xFF115E59))
                                .border(1.dp, Color(0xFF134E4A), RoundedCornerShape(20.dp))
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF2DD4BF))
                                )
                                Text(
                                    text = "TECNOLOGÍA QUE CUIDA",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF2DD4BF),
                                    letterSpacing = 0.8.sp
                                )
                            }
                        }

                        Text(
                            text = "Cuidamos\nhistorias, no\nsolo mascotas.",
                            fontSize = 38.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            lineHeight = 44.sp
                        )

                        Text(
                            text = "Sistema de Gestión Veterinaria e Historial Clínico",
                            fontSize = 14.sp,
                            color = TextMuted
                        )

                        // Bullets Informativos
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF115E59)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = Color(0xFF2DD4BF), modifier = Modifier.size(14.dp))
                                }
                                Text("Información clínica protegida", fontSize = 13.sp, color = Color(0xFFCBD5E1))
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF115E59)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.History, contentDescription = null, tint = Color(0xFF2DD4BF), modifier = Modifier.size(14.dp))
                                }
                                Text("Todo el historial, siempre disponible", fontSize = 13.sp, color = Color(0xFFCBD5E1))
                            }
                        }
                    }

                    // Columna Derecha: Box Cuadrado de Ilustración
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(20.dp))
                            .background(cardDarkIllustration)
                            .border(1.dp, borderDark, RoundedCornerShape(20.dp))
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                    Icon(Icons.Default.HealthAndSafety, contentDescription = null, tint = ClinicalEmerald, modifier = Modifier.size(36.dp))
                                    Icon(Icons.Default.Favorite, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(36.dp))
                                    Icon(Icons.Default.Vaccines, contentDescription = null, tint = Color(0xFFA855F7), modifier = Modifier.size(36.dp))
                                }
                                Text(
                                    text = "PawsMedic Clinical Suite",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                }

                // 3. Footer: Textos en Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "© 2026 PawsMedic. Cuidado conectado.",
                        fontSize = 12.sp,
                        color = TextMuted
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(ClinicalEmerald)
                        )
                        Text(
                            text = "Plataforma operativa",
                            fontSize = 12.sp,
                            color = TextMuted
                        )
                    }
                }
            }

            // LADO DERECHO: Formulario Blanco Elevado
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
                        // Ícono Superior Encabezado
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(EmeraldLightBg),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.MedicalServices,
                                contentDescription = null,
                                tint = ClinicalEmerald,
                                modifier = Modifier.size(26.dp)
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Iniciar Sesión",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Ingresa tus credenciales para acceder",
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
                                Text("Email", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                                OutlinedTextField(
                                    value = email,
                                    onValueChange = { email = it },
                                    placeholder = { Text("nombre@clinica.com", fontSize = 13.sp, color = TextMuted) },
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
                                Text("Password", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                                OutlinedTextField(
                                    value = password,
                                    onValueChange = { password = it },
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

                        // Recordarme & Olvidé mi contraseña
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

                        // Botón Principal Esmeralda
                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    isLoading = true
                                    errorMessage = null
                                    try {
                                        val cleanEmail = email.trim()
                                        val cleanPassword = password

                                        if (cleanEmail.isBlank() || cleanPassword.isBlank()) {
                                            throw IllegalArgumentException("Por favor ingrese correo electrónico y contraseña")
                                        }
                                        if (!Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$").matches(cleanEmail)) {
                                            throw IllegalArgumentException("Ingresa un correo electrónico válido.")
                                        }

                                        supabase.auth.signInWith(SupabaseEmail) {
                                            this.email = cleanEmail
                                            this.password = cleanPassword
                                        }
                                        val session = supabase.auth.currentSessionOrNull()
                                            ?: error("Supabase no devolvió una sesión autenticada.")
                                        val userId = session.user?.id
                                            ?: error("La sesión no contiene un identificador de usuario.")
                                        val profile = supabase.postgrest["profiles"]
                                            .select {
                                                filter { eq("id", userId) }
                                            }
                                            .decodeSingle<UserProfile>()

                                        when (profile.role) {
                                            ProfileRole.SUPERADMIN -> onNavigateToAdminLogin()
                                            ProfileRole.VETERINARY_BUSINESS -> {
                                                if (profile.tenantId == null) {
                                                    onNavigateToAdminLogin()
                                                } else {
                                                    val tenantId = profile.tenantId
                                                        ?: error("La cuenta veterinaria todavía no tiene una clínica aprobada.")
                                                    val tenant = supabase.postgrest["tenants"]
                                                        .select {
                                                            filter { eq("id", tenantId) }
                                                        }
                                                        .decodeSingle<TenantLoginStatus>()
                                                    if (tenant.status != "ACTIVE") {
                                                        error("La clínica no está activa. Contacta al administrador.")
                                                    }
                                                    onNavigateToVetMain(cleanEmail)
                                                }
                                            }
                                            ProfileRole.USER -> {
                                                error("Esta aplicación de escritorio es exclusiva para negocios veterinarios. Las cuentas de usuario de clientes deben ingresar desde la app móvil.")
                                            }
                                        }

                                        SessionManager.saveSession(email = cleanEmail, rememberMe = rememberMe)
                                        } catch (e: Exception) {
                                        e.printStackTrace()
                                        println("=== PAWSMEDIC LOGIN ERROR ===")
                                        println(e.stackTraceToString())
                                        val rawMsg = e.message.orEmpty()
                                        errorMessage = when {
                                            rawMsg.contains("missing email or phone", ignoreCase = true) ||
                                            rawMsg.contains("invalid_credentials", ignoreCase = true) ||
                                            rawMsg.contains("invalid login", ignoreCase = true) ->
                                                "Correo o contraseña incorrectos, o la cuenta no ha sido verificada."
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
                                        text = "Ingresar al Sistema",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                        }

                        // Enlace de Registro
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("¿No tienes cuenta?", fontSize = 12.sp, color = TextSecondary)
                            TextButton(onClick = onNavigateToRegister) {
                                Text("Regístrate aquí", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = ClinicalEmerald)
                            }
                        }

                        HorizontalDivider(color = BorderLight)

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = TextMuted, modifier = Modifier.size(14.dp))
                            Text("Acceso Seguro SSL", fontSize = 11.sp, color = TextMuted)
                        }
                    }
                }
            }
        }

        // Botón sutil en la esquina inferior derecha para Superadmin
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.BottomEnd
        ) {
            IconButton(
                onClick = onNavigateToAdminLogin,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AdminPanelSettings,
                    contentDescription = "Acceso Superadmin",
                    tint = TextMuted.copy(alpha = 0.35f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Preview
@Composable
fun LoginScreenPreview() {
    LoginScreen(
        onNavigateToVetMain = {},
        onNavigateToAdminLogin = {},
        onNavigateToRegister = {}
    )
}

fun main() = singleWindowApplication(title = "Preview - Login Screen") {
    LoginScreen(
        onNavigateToVetMain = {},
        onNavigateToAdminLogin = {},
        onNavigateToRegister = {}
    )
}

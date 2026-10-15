package com.pawsmedic.desktop.veterinary.ui.auth

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.singleWindowApplication
import com.pawsmedic.desktop.veterinary.supabase
import com.pawsmedic.desktop.veterinary.ui.theme.*
import io.github.jan.supabase.auth.OtpType
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email as SupabaseEmail
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

@Serializable
private data class SubmittedBusinessApplication(
    @SerialName("id") val id: String
)

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
    var requiresEmailVerification by remember { mutableStateOf(false) }
    var verificationCode by remember { mutableStateOf("") }
    var submittedFolio by remember { mutableStateOf<String?>(null) }
    var accountCreated by remember { mutableStateOf(false) }
    var emailVerified by remember { mutableStateOf(false) }
    var useExistingAccount by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    suspend fun submitApplication() {
        try {
            val rpcResult = supabase.postgrest.rpc(
                function = "submit_business_application",
                parameters = buildJsonObject {
                    put("p_applicant_name", vetManagerName.trim())
                    put("p_business_name", clinicName.trim())
                    put("p_tax_id", rucTaxId.trim())
                    put("p_phone", contactPhone.trim())
                    put("p_address", clinicAddress.trim())
                    put("p_license_number", licenseNumber.trim())
                }
            )
            val application = kotlinx.serialization.json.Json { ignoreUnknownKeys = true }
                .decodeFromString<SubmittedBusinessApplication>(rpcResult.data)
            submittedFolio = application.id
            successMessage = "¡Solicitud enviada con éxito! Redirigiendo..."
            requiresEmailVerification = false
            kotlinx.coroutines.delay(2000)
            supabase.auth.signOut()
            onNavigateToLogin()
        } catch (e: Exception) {
            e.printStackTrace()
            println("=== PAWSMEDIC SUBMIT APPLICATION ERROR ===")
            println(e.stackTraceToString())
            throw e
        }
    }

    fun validatePassword(pwd: String): String? {
        if (pwd.length < 8) {
            return "La contraseña debe tener al menos 8 caracteres."
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
                if (submittedFolio != null) {
                    Text(
                        text = "Folio de solicitud: ${submittedFolio}\nEstado: PENDIENTE DE APROBACIÓN",
                        fontSize = 13.sp,
                        color = Color(0xFF166534),
                        fontWeight = FontWeight.Bold
                    )
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

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = {
                            useExistingAccount = !useExistingAccount
                            errorMessage = null
                            successMessage = null
                        }
                    ) {
                        Text(
                            if (useExistingAccount) {
                                "¿Aún no tienes cuenta? Regístrate con este correo"
                            } else {
                                "¿Ya tienes cuenta? Inicia sesión para solicitar aprobación"
                            }
                        )
                    }
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
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(44.dp)
                        ) {
                            Text("Cancelar", fontSize = 14.sp, color = TextPrimary)
                        }

                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    val pwdCheck = validatePassword(password)
                                    if (clinicName.isBlank() || adminEmail.isBlank() || rucTaxId.isBlank() ||
                                        contactPhone.isBlank() || clinicAddress.isBlank() ||
                                        vetManagerName.isBlank() || licenseNumber.isBlank()) {
                                        errorMessage = "Por favor complete todos los campos obligatorios (*)."
                                        return@launch
                                    }

                                    if (!Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
                                            .matches(adminEmail.trim())) {
                                        errorMessage = "El correo electrónico no tiene un formato válido."
                                        return@launch
                                    }
                                    if (!useExistingAccount && pwdCheck != null) {
                                        errorMessage = pwdCheck
                                        return@launch
                                    }
                                    if (!acceptTerms) {
                                        errorMessage = "Debe aceptar los Términos de Servicio para continuar."
                                        return@launch
                                    }

                                    isLoading = true
                                    errorMessage = null
                                    successMessage = null
                                    try {
                                        when {
                                            submittedFolio != null -> return@launch
                                            useExistingAccount -> {
                                                supabase.auth.signOut()
                                                supabase.auth.signInWith(SupabaseEmail) {
                                                    this.email = adminEmail.trim()
                                                    this.password = password
                                                }
                                                submitApplication()
                                            }
                                            emailVerified -> submitApplication()
                                            accountCreated -> {
                                                try {
                                                    supabase.auth.resendEmail(
                                                        type = OtpType.Email.SIGNUP,
                                                        email = adminEmail.trim()
                                                    )
                                                    requiresEmailVerification = true
                                                    successMessage = "Te reenviamos un código a ${adminEmail.trim()}."
                                                } catch (e: Exception) {
                                                    val rawMsg = e.message.orEmpty()
                                                    if (rawMsg.contains("missing email or phone", ignoreCase = true) ||
                                                        rawMsg.contains("confirmed", ignoreCase = true) ||
                                                        rawMsg.contains("already exists", ignoreCase = true)) {
                                                        useExistingAccount = true
                                                        errorMessage = "Este correo ya está verificado en el sistema. Inicia sesión para continuar."
                                                    } else {
                                                        throw e
                                                    }
                                                }
                                            }
                                            else -> {
                                                supabase.auth.signOut()
                                                try {
                                                    supabase.auth.signUpWith(SupabaseEmail) {
                                                        this.email = adminEmail.trim()
                                                        this.password = password
                                                        data = buildJsonObject {
                                                            put("full_name", vetManagerName.trim())
                                                        }
                                                    }
                                                } catch (e: Exception) {
                                                    val rawMsg = e.message.orEmpty()
                                                    if (rawMsg.contains("already registered", ignoreCase = true) ||
                                                        rawMsg.contains("already exists", ignoreCase = true) ||
                                                        rawMsg.contains("missing email or phone", ignoreCase = true)) {
                                                        useExistingAccount = true
                                                        errorMessage = "Este correo ya está registrado en el sistema. Inicia sesión con tus credenciales para enviar la solicitud."
                                                        return@launch
                                                    } else {
                                                        throw e
                                                    }
                                                }

                                                if (supabase.auth.currentSessionOrNull() != null) {
                                                    supabase.auth.signOut()
                                                    error("Activa la confirmación de correo en Supabase para exigir el código de verificación.")
                                                }
                                                accountCreated = true
                                                requiresEmailVerification = true
                                                verificationCode = ""
                                                successMessage =
                                                    "Si este correo es nuevo, recibirás un código en ${adminEmail.trim()}. " +
                                                        "Si ya tienes una cuenta de PawMedic, inicia sesión con esa cuenta para enviar la solicitud."
                                            }
                                        }
                                    } catch (e: CancellationException) {
                                        throw e
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                        println("=== PAWSMEDIC REGISTER FLOW ERROR ===")
                                        println(e.stackTraceToString())
                                        errorMessage = e.message ?: "Error al procesar la solicitud."
                                    } finally {
                                        isLoading = false
                                    }
                                }
                            },
                            enabled = !isLoading && submittedFolio == null,
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
                                    text = when {
                                        useExistingAccount -> "Iniciar sesión y enviar solicitud"
                                        emailVerified -> "Reintentar envío de solicitud"
                                        accountCreated -> "Reenviar código de verificación"
                                        else -> "Crear cuenta y verificar correo"
                                    },
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }

                    if (requiresEmailVerification) {
                        Dialog(onDismissRequest = { requiresEmailVerification = false }) {
                            Card(
                                modifier = Modifier.width(460.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = CardWhite)
                            ) {
                                Column(
                                    modifier = Modifier.padding(28.dp),
                                    verticalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    Text(
                                        text = "Verifica tu correo",
                                        fontSize = 22.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = "Ingresa el código de 6 dígitos que enviamos a ${adminEmail.trim()}.",
                                        fontSize = 14.sp,
                                        color = TextSecondary
                                    )
                                    TextButton(
                                        onClick = {
                                            requiresEmailVerification = false
                                            useExistingAccount = true
                                            accountCreated = false
                                            verificationCode = ""
                                            errorMessage = null
                                        }
                                    ) {
                                        Text("¿Ya registraste este correo en la app móvil? Usa esa cuenta.")
                                    }
                                    OutlinedTextField(
                                        value = verificationCode,
                                        onValueChange = { value ->
                                            if (value.length <= 6 && value.all(Char::isDigit)) {
                                                verificationCode = value
                                            }
                                        },
                                        label = { Text("Código de verificación") },
                                        keyboardOptions = KeyboardOptions(
                                            keyboardType = KeyboardType.Number,
                                            imeAction = ImeAction.Done
                                        ),
                                        singleLine = true,
                                        enabled = !isLoading && !emailVerified,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    Text(
                                        text = "Verificar el correo solo permite enviar la solicitud; la clínica seguirá pendiente hasta que Superadmin la apruebe.",
                                        fontSize = 12.sp,
                                        color = TextSecondary
                                    )
                                    if (errorMessage != null) {
                                        Text(errorMessage.orEmpty(), color = Color(0xFFB91C1C), fontSize = 13.sp)
                                    }
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.End)
                                    ) {
                                        TextButton(
                                            enabled = !isLoading,
                                            onClick = {
                                                coroutineScope.launch {
                                                    isLoading = true
                                                    errorMessage = null
                                                    try {
                                                        supabase.auth.resendEmail(
                                                            type = OtpType.Email.SIGNUP,
                                                            email = adminEmail.trim()
                                                        )
                                                        successMessage = "Te reenviamos un nuevo código."
                                                    } catch (e: CancellationException) {
                                                        throw e
                                                    } catch (e: Exception) {
                                                        val rawMsg = e.message.orEmpty()
                                                        errorMessage = when {
                                                            rawMsg.contains("missing email or phone", ignoreCase = true) ||
                                                            rawMsg.contains("confirmed", ignoreCase = true) ->
                                                                "Este correo ya fue verificado o ya está registrado. Selecciona la opción 'Usa esa cuenta'."
                                                            rawMsg.contains("rate limit", ignoreCase = true) ||
                                                            rawMsg.contains("429", ignoreCase = true) ->
                                                                "Por seguridad, debes esperar 60 segundos antes de reenviar otro código."
                                                            else -> rawMsg.ifBlank { "No se pudo reenviar el código." }
                                                        }
                                                    } finally {
                                                        isLoading = false
                                                    }
                                                }
                                            }
                                        ) {
                                            Text("Reenviar código")
                                        }
                                        Button(
                                            enabled = !isLoading && (emailVerified || verificationCode.length == 6),
                                            colors = ButtonDefaults.buttonColors(containerColor = ClinicalEmerald),
                                            onClick = {
                                                coroutineScope.launch {
                                                    isLoading = true
                                                    errorMessage = null
                                                    try {
                                                        if (!emailVerified) {
                                                            supabase.auth.verifyEmailOtp(
                                                                type = OtpType.Email.SIGNUP,
                                                                email = adminEmail.trim(),
                                                                token = verificationCode
                                                            )
                                                            emailVerified = true
                                                        }
                                                        submitApplication()
                                                    } catch (e: CancellationException) {
                                                        throw e
                                                    } catch (e: Exception) {
                                                        errorMessage = e.message ?: "No se pudo verificar el código."
                                                    } finally {
                                                        isLoading = false
                                                    }
                                                }
                                            }
                                        ) {
                                            if (isLoading) {
                                                CircularProgressIndicator(
                                                    modifier = Modifier.size(18.dp),
                                                    color = Color.White,
                                                    strokeWidth = 2.dp
                                                )
                                            } else {
                                                Text(if (emailVerified) "Reintentar" else "Verificar y enviar")
                                            }
                                        }
                                    }
                                }
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

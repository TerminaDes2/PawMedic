package com.pawsmedic.desktop.veterinary.ui.auth

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.Image
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.singleWindowApplication
import com.pawsmedic.desktop.veterinary.supabase
import com.pawsmedic.desktop.veterinary.ui.SelectedPhoto
import com.pawsmedic.desktop.veterinary.ui.ProfilePhotoPicker
import com.pawsmedic.desktop.veterinary.ui.theme.*
import com.pawsmedic.desktop.admin.ui.LocalDesktopAppearance
import com.pawsmedic.desktop.admin.ui.NavigationLanguageButton
import io.github.jan.supabase.auth.OtpType
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email as SupabaseEmail
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.storage.storage
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import java.util.UUID

@Serializable
private data class SubmittedBusinessApplication(
    @SerialName("id") val id: String
)

@Composable
fun RegisterScreen(
    onNavigateToLogin: () -> Unit
) {
    val english = LocalDesktopAppearance.current.english
    fun tr(spanish: String, englishText: String) = if (english) englishText else spanish
    var clinicName by remember { mutableStateOf("") }
    var rucTaxId by remember { mutableStateOf("") }
    var adminEmail by remember { mutableStateOf("") }
    var contactPhone by remember { mutableStateOf("") }

    var clinicAddress by remember { mutableStateOf("") }
    var vetManagerName by remember { mutableStateOf("") }
    var licenseNumber by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var profilePhoto by remember { mutableStateOf<SelectedPhoto?>(null) }
    var photoLoading by remember { mutableStateOf(false) }

    var acceptTerms by remember { mutableStateOf(false) }
    var legalSection by remember { mutableStateOf<LegalSection?>(null) }

    var phoneWarning by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<AuthMessage?>(null) }
    var successMessage by remember { mutableStateOf<AuthMessage?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var requiresEmailVerification by remember { mutableStateOf(false) }
    var verificationCode by remember { mutableStateOf("") }
    var submittedFolio by remember { mutableStateOf<String?>(null) }
    var accountCreated by remember { mutableStateOf(false) }
    var emailVerified by remember { mutableStateOf(false) }
    var useExistingAccount by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    suspend fun submitApplication() {
        val user = supabase.auth.currentUserOrNull()
            ?: error(tr("Debes verificar el correo e iniciar sesión antes de enviar la solicitud.", "Verify your email and sign in before submitting the application."))
        val userEmail = user.email ?: adminEmail.trim()
        profilePhoto?.let { photo ->
            val bucket = supabase.storage.from("profiles")
            val key = "${user.id}/${UUID.randomUUID()}.${photo.extension}"
            bucket.upload(key, photo.bytes)
            supabase.postgrest["profiles"].update({ set("avatar_url", bucket.publicUrl(key)) }) {
                filter { eq("id", user.id) }
                select()
            }.decodeSingle<kotlinx.serialization.json.JsonObject>()
        }
        val application = try {
            val result = supabase.postgrest.rpc(
                function = "submit_business_application",
                parameters = buildJsonObject {
                    put("p_veterinarian_name", vetManagerName.trim())
                    put("p_business_name", clinicName.trim())
                    put("p_tax_id", rucTaxId.trim())
                    put("p_contact_phone", contactPhone.trim())
                    put("p_address", clinicAddress.trim())
                    put("p_professional_license", licenseNumber.trim())
                    put("p_contact_email", userEmail)
                    put("p_city", "Mexico City")
                }
            )
            kotlinx.serialization.json.Json { ignoreUnknownKeys = true }
                .decodeFromString<SubmittedBusinessApplication>(result.data)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            if (!e.message.orEmpty().contains("Could not find the function", ignoreCase = true) &&
                !e.message.orEmpty().contains("PGRST202", ignoreCase = true)) throw e
            val result = try {
                supabase.postgrest.rpc("submit_business_application", buildJsonObject {
                    put("p_applicant_name", vetManagerName.trim())
                    put("p_business_name", clinicName.trim())
                    put("p_tax_id", rucTaxId.trim())
                    put("p_phone", contactPhone.trim())
                    put("p_address", clinicAddress.trim())
                    put("p_license_number", licenseNumber.trim())
                }).let {
                    kotlinx.serialization.json.Json { ignoreUnknownKeys = true }
                        .decodeFromString<SubmittedBusinessApplication>(it.data)
                }
            } catch (fallback: CancellationException) {
                throw fallback
            } catch (fallback: Exception) {
                if (!fallback.message.orEmpty().contains("Could not find the function", ignoreCase = true) &&
                    !fallback.message.orEmpty().contains("PGRST202", ignoreCase = true)) throw fallback
                supabase.postgrest["business_applications"].insert(
                    buildJsonObject {
                        put("applicant_id", user.id)
                        put("business_name", clinicName.trim())
                        put("address", clinicAddress.trim())
                        put("city", "Mexico City")
                        put("tax_id", rucTaxId.trim())
                        put("contact_phone", contactPhone.trim())
                        put("veterinarian_name", vetManagerName.trim())
                        put("professional_license", licenseNumber.trim())
                        put("contact_email", userEmail)
                        put("status", "PENDING")
                    }
                ) { select() }.decodeSingle<SubmittedBusinessApplication>()
            }
            supabase.postgrest["profiles"].update({ set("role", "VETERINARY_BUSINESS") }) {
                filter { eq("id", user.id) }
                select()
            }.decodeSingle<com.pawsmedic.shared.core.model.UserProfile>()
            result
        }
        submittedFolio = application.id
        successMessage = AuthMessage("¡Solicitud enviada con éxito! Redirigiendo...", "Application submitted successfully! Redirecting...")
        requiresEmailVerification = false
        kotlinx.coroutines.delay(2000)
        supabase.auth.signOut()
        onNavigateToLogin()
    }

    fun validatePassword(pwd: String): AuthMessage? {
        if (pwd.length < 8) {
            return AuthMessage("La contraseña debe tener al menos 8 caracteres.", "The password must contain at least 8 characters.")
        }
        if (!pwd.any { it.isUpperCase() }) {
            return AuthMessage("La contraseña debe incluir al menos una letra mayúscula (A-Z).", "The password must include an uppercase letter (A-Z).")
        }
        if (!pwd.any { it.isLowerCase() }) {
            return AuthMessage("La contraseña debe incluir al menos una letra minúscula (a-z).", "The password must include a lowercase letter (a-z).")
        }
        if (!pwd.any { it.isDigit() }) {
            return AuthMessage("La contraseña debe incluir al menos un número (0-9).", "The password must include a number (0-9).")
        }
        val specialChars = "!@#$%^&*()_+-=[]{}|;:,.<>/?"
        if (!pwd.any { it in specialChars }) {
            return AuthMessage("La contraseña debe incluir al menos un carácter especial (ej. !@#$).", "The password must include a special character (e.g. !@#$).")
        }
        return null
    }

    val passwordError = remember(password) {
        if (password.isNotEmpty()) validatePassword(password) else null
    }

    val clinicNameError = if (clinicName.isNotEmpty()) validateBusinessOrPersonName(clinicName) else null
    val rfcError = if (rucTaxId.isNotEmpty()) validateRfc(rucTaxId) else null
    val addressError = if (clinicAddress.isNotEmpty()) validateClinicAddress(clinicAddress) else null
    val vetNameError = if (vetManagerName.isNotEmpty()) validateBusinessOrPersonName(vetManagerName) else null
    val licenseError = if (licenseNumber.isNotEmpty()) validateProfessionalLicense(licenseNumber) else null

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

                    Column(Modifier.weight(1f)) {
                        Text(
                            text = tr("Registro de Clínica Veterinaria", "Veterinary clinic registration"),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = tr("Solicita el alta de tu establecimiento para comenzar a utilizar PawsMedic", "Apply to register your clinic and start using PawsMedic"),
                            fontSize = 13.sp,
                            color = TextSecondary
                        )
                    }
                    NavigationLanguageButton()
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
                            text = errorMessage?.text(english).orEmpty(),
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
                            text = successMessage?.text(english).orEmpty(),
                            fontSize = 12.sp,
                            color = Color(0xFF166534),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                if (submittedFolio != null) {
                    Text(
                        text = tr("Folio de solicitud: ${submittedFolio}\nEstado: PENDIENTE DE APROBACIÓN", "Application reference: ${submittedFolio}\nStatus: PENDING APPROVAL"),
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
                            isError = clinicNameError != null,
                            supportingText = clinicNameError?.let { { Text(it.text(english)) } },
                            label = { Text(tr("Nombre de la Clínica *", "Clinic name *"), color = TextSecondary) },
                            placeholder = { Text(tr("ej. Veterinaria San Miguel", "e.g. San Miguel Veterinary Clinic"), color = TextMuted) },
                            leadingIcon = { Icon(Icons.Default.Store, contentDescription = null, tint = TextMuted) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ClinicalEmerald, unfocusedBorderColor = BorderLight)
                        )

                        OutlinedTextField(
                            value = rucTaxId,
                            onValueChange = { rucTaxId = it.uppercase() },
                            label = { Text(tr("RFC *", "RFC (tax ID) *"), color = TextSecondary) },
                            placeholder = { Text("ABC123456XY1", color = TextMuted) },
                            leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null, tint = TextMuted) },
                            singleLine = true,
                            isError = rfcError != null,
                            supportingText = rfcError?.let { { Text(it.text(english)) } },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ClinicalEmerald, unfocusedBorderColor = BorderLight)
                        )

                        OutlinedTextField(
                            value = adminEmail,
                            onValueChange = { adminEmail = it },
                            label = { Text(tr("Correo Electrónico Administrador *", "Administrator email *"), color = TextSecondary) },
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
                                label = { Text(tr("Teléfono de Contacto *", "Contact phone *"), color = TextSecondary) },
                                placeholder = { Text("+52 55 1234 5678", color = TextMuted) },
                                leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = TextMuted) },
                                singleLine = true,
                                isError = phoneWarning,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ClinicalEmerald, unfocusedBorderColor = BorderLight)
                            )
                            if (phoneWarning) {
                                Text(tr("⚠️ En este campo solo se permiten números, espacios, guiones y '+'.", "⚠️ Only numbers, spaces, hyphens, and '+' are allowed."), fontSize = 11.sp, color = Color(0xFFDC2626), fontWeight = FontWeight.Bold)
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
                            isError = addressError != null,
                            supportingText = addressError?.let { { Text(it.text(english)) } },
                            label = { Text(tr("Dirección de la Clínica *", "Clinic address *"), color = TextSecondary) },
                            placeholder = { Text(tr("Av. Principal 123, Ciudad", "123 Main Street, City"), color = TextMuted) },
                            leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = TextMuted) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ClinicalEmerald, unfocusedBorderColor = BorderLight)
                        )

                        OutlinedTextField(
                            value = vetManagerName,
                            onValueChange = { vetManagerName = it },
                            isError = vetNameError != null,
                            supportingText = vetNameError?.let { { Text(it.text(english)) } },
                            label = { Text(tr("Nombre del Veterinario Responsable *", "Veterinarian in charge *"), color = TextSecondary) },
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
                            isError = licenseError != null,
                            supportingText = licenseError?.let { { Text(it.text(english)) } },
                            label = { Text(tr("Cédula Profesional *", "Professional license *"), color = TextSecondary) },
                            placeholder = { Text("1234567", color = TextMuted) },
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
                                label = { Text(tr("Contraseña (Seguridad Alta) *", "Password (high security) *"), color = TextSecondary) },
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
                                Text("⚠️ ${passwordError.text(english)}", fontSize = 11.sp, color = Color(0xFFDC2626), fontWeight = FontWeight.Medium)
                            } else if (password.isNotEmpty()) {
                                Text(tr("✓ Contraseña segura con formato correcto.", "✓ Strong password with valid format."), fontSize = 11.sp, color = Color(0xFF166534), fontWeight = FontWeight.Bold)
                            }
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            profilePhoto?.let { photo ->
                                Image(photo.preview, contentDescription = tr("Vista previa de la foto", "Photo preview"),
                                    modifier = Modifier.size(72.dp).clip(CircleShape), contentScale = ContentScale.Crop)
                            }
                            Column {
                                ProfilePhotoPicker(
                                    enabled = !isLoading,
                                    onPhotoSelected = { profilePhoto = it; errorMessage = null },
                                    onError = { errorMessage = photoErrorMessage(it) },
                                    onBusyChange = { photoLoading = it }
                                )
                                Text(tr("JPG o PNG, máximo 5 MB", "JPG or PNG, maximum 5 MB"), color = TextSecondary, fontSize = 12.sp)
                                if (profilePhoto != null) TextButton(onClick = { profilePhoto = null }, enabled = !isLoading) {
                                    Text(tr("Quitar foto", "Remove photo"))
                                }
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
                                tr("¿Aún no tienes cuenta? Regístrate con este correo", "Don't have an account? Register with this email")
                            } else {
                                tr("¿Ya tienes cuenta? Inicia sesión para solicitar aprobación", "Already have an account? Sign in to apply")
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
                    ) {
                        Checkbox(
                            checked = acceptTerms,
                            onCheckedChange = { acceptTerms = it },
                            colors = CheckboxDefaults.colors(checkedColor = ClinicalEmerald)
                        )
                        Column(Modifier.weight(1f)) {
                            Text(
                                text = tr("He leído y acepto:", "I have read and accept:"),
                                fontSize = 12.sp,
                                color = TextPrimary,
                                modifier = Modifier.clickable { acceptTerms = !acceptTerms }
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                TextButton(onClick = { legalSection = LegalSection.TERMS }, contentPadding = PaddingValues(0.dp), modifier = Modifier.height(24.dp)) {
                                    Text(tr("Términos de servicio", "Terms of service"), fontSize = 12.sp, textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline)
                                }
                                Text(tr("  y  ", "  and  "), fontSize = 12.sp, color = TextPrimary)
                                TextButton(onClick = { legalSection = LegalSection.PRIVACY }, contentPadding = PaddingValues(0.dp), modifier = Modifier.height(24.dp)) {
                                    Text(tr("Política de privacidad", "Privacy policy"), fontSize = 12.sp, textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline)
                                }
                            }
                        }
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
                            Text(tr("Cancelar", "Cancel"), fontSize = 14.sp, color = TextPrimary)
                        }

                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    val pwdCheck = validatePassword(password)
                                    if (clinicName.isBlank() || adminEmail.isBlank() || rucTaxId.isBlank() ||
                                        contactPhone.isBlank() || clinicAddress.isBlank() ||
                                        vetManagerName.isBlank() || licenseNumber.isBlank()) {
                                        errorMessage = AuthMessage("Por favor complete todos los campos obligatorios (*).", "Complete all required fields (*).")
                                        return@launch
                                    }

                                    val fieldError = validateBusinessOrPersonName(clinicName)
                                        ?: validateRfc(rucTaxId)
                                        ?: validateClinicAddress(clinicAddress)
                                        ?: validateBusinessOrPersonName(vetManagerName)
                                        ?: validateProfessionalLicense(licenseNumber)
                                    if (fieldError != null) {
                                        errorMessage = fieldError
                                        return@launch
                                    }

                                    if (!Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
                                            .matches(adminEmail.trim())) {
                                        errorMessage = AuthMessage("El correo electrónico no tiene un formato válido.", "Enter a valid email address.")
                                        return@launch
                                    }
                                    if (!useExistingAccount && pwdCheck != null) {
                                        errorMessage = pwdCheck
                                        return@launch
                                    }
                                    if (!acceptTerms) {
                                        errorMessage = AuthMessage("Debe aceptar los Términos de Servicio para continuar.", "Accept the Terms of Service to continue.")
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
                                                    successMessage = AuthMessage("Te reenviamos un código a ${adminEmail.trim()}.", "A code was resent to ${adminEmail.trim()}.")
                                                } catch (e: CancellationException) {
                                                    throw e
                                                } catch (e: Exception) {
                                                    val rawMsg = e.message.orEmpty()
                                                    if (rawMsg.contains("missing email or phone", ignoreCase = true) ||
                                                        rawMsg.contains("confirmed", ignoreCase = true) ||
                                                        rawMsg.contains("already exists", ignoreCase = true)) {
                                                        useExistingAccount = true
                                                        errorMessage = AuthMessage("Este correo ya está verificado en el sistema. Inicia sesión para continuar.", "This email is already verified. Sign in to continue.")
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
                                                            put("role", "VETERINARY_BUSINESS")
                                                        }
                                                    }
                                                } catch (e: CancellationException) {
                                                    throw e
                                                } catch (e: Exception) {
                                                    val rawMsg = e.message.orEmpty()
                                                    if (rawMsg.contains("already registered", ignoreCase = true) ||
                                                        rawMsg.contains("already exists", ignoreCase = true) ||
                                                        rawMsg.contains("missing email or phone", ignoreCase = true)) {
                                                        useExistingAccount = true
                                                        errorMessage = AuthMessage("Este correo ya está registrado en el sistema. Inicia sesión con tus credenciales para enviar la solicitud.", "This email is already registered. Sign in to submit the application.")
                                                        return@launch
                                                    } else if (rawMsg.contains("rate limit", ignoreCase = true) || rawMsg.contains("429", ignoreCase = true)) {
                                                        errorMessage = AuthMessage("Supabase ha alcanzado el límite de envío de correos por hora. Por favor espera unos minutos o usa la opción 'Ya tengo cuenta'.", "The email sending limit has been reached. Wait a few minutes or use the existing account option.")
                                                        return@launch
                                                    } else {
                                                        throw e
                                                    }
                                                }

                                                // Si Supabase no exige confirmación de correo (o auto-confirma),
                                                // el usuario ya tiene sesión activa y se envía la solicitud de inmediato.
                                                if (supabase.auth.currentSessionOrNull() != null) {
                                                    submitApplication()
                                                } else {
                                                    accountCreated = true
                                                    requiresEmailVerification = true
                                                    verificationCode = ""
                                                    successMessage = AuthMessage(
                                                        "Si este correo es nuevo, recibirás un código en ${adminEmail.trim()}. Si ya tienes una cuenta de PawMedic, inicia sesión con esa cuenta para enviar la solicitud.",
                                                        "If this email is new, you will receive a code at ${adminEmail.trim()}. If you already have a PawMedic account, sign in with it to submit the application."
                                                    )
                                                }
                                            }
                                        }
                                    } catch (e: CancellationException) {
                                        throw e
                                    } catch (e: Exception) {
                                        System.err.println("Error al procesar la solicitud: ${e.javaClass.simpleName}")
                                        errorMessage = AuthMessage("No se pudo procesar la solicitud. Comprueba los datos y la conexión.", "Could not process the application. Check your information and connection.")
                                    } finally {
                                        isLoading = false
                                    }
                                }
                            },
                            enabled = !isLoading && !photoLoading && submittedFolio == null,
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
                                        useExistingAccount -> tr("Iniciar sesión y enviar solicitud", "Sign in and submit application")
                                        emailVerified -> tr("Reintentar envío de solicitud", "Retry application")
                                        accountCreated -> tr("Reenviar código de verificación", "Resend verification code")
                                        else -> tr("Crear cuenta y verificar correo", "Create account and verify email")
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
                                        text = tr("Verifica tu correo", "Verify your email"),
                                        fontSize = 22.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = tr("Ingresa el código de 6 dígitos que enviamos a ${adminEmail.trim()}.", "Enter the 6-digit code sent to ${adminEmail.trim()}."),
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
                                        Text(tr("¿Ya registraste este correo en la app móvil? Usa esa cuenta.", "Already registered this email in the mobile app? Use that account."))
                                    }
                                    OutlinedTextField(
                                        value = verificationCode,
                                        onValueChange = { value ->
                                            if (value.length <= 6 && value.all(Char::isDigit)) {
                                                verificationCode = value
                                            }
                                        },
                                        label = { Text(tr("Código de verificación", "Verification code")) },
                                        keyboardOptions = KeyboardOptions(
                                            keyboardType = KeyboardType.Number,
                                            imeAction = ImeAction.Done
                                        ),
                                        singleLine = true,
                                        enabled = !isLoading && !emailVerified,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    Text(
                                        text = tr("Verificar el correo solo permite enviar la solicitud; la clínica seguirá pendiente hasta que Superadmin la apruebe.", "Email verification only allows submission; the clinic remains pending until approved by a superadmin."),
                                        fontSize = 12.sp,
                                        color = TextSecondary
                                    )
                                    if (errorMessage != null) {
                                        Text(errorMessage?.text(english).orEmpty(), color = Color(0xFFB91C1C), fontSize = 13.sp)
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
                                                        successMessage = AuthMessage("Te reenviamos un nuevo código.", "A new code was sent.")
                                                    } catch (e: CancellationException) {
                                                        throw e
                                                    } catch (e: Exception) {
                                                        val rawMsg = e.message.orEmpty()
                                                        errorMessage = when {
                                                            rawMsg.contains("missing email or phone", ignoreCase = true) ||
                                                            rawMsg.contains("confirmed", ignoreCase = true) ->
                                                                AuthMessage("Este correo ya fue verificado o ya está registrado. Selecciona la opción 'Usa esa cuenta'.", "This email is already verified or registered. Select 'Use that account'.")
                                                            rawMsg.contains("rate limit", ignoreCase = true) ||
                                                            rawMsg.contains("429", ignoreCase = true) ->
                                                                AuthMessage("Por seguridad, debes esperar 60 segundos antes de reenviar otro código.", "For security, wait 60 seconds before requesting another code.")
                                                            else -> AuthMessage("No se pudo reenviar el código.", "Could not resend the code.")
                                                        }
                                                    } finally {
                                                        isLoading = false
                                                    }
                                                }
                                            }
                                        ) {
                                            Text(tr("Reenviar código", "Resend code"))
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
                                                        System.err.println("No se pudo verificar el código: ${e.javaClass.simpleName}")
                                                        errorMessage = AuthMessage("No se pudo verificar el código o enviar la solicitud. Comprueba los datos y la conexión.", "Could not verify the code or submit the application. Check your information and connection.")
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
                                                Text(if (emailVerified) tr("Reintentar", "Retry") else tr("Verificar y enviar", "Verify and submit"))
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
    legalSection?.let { section ->
        LegalDocumentDialog(initialSection = section, onDismiss = { legalSection = null })
    }
}

@Preview
@Composable
fun RegisterScreenPreview() {
    RegisterScreen(
        onNavigateToLogin = {}
    )
}

fun main() = singleWindowApplication(title = "Preview - Register Screen") {
    RegisterScreen(
        onNavigateToLogin = {}
    )
}

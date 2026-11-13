package com.pawsmedic.desktop.veterinary.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.pawsmedic.desktop.veterinary.supabase
import com.pawsmedic.desktop.veterinary.ui.theme.*
import com.pawsmedic.desktop.veterinary.verifyAccountPassword
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
import java.util.UUID

private fun JsonObject.value(key: String): String =
    this[key]?.jsonPrimitive?.contentOrNull.orEmpty()

@Composable
internal fun AccountSidebar(
    avatar: ImageBitmap?,
    onClose: () -> Unit,
    onLogout: () -> Unit,
    onProfileLoaded: (String) -> Unit,
    onProfileSaved: (String) -> Unit
) {
    val scope = rememberCoroutineScope()
    val user = supabase.auth.currentUserOrNull()
    var profile by remember { mutableStateOf<JsonObject?>(null) }
    var clinic by remember { mutableStateOf<JsonObject?>(null) }
    var page by remember { mutableStateOf("menu") }
    var error by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(true) }
    var busy by remember { mutableStateOf(false) }
    var revealed by remember { mutableStateOf(false) }
    var passwordPrompt by remember { mutableStateOf(false) }
    var password by remember { mutableStateOf("") }
    var photo by remember { mutableStateOf<SelectedPhoto?>(null) }
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var reload by remember { mutableIntStateOf(0) }

    LaunchedEffect(user?.id, reload) {
        loading = true
        error = null
        try {
            val id = user?.id ?: error("No hay una sesión activa.")
            val loadedProfile = supabase.postgrest["profiles"].select { filter { eq("id", id) } }
                .decodeSingle<JsonObject>()
            val applications = supabase.postgrest["business_applications"].select {
                filter { eq("applicant_id", id); eq("status", "APPROVED") }
            }.decodeList<JsonObject>()
            profile = loadedProfile
            clinic = applications.firstOrNull()
            name = loadedProfile.value("display_name")
            phone = loadedProfile.value("phone")
            onProfileLoaded(name)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            error = "No se pudieron cargar los datos de la cuenta. Inténtalo de nuevo."
        } finally {
            loading = false
        }
    }

    LaunchedEffect(revealed) {
        if (revealed) {
            delay(60_000)
            revealed = false
        }
    }

    Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.4f))) {
        Box(Modifier.fillMaxSize().clickable(enabled = !busy) { onClose() })
        Column(
            Modifier.align(Alignment.CenterEnd).fillMaxHeight().widthIn(max = 440.dp)
                .fillMaxWidth().background(CardWhite).padding(24.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(if (page == "menu") "Mi perfil" else if (page == "info") "Información de la cuenta" else "Editar perfil",
                    style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
                IconButton(onClick = onClose, enabled = !busy) {
                    Icon(Icons.Default.Close, contentDescription = "Cerrar panel")
                }
            }
            if (page != "menu") {
                TextButton(onClick = {
                    page = "menu"; revealed = false; photo = null; error = null
                }, enabled = !busy) { Text("Volver") }
            }
            if (loading) {
                CircularProgressIndicator()
            } else if (profile == null) {
                Text(error ?: "No se pudieron cargar los datos.", color = MaterialTheme.colorScheme.error)
                OutlinedButton(onClick = { reload++ }) { Text("Reintentar") }
            } else {
                error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                when (page) {
                    "menu" -> {
                        Box(
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                                .size(140.dp).clip(CircleShape).background(EmeraldLightBg),
                            contentAlignment = Alignment.Center
                        ) {
                            if (avatar != null) {
                                Image(avatar, "Foto de perfil", Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                            } else {
                                Icon(Icons.Default.Person, "Sin foto de perfil", modifier = Modifier.size(72.dp), tint = ClinicalEmerald)
                            }
                        }
                        Text(profile?.value("display_name").orEmpty().ifBlank { "Cuenta veterinaria" },
                            modifier = Modifier.align(Alignment.CenterHorizontally),
                            style = MaterialTheme.typography.titleMedium)
                        OutlinedButton(onClick = { error = null; page = "info" }, modifier = Modifier.fillMaxWidth()) {
                            Text("Información de la cuenta")
                        }
                        OutlinedButton(onClick = {
                            name = profile?.value("display_name").orEmpty()
                            phone = profile?.value("phone").orEmpty()
                            error = null
                            page = "edit"
                        }, modifier = Modifier.fillMaxWidth()) { Text("Editar perfil") }
                        TextButton(onClick = onLogout, modifier = Modifier.fillMaxWidth()) {
                            Text("Cerrar sesión", color = MaterialTheme.colorScheme.error)
                        }
                    }
                    "info" -> {
                        AccountField("Nombre", profile?.value("display_name").orEmpty())
                        AccountField("Teléfono", profile?.value("phone").orEmpty().ifBlank { clinic?.value("contact_phone").orEmpty() })
                        AccountField("Clínica", clinic?.value("business_name").orEmpty())
                        AccountField("Ubicación", listOfNotNull(
                            clinic?.value("address")?.takeIf { it.isNotBlank() },
                            clinic?.value("city")?.takeIf { it.isNotBlank() }
                        ).joinToString(", "))
                        AccountField("Veterinario responsable", clinic?.value("veterinarian_name").orEmpty())
                        if (clinic == null) Text("No se encontró una solicitud de clínica aprobada.")
                        HorizontalDivider()
                        SensitiveField("Correo", user?.email.orEmpty(), revealed) {
                            if (revealed) revealed = false else passwordPrompt = true
                        }
                        SensitiveField("RFC", clinic?.value("tax_id").orEmpty(), revealed) {
                            if (revealed) revealed = false else passwordPrompt = true
                        }
                        SensitiveField("Cédula profesional", clinic?.value("professional_license").orEmpty(), revealed) {
                            if (revealed) revealed = false else passwordPrompt = true
                        }
                    }
                    "edit" -> {
                        OutlinedTextField(name, { name = it }, label = { Text("Nombre") },
                            singleLine = true, modifier = Modifier.fillMaxWidth(), enabled = !busy)
                        OutlinedTextField(phone, { phone = it }, label = { Text("Teléfono") },
                            singleLine = true, modifier = Modifier.fillMaxWidth(), enabled = !busy)
                        photo?.let {
                            Image(it.preview, "Vista previa de la foto", Modifier.size(90.dp).clip(CircleShape), contentScale = ContentScale.Crop)
                        }
                        ProfilePhotoPicker(
                            enabled = !busy,
                            onPhotoSelected = { photo = it; error = null },
                            onError = { error = it },
                            onBusyChange = { busy = it }
                        )
                        if (photo != null) TextButton(onClick = { photo = null }, enabled = !busy) {
                            Text("Descartar foto seleccionada")
                        }
                        Button(onClick = {
                            if (name.isBlank()) {
                                error = "Escribe un nombre."
                            } else {
                                busy = true
                                scope.launch {
                                    try {
                                        val id = user?.id ?: error("No hay una sesión activa.")
                                        val selected = photo
                                        val avatarUrl = if (selected != null) {
                                            val bucket = supabase.storage.from("profiles")
                                            val key = "$id/${UUID.randomUUID()}.${selected.extension}"
                                            bucket.upload(key, selected.bytes)
                                            bucket.publicUrl(key)
                                        } else null
                                        val updated = supabase.postgrest["profiles"].update({
                                            set("display_name", name.trim())
                                            set("phone", phone.trim())
                                            if (avatarUrl != null) set("avatar_url", avatarUrl)
                                        }) {
                                            filter { eq("id", id) }
                                            select()
                                        }.decodeSingle<JsonObject>()
                                        profile = updated
                                        photo = null
                                        error = null
                                        onProfileSaved(updated.value("display_name"))
                                        page = "menu"
                                    } catch (e: CancellationException) {
                                        throw e
                                    } catch (e: Exception) {
                                        error = "No se pudieron guardar los cambios. Revisa tu conexión y los permisos del perfil."
                                    } finally { busy = false }
                                }
                            }
                        }, enabled = !busy, modifier = Modifier.fillMaxWidth()) { Text("Guardar cambios") }
                    }
                }
            }
        }
    }
    if (passwordPrompt) {
        AlertDialog(
            onDismissRequest = {
                if (!busy) { passwordPrompt = false; password = "" }
            },
            title = { Text("Confirma tu contraseña") },
            text = {
                Column {
                    Text("Para mostrar los datos sensibles, confirma tu contraseña.")
                    OutlinedTextField(password, { password = it }, label = { Text("Contraseña") },
                        singleLine = true, visualTransformation = PasswordVisualTransformation(), enabled = !busy)
                    error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                }
            },
            confirmButton = {
                TextButton(enabled = !busy && password.isNotBlank(), onClick = {
                    busy = true
                    scope.launch {
                        try {
                            val id = user?.id ?: error("No hay una sesión activa.")
                            val email = user.email ?: error("La cuenta no tiene correo.")
                            check(verifyAccountPassword(email, password, id))
                            password = ""
                            error = null
                            passwordPrompt = false
                            revealed = true
                        } catch (e: CancellationException) {
                            throw e
                        } catch (e: Exception) {
                            password = ""
                            error = "No se pudo confirmar la contraseña. Verifica los datos y tu conexión."
                        } finally { busy = false }
                    }
                }) { Text("Confirmar") }
            },
            dismissButton = {
                TextButton(enabled = !busy, onClick = {
                    password = ""; error = null; passwordPrompt = false
                }) { Text("Cancelar") }
            }
        )
    }
}

@Composable
private fun AccountField(label: String, value: String) {
    Column {
        Text(label, style = MaterialTheme.typography.labelMedium, color = TextSecondary)
        Text(value.ifBlank { "No disponible" }, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun SensitiveField(label: String, value: String, revealed: Boolean, onToggle: () -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.weight(1f)) { AccountField(label, if (revealed) value else "****") }
        IconButton(onClick = onToggle) {
            Icon(if (revealed) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                contentDescription = if (revealed) "Ocultar $label" else "Mostrar $label")
        }
    }
}

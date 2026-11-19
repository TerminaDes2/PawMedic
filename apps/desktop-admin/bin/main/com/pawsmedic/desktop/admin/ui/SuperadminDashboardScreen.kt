package com.pawsmedic.desktop.admin.ui

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.singleWindowApplication
import com.pawsmedic.desktop.admin.model.BusinessApplicationDto
import com.pawsmedic.desktop.admin.translation.translateBatchLocally
import com.pawsmedic.desktop.admin.supabase
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.coroutines.launch
import kotlinx.coroutines.CancellationException

val AdminOffWhiteBg: Color @Composable @ReadOnlyComposable get() = if (LocalDesktopAppearance.current.dark) Color(0xFF0F172A) else Color(0xFFF8FAFC)
val AdminDarkSlate = Color(0xFF0F172A)
val AdminBorderLight: Color @Composable @ReadOnlyComposable get() = if (LocalDesktopAppearance.current.dark) Color(0xFF475569) else Color(0xFFE2E8F0)
val AdminSelectedBg: Color @Composable @ReadOnlyComposable get() = if (LocalDesktopAppearance.current.dark) Color(0xFF134E4A) else Color(0xFFCCFBF1)
val AdminSelectedText: Color @Composable @ReadOnlyComposable get() = if (LocalDesktopAppearance.current.dark) Color(0xFF5EEAD4) else Color(0xFF0D9488)
val AdminEmeraldGreen = Color(0xFF0D9488)
val AdminTextPrimary: Color @Composable @ReadOnlyComposable get() = if (LocalDesktopAppearance.current.dark) Color(0xFFF1F5F9) else Color(0xFF1E293B)
val AdminTextSecondary: Color @Composable @ReadOnlyComposable get() = if (LocalDesktopAppearance.current.dark) Color(0xFFCBD5E1) else Color(0xFF64748B)
val AdminTextMuted: Color @Composable @ReadOnlyComposable get() = if (LocalDesktopAppearance.current.dark) Color(0xFFCBD5E1) else Color(0xFF94A3B8)

data class RegistrationRequest(
    val id: String,
    val name: String,
    val rep: String,
    val rfc: String,
    val date: String,
    var status: String,
    val email: String = "",
    val phone: String = "",
    val address: String = "",
    val licenseNumber: String = "",
    val decisionReason: String = ""
)

fun adminErrorMessage(error: Throwable, fallback: String): String {
    System.err.println("Operación de administración no completada: ${error.javaClass.simpleName}")
    return fallback
}

@Composable
fun SuperadminDashboardScreen(
    userEmail: String = "",
    onLogout: () -> Unit = {},
    supabaseClient: SupabaseClient = supabase
) {
    val english = LocalDesktopAppearance.current.english
    var selectedModule by remember { mutableStateOf(0) }

    val requests = remember { mutableStateListOf<RegistrationRequest>() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AdminOffWhiteBg)
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            // SIDEBAR IZQUIERDO (260.dp)
            Column(
                modifier = Modifier
                    .width(260.dp)
                    .fillMaxHeight()
                    .background(MaterialTheme.colorScheme.surface)
                    .border(1.dp, AdminBorderLight)
                    .padding(vertical = 20.dp, horizontal = 16.dp)
            ) {
                // Header Marca Superadmin (Paleta Esmeralda Clínico)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(AdminEmeraldGreen),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AdminPanelSettings,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Paws",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = AdminTextPrimary
                        )
                        Text(
                            text = "Admin",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = AdminEmeraldGreen
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // LOS 3 MÓDULOS DE ADMINISTRACIÓN
                val modules = listOf(
                    Triple(0, if (english) "Monitoring" else "Monitoreo", Icons.Default.Monitor),
                    Triple(1, if (english) "Applications" else "Solicitudes", Icons.Default.Assignment),
                    Triple(2, if (english) "Audit" else "Auditoría", Icons.Default.Security)
                )

                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    modules.forEach { (index, label, icon) ->
                        val isSelected = selectedModule == index

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) AdminSelectedBg else Color.Transparent)
                                .clickable { selectedModule = index }
                                .padding(horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = if (isSelected) AdminSelectedText else AdminTextSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = label,
                                fontSize = 14.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) AdminSelectedText else AdminTextSecondary
                            )
                        }
                    }
                }
                SupportFooter()
            }

            // CONTENIDO DERECHO
            Column(modifier = Modifier.fillMaxSize()) {
                // TOP BAR
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .background(MaterialTheme.colorScheme.surface)
                        .border(1.dp, AdminBorderLight)
                        .padding(horizontal = 24.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = when (selectedModule) {
                            0 -> if (english) "Veterinary monitoring" else "Supervisión de veterinarias"
                            1 -> if (english) "Veterinary applications" else "Solicitudes de incorporación veterinaria"
                            else -> if (english) "Administrative audit" else "Auditoría administrativa"
                        },
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = AdminTextPrimary
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        AppearanceButton()
                        NavigationLanguageButton()
                        // Profile Root & Cerrar Sesión
                        val displayName = if (userEmail.isNotBlank()) userEmail.substringBefore("@") else "admin"
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(AdminSelectedBg),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AdminPanelSettings,
                                        contentDescription = null,
                                        tint = AdminSelectedText,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                Column {
                                    Text(
                                        text = displayName,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = AdminTextPrimary
                                    )
                                    Text(
                                        text = userEmail.ifBlank { if (english) "Central control" else "Control Central" },
                                        fontSize = 11.sp,
                                        color = AdminTextMuted
                                    )
                                }
                            }

                            // Botón Cerrar Sesión
                            Box(
                                modifier = Modifier
                                    .height(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(AdminOffWhiteBg)
                                    .border(1.dp, AdminBorderLight, RoundedCornerShape(8.dp))
                                    .clickable { onLogout() }
                                    .padding(horizontal = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                                        contentDescription = null,
                                        tint = Color(0xFFDC2626),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = if (english) "Sign out" else "Cerrar Sesión",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFDC2626)
                                    )
                                }
                            }
                        }
                    }
                }

                // ÁREA DE CONTENIDO SEGÚN MÓDULO SELECCIONADO
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp)
                ) {
                    when (selectedModule) {
                        0 -> MonitoreoModule(supabaseClient)
                        1 -> SolicitudesModule(requests, supabaseClient)
                        2 -> AuditoriaModule(supabaseClient, userEmail)
                    }
                }
            }
        }
    }
}

private fun BusinessApplicationDto.toRegistrationRequest() = RegistrationRequest(
    id = id,
    name = publicName,
    rep = applicantName,
    rfc = taxId,
    date = createdAt?.substringBefore("T") ?: "—",
    status = when (status) {
        "APPROVED" -> "Aprobado"
        "REJECTED" -> "Rechazado"
        "SUSPENDED" -> "Suspendido"
        else -> "Pendiente"
    },
    email = email,
    phone = phone,
    address = address,
    licenseNumber = licenseNumber,
    decisionReason = decisionReason.orEmpty()
)

private val jsonDecoder = kotlinx.serialization.json.Json { ignoreUnknownKeys = true }

private fun Exception.isMissingDatabaseObject(): Boolean {
    val message = message.orEmpty()
    return message.contains("PGRST202", ignoreCase = true) ||
        message.contains("PGRST205", ignoreCase = true) ||
        message.contains("42P01", ignoreCase = true) ||
        message.contains("Could not find the function", ignoreCase = true) ||
        message.contains("Could not find the table", ignoreCase = true)
}

@Serializable
private data class BusinessStatusRow(val id: String, val status: String)

// El estado real vive en `businesses`; las solicitudes pueden conservar el estado de aprobación original.
internal suspend fun fetchApplicationsFromDatabase(supabaseClient: SupabaseClient): List<BusinessApplicationDto> {
    val applications = fetchApplicationsRaw(supabaseClient)
    val statuses = try {
        supabaseClient.postgrest["businesses"].select().decodeList<BusinessStatusRow>().associate { it.id to it.status }
    } catch (e: CancellationException) {
        throw e
    } catch (_: Exception) {
        emptyMap()
    }
    return applications.map { app ->
        val current = statuses[app.id]
        if (current != null && app.status != "PENDING" && app.status != current) app.copy(status = current) else app
    }
}

private suspend fun fetchApplicationsRaw(supabaseClient: SupabaseClient): List<BusinessApplicationDto> {
    val result = mutableListOf<BusinessApplicationDto>()
    var succeeded = false
    var failure: Exception? = null
    try {
        val rpcResult = supabaseClient.postgrest.rpc("get_all_business_applications")
        return jsonDecoder.decodeFromString<List<BusinessApplicationDto>>(rpcResult.data)
    } catch (e: kotlinx.coroutines.CancellationException) { throw e
    } catch (e: Exception) {
        if (!e.isMissingDatabaseObject()) throw e
        failure = e
    }

    try {
        val list1 = supabaseClient.postgrest["business_applications"].select().decodeList<BusinessApplicationDto>()
        result.addAll(list1.filter { item -> result.none { it.id == item.id } })
        succeeded = true
    } catch (e: kotlinx.coroutines.CancellationException) { throw e
    } catch (e: Exception) {
        if (!e.isMissingDatabaseObject()) throw e
        failure = e
    }
    try {
        val list2 = supabaseClient.postgrest["businesses"].select().decodeList<BusinessApplicationDto>()
        for (item in list2) {
            if (result.none { it.id == item.id }) {
                result.add(item)
            }
        }
        succeeded = true
    } catch (e: kotlinx.coroutines.CancellationException) { throw e
    } catch (e: Exception) {
        if (!e.isMissingDatabaseObject()) throw e
        failure = e
    }
    if (!succeeded) throw failure ?: IllegalStateException("No se pudieron consultar las solicitudes.")
    return result
}

// MÓDULO 1: MONITOREO
@Composable
fun MonitoreoModule(supabaseClient: SupabaseClient) {
    val english = LocalDesktopAppearance.current.english
    fun tr(spanish: String, translated: String) = if (english) translated else spanish
    val businesses = remember { mutableStateListOf<BusinessApplicationDto>() }
    var isLoading by remember { mutableStateOf(true) }
    var busyBusinessId by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(supabaseClient) {
        try {
            businesses.clear()
            val list = fetchApplicationsFromDatabase(supabaseClient)
            businesses.addAll(
                list.filter { it.status == "APPROVED" || it.status == "SUSPENDED" }
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            errorMessage = adminErrorMessage(e, "No se pudieron cargar las veterinarias.")
        } finally {
            isLoading = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, AdminBorderLight, RoundedCornerShape(12.dp))
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        val activeCount = businesses.count { it.status == "APPROVED" }
        val suspendedCount = businesses.count { it.status == "SUSPENDED" }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            listOf(
                tr("Veterinarias activas", "Active clinics") to activeCount,
                tr("Veterinarias suspendidas", "Suspended clinics") to suspendedCount
            ).forEach { (label, count) ->
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(AdminOffWhiteBg)
                        .border(1.dp, AdminBorderLight, RoundedCornerShape(12.dp))
                        .padding(16.dp)
                ) {
                    Text(label, fontSize = 13.sp, color = AdminTextSecondary)
                    Text(count.toString(), fontSize = 26.sp, fontWeight = FontWeight.Bold, color = AdminTextPrimary)
                }
            }
        }

        Text(tr("Supervisión de veterinarias", "Clinic monitoring"), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = AdminTextPrimary)
        errorMessage?.let { Text(it, color = Color(0xFFB91C1C), fontSize = 13.sp) }
        if (isLoading) CircularProgressIndicator(color = AdminEmeraldGreen)
        if (!isLoading && businesses.isEmpty() && errorMessage == null) {
            Text(tr("No hay veterinarias aprobadas para mostrar.", "There are no approved clinics to display."), color = AdminTextSecondary)
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(businesses) { business ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(AdminOffWhiteBg)
                        .border(1.dp, AdminBorderLight, RoundedCornerShape(8.dp))
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(business.publicName, fontWeight = FontWeight.Bold, color = AdminTextPrimary)
                        Text("${tr("Estado", "Status")}: ${business.status}", fontSize = 12.sp, color = AdminTextSecondary)
                    }
                    val nextStatus = if (business.status == "APPROVED") "SUSPENDED" else "APPROVED"
                    Button(
                        enabled = busyBusinessId == null,
                        onClick = {
                            coroutineScope.launch {
                                busyBusinessId = business.id
                                errorMessage = null
                                try {
                                    supabaseClient.postgrest.rpc(
                                        "set_business_status",
                                        buildJsonObject {
                                            put("p_business_id", business.id)
                                            put("p_status", nextStatus)
                                        }
                                    )
                                    val index = businesses.indexOf(business)
                                    if (index >= 0) businesses[index] = business.copy(status = nextStatus)
                                } catch (e: CancellationException) {
                                    throw e
                                } catch (e: Exception) {
                                    errorMessage = adminErrorMessage(e, tr("No se pudo cambiar el estado. Inténtalo de nuevo.", "Could not change the status. Please try again."))
                                } finally {
                                    busyBusinessId = null
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AdminEmeraldGreen)
                    ) {
                        Text(if (nextStatus == "SUSPENDED") tr("Suspender", "Suspend") else tr("Reactivar", "Reactivate"))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SolicitudesModule(
    requests: MutableList<RegistrationRequest>,
    supabaseClient: SupabaseClient
) {
    val english = LocalDesktopAppearance.current.english
    fun tr(spanish: String, translated: String) = if (english) translated else spanish
    fun statusText(status: String) = if (!english) status else when (status) {
        "Aprobado" -> "Approved"
        "Rechazado" -> "Rejected"
        "Suspendido" -> "Suspended"
        "Pendiente" -> "Pending"
        else -> status
    }
    var selectedRequestForDetail by remember { mutableStateOf<RegistrationRequest?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var isActionLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val translations = remember(supabaseClient) { mutableStateMapOf<String, String>() }
    var showOriginal by remember { mutableStateOf(false) }
    var translating by remember { mutableStateOf(false) }
    var translationError by remember { mutableStateOf(false) }
    var translationAttempt by remember { mutableIntStateOf(0) }
    val coroutineScope = rememberCoroutineScope()
    val visibleRequests = requests
    val applicationTexts = (requests.map { it.name } + listOfNotNull(selectedRequestForDetail?.address))
        .filter { it.isNotBlank() }.distinct()

    LaunchedEffect(english, applicationTexts, translationAttempt) {
        if (!english) {
            translating = false
            translationError = false
            return@LaunchedEffect
        }
        val missing = applicationTexts.filterNot { translations.containsKey(it) }
        val pending = missing.filter { it.length <= 4000 }
        translationError = missing.size != pending.size
        if (pending.isNotEmpty()) {
            translating = true
            try {
                for (batch in pending.chunked(50)) {
                    batch.zip(translateBatchLocally(batch)).forEach { (original, translated) ->
                        translations[original] = translated
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                translationError = true
            } finally {
                translating = false
            }
        }
    }
    fun displayText(value: String): String =
        if (english && !showOriginal) translations[value] ?: value else value
    fun displayError(message: String): String = if (!english) message else when (message) {
        "No se pudieron cargar las solicitudes." -> "Could not load applications."
        "No se pudo guardar la decisión." -> "Could not save the decision."
        "No se pudo aprobar: el servicio inicial necesita una categoría. Revisa la configuración de servicios en Supabase." ->
            "Could not approve: the initial service requires a category. Check the Supabase service configuration."
        else -> message
    }

    LaunchedEffect(supabaseClient) {
        try {
            val fetchedApps = fetchApplicationsFromDatabase(supabaseClient)
            requests.clear()
            requests.addAll(fetchedApps.map { it.toRegistrationRequest() })
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            errorMessage = adminErrorMessage(e, "No se pudieron cargar las solicitudes.")
        } finally {
            isLoading = false
        }
    }

    fun updateStatus(req: RegistrationRequest, newStatus: String) {
        coroutineScope.launch {
            isActionLoading = true
            errorMessage = null
            try {
                val parameters = buildJsonObject {
                    put("p_business_id", req.id)
                    put("p_application_id", req.id)
                    put("p_id", req.id)
                    if (newStatus == "Rechazado") put("p_reason", "")
                }
                val function = if (newStatus == "Aprobado") {
                    "approve_business_application"
                } else {
                    "reject_business_application"
                }
                supabaseClient.postgrest.rpc(function, parameters)

                val idx = requests.indexOf(req)
                if (idx >= 0) requests[idx] = req.copy(status = newStatus)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                errorMessage = adminErrorMessage(e, "No se pudo guardar la decisión.")
            } finally {
                isActionLoading = false
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, AdminBorderLight, RoundedCornerShape(12.dp))
            .padding(20.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(tr("Solicitudes de Registro Pendientes", "Pending registration applications"), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = AdminTextPrimary)
                    Text(tr("Revisa los datos y decide la incorporación de cada clínica.", "Review each clinic's information and decide whether to approve it."), fontSize = 12.sp, color = AdminTextSecondary)
                }

                Button(
                    onClick = {
                        coroutineScope.launch {
                            isLoading = true
                            errorMessage = null
                            try {
                                val fetchedApps = fetchApplicationsFromDatabase(supabaseClient)
                                requests.clear()
                                requests.addAll(fetchedApps.map { it.toRegistrationRequest() })
                            } catch (e: CancellationException) {
                                throw e
                            } catch (e: Exception) {
                                errorMessage = adminErrorMessage(e, "No se pudieron cargar las solicitudes.")
                            } finally {
                                isLoading = false
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AdminEmeraldGreen),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(36.dp)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(tr("Refrescar", "Refresh"), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            if (errorMessage != null) {
                Text(
                    text = displayError(errorMessage.orEmpty()),
                    color = Color(0xFFB91C1C),
                    fontSize = 13.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            if (isLoading) {
                CircularProgressIndicator(color = AdminEmeraldGreen)
            }

            if (english && applicationTexts.isNotEmpty()) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Machine-translated details may be inaccurate. Verify the original before deciding.", modifier = Modifier.weight(1f), fontSize = 12.sp, color = AdminTextSecondary)
                    TextButton(onClick = { showOriginal = !showOriginal }) { Text(if (showOriginal) "Show translation" else "Show original") }
                }
                Box(Modifier.fillMaxWidth().height(40.dp), contentAlignment = Alignment.CenterStart) {
                    if (translationError) Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Some details could not be translated; the original is shown.", modifier = Modifier.weight(1f), color = Color(0xFFB91C1C), fontSize = 12.sp)
                        TextButton(onClick = { translationAttempt++ }, enabled = !translating) { Text("Retry") }
                    } else if (translating) Text("Translating…", color = AdminTextSecondary, fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Encabezados Tabla
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(AdminOffWhiteBg)
                    .border(1.dp, AdminBorderLight, RoundedCornerShape(8.dp))
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(tr("FOLIO", "REFERENCE"), fontWeight = FontWeight.Bold, fontSize = 12.sp, color = AdminTextMuted, modifier = Modifier.weight(0.10f))
                Text(tr("NOMBRE VETERINARIA", "CLINIC NAME"), fontWeight = FontWeight.Bold, fontSize = 12.sp, color = AdminTextMuted, modifier = Modifier.weight(0.22f))
                Text(tr("SOLICITANTE / MVZ", "APPLICANT / VET"), fontWeight = FontWeight.Bold, fontSize = 12.sp, color = AdminTextMuted, modifier = Modifier.weight(0.18f))
                Text(tr("RFC / CÉDULA", "TAX / LICENSE ID"), fontWeight = FontWeight.Bold, fontSize = 12.sp, color = AdminTextMuted, modifier = Modifier.weight(0.13f))
                Text(tr("FECHA", "DATE"), fontWeight = FontWeight.Bold, fontSize = 12.sp, color = AdminTextMuted, modifier = Modifier.weight(0.10f))
                Text(tr("ACCIONES", "ACTIONS"), fontWeight = FontWeight.Bold, fontSize = 12.sp, color = AdminTextMuted, modifier = Modifier.weight(0.27f))
            }

            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                if (!isLoading && visibleRequests.isEmpty() && errorMessage == null) {
                    item {
                        Text(
                            tr("No hay solicitudes de veterinarias para mostrar.", "There are no clinic applications to display."),
                            color = AdminTextSecondary,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
                items(visibleRequests) { req ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .border(1.dp, AdminBorderLight, RoundedCornerShape(6.dp))
                            .combinedClickable(
                                onClick = {},
                                onDoubleClick = { selectedRequestForDetail = req }
                            )
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(req.id, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = AdminTextPrimary, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis, modifier = Modifier.weight(0.10f).padding(end = 6.dp))
                        Text(displayText(req.name), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = AdminTextPrimary, modifier = Modifier.weight(0.22f))
                        Text(req.rep, fontSize = 12.sp, color = AdminTextSecondary, modifier = Modifier.weight(0.18f))
                        Text(req.rfc, fontSize = 12.sp, color = AdminTextMuted, modifier = Modifier.weight(0.13f))
                        Text(req.date, fontSize = 12.sp, color = AdminTextMuted, modifier = Modifier.weight(0.10f))

                        Row(
                            modifier = Modifier.weight(0.27f),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(32.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .border(1.dp, AdminEmeraldGreen, RoundedCornerShape(6.dp))
                                    .clickable { selectedRequestForDetail = req },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(tr("Ver detalle", "View details"), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AdminEmeraldGreen)
                            }
                            if (req.status == "Pendiente") {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(32.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(AdminEmeraldGreen)
                                        .clickable(enabled = !isActionLoading) { updateStatus(req, "Aprobado") },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(tr("Aprobar", "Approve"), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(32.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(MaterialTheme.colorScheme.surface)
                                        .border(1.dp, AdminBorderLight, RoundedCornerShape(6.dp))
                                        .clickable(enabled = !isActionLoading) { updateStatus(req, "Rechazado") },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(tr("Rechazar", "Reject"), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AdminTextPrimary)
                                }
                            } else {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(32.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (req.status == "Aprobado") AdminSelectedBg else Color(0xFFFEE2E2)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = statusText(req.status),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (req.status == "Aprobado") AdminSelectedText else Color(0xFF991B1B)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // DIÁLOGO MODAL DE DETALLE DE LA SOLICITUD
    if (selectedRequestForDetail != null) {
        val req = selectedRequestForDetail!!
        Dialog(onDismissRequest = { selectedRequestForDetail = null }) {
            Card(
                modifier = Modifier
                    .width(660.dp)
                    .heightIn(max = 760.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(28.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // Header del Diálogo
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .background(AdminSelectedBg),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Store,
                                    contentDescription = null,
                                    tint = AdminEmeraldGreen,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = displayText(req.name),
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AdminTextPrimary
                                )
                                Text(
                                    text = "${tr("Folio", "Reference")}: ${req.id} · ${tr("Fecha de Solicitud", "Application date")}: ${req.date}",
                                    fontSize = 12.sp,
                                    color = AdminTextSecondary
                                )
                            }
                        }

                        // Badge de Estado
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(
                                    when (req.status) {
                                        "Aprobado" -> AdminSelectedBg
                                        "Rechazado" -> Color(0xFFFEE2E2)
                                        else -> Color(0xFFFEF3C7)
                                    }
                                )
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = statusText(req.status),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = when (req.status) {
                                    "Aprobado" -> AdminSelectedText
                                    "Rechazado" -> Color(0xFF991B1B)
                                    else -> Color(0xFFD97706)
                                }
                            )
                        }
                    }

                    HorizontalDivider(color = AdminBorderLight)

                    if (english) {
                        Column {
                            Text("Machine-translated details may be inaccurate. Verify the original before deciding.", color = AdminTextSecondary, fontSize = 12.sp)
                            TextButton(onClick = { showOriginal = !showOriginal }) { Text(if (showOriginal) "Show translation" else "Show original") }
                            if (translating) Text("Translating…", color = AdminTextSecondary, fontSize = 12.sp)
                            if (translationError) {
                                Text("Some details could not be translated; the original is shown.", color = Color(0xFFB91C1C), fontSize = 12.sp)
                                TextButton(onClick = { translationAttempt++ }, enabled = !translating) { Text("Retry") }
                            }
                        }
                    }

                    // Detalles en 2 columnas
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        // Columna 1: Datos Institucionales
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            DetailItem(label = tr("RUC / ID Fiscal", "Tax ID"), value = req.rfc, icon = Icons.Default.Badge)
                            DetailItem(label = tr("Dirección Registrada", "Registered address"), value = displayText(req.address), icon = Icons.Default.LocationOn)
                            DetailItem(label = tr("Correo Administrador", "Administrator email"), value = req.email, icon = Icons.Default.Email)
                        }

                        // Columna 2: Datos Responsable Médico / Operador
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            DetailItem(label = tr("Solicitante Responsable", "Responsible applicant"), value = req.rep, icon = Icons.Default.Person)
                            DetailItem(label = tr("Cédula / Licencia", "Professional license"), value = req.licenseNumber, icon = Icons.Default.Verified)
                            DetailItem(label = tr("Teléfono de Contacto", "Contact phone"), value = req.phone, icon = Icons.Default.Phone)
                            DetailItem(label = tr("Fecha de solicitud", "Application date"), value = req.date, icon = Icons.Default.CalendarMonth)
                        }
                    }

                    if (req.decisionReason.isNotBlank()) {
                        DetailItem(label = tr("Motivo de la decisión", "Decision reason"), value = req.decisionReason, icon = Icons.Default.Info)
                    }

                    HorizontalDivider(color = AdminBorderLight)

                    // Acciones del modal
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = { selectedRequestForDetail = null },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(tr("Cerrar", "Close"), fontSize = 13.sp, color = AdminTextPrimary)
                        }

                        if (req.status == "Pendiente") {
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                OutlinedButton(
                                    onClick = {
                                        updateStatus(req, "Rechazado")
                                        selectedRequestForDetail = null
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFDC2626))
                                ) {
                                    Text(tr("Rechazar Solicitud", "Reject application"), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                }

                                Button(
                                    onClick = {
                                        updateStatus(req, "Aprobado")
                                        selectedRequestForDetail = null
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = AdminEmeraldGreen)
                                ) {
                                    Text(tr("Aprobar Habilitación", "Approve application"), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailItem(label: String, value: String, icon: ImageVector) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(AdminOffWhiteBg)
                .border(1.dp, AdminBorderLight, RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = AdminTextMuted, modifier = Modifier.size(18.dp))
        }
        Column {
            Text(text = label, fontSize = 11.sp, color = AdminTextMuted)
            Text(text = value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = AdminTextPrimary)
        }
    }
}

@Preview
@Composable
fun SuperadminDashboardScreenPreview() {
    SuperadminDashboardScreen()
}

fun main() = singleWindowApplication(title = "Preview - Superadmin Dashboard") {
    SuperadminDashboardScreen()
}

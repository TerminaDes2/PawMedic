package com.pawsmedic.desktop.admin.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

private val AdminBackground = Color(0xFFF8FAFC)
private val AdminGreen = Color(0xFF0D9488)
private val AdminText = Color(0xFF1E293B)
private val AdminMuted = Color(0xFF64748B)

@Serializable
private data class AdminBusinessApplication(
    @SerialName("id") val id: String,
    @SerialName("applicant_id") val applicantId: String,
    @SerialName("nombre_negocio") val businessName: String,
    @SerialName("domicilio") val address: String,
    @SerialName("municipio") val municipality: String,
    @SerialName("estado") val status: String,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("ruc") val taxId: String = "",
    @SerialName("telefono_contacto") val phone: String = "",
    @SerialName("responsable_veterinario") val veterinarianName: String = "",
    @SerialName("cedula_profesional") val licenseNumber: String = "",
    @SerialName("correo_contacto") val email: String = ""
)

@Composable
fun BusinessApplicationsAdminScreen(
    supabaseClient: SupabaseClient,
    onLogout: () -> Unit
) {
    var applications by remember { mutableStateOf<List<AdminBusinessApplication>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var busyApplicationId by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

    suspend fun refreshApplications() {
        isLoading = true
        errorMessage = null
        try {
            applications = supabaseClient
                .from("business_applications")
                .select(
                    columns = Columns.list(
                        "id",
                        "applicant_id",
                        "nombre_negocio",
                        "domicilio",
                        "municipio",
                        "estado",
                        "created_at",
                        "ruc",
                        "telefono_contacto",
                        "responsable_veterinario",
                        "cedula_profesional",
                        "correo_contacto"
                    )
                ) {
                    filter { eq("estado", "PENDIENTE") }
                }
                .decodeList<AdminBusinessApplication>()
        } catch (error: CancellationException) {
            throw error
        } catch (error: Exception) {
            errorMessage = error.message ?: "No se pudieron cargar las solicitudes."
        } finally {
            isLoading = false
        }
    }

    fun resolveApplication(applicationId: String, approve: Boolean) {
        coroutineScope.launch {
            busyApplicationId = applicationId
            errorMessage = null
            try {
                val function = if (approve) {
                    "aprobar_solicitud_veterinaria"
                } else {
                    "rechazar_solicitud_veterinaria"
                }
                supabaseClient.postgrest.rpc(
                    function = function,
                    parameters = buildJsonObject {
                        put("p_solicitud_id", applicationId)
                    }
                )
                refreshApplications()
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                errorMessage = error.message ?: "No se pudo actualizar la solicitud."
            } finally {
                busyApplicationId = null
            }
        }
    }

    LaunchedEffect(supabaseClient) {
        refreshApplications()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AdminBackground)
            .padding(24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "Solicitudes veterinarias",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = AdminText
                )
                Text(
                    text = "Solo las solicitudes pendientes aparecen aquí.",
                    fontSize = 14.sp,
                    color = AdminMuted
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(
                    onClick = { coroutineScope.launch { refreshApplications() } },
                    enabled = !isLoading
                ) {
                    Text("Actualizar")
                }
                OutlinedButton(onClick = onLogout) {
                    Text("Cerrar sesión")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (errorMessage != null) {
            Text(
                text = errorMessage.orEmpty(),
                color = Color(0xFFB91C1C),
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }

        when {
            isLoading -> CircularProgressIndicator(color = AdminGreen)
            applications.isEmpty() -> Text(
                text = "No hay solicitudes pendientes.",
                color = AdminMuted
            )
            else -> LazyColumn(
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(applications, key = { it.id }) { application ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = application.businessName,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = AdminText
                            )
                            Text("Representante: ${application.veterinarianName}", color = AdminText)
                            Text("Correo: ${application.email}", color = AdminText)
                            Text("RUC / ID fiscal: ${application.taxId}", color = AdminText)
                            Text("Cédula: ${application.licenseNumber}", color = AdminText)
                            Text("Teléfono: ${application.phone}", color = AdminText)
                            Text(
                                text = "Domicilio: ${application.address}, ${application.municipality}",
                                color = AdminText
                            )
                            Text(
                                text = "Folio: ${application.id} · Enviada: ${application.createdAt.orEmpty()}",
                                fontSize = 12.sp,
                                color = AdminMuted
                            )

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 10.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        resolveApplication(application.id, approve = false)
                                    },
                                    enabled = busyApplicationId == null
                                ) {
                                    Text("Rechazar")
                                }
                                Button(
                                    onClick = {
                                        resolveApplication(application.id, approve = true)
                                    },
                                    enabled = busyApplicationId == null,
                                    colors = ButtonDefaults.buttonColors(containerColor = AdminGreen)
                                ) {
                                    Text("Aprobar", color = Color.White)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

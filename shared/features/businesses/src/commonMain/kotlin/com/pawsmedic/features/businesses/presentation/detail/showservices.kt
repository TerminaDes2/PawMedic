package com.pawsmedic.features.businesses.presentation.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pawsmedic.shared.core.designsystem.theme.PawMedicColors
import com.pawsmedic.shared.core.designsystem.components.PawMedicSecondaryButton
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
private data class ShowServiceRecord(
    val id: String,
    val name: String,
    val description: String = "",
    @SerialName("price_cents") val priceCents: Int = 0,
    @SerialName("duration_minutes") val durationMinutes: Int = 30,
    @SerialName("photo_url") val photoUrl: String? = null,
    val category: String = "otros"
)

@Serializable
private data class ServiceSpeciesRecord(
    @SerialName("service_id") val serviceId: String,
    @SerialName("species_id") val speciesId: Int
)

@Serializable
private data class SpeciesRecord(
    val id: Int,
    val name: String
)

private data class ShowService(
    val service: ShowServiceRecord,
    val species: List<String>
)

@Composable
fun ShowServices(
    tenantId: String,
    supabaseClient: SupabaseClient,
    modifier: Modifier = Modifier
) {
    var services by remember(tenantId) { mutableStateOf<List<ShowService>>(emptyList()) }
    var isLoading by remember(tenantId) { mutableStateOf(true) }
    var errorMessage by remember(tenantId) { mutableStateOf<String?>(null) }
    var selectedService by remember { mutableStateOf<ShowService?>(null) }
    var showAllServices by remember(tenantId) { mutableStateOf(false) }
    var selectedCategory by remember(tenantId) { mutableStateOf("todos") }
    var categoryMenuExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(tenantId) {
        isLoading = true
        errorMessage = null
        try {
            val serviceRecords = supabaseClient.postgrest["services"].select {
                filter {
                    eq("tenant_id", tenantId)
                    eq("active", true)
                }
            }.decodeList<ShowServiceRecord>()

            val links = supabaseClient.postgrest["service_species"]
                .select()
                .decodeList<ServiceSpeciesRecord>()
                .filter { link -> serviceRecords.any { it.id == link.serviceId } }

            val speciesById = supabaseClient.postgrest["species"]
                .select()
                .decodeList<SpeciesRecord>()
                .associateBy { it.id }

            services = serviceRecords.map { service ->
                ShowService(
                    service = service,
                    species = links
                        .filter { it.serviceId == service.id }
                        .mapNotNull { speciesById[it.speciesId]?.name }
                )
            }
        } catch (error: kotlinx.coroutines.CancellationException) {
            throw error
        } catch (error: Exception) {
            errorMessage = error.message ?: "No se pudieron cargar los servicios."
        } finally {
            isLoading = false
        }
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        when {
            isLoading -> Text("Cargando servicios...", color = PawMedicColors.Gray600)
            errorMessage != null -> Text(
                text = errorMessage.orEmpty(),
                color = PawMedicColors.Error,
                fontSize = 13.sp
            )
            services.isEmpty() -> Text(
                text = "No hay servicios disponibles.",
                color = PawMedicColors.Gray600
            )
            else -> {
                val categoryOptions = listOf("todos") +
                    services.map { it.service.category }.distinct()
                val effectiveCategory = selectedCategory.takeIf { it in categoryOptions } ?: "todos"
                val selectedCategoryLabel = categoryLabel(effectiveCategory)
                val filteredServices = if (effectiveCategory == "todos") {
                    services
                } else {
                    services.filter { it.service.category == effectiveCategory }
                }

                Text(
                    text = "Selecciona una categoría",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = PawMedicColors.Gray700
                )
                Box {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(PawMedicColors.White, RoundedCornerShape(12.dp))
                            .border(1.dp, PawMedicColors.Gray300, RoundedCornerShape(12.dp))
                            .clickable { categoryMenuExpanded = true }
                            .padding(horizontal = 14.dp, vertical = 13.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = selectedCategoryLabel,
                            modifier = Modifier.weight(1f),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = PawMedicColors.Gray900
                        )
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = "Mostrar categorías",
                            tint = PawMedicColors.Teal600
                        )
                    }
                    DropdownMenu(
                        expanded = categoryMenuExpanded,
                        onDismissRequest = { categoryMenuExpanded = false },
                        modifier = Modifier.fillMaxWidth(0.88f)
                    ) {
                        categoryOptions.forEach { category ->
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = categoryLabel(category),
                                            modifier = Modifier.weight(1f),
                                            fontWeight = if (category == selectedCategory) {
                                                FontWeight.Bold
                                            } else {
                                                FontWeight.Normal
                                            }
                                        )
                                        Text(
                                            text = services.count {
                                                category == "todos" || it.service.category == category
                                            }.toString(),
                                            color = PawMedicColors.Gray500,
                                            fontSize = 12.sp
                                        )
                                    }
                                },
                                onClick = {
                                    selectedCategory = category
                                    showAllServices = false
                                    categoryMenuExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                val visibleServices = if (showAllServices) {
                    filteredServices
                } else {
                    filteredServices.take(5)
                }
                if (visibleServices.isEmpty()) {
                    Text(
                        text = "No hay servicios en esta categoría.",
                        color = PawMedicColors.Gray600,
                        fontSize = 13.sp
                    )
                }
                visibleServices.forEach { item ->
                    ServiceCard(item = item, onClick = { selectedService = item })
                }
                if (filteredServices.size > 5) {
                    PawMedicSecondaryButton(
                        text = if (showAllServices) "Ver menos" else "Ver más",
                        onClick = { showAllServices = !showAllServices }
                    )
                }

            }
        }
    }

    selectedService?.let { item ->
        ServiceDetailDialog(
            item = item,
            onDismiss = { selectedService = null }
        )
    }
}

private fun categoryLabel(category: String): String = when (category) {
    "todos" -> "Todos los servicios"
    "consulta" -> "Consultas"
    "vacunacion" -> "Vacunación"
    "desparasitacion" -> "Desparasitación"
    "estetica" -> "Estética"
    "cirugia" -> "Cirugías"
    "diagnostico" -> "Diagnóstico"
    "otros" -> "Otros servicios"
    else -> category.replaceFirstChar { it.uppercase() }
}

@Composable
private fun ServiceCard(item: ShowService, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(PawMedicColors.White, RoundedCornerShape(14.dp))
            .border(1.dp, PawMedicColors.Gray200, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(PawMedicColors.Teal50, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.MedicalServices,
                    contentDescription = null,
                    tint = PawMedicColors.Teal600
                )
            }
            Spacer(modifier = Modifier.size(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.service.name,
                    fontWeight = FontWeight.Bold,
                    color = PawMedicColors.Gray900
                )
                Text(
                    text = item.species.joinToString().ifBlank { "Todas las especies" },
                    fontSize = 12.sp,
                    color = PawMedicColors.Gray600
                )
            }
            Text(
                text = formatPrice(item.service.priceCents),
                fontWeight = FontWeight.Bold,
                color = PawMedicColors.Teal600
            )
        }
    }
}

@Composable
private fun ServiceDetailDialog(item: ShowService, onDismiss: () -> Unit) {
    val service = item.service
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = service.name,
                    modifier = Modifier.weight(1f),
                    fontWeight = FontWeight.Bold
                )
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Cerrar",
                    tint = PawMedicColors.Gray600,
                    modifier = Modifier.clickable(onClick = onDismiss)
                )
            }
        },
        text = {
            Column {
                Text(
                    text = service.description.ifBlank { "Sin descripción disponible." },
                    color = PawMedicColors.Gray700
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Especies atendidas",
                    fontWeight = FontWeight.Bold,
                    color = PawMedicColors.Gray900
                )
                Text(
                    text = item.species.joinToString().ifBlank { "Todas las especies" },
                    color = PawMedicColors.Gray700
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AccessTime,
                        contentDescription = null,
                        tint = PawMedicColors.Teal600,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = " ${service.durationMinutes} minutos · ${formatPrice(service.priceCents)}",
                        fontWeight = FontWeight.Bold,
                        color = PawMedicColors.Teal600
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cerrar")
            }
        }
    )
}

private fun formatPrice(priceCents: Int): String {
    val whole = priceCents / 100
    val cents = (priceCents % 100).toString().padStart(2, '0')
    return "$$whole.$cents"
}

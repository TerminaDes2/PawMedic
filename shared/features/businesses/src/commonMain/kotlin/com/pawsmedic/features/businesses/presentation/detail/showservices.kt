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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import com.pawsmedic.shared.core.designsystem.language.AppLanguage
import com.pawsmedic.shared.core.designsystem.language.LocalLanguagePreference
import com.pawsmedic.shared.core.designsystem.language.petCatalogLabel
import com.pawsmedic.shared.core.designsystem.language.serviceCategoryLabel
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
import com.pawsmedic.shared.core.designsystem.theme.PawMedicSemanticColors
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
    val language = LocalLanguagePreference.current?.selected ?: AppLanguage.SPANISH
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
            isLoading -> Text(language.text("Cargando servicios...", "Loading services..."), color = PawMedicSemanticColors.current.secondaryText)
            errorMessage != null -> Text(
                text = errorMessage.orEmpty(),
                color = MaterialTheme.colorScheme.error,
                fontSize = 13.sp
            )
            services.isEmpty() -> Text(
                text = language.text("No hay servicios disponibles.", "No services available."),
                color = PawMedicSemanticColors.current.secondaryText
            )
            else -> {
                val categoryOptions = listOf("todos") +
                    services.map { it.service.category }.distinct()
                val effectiveCategory = selectedCategory.takeIf { it in categoryOptions } ?: "todos"
                val selectedCategoryLabel = categoryLabel(effectiveCategory, language)
                val filteredServices = if (effectiveCategory == "todos") {
                    services
                } else {
                    services.filter { it.service.category == effectiveCategory }
                }

                Text(
                    text = language.text("Selecciona una categoría", "Select a category"),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = PawMedicSemanticColors.current.text
                )
                Box {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(PawMedicSemanticColors.current.surface, RoundedCornerShape(12.dp))
                            .border(1.dp, PawMedicSemanticColors.current.border, RoundedCornerShape(12.dp))
                            .clickable { categoryMenuExpanded = true }
                            .padding(horizontal = 14.dp, vertical = 13.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = selectedCategoryLabel,
                            modifier = Modifier.weight(1f),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = PawMedicSemanticColors.current.text
                        )
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = language.text("Mostrar categorías", "Show categories"),
                            tint = PawMedicSemanticColors.current.accent
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
                                            text = categoryLabel(category, language),
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
                                            color = PawMedicSemanticColors.current.hint,
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
                        text = language.text("No hay servicios en esta categoría.", "No services in this category."),
                        color = PawMedicSemanticColors.current.secondaryText,
                        fontSize = 13.sp
                    )
                }
                visibleServices.forEach { item ->
                    ServiceCard(item = item, onClick = { selectedService = item })
                }
                if (filteredServices.size > 5) {
                    PawMedicSecondaryButton(
                        text = if (showAllServices) language.text("Ver menos", "Show less") else language.text("Ver más", "Show more"),
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

private fun categoryLabel(category: String, language: AppLanguage): String =
    if (category == "todos") language.text("Todos los servicios", "All services")
    else serviceCategoryLabel(category, language).replaceFirstChar { it.uppercase() }

@Composable
private fun ServiceCard(item: ShowService, onClick: () -> Unit) {
    val language = LocalLanguagePreference.current?.selected ?: AppLanguage.SPANISH
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(PawMedicSemanticColors.current.surface, RoundedCornerShape(14.dp))
            .border(1.dp, PawMedicSemanticColors.current.separator, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(PawMedicSemanticColors.current.softAccent, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.MedicalServices,
                    contentDescription = null,
                    tint = PawMedicSemanticColors.current.accent
                )
            }
            Spacer(modifier = Modifier.size(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.service.name,
                    fontWeight = FontWeight.Bold,
                    color = PawMedicSemanticColors.current.text
                )
                Text(
                    text = item.species.joinToString { petCatalogLabel(it, language) }.ifBlank { language.text("Todas las especies", "All species") },
                    fontSize = 12.sp,
                    color = PawMedicSemanticColors.current.secondaryText
                )
            }
            Text(
                text = formatPrice(item.service.priceCents),
                fontWeight = FontWeight.Bold,
                color = PawMedicSemanticColors.current.accent
            )
        }
    }
}

@Composable
private fun ServiceDetailDialog(item: ShowService, onDismiss: () -> Unit) {
    val language = LocalLanguagePreference.current?.selected ?: AppLanguage.SPANISH
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
                    contentDescription = language.text("Cerrar", "Close"),
                    tint = PawMedicSemanticColors.current.secondaryText,
                    modifier = Modifier.clickable(onClick = onDismiss)
                )
            }
        },
        text = {
            Column {
                Text(
                    text = service.description.ifBlank { language.text("Sin descripción disponible.", "No description available.") },
                    color = PawMedicSemanticColors.current.secondaryText
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = language.text("Especies atendidas", "Species treated"),
                    fontWeight = FontWeight.Bold,
                    color = PawMedicSemanticColors.current.text
                )
                Text(
                    text = item.species.joinToString { petCatalogLabel(it, language) }.ifBlank { language.text("Todas las especies", "All species") },
                    color = PawMedicSemanticColors.current.secondaryText
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AccessTime,
                        contentDescription = null,
                        tint = PawMedicSemanticColors.current.accent,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = language.text(" ${service.durationMinutes} minutos · ${formatPrice(service.priceCents)}", " ${service.durationMinutes} minutes · ${formatPrice(service.priceCents)}"),
                        fontWeight = FontWeight.Bold,
                        color = PawMedicSemanticColors.current.accent
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(language.text("Cerrar", "Close"))
            }
        }
    )
}

private fun formatPrice(priceCents: Int): String {
    val whole = priceCents / 100
    val cents = (priceCents % 100).toString().padStart(2, '0')
    return "$$whole.$cents"
}

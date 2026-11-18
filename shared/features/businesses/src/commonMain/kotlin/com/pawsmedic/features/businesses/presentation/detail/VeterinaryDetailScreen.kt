package com.pawsmedic.features.businesses.presentation.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Star
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pawsmedic.features.businesses.presentation.VeterinaryDetailViewModel
import com.pawsmedic.shared.core.designsystem.components.PawMedicPrimaryButton
import com.pawsmedic.shared.core.designsystem.components.PawMedicTopBar
import com.pawsmedic.shared.core.designsystem.theme.PawMedicColors
import io.github.jan.supabase.SupabaseClient
import com.pawsmedic.shared.core.designsystem.theme.PawMedicSemanticColors
import com.pawsmedic.shared.core.designsystem.language.AppLanguage
import com.pawsmedic.shared.core.designsystem.language.LocalLanguagePreference

@Composable
fun VeterinaryDetailScreen(
    viewModel: VeterinaryDetailViewModel,
    supabaseClient: SupabaseClient? = null,
    onBackClick: () -> Unit,
    onStartBooking: (String) -> Unit,
    onOpenProductCatalog: (String, String) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    val language = LocalLanguagePreference.current?.selected ?: AppLanguage.SPANISH
    val uiState by viewModel.uiState.collectAsState()
    val clinic = uiState.clinic
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(PawMedicSemanticColors.current.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        PawMedicTopBar(
            title = language.text("Perfil Veterinaria", "Clinic profile"),
            onBackClick = onBackClick
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (uiState.isLoading) {
            Text(language.text("Cargando perfil de la clínica…", "Loading clinic profile…"), color = PawMedicSemanticColors.current.secondaryText)
        }
        uiState.errorMessage?.let { message ->
            Text(
                text = language.text("No se pudo cargar el perfil: $message", "Could not load profile: $message"),
                fontSize = 14.sp,
                color = PawMedicColors.Error
            )
            Spacer(modifier = Modifier.height(8.dp))
            PawMedicPrimaryButton(text = language.text("Reintentar", "Retry"), onClick = viewModel::loadClinic)
        }
        if (clinic == null && !uiState.isLoading && uiState.errorMessage == null) {
            Text(language.text("La clínica no está disponible en el directorio activo.", "This clinic is not available in the active directory."), color = PawMedicSemanticColors.current.secondaryText)
        }

        if (clinic != null) {
            // --- GALLERY IMAGE BANNER ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(PawMedicSemanticColors.current.softAccent),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.LocalHospital,
                    contentDescription = null,
                    tint = PawMedicSemanticColors.current.accent,
                    modifier = Modifier.size(64.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // --- TITLE ---
            Text(
                text = clinic.name,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = PawMedicSemanticColors.current.accent
            )

            Spacer(modifier = Modifier.height(16.dp))

            // --- RATING CARD ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(PawMedicSemanticColors.current.surface)
                    .border(1.dp, PawMedicSemanticColors.current.separator, RoundedCornerShape(16.dp))
                    .padding(14.dp)
            ) {
                Column {
                    Text(
                        text = language.text("Calificación", "Rating"),
                        fontSize = 12.sp,
                        color = PawMedicSemanticColors.current.hint
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = Color(0xFFF59E0B),
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = if (clinic.rating > 0) {
                                "${clinic.rating} (${clinic.reviewCount})"
                            } else {
                                clinic.reviewCount
                            },
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = PawMedicSemanticColors.current.text,
                            modifier = Modifier.padding(start = 6.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // --- INFORMACION GENERAL ---
            Text(
                text = language.text("Información General", "General information"),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = PawMedicSemanticColors.current.text
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = PawMedicSemanticColors.current.accent,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = clinic.address?.takeIf(String::isNotBlank) ?: language.text("Dirección no registrada", "Address not recorded"),
                    fontSize = 14.sp,
                    color = PawMedicSemanticColors.current.text
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Phone,
                    contentDescription = null,
                    tint = PawMedicSemanticColors.current.accent,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = clinic.phone?.takeIf(String::isNotBlank) ?: language.text("Teléfono no registrado", "Phone number not recorded"),
                    fontSize = 14.sp,
                    color = PawMedicSemanticColors.current.text
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            clinic.description?.takeIf(String::isNotBlank)?.let { description ->
                Text(
                    text = description,
                    fontSize = 14.sp,
                    color = PawMedicSemanticColors.current.secondaryText,
                    lineHeight = 20.sp
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            OpeningHoursCard(
                schedules = clinic.schedules,
                timeZone = clinic.timeZone
            )

            Spacer(modifier = Modifier.height(24.dp))

            // --- SERVICIOS DISPONIBLES ---
            Text(
                text = language.text("Servicios disponibles", "Available services"),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = PawMedicSemanticColors.current.text
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (supabaseClient != null) {
                ShowServices(
                    tenantId = clinic.id,
                    supabaseClient = supabaseClient
                )
            } else {
                Text(
                    text = "No se pudo conectar con el catálogo de servicios.",
                    color = PawMedicColors.Gray600
                )
            }

            if (clinic.specialties.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                clinic.specialties.take(6).chunked(3).forEach { rowSpecs ->
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        rowSpecs.forEach { spec ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(PawMedicSemanticColors.current.surface)
                                    .border(1.dp, PawMedicSemanticColors.current.border, RoundedCornerShape(20.dp))
                                    .padding(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = spec,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = PawMedicSemanticColors.current.text
                                )
                            }
                        }
                    }
                }
            }
            } else if (supabaseClient == null) {
                Text(
                    text = language.text("Especialidades no registradas.", "No specialties recorded."),
                    color = PawMedicSemanticColors.current.secondaryText
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = language.text("Productos disponibles", "Available products"),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = PawMedicSemanticColors.current.text
            )

            Spacer(modifier = Modifier.height(10.dp))

            when {
                uiState.productsLoading -> Text(
                    language.text("Cargando productos…", "Loading products…"),
                    color = PawMedicSemanticColors.current.secondaryText
                )
                uiState.productsError -> {
                    Text(language.text("No se pudieron cargar los productos.", "Could not load products."), color = PawMedicColors.Error)
                    PawMedicPrimaryButton(text = language.text("Reintentar", "Retry"), onClick = viewModel::refreshProducts)
                }
                uiState.products.isEmpty() -> Text(
                    language.text("No hay productos disponibles.", "No products available."),
                    color = PawMedicSemanticColors.current.secondaryText
                )
                else -> {
                    PawMedicPrimaryButton(
                        text = language.text("Ver catálogo de productos", "View product catalog"),
                        onClick = { onOpenProductCatalog(clinic.id, clinic.name) }
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    uiState.products.take(3).forEach { product ->
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(PawMedicColors.White)
                                .border(1.dp, PawMedicColors.Gray200, RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            if (product.imageUrl.isNotBlank()) {
                                ProductImage(product.imageUrl, Modifier.size(100.dp).clip(RoundedCornerShape(8.dp)))
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                            Text(product.name, fontWeight = FontWeight.Bold, color = PawMedicColors.Gray900)
                            if (product.description.isNotBlank()) {
                                Text(product.description, color = PawMedicColors.Gray600)
                            }
                            Text(
                                "$${product.priceCents / 100}.${(product.priceCents % 100).toString().padStart(2, '0')} MXN",
                                color = PawMedicColors.Teal600,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))

            // --- AGENDAR CITA BUTTON ---
            PawMedicPrimaryButton(
                text = language.text("Agendar cita", "Book an appointment"),
                onClick = { onStartBooking(clinic.id) }
            )

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

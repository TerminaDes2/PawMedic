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
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Star
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pawsmedic.features.businesses.presentation.VeterinaryDetailViewModel
import com.pawsmedic.features.products.domain.model.Product
import com.pawsmedic.features.products.domain.model.formattedPrice
import com.pawsmedic.features.products.domain.repository.ProductRepository
import com.pawsmedic.shared.core.designsystem.components.PawMedicPrimaryButton
import com.pawsmedic.shared.core.designsystem.components.PawMedicTopBar
import com.pawsmedic.shared.core.designsystem.theme.PawMedicColors
import com.pawsmedic.shared.core.designsystem.theme.PawMedicSemanticColors

@Composable
fun VeterinaryDetailScreen(
    viewModel: VeterinaryDetailViewModel,
    productRepository: ProductRepository? = null,
    onBackClick: () -> Unit,

    onStartBooking: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val clinic = uiState.clinic
    var products by remember(clinic?.id) {
        mutableStateOf<List<Product>>(emptyList())
    }
    var productsLoading by remember(clinic?.id) {
        mutableStateOf(false)
    }
    var productsError by remember(clinic?.id) {
        mutableStateOf<String?>(null)
    }

    LaunchedEffect(clinic?.id, productRepository) {
        val tenantId = clinic?.id ?: return@LaunchedEffect
        val repository = productRepository ?: return@LaunchedEffect
        productsLoading = true
        productsError = null
        try {
            products = repository.getProductsByTenant(tenantId)
        } catch (error: kotlinx.coroutines.CancellationException) {
            throw error
        } catch (error: Exception) {
            productsError = error.message ?: "No se pudo cargar el catálogo."
        } finally {
            productsLoading = false
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(PawMedicSemanticColors.current.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        PawMedicTopBar(
            title = "Perfil Veterinaria",
            onBackClick = onBackClick
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (uiState.isLoading) {
            Text("Cargando perfil de la clínica…", color = PawMedicSemanticColors.current.secondaryText)
        }
        uiState.errorMessage?.let { message ->
            Text(
                text = "No se pudo cargar el perfil: $message",
                fontSize = 14.sp,
                color = PawMedicColors.Error
            )
            Spacer(modifier = Modifier.height(8.dp))
            PawMedicPrimaryButton(text = "Reintentar", onClick = viewModel::loadClinic)
        }
        if (clinic == null && !uiState.isLoading && uiState.errorMessage == null) {
            Text("La clínica no está disponible en el directorio activo.", color = PawMedicSemanticColors.current.secondaryText)
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
                        text = "Calificación",
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
                text = "Información General",
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
                    text = clinic.address?.takeIf(String::isNotBlank) ?: "Dirección no registrada",
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
                    text = clinic.phone?.takeIf(String::isNotBlank) ?: "Teléfono no registrado",
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

            // --- HORARIOS DE ATENCION CARD ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(PawMedicSemanticColors.current.warningSurface)
                    .border(1.dp, Color(0xFFFDE047), RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            tint = PawMedicSemanticColors.current.text,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Horarios de Atención",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = PawMedicSemanticColors.current.text
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    if (clinic.hours.isEmpty()) {
                        Text(
                            text = "Horario no registrado.",
                            fontSize = 13.sp,
                            color = PawMedicSemanticColors.current.text
                        )
                    } else {
                        clinic.hours.forEach { hour ->
                            Text(
                                text = "• $hour",
                                fontSize = 13.sp,
                                color = PawMedicSemanticColors.current.text,
                                modifier = Modifier.padding(vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- ESPECIALIDADES DISPONIBLES ---
            Text(
                text = "Especialidades Disponibles",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = PawMedicSemanticColors.current.text
            )

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
                            if (clinic.specialties.isEmpty()) {
                                Text("Especialidades no registradas.", color = PawMedicSemanticColors.current.secondaryText)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = "Productos disponibles",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = PawMedicSemanticColors.current.text
            )

            Spacer(modifier = Modifier.height(10.dp))

            when {
                productsLoading -> Text(
                    text = "Cargando catálogo...",
                    color = PawMedicSemanticColors.current.secondaryText
                )
                productsError != null -> Text(
                    text = productsError.orEmpty(),
                    color = Color(0xFFB91C1C)
                )
                products.isEmpty() -> Text(
                    text = "No hay productos disponibles.",
                    color = PawMedicSemanticColors.current.secondaryText
                )
                else -> Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    products.forEach { product ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(PawMedicSemanticColors.current.surface)
                                .border(
                                    1.dp,
                                    PawMedicSemanticColors.current.separator,
                                    RoundedCornerShape(14.dp)
                                )
                                .padding(14.dp)
                        ) {
                            Column {
                                Text(
                                    text = product.name,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PawMedicSemanticColors.current.text
                                )
                                if (!product.description.isNullOrBlank()) {
                                    Text(
                                        text = product.description.orEmpty(),
                                        fontSize = 13.sp,
                                        color = PawMedicSemanticColors.current.secondaryText,
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                }
                                Text(
                                    text = product.formattedPrice(),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PawMedicSemanticColors.current.accent,
                                    modifier = Modifier.padding(top = 8.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // --- AGENDAR CITA BUTTON ---
            PawMedicPrimaryButton(
                text = "Agendar cita",
                onClick = { onStartBooking(clinic.id) }
            )

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

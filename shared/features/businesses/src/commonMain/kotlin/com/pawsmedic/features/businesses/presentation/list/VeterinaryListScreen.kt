package com.pawsmedic.features.businesses.presentation.list

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Search
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
import com.pawsmedic.features.businesses.domain.model.Business
import com.pawsmedic.features.businesses.presentation.VeterinaryListViewModel
import com.pawsmedic.features.businesses.presentation.detail.ClinicImage
import com.pawsmedic.shared.core.designsystem.components.PawMedicPrimaryButton
import com.pawsmedic.shared.core.designsystem.components.PawMedicSecondaryButton
import com.pawsmedic.shared.core.designsystem.theme.PawMedicColors
import com.pawsmedic.shared.core.designsystem.theme.PawMedicSemanticColors
import com.pawsmedic.shared.core.designsystem.language.AppLanguage
import com.pawsmedic.shared.core.designsystem.language.LocalLanguagePreference

@Composable
fun VeterinaryListScreen(
    viewModel: VeterinaryListViewModel,
    onNavigateToDetail: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val language = LocalLanguagePreference.current?.selected ?: AppLanguage.SPANISH
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(PawMedicSemanticColors.current.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        Text(
            text = language.text("Veterinarias Disponibles", "Available veterinary clinics"),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = PawMedicSemanticColors.current.text,
            modifier = Modifier.padding(vertical = 12.dp)
        )

        // --- SEARCH BAR ---
        OutlinedTextField(
            value = uiState.searchQuery,
            onValueChange = viewModel::onSearchQueryChanged,
            placeholder = {
                Text(
                    text = language.text("Buscar veterinarias o especialistas...", "Search clinics or specialists..."),
                    color = PawMedicSemanticColors.current.hint,
                    fontSize = 14.sp
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = language.text("Buscar", "Search"),
                    tint = PawMedicSemanticColors.current.hint,
                    modifier = Modifier.size(20.dp)
                )
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PawMedicColors.Teal600,
                unfocusedBorderColor = PawMedicSemanticColors.current.border,
                focusedContainerColor = PawMedicSemanticColors.current.surface,
                unfocusedContainerColor = PawMedicSemanticColors.current.surface
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // --- FILTER TAGS ROW ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            listOf("Cerca de mí", "Abierto 24h", "Urgencias").forEach { filter ->
                val isSelected = filter == uiState.selectedFilter
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isSelected) PawMedicSemanticColors.current.softAccent else PawMedicSemanticColors.current.surface)
                        .border(
                            width = 1.dp,
                            color = if (isSelected) PawMedicColors.Teal600 else PawMedicSemanticColors.current.border,
                            shape = RoundedCornerShape(20.dp)
                        )
                        .clickable { viewModel.onFilterSelected(filter) }
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = language.text(filter, when (filter) {
                            "Cerca de mí" -> "Near me"
                            "Abierto 24h" -> "Open 24h"
                            "Urgencias" -> "Emergency"
                            else -> filter
                        }),
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) PawMedicSemanticColors.current.accent else PawMedicSemanticColors.current.text
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // --- CLINICS CARDS LIST ---
        if (uiState.isLoading) {
            Text(
                text = language.text("Cargando clínicas activas…", "Loading active clinics…"),
                fontSize = 14.sp,
                color = PawMedicSemanticColors.current.secondaryText
            )
        }
        uiState.errorMessage?.let { message ->
            Text(
                text = language.text("No se pudo cargar el directorio. Verifica tu conexión e inténtalo de nuevo.", "Could not load the directory. Check your connection and try again."),
                fontSize = 14.sp,
                color = PawMedicColors.Error
            )
            Spacer(modifier = Modifier.height(8.dp))
            PawMedicPrimaryButton(text = language.text("Reintentar", "Retry"), onClick = viewModel::retrySearch)
        }
        if (!uiState.isLoading && uiState.errorMessage == null && uiState.clinics.isEmpty()) {
            Text(
                text = language.text("No hay clínicas activas disponibles en este momento.", "There are no active clinics available at the moment."),
                fontSize = 14.sp,
                color = PawMedicSemanticColors.current.secondaryText
            )
        }
        if (!uiState.isLoading && uiState.errorMessage == null) {
            uiState.clinics.forEach { clinic ->
                VeterinaryCard(
                    clinic = clinic,
                    onDetailClick = { onNavigateToDetail(clinic.id) }
                )
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Composable
private fun VeterinaryCard(
    clinic: Business,
    onDetailClick: () -> Unit
) {
    val language = LocalLanguagePreference.current?.selected ?: AppLanguage.SPANISH
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(PawMedicSemanticColors.current.surface)
            .border(1.dp, PawMedicSemanticColors.current.separator, RoundedCornerShape(20.dp))
            .padding(16.dp)
    ) {
        Column {
            // Clinic Photo / Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(PawMedicSemanticColors.current.softAccent),
                contentAlignment = Alignment.Center
            ) {
                if (clinic.imageUrl.isNullOrBlank()) {
                    Icon(
                        imageVector = Icons.Default.LocalHospital,
                        contentDescription = null,
                        tint = PawMedicSemanticColors.current.accent,
                        modifier = Modifier.size(56.dp)
                    )
                } else {
                    ClinicImage(
                        url = clinic.imageUrl,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Name & Rating
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = clinic.name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = PawMedicSemanticColors.current.text
                )
                if (clinic.rating > 0) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = Color(0xFFF59E0B),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = clinic.rating.toString(),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = PawMedicSemanticColors.current.text,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = clinic.address?.takeIf(String::isNotBlank) ?: language.text("Dirección no registrada", "Address not recorded"),
                fontSize = 13.sp,
                color = PawMedicSemanticColors.current.hint
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Specialties Tags
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                clinic.specialties.take(3).forEach { spec ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(PawMedicSemanticColors.current.softAccent)
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = spec,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = PawMedicSemanticColors.current.accent
                        )
                    }
                }
                if (clinic.specialties.isEmpty()) {
                    Text(
                        text = language.text("Especialidades no registradas", "No specialties recorded"),
                        fontSize = 12.sp,
                        color = PawMedicSemanticColors.current.hint
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            PawMedicSecondaryButton(
                text = language.text("Ver detalles", "View details"),
                onClick = onDetailClick
            )
        }
    }
}

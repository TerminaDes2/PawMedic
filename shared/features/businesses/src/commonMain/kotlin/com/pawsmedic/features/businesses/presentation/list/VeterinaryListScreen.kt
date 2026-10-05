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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
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
import com.pawsmedic.shared.core.designsystem.components.PawMedicSecondaryButton
import com.pawsmedic.shared.core.designsystem.theme.PawMedicColors

@Composable
fun VeterinaryListScreen(
    viewModel: VeterinaryListViewModel,
    onNavigateToDetail: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(PawMedicColors.BackgroundScreen)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        Text(
            text = "Veterinarias Disponibles",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = PawMedicColors.Gray900,
            modifier = Modifier.padding(vertical = 12.dp)
        )

        // --- SEARCH BAR ---
        OutlinedTextField(
            value = uiState.searchQuery,
            onValueChange = viewModel::onSearchQueryChanged,
            placeholder = {
                Text(
                    text = "Buscar veterinarias o especialistas...",
                    color = PawMedicColors.Gray400,
                    fontSize = 14.sp
                )
            },
            leadingIcon = {
                Text(text = "🔍", fontSize = 16.sp, color = PawMedicColors.Gray400)
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PawMedicColors.Teal600,
                unfocusedBorderColor = PawMedicColors.Gray300,
                focusedContainerColor = PawMedicColors.White,
                unfocusedContainerColor = PawMedicColors.White
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
                        .background(if (isSelected) Color(0xFFE6F4EA) else PawMedicColors.White)
                        .border(
                            width = 1.dp,
                            color = if (isSelected) PawMedicColors.Teal600 else PawMedicColors.Gray300,
                            shape = RoundedCornerShape(20.dp)
                        )
                        .clickable { viewModel.onFilterSelected(filter) }
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = filter,
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) PawMedicColors.Teal600 else PawMedicColors.Gray700
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // --- CLINICS CARDS LIST ---
        uiState.clinics.forEach { clinic ->
            VeterinaryCard(
                clinic = clinic,
                onDetailClick = { onNavigateToDetail(clinic.id) }
            )
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun VeterinaryCard(
    clinic: Business,
    onDetailClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(PawMedicColors.White)
            .border(1.dp, PawMedicColors.Gray200, RoundedCornerShape(20.dp))
            .padding(16.dp)
    ) {
        Column {
            // Clinic Photo / Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(PawMedicColors.Teal50),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "🏥", fontSize = 64.sp)
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
                    color = PawMedicColors.Gray900
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "⭐", fontSize = 14.sp)
                    Text(
                        text = clinic.rating.toString(),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = PawMedicColors.Gray900,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = clinic.address ?: "Calle 100 #15-45, Bogotá",
                fontSize = 13.sp,
                color = PawMedicColors.Gray500
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
                            .background(Color(0xFFE6F4EA))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = spec,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = PawMedicColors.Teal600
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            PawMedicSecondaryButton(
                text = "Ver detalles",
                onClick = onDetailClick
            )
        }
    }
}

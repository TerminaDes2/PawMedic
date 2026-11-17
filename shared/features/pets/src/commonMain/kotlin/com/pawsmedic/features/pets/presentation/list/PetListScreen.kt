package com.pawsmedic.features.pets.presentation.list

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pawsmedic.features.pets.domain.model.Pet
import com.pawsmedic.features.pets.presentation.PetListViewModel
import com.pawsmedic.features.pets.presentation.components.PetAvatarImage
import com.pawsmedic.shared.core.designsystem.components.PawMedicSecondaryButton
import com.pawsmedic.shared.core.designsystem.theme.PawMedicColors
import com.pawsmedic.shared.core.designsystem.theme.PawMedicSemanticColors
import com.pawsmedic.shared.core.designsystem.language.AppLanguage
import com.pawsmedic.shared.core.designsystem.language.LocalLanguagePreference
import com.pawsmedic.shared.core.designsystem.language.petCatalogLabel

@Composable
fun PetListScreen(
    viewModel: PetListViewModel,
    onNavigateToAddPet: () -> Unit,
    onNavigateToPetDetail: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val language = LocalLanguagePreference.current?.selected ?: AppLanguage.SPANISH
    val uiState by viewModel.uiState.collectAsState()
    val selectedPet = uiState.selectedPet
        ?.let { selected -> uiState.pets.firstOrNull { it.id == selected.id } }
        ?: uiState.pets.firstOrNull()

    Box(modifier = modifier.fillMaxSize().background(PawMedicSemanticColors.current.background)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // --- TOP PETS BAR HEADER ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(PawMedicColors.Teal600)
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = language.text("MASCOTAS", "PETS"),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = PawMedicColors.White,
                    letterSpacing = 1.sp
                )
            }

            Column(modifier = Modifier.padding(16.dp)) {
                if (uiState.pets.isEmpty() && !uiState.isLoading) {
                    Text(
                        text = language.text("Aún no tienes mascotas registradas. Agrega una para comenzar.", "You have no pets registered yet. Add one to get started."),
                        fontSize = 15.sp,
                        color = PawMedicSemanticColors.current.secondaryText,
                        modifier = Modifier.padding(vertical = 24.dp)
                    )
                }

                // --- TOP HORIZONTAL AVATARS ROW ---
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    items(uiState.pets) { pet ->
                        val isSelected = pet.id == selectedPet?.id
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.clickable { viewModel.selectPet(pet) }
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(68.dp)
                                    .clip(CircleShape)
                                    .border(
                                        width = if (isSelected) 3.dp else 1.dp,
                                        color = if (isSelected) PawMedicColors.Teal600 else PawMedicSemanticColors.current.border,
                                        shape = CircleShape
                                    )
                            ) {
                                key(pet.id, pet.photo_url) {
                                    PetAvatarImage(
                                        photo_url = pet.photo_url,
                                        species = pet.species,
                                        size = 68.dp
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = pet.name,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = PawMedicSemanticColors.current.text
                            )
                        }
                    }

                    item {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.clickable { onNavigateToAddPet() }
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .background(PawMedicSemanticColors.current.surface)
                                    .border(2.dp, PawMedicColors.Teal600, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = language.text("Agregar", "Add"),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PawMedicSemanticColors.current.accent,
                                    textAlign = TextAlign.Center
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = " ", fontSize = 13.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // --- HERO CARD FOR SELECTED PET ---
                if (selectedPet != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(PawMedicSemanticColors.current.surface)
                            .border(1.dp, PawMedicSemanticColors.current.separator, RoundedCornerShape(20.dp))
                            .padding(16.dp)
                    ) {
                        Column {
                            // Pet Image Banner
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(PawMedicSemanticColors.current.softAccent),
                                contentAlignment = Alignment.Center
                            ) {
                                key(selectedPet.id, selectedPet.photo_url) {
                                    PetAvatarImage(
                                        photo_url = selectedPet.photo_url,
                                        species = selectedPet.species,
                                        size = 140.dp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${selectedPet.name} ${if (selectedPet.name.contains("(")) "" else language.text("(Mascota)", "(Pet)")}",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PawMedicSemanticColors.current.text
                                )

                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Grid Info Chips
                            Row(modifier = Modifier.fillMaxWidth()) {
                                InfoGridChip(
                                    icon = "🎂",
                                    text = selectedPet.dateOfBirth ?: language.text("Nacimiento no registrado", "Birth date not recorded"),
                                    modifier = Modifier.weight(1f)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                InfoGridChip(
                                    icon = "⚖️",
                                    text = selectedPet.weight ?: language.text("Peso no registrado", "Weight not recorded"),
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            InfoGridChip(
                                icon = "🐾",
                                text = selectedPet.breed?.let { petCatalogLabel(it, language) } ?: language.text("Raza no registrada", "Breed not recorded"),
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            PawMedicSecondaryButton(
                                text = language.text("Ver perfil completo", "View full profile"),
                                onClick = { onNavigateToPetDetail(selectedPet.id) }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

            }
        }

        // --- FLOATING ADD PET BUTTON ---
        FloatingActionButton(
            onClick = onNavigateToAddPet,
            containerColor = PawMedicColors.Teal600,
            contentColor = PawMedicColors.White,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = language.text("Agregar mascota", "Add pet"),
                tint = PawMedicColors.White
            )
        }
    }
}

@Composable
private fun InfoGridChip(
    icon: String,
    text: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(PawMedicSemanticColors.current.subtleSurface)
            .border(1.dp, PawMedicSemanticColors.current.separator, RoundedCornerShape(12.dp))
            .padding(vertical = 10.dp, horizontal = 12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = icon, fontSize = 16.sp)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = text,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = PawMedicSemanticColors.current.text
            )
        }
    }
}

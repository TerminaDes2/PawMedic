package com.pawsmedic.features.pets.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pawsmedic.shared.core.designsystem.theme.PawMedicColors

@Composable
fun PetAvatarImage(
    photoUrl: String?,
    species: String?,
    size: Dp = 64.dp,
    backgroundColor: Color = PawMedicColors.Teal100,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        val emoji = when {
            species?.contains("Gato", true) == true || species?.contains("Felino", true) == true -> "🐱"
            species?.contains("Perro", true) == true || species?.contains("Canino", true) == true -> "🐕"
            species?.contains("Conejo", true) == true -> "🐰"
            species?.contains("Ave", true) == true || species?.contains("Pájaro", true) == true -> "🦜"
            else -> "🐾"
        }
        val fontSize = (size.value * 0.45f).sp

        if (!photoUrl.isNullOrBlank()) {
            AsyncPetImage(
                photoUrl = photoUrl,
                fallbackEmoji = emoji,
                size = size
            )
        } else {
            Text(text = emoji, fontSize = fontSize)
        }
    }
}

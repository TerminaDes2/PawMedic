package com.pawsmedic.features.pets.presentation.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp

@Composable
actual fun AsyncPetImage(
    photoUrl: String,
    fallbackEmoji: String,
    size: Dp,
    modifier: Modifier
) {
    Text(text = fallbackEmoji, fontSize = (size.value * 0.5f).sp)
}

package com.pawsmedic.features.pets.presentation.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp

@Composable
expect fun AsyncPetImage(
    photoUrl: String,
    fallbackEmoji: String,
    size: Dp,
    modifier: Modifier = Modifier
)

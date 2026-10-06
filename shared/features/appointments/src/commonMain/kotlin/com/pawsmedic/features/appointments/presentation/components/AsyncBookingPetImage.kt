package com.pawsmedic.features.appointments.presentation.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp

@Composable
expect fun AsyncBookingPetImage(
    photoUrl: String,
    fallbackEmoji: String,
    size: Dp,
    modifier: Modifier = Modifier
)

package com.pawsmedic.features.appointments.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.pawsmedic.shared.core.designsystem.theme.PawMedicColors

@Composable
actual fun AsyncBookingPetImage(
    photoUrl: String,
    fallbackEmoji: String,
    size: Dp,
    modifier: Modifier
) {
    val context = LocalContext.current

    if (photoUrl.isBlank()) {
        Box(
            modifier = modifier
                .size(size)
                .clip(CircleShape)
                .background(PawMedicColors.Teal100),
            contentAlignment = Alignment.Center
        ) {
            Text(text = fallbackEmoji, fontSize = (size.value * 0.45f).sp)
        }
    } else {
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(photoUrl)
                .crossfade(true)
                .build(),
            contentDescription = "Pet Photo",
            contentScale = ContentScale.Crop,
            modifier = modifier
                .size(size)
                .clip(CircleShape)
        )
    }
}

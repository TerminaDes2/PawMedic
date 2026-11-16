package com.pawsmedic.features.businesses.presentation.catalog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.pawsmedic.shared.core.designsystem.theme.PawMedicColors

@Composable
actual fun ProductImage(imageUrl: String?, size: Dp, modifier: Modifier) {
    Box(
        modifier = modifier.size(size).background(PawMedicColors.Gray50, RoundedCornerShape(14.dp)),
        contentAlignment = Alignment.Center
    ) {
        ProductImageFallback()
    }
}

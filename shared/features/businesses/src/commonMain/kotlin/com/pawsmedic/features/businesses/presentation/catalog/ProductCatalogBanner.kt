package com.pawsmedic.features.businesses.presentation.catalog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.pawsmedic.shared.core.designsystem.theme.PawMedicColors

@Composable
fun ProductCatalogBanner(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize().background(PawMedicColors.Teal600))
}

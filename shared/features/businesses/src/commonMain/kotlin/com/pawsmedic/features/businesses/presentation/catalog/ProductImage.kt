package com.pawsmedic.features.businesses.presentation.catalog

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp

@Composable
expect fun ProductImage(imageUrl: String?, size: Dp, modifier: Modifier = Modifier)
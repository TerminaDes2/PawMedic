package com.pawsmedic.shared.core.common

import androidx.compose.runtime.Composable

@Composable
expect fun rememberPhotoPicker(
    onPhotoPicked: (String) -> Unit
): () -> Unit

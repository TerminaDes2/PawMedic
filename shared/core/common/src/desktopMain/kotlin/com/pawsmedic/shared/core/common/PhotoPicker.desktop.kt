package com.pawsmedic.shared.core.common

import androidx.compose.runtime.Composable
import io.github.jan.supabase.SupabaseClient

@Composable
actual fun rememberPhotoPicker(
    supabaseClient: SupabaseClient,
    onPhotoPicked: (String) -> Unit,
    onUploadError: (String) -> Unit
): () -> Unit {
    return {
        // Desktop mock photo picker
    }
}

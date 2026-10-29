package com.pawsmedic.shared.core.common

import androidx.compose.runtime.Composable
import io.github.jan.supabase.SupabaseClient

@Composable
expect fun rememberPhotoPicker(
    supabaseClient: SupabaseClient,
    bucketName: String = "pets",
    folderName: String = "uploads",
    onPhotoPicked: (String) -> Unit,
    onUploadError: (String) -> Unit = {}
): () -> Unit

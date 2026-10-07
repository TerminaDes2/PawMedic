package com.pawsmedic.shared.core.common

import android.content.Context
import android.net.Uri
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID

// 1. Selector de imágenes reutilizable para cualquier pantalla de Compose
@Composable
fun rememberImagePicker(
    onImagePicked: (Uri?) -> Unit
): ManagedActivityResultLauncher<PickVisualMediaRequest, Uri?> {
    return rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> onImagePicked(uri) }
    )
}

// 2. Función genérica para subir cualquier imagen a Supabase
suspend fun uploadImageToSupabase(
    context: Context,
    supabaseClient: SupabaseClient,
    imageUri: Uri,
    bucketName: String = "pets",
    folderName: String = "uploads"
): String? {
    return withContext(Dispatchers.IO) {
        try {
            // Convertir el Uri de Android a un ByteArray para Supabase
            val bytes = context.contentResolver.openInputStream(imageUri)?.use { it.readBytes() }
                ?: return@withContext null

            // Generar un nombre único para evitar sobreescribir archivos
            val fileName = "$folderName/${UUID.randomUUID()}.jpg"

            // Subir al bucket de Supabase
            val bucket = supabaseClient.storage.from(bucketName)
            bucket.upload(fileName, bytes) {
                upsert = true
            }

            // Retornar la URL pública de la imagen
            bucket.publicUrl(fileName)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

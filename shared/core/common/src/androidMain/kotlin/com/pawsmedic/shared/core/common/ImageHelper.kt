package com.pawsmedic.shared.core.common

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.storage.storage
import io.ktor.http.ContentType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
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
): Result<String> {
    return withContext(Dispatchers.IO) {
        try {
            val bitmap = context.contentResolver.openInputStream(imageUri)?.use {
                BitmapFactory.decodeStream(it)
            }
            if (bitmap == null) {
                return@withContext Result.failure(
                    IllegalArgumentException("No se pudo leer la imagen seleccionada")
                )
            }
            val output = ByteArrayOutputStream()
            if (!bitmap.compress(Bitmap.CompressFormat.JPEG, 90, output)) {
                bitmap.recycle()
                return@withContext Result.failure(
                    IllegalArgumentException("La imagen seleccionada está vacía")
                )
            }
            bitmap.recycle()
            val bytes = output.toByteArray()
            val fileName = "$folderName/${UUID.randomUUID()}.jpg"

            val bucket = supabaseClient.storage.from(bucketName)
            bucket.upload(fileName, bytes) {
                upsert = false
                contentType = ContentType.Image.JPEG
            }

            Result.success(bucket.publicUrl(fileName))
        } catch (e: Exception) {
            Result.failure(
                IllegalStateException(
                    "No se pudo subir la imagen. Verifica que exista el bucket \"$bucketName\" en Supabase.",
                    e
                )
            )
        }
    }
}

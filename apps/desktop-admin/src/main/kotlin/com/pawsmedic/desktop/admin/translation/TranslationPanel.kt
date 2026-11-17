package com.pawsmedic.desktop.admin.translation

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

@Composable
fun TranslationPanel(original: String) {
    if (original.isBlank()) return
    var translated by remember(original) { mutableStateOf<String?>(null) }
    var failed by remember(original) { mutableStateOf(false) }
    var busy by remember(original) { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    Column {
        TextButton(enabled = !busy, onClick = {
            scope.launch {
                busy = true
                failed = false
                try {
                    translated = translateLocally(original)
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    failed = true
                } finally {
                    busy = false
                }
            }
        }) { Text(if (busy) "Translating…" else "Translate locally") }
        translated?.let {
            Text("Automatic translation may be inaccurate; check the original. Do not rely on it for clinical decisions.", fontSize = 11.sp)
            Text(it)
        }
        if (failed) {
            Text("Translation failed. Check the local engine and model; text must be at most 4,000 characters.",
                color = MaterialTheme.colorScheme.error, fontSize = 11.sp)
        }
    }
}

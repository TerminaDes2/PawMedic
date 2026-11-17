package com.pawsmedic.desktop.veterinary.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.pawsmedic.desktop.admin.ui.LocalDesktopAppearance
import com.pawsmedic.desktop.veterinary.ui.theme.*

internal enum class LegalSection { TERMS, PRIVACY }

internal data class LegalDocument(
    val introduction: List<String>,
    val terms: List<String>,
    val privacy: List<String>
)

internal fun parseLegalDocument(source: String): LegalDocument {
    val lines = source.lines()
    val terms = lines.indexOfFirst { it.startsWith("## PARTE I.") }
    val privacy = lines.indexOfFirst { it.startsWith("## PARTE II.") }
    require(terms >= 0 && privacy > terms && lines.drop(privacy + 1).any { it.isNotBlank() }) {
        "El documento legal no contiene las secciones esperadas."
    }
    return LegalDocument(lines.take(terms), lines.subList(terms, privacy), lines.drop(privacy))
}

internal fun loadLegalDocument(): LegalDocument {
    val stream = object {}.javaClass.getResourceAsStream("/legal/terminos-y-privacidad.md")
        ?: error("No se encontró el documento legal.")
    return stream.bufferedReader(Charsets.UTF_8).use { parseLegalDocument(it.readText()) }
}

@Composable
internal fun LegalDocumentDialog(initialSection: LegalSection, onDismiss: () -> Unit) {
    val english = LocalDesktopAppearance.current.english
    var section by remember(initialSection) { mutableStateOf(initialSection) }
    var attempt by remember { mutableIntStateOf(0) }
    val document = remember(attempt) { runCatching { loadLegalDocument() } }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.widthIn(max = 860.dp).fillMaxWidth().fillMaxHeight(0.9f),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = CardWhite),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(Modifier.fillMaxSize()) {
                Row(
                    Modifier.fillMaxWidth().background(OffWhiteBg).padding(horizontal = 24.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        Modifier.size(44.dp).background(EmeraldLightBg, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Description, contentDescription = null, tint = ClinicalEmerald)
                    }
                    Column(Modifier.weight(1f)) {
                        Text(if (english) "Terms and privacy" else "Términos y privacidad", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text(if (english) "PawsMedic · Version 1.0" else "PawsMedic · Versión 1.0", fontSize = 12.sp, color = TextSecondary)
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = if (english) "Close" else "Cerrar", tint = TextSecondary)
                    }
                }
                Row(Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = section == LegalSection.TERMS,
                        onClick = { section = LegalSection.TERMS },
                        label = { Text(if (english) "Terms of service" else "Términos de servicio") }
                    )
                    FilterChip(
                        selected = section == LegalSection.PRIVACY,
                        onClick = { section = LegalSection.PRIVACY },
                        label = { Text(if (english) "Privacy policy" else "Política de privacidad") }
                    )
                }
                HorizontalDivider(color = BorderLight)
                document.fold(
                    onSuccess = { legal ->
                        key(section) {
                            Column(
                                Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 28.dp, vertical = 20.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    if (english) "Original document in Spanish · Pending legal and technical review"
                                    else "Documento original en español · Pendiente de revisión legal y técnica",
                                    modifier = Modifier.fillMaxWidth().background(EmeraldLightBg, RoundedCornerShape(8.dp))
                                        .border(1.dp, BorderLight, RoundedCornerShape(8.dp)).padding(12.dp),
                                    color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Medium
                                )
                                val lines = legal.introduction + if (section == LegalSection.TERMS) legal.terms else legal.privacy
                                lines.forEach { LegalDocumentLine(it) }
                            }
                        }
                    },
                    onFailure = {
                        Column(
                            Modifier.weight(1f).fillMaxWidth().padding(28.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                if (english) "Could not load the document. Please try again."
                                else "No se pudo cargar el documento. Inténtalo de nuevo.",
                                color = MaterialTheme.colorScheme.error
                            )
                            OutlinedButton(onClick = { attempt++ }) { Text(if (english) "Retry" else "Reintentar") }
                        }
                    }
                )
                HorizontalDivider(color = BorderLight)
                Row(Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 12.dp), horizontalArrangement = Arrangement.End) {
                    Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = ClinicalEmerald)) {
                        Text(if (english) "Close" else "Cerrar", color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
private fun LegalDocumentLine(line: String) {
    val trimmed = line.trim()
    if (trimmed.isEmpty()) {
        Spacer(Modifier.height(3.dp))
        return
    }
    if (trimmed == "---") {
        HorizontalDivider(color = BorderLight, modifier = Modifier.padding(vertical = 6.dp))
        return
    }
    val heading = trimmed.takeWhile { it == '#' }.length
    val isQuote = trimmed.startsWith("> ")
    val isBullet = trimmed.startsWith("* ")
    val content = when {
        heading > 0 -> trimmed.drop(heading).trimStart()
        isQuote || isBullet -> trimmed.drop(2)
        else -> trimmed
    }
    val formatted = buildAnnotatedString {
        if (isBullet) append("•  ")
        var index = 0
        val pattern = Regex("(\\*\\*[^*]+\\*\\*|`[^`]+`)")
        pattern.findAll(content).forEach { match ->
            append(content.substring(index, match.range.first))
            val bold = match.value.startsWith("**")
            withStyle(SpanStyle(fontWeight = if (bold) FontWeight.Bold else FontWeight.Medium)) {
                append(if (bold) match.value.drop(2).dropLast(2) else match.value.drop(1).dropLast(1))
            }
            index = match.range.last + 1
        }
        append(content.substring(index))
    }
    Text(
        text = formatted,
        modifier = if (isQuote) Modifier.fillMaxWidth().background(OffWhiteBg, RoundedCornerShape(8.dp)).padding(12.dp)
            else Modifier.fillMaxWidth(),
        color = if (heading > 0) TextPrimary else TextSecondary,
        fontSize = when (heading) { 1 -> 19.sp; 2 -> 17.sp; 3 -> 15.sp; else -> 13.sp },
        fontWeight = if (heading > 0) FontWeight.Bold else FontWeight.Normal,
        lineHeight = 20.sp
    )
}

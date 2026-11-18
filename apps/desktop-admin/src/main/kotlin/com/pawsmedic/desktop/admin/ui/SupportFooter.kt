package com.pawsmedic.desktop.admin.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SupportFooter(
    modifier: Modifier = Modifier,
    labelColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    contactColor: Color = MaterialTheme.colorScheme.primary
) {
    val clipboard = LocalClipboardManager.current
    val english = LocalDesktopAppearance.current.english
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Text(
            if (english) "Support · click to copy" else "Soporte · clic para copiar",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = labelColor
        )
        listOf("3141747346", "sendersupabasecode@gmail.com").forEach { contact ->
            Text(
                text = contact,
                modifier = Modifier.clickable(
                    role = Role.Button,
                    onClickLabel = if (english) "Copy $contact" else "Copiar $contact"
                ) { clipboard.setText(AnnotatedString(contact)) },
                fontSize = 11.sp,
                color = contactColor
            )
        }
    }
}

package com.pawsmedic.features.auth.presentation.terms

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pawsmedic.shared.core.designsystem.language.AppLanguage
import com.pawsmedic.shared.core.designsystem.language.LocalLanguagePreference
import com.pawsmedic.shared.core.designsystem.theme.PawMedicSemanticColors

private const val TERMS_AND_CONDITIONS = """
PawsMedic: Términos y Condiciones de Uso y Política de Privacidad

PARTE I. TÉRMINOS Y CONDICIONES DE USO

1. Aceptación de los términos
Estos términos regulan el acceso y uso de PawMedic. Al crear una cuenta, marcar la casilla de aceptación o usar la plataforma, la persona usuaria declara que leyó, entendió y acepta estos términos y el Aviso de Privacidad. Actualmente, esta aceptación se valida únicamente durante el registro en la aplicación y no existe una tabla de consentimiento persistente o auditable.

2. Servicio
PawMedic conecta a usuarios con negocios veterinarios y permite gestionar mascotas, citas, expedientes clínicos, catálogos informativos y reportes operativos. PawMedic no presta servicios veterinarios ni emite diagnósticos; cada negocio es responsable de sus diagnósticos, tratamientos, precios, horarios y servicios.

3. Cuenta y registro
La persona usuaria debe proporcionar información verdadera y mantener seguras sus credenciales. La cuenta es personal e intransferible. Los menores pueden usar la plataforma con conocimiento y supervisión de su madre, padre o tutor.

4. Reglas de uso
La plataforma debe utilizarse únicamente para fines lícitos. Está prohibido acceder a datos de otras cuentas o negocios, manipular roles o identificadores, realizar ataques o ingeniería inversa, cargar malware o contenido ilícito y usar los datos para fines distintos a la atención veterinaria o la gestión de citas.

5. Citas y expedientes
Las citas se gestionan conforme a los estados y validaciones disponibles en la plataforma. Solo el negocio autorizado puede registrar consultas clínicas. Las correcciones de expedientes deben conservar el contenido original y su auditoría.

6. Propiedad intelectual y disponibilidad
El software, diseño, marca y documentación pertenecen a sus titulares. PawMedic se ofrece según disponibilidad y pueden ocurrir interrupciones por mantenimiento, conectividad o proveedores externos.

PARTE II. AVISO DE PRIVACIDAD INTEGRAL

1. Responsable y datos recabados
PawMedic recaba los datos necesarios para la cuenta, autenticación, mascotas, citas, notificaciones, expedientes y seguridad. La contraseña se gestiona mediante el proveedor de autenticación y no se duplica en la base de datos.

2. Finalidades y uso compartido
Los datos se utilizan para operar cuentas, gestionar mascotas y citas, facilitar la atención veterinaria, mantener la seguridad y atender soporte. Se comparten solo con el negocio relacionado, el usuario responsable, proveedores tecnológicos necesarios y autoridades cuando exista obligación legal.

3. Seguridad y conservación
Se aplican autenticación, HTTPS/TLS, aislamiento por tenant, autorización por rol y controles de almacenamiento y auditoría según las funciones disponibles. Las cuentas se conservan mientras estén activas y los registros se administran conforme a las políticas operativas.

4. Derechos ARCO y consentimiento
La persona usuaria puede solicitar acceso, rectificación, cancelación u oposición mediante los canales de privacidad disponibles en la aplicación. Al marcar la casilla confirma que leyó estos términos y consiente las finalidades necesarias.

Fecha de última actualización: 16 de noviembre de 2026 · Versión: 1.0
"""

enum class LegalSection {
    ALL,
    TERMS,
    PRIVACY
}

@Composable
fun TermsAndConditionsDialog(
    section: LegalSection = LegalSection.ALL,
    onDismiss: () -> Unit
) {
    val language = LocalLanguagePreference.current?.selected ?: AppLanguage.SPANISH
    val title = when (section) {
        LegalSection.TERMS -> language.text("Términos de servicio", "Terms of service")
        LegalSection.PRIVACY -> language.text("Política de privacidad", "Privacy policy")
        LegalSection.ALL -> language.text("Términos y privacidad", "Terms and privacy")
    }

    val text = when (section) {
        LegalSection.TERMS -> TERMS_AND_CONDITIONS.substringBefore("PARTE II.").trim()
        LegalSection.PRIVACY -> "PARTE II. " + TERMS_AND_CONDITIONS.substringAfter("PARTE II.").trim()
        LegalSection.ALL -> TERMS_AND_CONDITIONS.trim()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, color = PawMedicSemanticColors.current.text) },
        text = {
            Column(modifier = Modifier.heightIn(max = 480.dp).verticalScroll(rememberScrollState())) {
                Text(text, color = PawMedicSemanticColors.current.secondaryText, fontSize = 13.sp, lineHeight = 19.sp)
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(language.text("Cerrar", "Close"))
            }
        }
    )
}

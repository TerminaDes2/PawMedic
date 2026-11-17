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
import com.pawsmedic.shared.core.designsystem.theme.PawMedicColors

private const val TERMS_AND_CONDITIONS = """
PawsMedic: Términos y Condiciones de Uso y Política de Privacidad

PARTE I. TÉRMINOS Y CONDICIONES DE USO

1. Aceptación de los términos
Estos términos regulan el acceso y uso de PawsMedic. Al crear una cuenta, marcar la casilla de aceptación o usar la plataforma, la persona usuaria declara que leyó, entendió y acepta estos términos y el Aviso de Privacidad. La aceptación se registra electrónicamente con su versión, fecha y hora.

2. Servicio
PawsMedic conecta a usuarios con negocios veterinarios y permite gestionar mascotas, citas, expedientes clínicos, catálogos informativos y reportes operativos. PawsMedic no presta servicios veterinarios ni emite diagnósticos; cada negocio es responsable de sus diagnósticos, tratamientos, precios, horarios y servicios.

3. Cuenta y registro
La persona usuaria debe proporcionar información verdadera y mantener seguras sus credenciales. La cuenta es personal e intransferible. Los menores pueden usar la plataforma con conocimiento y supervisión de su madre, padre o tutor. PawsMedic no solicita fecha de nacimiento, identificación oficial ni datos de salud humana para abrir una cuenta.

4. Reglas de uso
La plataforma debe utilizarse únicamente para fines lícitos. Está prohibido acceder a datos de otras cuentas o negocios, manipular roles o identificadores, realizar ataques o ingeniería inversa, cargar malware o contenido ilícito, suplantar identidades y utilizar los datos para fines distintos a la atención veterinaria o la gestión de citas.

5. Citas y expedientes
Las citas se gestionan conforme a los estados, confirmaciones y plazos indicados en la plataforma. Los acuerdos sobre costos, pagos, reembolsos o inasistencias se celebran directamente entre el usuario y el negocio. Solo el negocio autorizado puede registrar consultas clínicas; una consulta guardada no se edita ni se elimina y sus correcciones se registran como adendas.

6. Propiedad intelectual y disponibilidad
El software, diseño, marca, logotipos, nombre PawsMedic y documentación pertenecen a sus titulares. Se concede una licencia limitada, personal, revocable, no exclusiva e intransferible. La plataforma se ofrece tal cual y según disponibilidad, por lo que pueden ocurrir interrupciones por mantenimiento, conectividad o proveedores externos.

7. Responsabilidad, terminación y contacto
PawsMedic puede restringir o suspender cuentas por incumplimiento, riesgo de seguridad, información falsa o requerimiento de autoridad. La persona usuaria puede solicitar el cierre de su cuenta mediante el canal de privacidad de la aplicación.

PARTE II. AVISO DE PRIVACIDAD INTEGRAL

1. Responsable y datos recabados
El Equipo 3 del proyecto académico PIGSV de la Facultad de Ingeniería Electromecánica de la Universidad de Colima es responsable del tratamiento. Se recaban los datos necesarios para la cuenta, autenticación, mascotas, citas, consentimientos, notificaciones, soporte y seguridad. La contraseña es gestionada por el proveedor de autenticación y no se duplica en la base de datos.

2. Finalidades y uso compartido
Los datos se utilizan para operar la cuenta, gestionar mascotas y citas, facilitar la atención veterinaria, mantener la seguridad, atender soporte y cumplir obligaciones legales. Se comparten solo con el negocio relacionado con la cita o atención, el usuario responsable, proveedores tecnológicos necesarios y autoridades cuando exista obligación legal. PawsMedic no vende ni renta datos personales.

3. Seguridad y conservación
Se aplican HTTPS/TLS, aislamiento por negocio, autorización por rol, autenticación multifactor para cuentas administrativas, almacenamiento privado y auditoría. Las cuentas activas se conservan mientras estén activas; las citas y expedientes se conservan solo durante los plazos operativos informados en el aviso.

4. Derechos ARCO y consentimiento
La persona usuaria puede acceder, rectificar, cancelar u oponerse al tratamiento de sus datos, así como revocar su consentimiento, mediante el canal de privacidad de la aplicación o escribiendo a PawsMedicVeterinaria@gmail.com. Al marcar la casilla, confirma que leyó estos términos y el Aviso de Privacidad y consiente las finalidades necesarias.

Fecha de última actualización: 16 de noviembre de 2026 · Versión: 1.0
"""

@Composable
fun TermsAndConditionsDialog(
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Términos y condiciones",
                color = PawMedicColors.Gray900
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .heightIn(max = 480.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = TERMS_AND_CONDITIONS.trimIndent(),
                    color = PawMedicColors.Gray700,
                    fontSize = 13.sp,
                    lineHeight = 19.sp
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cerrar", color = PawMedicColors.Teal600)
            }
        }
    )
}

# PawsMedic: Términos y Condiciones de Uso y Política de Privacidad

**Plataforma Integral de Gestión y Servicios Veterinarios (PIGSV)**
**Equipo 3 — Universidad de Colima, Facultad de Ingeniería Electromecánica**
**Versión:** 1.0
**Fecha de vigencia:** 16 de noviembre de 2026

> **Documento pendiente de revisión:** Se asumió al Equipo 3 como responsable del tratamiento y se usa el domicilio de la Facultad, por lo que conviene contar con la autorización de la Universidad de Colima. Este documento requiere revisión legal y técnica antes de su publicación: no todas sus afirmaciones se han verificado contra la implementación y no constituye asesoría legal.

---

## PARTE I. TÉRMINOS Y CONDICIONES DE USO

### 1. Aceptación de los términos
Estos Términos y Condiciones (los «Términos») regulan el acceso y uso de PawsMedic (la «Plataforma»), compuesta por una aplicación móvil Android para propietarios de mascotas, una aplicación de escritorio (Windows 11) para negocios veterinarios y un panel de administración global, todos sobre un backend común.

Al crear una cuenta, marcar la casilla de aceptación o usar la Plataforma, la persona usuaria declara que leyó, entendió y acepta estos Términos y el Aviso de Privacidad de la Parte II. Si no está de acuerdo, no debe usar la Plataforma. El formulario veterinario requiere marcar la casilla para continuar; actualmente no se ha verificado el registro persistente de la versión, fecha y hora de esa aceptación.

### 2. Definiciones
* **PawsMedic / Responsable:** Equipo 3, integrantes del proyecto académico PIGSV de la Facultad de Ingeniería Electromecánica de la Universidad de Colima, quien opera la Plataforma.
* **Usuario:** propietario o tutor de una mascota que usa la aplicación Android.
* **Negocio:** clínica, consultorio o establecimiento veterinario que usa la aplicación de escritorio una vez aprobado.
* **Superadmin:** personal autorizado de PawsMedic que supervisa la Plataforma.
* **Tenant:** espacio lógico independiente asignado a cada Negocio aprobado.
* **Cita:** solicitud de servicio veterinario gestionada en la Plataforma.
* **Expediente clínico:** registro estructurado de consultas de una mascota.

### 3. Naturaleza y alcance del servicio
**3.1.** PawsMedic es una herramienta tecnológica que conecta a Usuarios con Negocios veterinarios y permite gestionar mascotas, citas, expedientes clínicos, catálogos informativos y reportes operativos.

**3.2.** PawsMedic no presta servicios veterinarios. La atención médica la brinda exclusivamente cada Negocio, que es el único responsable de sus diagnósticos, tratamientos, precios, horarios y servicios. PawsMedic no emite diagnósticos, ni automatizados ni de ningún otro tipo, y no sustituye el criterio de un médico veterinario.

**3.3. Funciones fuera del alcance actual.** La Plataforma no procesa pagos, no permite comprar productos, no incluye carrito ni módulo financiero, no genera facturas y no cuenta con versión iOS. La aplicación veterinaria de escritorio permite exportar reportes operativos a PDF. El catálogo de productos es únicamente informativo; los precios y la disponibilidad mostrados son orientativos y los actualiza manualmente cada Negocio.

**3.4.** Para usar la Plataforma se requiere conexión a Internet, un dispositivo compatible (Android 8.0 o superior; Windows 11 de 64 bits) y la disponibilidad de proveedores externos (Supabase, Firebase Cloud Messaging).

### 4. Cuentas y registro
**4.1. Usuarios.** Pueden ser mayores o menores de edad, ya que la aplicación no procesa pagos y solo funciona como herramienta para propietarios de mascotas; los menores deben usarla con el conocimiento y la supervisión de su madre, padre o tutor. Deben proporcionar nombre, correo electrónico verificado y contraseña; el teléfono es opcional. PawsMedic no solicita fecha de nacimiento, identificación oficial ni datos de salud humana.

**4.2. Negocios.** Deben presentar una solicitud de incorporación con información veraz (nombre comercial, razón social si existe, contacto responsable, correo, teléfono, domicilio, descripción, servicios, horarios y, en su caso, documentos de respaldo). Ningún Negocio puede operar hasta ser aprobado por el Superadmin. Una solicitud RECHAZADA puede volver a presentarse con la misma cuenta.

**4.3. Veracidad y seguridad.** La persona usuaria garantiza que sus datos son verdaderos y actualizados, y es responsable de custodiar sus credenciales. Debe notificar de inmediato cualquier uso no autorizado. Cada cuenta es personal e intransferible.

**4.4. Superadmin.** Las cuentas de Superadmin son individuales, usan autenticación multifactor y son creadas y revocadas por el responsable técnico. No existe registro público de Superadmin.

### 5. Reglas de uso de la Plataforma
La persona usuaria se obliga a:
* usar la Plataforma solo para fines lícitos y conforme a estos Términos;
* no intentar acceder a datos de otras cuentas o de otros tenants, ni manipular identificadores, roles o el contexto de tenant;
* no realizar ingeniería inversa, ataques, pruebas de intrusión no autorizadas ni sobrecargar la infraestructura;
* no cargar archivos que contengan malware, contenido ilícito o que violen derechos de terceros;
* no suplantar la identidad de otra persona o negocio;
* no usar los datos obtenidos en la Plataforma para fines distintos a la atención veterinaria o la gestión de citas.

PawsMedic puede restringir o suspender cuentas que incumplan estas reglas.

### 6. Citas
**6.1. Solicitud y reserva.** Toda cita nueva inicia en estado `PENDIENTE` y reserva el horario desde que el backend la valida. Si dos solicitudes compiten por un horario solapado, solo gana la primera validada y las demás reciben el mensaje «Horario no disponible». No se permiten citas solapadas dentro del mismo Negocio.

**6.2. Aceptación por el Negocio.** El Negocio debe aceptar o rechazar la solicitud dentro de las 24 horas posteriores a su creación o antes de la hora de inicio, lo que ocurra primero. De lo contrario, la cita pasa a `EXPIRADA` y el horario se libera.

**6.3. Confirmación de asistencia.** Para citas creadas con 24 horas o más de anticipación, el Usuario debe confirmar su asistencia desde la aplicación en dos momentos: 24 horas y 1 hora antes. Si la cita se creó con menos de 24 horas pero al menos 1 hora de anticipación, solo se exige la confirmación de 1 hora. Si se creó con menos de 1 hora, no se exige confirmación del Usuario y solo queda concretada si el Negocio la acepta.

**6.4. Cancelación automática.** Si vence sin confirmación alguno de los hitos requeridos, la cita se cancela automáticamente, se libera el horario y se notifica a ambas partes. Esta cancelación no se revierte; el Usuario deberá solicitar una nueva cita. Las notificaciones push son un canal adicional: su fallo no amplía los plazos. Las notificaciones internas de la aplicación son el registro confiable de los avisos.

**6.5. Sin cancelación manual.** En esta versión no existe cancelación ni reprogramación manual de citas. Las únicas cancelaciones son las automáticas por falta de confirmación, por suspensión del Negocio o por cierre de la cuenta del Usuario.

**6.6. Estados finales.** Las citas `RECHAZADA`, `CANCELADA`, `COMPLETADA` y `EXPIRADA` son estados finales y no se reactivan.

**6.7. Suspensión de un Negocio.** Si un Negocio es suspendido, todas sus citas `PENDIENTE` o `ACEPTADA` se cancelan, se liberan los horarios y se notifica a los Usuarios. La reactivación del Negocio no reactiva esas citas.

**6.8.** Cualquier acuerdo sobre costos, pagos, reembolsos o responsabilidades por inasistencia se celebra directamente entre el Usuario y el Negocio, fuera de la Plataforma.

### 7. Expedientes clínicos
**7.1.** Solo el Negocio autorizado, con relación de atención sobre la mascota dentro de su propio tenant, puede registrar consultas clínicas. Los campos obligatorios son motivo, hallazgos, evaluación y plan.

**7.2. Inmutabilidad.** Una consulta guardada no se edita ni se elimina. Las correcciones se registran como addenda vinculados, con motivo obligatorio, autor y fecha y hora, preservando el contenido original y su auditoría.

**7.3. Adjuntos.** Se admite una fotografía de receta en formato JPEG o PNG de hasta 10 MiB. Se verifica el tipo real del archivo, se eliminan los metadatos EXIF y se almacena en un espacio privado con enlaces de descarga firmados de vigencia máxima de 5 minutos.

**7.4. Acceso del Usuario.** El Usuario puede consultar el historial clínico autorizado de sus mascotas en modo de solo lectura y no puede modificarlo. Tras el cierre de su cuenta ya no podrá ver su historial.

**7.5.** El Superadmin no tiene acceso a expedientes clínicos, citas individuales ni datos identificables de Usuarios o mascotas.

**7.6.** El Negocio es responsable de la veracidad, exactitud y confidencialidad de la información clínica que registra, así como del cumplimiento de la normativa profesional y sanitaria aplicable.

### 8. Mascotas
El Usuario solo puede registrar y administrar mascotas propias o bajo su legítima responsabilidad. No es posible eliminar una mascota con citas o expedientes asociados; en ese caso la Plataforma informará el motivo.

### 9. Obligaciones específicas de los Negocios
Cada Negocio se obliga a:
* proporcionar información veraz en su solicitud y mantenerla actualizada;
* contar con las licencias, avisos y autorizaciones que la ley exija para operar como establecimiento veterinario;
* atender las citas aceptadas y mantener actualizados servicios, horarios y bloqueos;
* tratar los datos personales de los Usuarios únicamente para la atención veterinaria y la gestión de citas, y cumplir con la LFPDPPP en su carácter de responsable de los datos que trate para sus propios fines;
* no intentar acceder a datos de otros tenants ni compartir sus credenciales;
* aceptar que PawsMedic puede suspender el tenant, con las consecuencias del apartado 6.7.

### 10. Funciones del Superadmin, aprobación y suspensión
**10.1.** PawsMedic puede aprobar, rechazar, suspender o reactivar Negocios, registrando la decisión, el actor, la fecha y el motivo en una bitácora de auditoría.

**10.2.** Mientras la solicitud está pendiente, el Superadmin puede revisar los datos y documentos de alta para decidir. Tras la decisión, solo accede a indicadores agregados (conteos de citas por periodo, estado, negocio y servicio; solicitudes por estado; negocios activos y suspendidos) y al historial mínimo de decisiones.

**10.3.** Un Negocio suspendido o pendiente solo puede ver información de su estado y no realiza operaciones normales.

### 11. Propiedad intelectual
**11.1.** El software, diseño, marca, logotipos, nombre «PawsMedic» y documentación de la Plataforma pertenecen a sus titulares, el Equipo 3 (el nombre «PawsMedic» no cuenta con registro de marca a la fecha). Se otorga a la persona usuaria una licencia limitada, personal, revocable, no exclusiva e intransferible para usar la Plataforma conforme a estos Términos.

**11.2.** La información, imágenes y contenidos que el Usuario o el Negocio cargan siguen siendo de su titular. Al cargarlos, otorgan a PawsMedic únicamente la licencia necesaria para almacenarlos, procesarlos y mostrarlos con el fin de prestar el servicio.

**11.3.** Están prohibidas la copia, distribución, descompilación o explotación comercial de la Plataforma sin autorización por escrito.

### 12. Disponibilidad y exclusión de garantías
**12.1.** La Plataforma se ofrece «tal cual» y según disponibilidad, como un producto mínimo viable. Pueden ocurrir interrupciones por mantenimiento, fallas de conectividad o de proveedores externos (Supabase, FCM, entre otros). El sistema no informa como exitosa una operación crítica sin la confirmación del backend.

**12.2.** PawsMedic no garantiza que la Plataforma esté libre de errores ni que sus resultados satisfagan necesidades específicas. El sistema se validó en entornos de prueba y no ha sido evaluado en clínicas reales en operación.

### 13. Limitación de responsabilidad
En la máxima medida permitida por la ley, PawsMedic no será responsable por:
* diagnósticos, tratamientos, resultados clínicos o conducta profesional de los Negocios;
* inasistencias, cancelaciones automáticas, expiraciones o retrasos de notificaciones, incluidas fallas del canal push;
* información incorrecta capturada por Usuarios o Negocios;
* pérdidas derivadas de fallas de Internet, del dispositivo o de proveedores externos;
* acuerdos económicos entre Usuarios y Negocios;
* uso indebido de credenciales por parte del titular.

Estas limitaciones no excluyen la responsabilidad que no pueda limitarse conforme a la legislación aplicable.

### 14. Terminación y suspensión
**14.1.** El Usuario puede solicitar el cierre de su cuenta mediante el canal de privacidad de la aplicación. Las consecuencias se describen en la sección 9 de la Parte II.

**14.2.** PawsMedic puede suspender o cancelar cuentas, de forma temporal o definitiva, por incumplimiento de estos Términos, riesgo de seguridad, información falsa o requerimiento de autoridad competente.

### 15. Modificaciones
PawsMedic puede modificar estos Términos. La notificación de cambios y el registro persistente de nuevas aceptaciones requieren revisión e implementación antes de ofrecerse como funciones disponibles. Las funciones fuera del alcance actual podrán incorporarse mediante un proceso formal de control de cambios.

### 16. Legislación aplicable y jurisdicción
Estos Términos se rigen por las leyes de los Estados Unidos Mexicanos. Para su interpretación y cumplimiento, las partes se someten a los tribunales competentes de Manzanillo, Colima, México, renunciando a cualquier otro fuero que pudiera corresponderles por su domicilio presente o futuro.

### 17. Contacto
Para dudas sobre estos Términos: `PawsMedicVeterinaria@gmail.com`.
**Domicilio:** Facultad de Ingeniería Electromecánica, Universidad de Colima, Carretera Manzanillo-Cihuatlán km 20, El Naranjo, 28860 Manzanillo, Colima, México.

---

## PARTE II. AVISO DE PRIVACIDAD INTEGRAL Y POLÍTICA DE PRIVACIDAD

### 1. Identidad y domicilio del responsable
El Equipo 3 del proyecto académico PIGSV (Facultad de Ingeniería Electromecánica, Universidad de Colima), con domicilio en Carretera Manzanillo-Cihuatlán km 20, El Naranjo, 28860 Manzanillo, Colima, México, es responsable del tratamiento de los datos personales recabados a través de PawsMedic, conforme a la Ley Federal de Protección de Datos Personales en Posesión de los Particulares (LFPDPPP, publicada en el Diario Oficial de la Federación el 20 de marzo de 2025) y su normativa aplicable.

**Contacto de privacidad:** `PawsMedicVeterinaria@gmail.com`. También puede presentar solicitudes desde la sección de Privacidad de la aplicación, que entrega un acuse y folio de seguimiento.

> **Aviso breve.** PawsMedic recaba solo los datos necesarios para operar su cuenta, agendar citas, facilitar la atención veterinaria, garantizar la seguridad y brindar soporte. No pedimos datos de salud humana. Los datos de su mascota y de su atención solo los ven usted y el Negocio relacionado con su cita. Puede ejercer sus derechos ARCO en todo momento.

### 2. Datos personales que recabamos
**Usuarios (propietarios):**
* Nombre, correo electrónico verificado y contraseña (esta última se gestiona únicamente por el proveedor de autenticación y no se duplica en nuestra base de datos); teléfono, de forma opcional.
* Datos de sus mascotas: nombre, especie, raza y demás datos requeridos para su registro.
* Datos de citas: negocio, servicio, fecha y hora, motivo opcional, estado y confirmaciones de asistencia.
* El formulario veterinario solicita marcar una casilla de aceptación. El registro persistente de versión, fecha y hora de aceptación está pendiente de verificación.
* Datos técnicos: identificadores de sesión, token del dispositivo para notificaciones push, y registros de errores y disponibilidad.

**Negocios:**
* Nombre comercial, razón social, nombre del contacto responsable, correo, teléfono, domicilio y ubicación, descripción, servicios, horarios, logotipo o fotografía y documentos de respaldo.
* Datos de operación: servicios, bloqueos de horario, fichas de producto y citas de su tenant.

**Expedientes clínicos de mascotas (capturados por el Negocio):**
* Motivo de consulta, hallazgos, evaluación, plan, adendas de corrección y, opcionalmente, fotografía de receta (sin metadatos EXIF).

PawsMedic no solicita fecha de nacimiento, identificación oficial ni datos de salud humana para abrir una cuenta. El expediente de la mascota puede contener datos del propietario solo en la medida necesaria para la atención.

### 3. Finalidades del tratamiento
**Finalidades necesarias (sin ellas no es posible el servicio):**
1. Crear y administrar su cuenta, autenticarlo y restaurar su sesión.
2. Registrar y administrar sus mascotas.
3. Mostrarle negocios, servicios, productos y disponibilidad.
4. Solicitar, gestionar, confirmar, expirar y cancelar citas, y enviarle notificaciones sobre ellas.
5. Permitir al Negocio registrar y consultar expedientes clínicos y permitirle a usted consultarlos en modo de solo lectura.
6. Evaluar, aprobar, rechazar, suspender o reactivar Negocios.
7. Garantizar la seguridad, el aislamiento entre negocios, la auditoría y la prevención de fraudes y accesos no autorizados.
8. Atender solicitudes de soporte y de ejercicio de derechos de privacidad.
9. Generar indicadores agregados y reportes operativos sin datos identificables.
10. Cumplir obligaciones legales y atender requerimientos de autoridad competente.

**Finalidades opcionales:**
* Ninguna: PawsMedic no usa sus datos para mercadotecnia, publicidad ni perfilamiento.

En caso de incorporarse en el futuro, las finalidades opcionales requieren su consentimiento por separado y están desactivadas por defecto. Negarse a ellas no afecta el uso de la Plataforma, y puede retirar su consentimiento en cualquier momento.

### 4. Con quién compartimos sus datos
* **Negocio veterinario relacionado con su cita o atención:** recibe los datos del Usuario, de la mascota y de la cita necesarios para prestar el servicio. Cada Negocio solo accede a los datos de su propio tenant.
* **Usuario responsable:** consulta únicamente los datos de sus propias mascotas y citas.
* **Superadmin de PawsMedic:** durante la revisión de una solicitud de alta ve los datos y documentos del Negocio. Después, solo accede a indicadores agregados y al historial mínimo de decisiones. No accede a expedientes clínicos, citas individuales ni datos identificables de Usuarios o mascotas.
* **Proveedores tecnológicos (encargados del tratamiento):**
  * **Supabase:** autenticación, base de datos PostgreSQL, almacenamiento de archivos y funciones en la nube.
  * **Google Firebase Cloud Messaging (FCM):** envío de notificaciones push en Android.
  * **Argos Translate:** traducción local en las funciones de escritorio que la tienen integrada; estos textos no se envían a Microsoft Azure para traducirse.
* **Autoridades:** cuando exista un mandato legal o resolución judicial.

PawsMedic no vende ni renta sus datos personales. Algunos proveedores pueden almacenar o procesar información en servidores ubicados fuera de México; en ese caso se exige un nivel de protección adecuado conforme a la ley. Al aceptar este aviso, usted consiente estas transferencias y remisiones que son necesarias para prestar el servicio.

### 5. Medidas de seguridad
PawsMedic aplica medidas administrativas, técnicas y físicas razonables, entre ellas:
* Toda la comunicación entre las aplicaciones y el backend usa $HTTPS/TLS$.
* Aislamiento por tenant mediante políticas de seguridad a nivel de fila (RLS) en la base de datos. El tenant y el rol se derivan de la sesión autenticada y nunca son aceptados como autoridad desde el cliente.
* Autorización por rol, estado y propiedad, validada siempre en el backend.
* Cuentas de Superadmin individuales con autenticación multifactor; ningún secreto privilegiado se distribuye en las aplicaciones.
* Archivos clínicos en un bucket privado, con verificación del tipo real, eliminación de EXIF y enlaces firmados de vigencia máxima de 5 minutos.
* Recuperación de contraseña mediante enlace de un solo uso que expira en 1 hora.
* Auditoría de acciones administrativas y de creación o corrección de registros clínicos, sin duplicar valores clínicos en los registros técnicos.
* Monitoreo básico de errores y disponibilidad.

Ningún sistema es completamente invulnerable. En caso de una vulneración que afecte de forma significativa sus derechos, se le notificará oportunamente.

### 6. Derechos ARCO y revocación del consentimiento
Usted tiene derecho a **Acceder** a sus datos, **Rectificarlos**, **Cancelarlos** u **Oponerse** a su tratamiento, así como a limitar su uso o divulgación y revocar su consentimiento.

Para ejercerlos, use el canal de privacidad dentro de la aplicación o escriba a `PawsMedicVeterinaria@gmail.com`, indicando su nombre, un medio para recibir respuesta, la descripción clara de lo que solicita y los elementos que permitan localizar sus datos. Recibirá un acuse con folio de seguimiento. La respuesta se dará en un plazo máximo de 20 días hábiles contados desde la recepción de su solicitud; si procede, se hará efectiva dentro de los 15 días hábiles siguientes a la comunicación de la respuesta.

Tenga en cuenta que los registros clínicos son inmutables: la rectificación se realiza mediante una adenda que conserva el valor original y su auditoría, y la cancelación puede estar sujeta a los plazos de conservación del apartado 8 y a obligaciones legales.

### 7. Datos de mascotas y expedientes
Los datos de la mascota y de su atención se comparten únicamente con el Usuario responsable y el Negocio relacionado con la cita o atención, según permisos. El Usuario ve solo los campos y adjuntos que se determinen como compartibles. Una consulta clínica guardada no puede sobrescribirse ni eliminarse; las correcciones se agregan como adendas con motivo, autor y fecha y hora.

### 8. Conservación de los datos
* **Cuentas activas:** sus datos se conservan mientras la cuenta esté activa.
* **Cierre de cuenta:** tras una solicitud verificada por el canal de privacidad, se cancelan las citas futuras `PENDIENTE` o `ACEPTADA` (liberando el horario y notificando a ambas partes), la cuenta se deshabilita en un máximo de 24 horas y los datos de perfil no necesarios se eliminan o disocian dentro de 30 días.
* **Citas y expedientes:** se conservan solo los campos necesarios durante 5 años desde la última atención; después se eliminan o disocian, salvo obligación legal o controversia vigente. Las referencias de una cuenta cerrada se desvinculan del acceso y usan un identificador aleatorio para preservar la integridad histórica.
* **Registros de auditoría:** 5 años.
* **Copias de respaldo:** expiran en un máximo adicional de 90 días.

Cada proceso de eliminación o disociación se registra (qué conjunto, cuándo y resultado). Estos plazos son operativos del producto y no constituyen una declaración de plazo legal obligatorio.

### 9. Cierre de cuenta
Al cerrar su cuenta de Usuario no podrá iniciar sesión ni consultar su historial. Las citas futuras se cancelan y no se reactivan. Los datos se tratarán conforme al apartado 8.

### 10. Uso de tecnologías en la aplicación
Las aplicaciones usan identificadores de sesión y un token del dispositivo para entregar notificaciones push. No se emplean para publicidad de terceros. Puede deshabilitar las notificaciones push desde la configuración de su dispositivo; las notificaciones internas de la aplicación seguirán disponibles como registro confiable.

### 11. Menores de edad
PawsMedic puede ser utilizado por menores de edad en su calidad de propietarios de mascotas, ya que la aplicación no procesa pagos. Los menores deben usarla con el conocimiento y la supervisión de su madre, padre o tutor, y solo se les solicitan los datos estrictamente necesarios. Su madre, padre o tutor puede ejercer los derechos ARCO en su nombre. Si detectamos datos de menores recabados sin autorización, serán eliminados.

### 12. Cambios al Aviso de Privacidad
Cualquier modificación requiere revisión antes de su aplicación. La notificación de cambios y el registro persistente de la versión, fecha y hora de aceptación están pendientes de revisión e implementación.

### 13. Autoridad en materia de protección de datos
Si considera que su derecho a la protección de datos personales ha sido vulnerado, puede acudir ante la autoridad competente en México en materia de protección de datos personales en posesión de particulares, actualmente la Secretaría Anticorrupción y Buen Gobierno (SABG), que asumió las funciones del extinto INAI.

### 14. Consentimiento
Al marcar la casilla de aceptación en la aplicación, usted confirma que leyó este Aviso de Privacidad y los Términos y Condiciones, y consiente el tratamiento de sus datos para las finalidades necesarias aquí descritas. Los consentimientos opcionales se otorgan por separado.

---

**Fecha de última actualización:** 16 de noviembre de 2026
**Versión:** 1.0

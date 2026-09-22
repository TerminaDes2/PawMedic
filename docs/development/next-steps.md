# Próximos pasos de PawsMedic

Este documento define el incremento recomendado después del scaffolding inicial.
El orden prioriza seguridad, autenticación y los flujos necesarios para habilitar
la operación multi-tenant antes de ampliar las capacidades de producto.

## 1. Conectar Supabase Auth real

- Configurar el cliente Supabase para Android y Desktop usando variables de
  entorno o configuración segura por plataforma.
- Implementar registro, inicio de sesión, recuperación de contraseña y cierre
  de sesión.
- Persistir y restaurar la sesión de forma segura.
- Obtener el perfil desde `public.profiles` después de autenticar al usuario.
- Resolver el rol únicamente desde el perfil validado por backend.
- Redirigir cada sesión a la aplicación y ruta correspondiente:
  - `USER` -> aplicación Android.
  - `VETERINARY_BUSINESS` -> Desktop veterinario.
  - `SUPERADMIN` -> Desktop administrativo.
- Rechazar sesiones cuyo rol no corresponda a la aplicación actual.

## 2. Completar y probar Row Level Security

- Revisar cada política de las migraciones contra el modelo final de datos.
- Confirmar que ningún cliente pueda elegir un `tenant_id` para obtener
  autorización.
- Derivar el tenant desde el JWT, el perfil y la membresía aprobada.
- Agregar pruebas negativas con:
  - usuario A y usuario B;
  - tenant A y tenant B;
  - usuario final, negocio veterinario y superadministrador.
- Verificar acceso a mascotas, servicios, productos, citas, expedientes,
  consultas clínicas, auditoría y Storage.
- Probar que una modificación de rol o tenant invalida correctamente el acceso
  previo.

## 3. Implementar aprobación de negocio y creación de tenant

- Completar el flujo de solicitud de incorporación del negocio veterinario.
- Validar la identidad y el rol `SUPERADMIN` en la Edge Function.
- Ejecutar aprobación, creación de tenant, asociación del perfil y auditoría en
  una única operación transaccional.
- Definir estados explícitos para la solicitud y el tenant.
- Implementar rechazo, reactivación y suspensión con motivo y trazabilidad.
- Exponer al Desktop veterinario el estado actual de su solicitud.

## 4. Completar mascotas y servicios

### Mascotas

- Implementar alta, edición, consulta y eliminación lógica de mascotas.
- Validar que `owner_id` siempre sea el usuario autenticado.
- Agregar campos clínicos mínimos definidos por el modelo de datos.
- Implementar pruebas de propiedad y aislamiento entre usuarios.

### Servicios

- Implementar CRUD de servicios para el tenant autenticado.
- Permitir al usuario consultar únicamente servicios publicados.
- Validar precio, duración, estado y tenant en backend.
- Conectar la pantalla de publicación y edición del Desktop veterinario.

## 5. Implementar disponibilidad y citas

- Completar horarios semanales del negocio.
- Completar bloqueos de disponibilidad.
- Calcular slots disponibles exclusivamente en backend o mediante una operación
  validada por backend.
- Implementar solicitud, confirmación, cancelación y consulta de citas.
- Ejecutar la reserva crítica mediante Edge Function y RPC transaccional.
- Validar solapamientos, estado del servicio, pertenencia de la mascota y
  tenant derivado.
- Probar idempotencia y condiciones de carrera.

## 6. Añadir expedientes clínicos y autorización explícita

- Implementar expedientes clínicos y consultas clínicas dentro del tenant.
- Permitir al negocio registrar consultas únicamente para citas o relaciones
  autorizadas.
- Permitir al usuario consultar el historial únicamente de sus mascotas.
- Implementar autorización explícita para compartir información clínica.
- Incorporar adjuntos de receta con rutas de Storage verificadas por RLS.
- Registrar accesos y modificaciones sensibles en `audit_logs`.
- Añadir pruebas de lectura autorizada y denegación de acceso clínico.

## Criterios de finalización del siguiente incremento

El incremento se considerará listo cuando:

- las tres aplicaciones puedan autenticar y restaurar sesión;
- el routing por rol rechace aplicaciones incompatibles;
- la aprobación de un negocio cree tenant y asociación de perfil de forma
  transaccional;
- dos tenants no puedan leer ni modificar datos operativos entre sí;
- un usuario solo pueda administrar sus propias mascotas;
- la reserva de citas sea transaccional e idempotente;
- el historial clínico requiera autorización verificable;
- las pruebas unitarias, de integración y RLS estén documentadas y ejecutadas
  en el entorno local de Supabase;
- no existan secretos en el repositorio.

## Dependencias de implementación

1. Supabase Auth real debe estar disponible antes de validar el routing final.
2. El modelo de perfiles, roles y tenants debe estabilizarse antes de cerrar
   las políticas RLS.
3. Las políticas RLS deben estar probadas antes de exponer operaciones
   multi-tenant desde las aplicaciones.
4. Disponibilidad debe estar definida antes de implementar reservas.
5. La autorización clínica debe existir antes de publicar expedientes o
   adjuntos.

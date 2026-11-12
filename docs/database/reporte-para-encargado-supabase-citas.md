# Reporte para el encargado de Supabase: compatibilidad del agendamiento

**Proyecto:** PawMedic  
**Fecha:** 3 de noviembre de 2026  
**Entorno observado:** proyecto Supabase compartido, rama `main` / `PRODUCTION`  
**Estado:** cambio preparado en el repositorio; no aplicado a Supabase

## Objetivo

Habilitar el flujo de agendamiento de PawMedic para consultar horarios
disponibles, reservar una cita con la mascota y servicio seleccionados, y hacer
que la cita aparezca en la agenda de la clínica. La implementación debe respetar
el esquema que ya existe y mantener operativas las funciones antiguas que
puedan usar clientes anteriores.

## Lo confirmado en el esquema actual

Las consultas de solo lectura ejecutadas en Supabase mostraron:

- `appointments` almacena `tenant_id`, `pet_id`, `service_id`, `owner_id`,
  `booked_by`, `starts_at`, `ends_at`, `status`, `idempotency_key` y
  `created_at`. Los tiempos son `timestamptz`.
- `availability_blocks` almacena intervalos `starts_at`/`ends_at`.
- `business_schedules` usa `day_of_week`, `open_time`, `close_time` e
  `is_active`.
- `services` proporciona `duration_minutes` y `active`.
- El enum `appointment_status` observado contiene `REQUESTED`, `CONFIRMED`,
  `CANCELLED`, `COMPLETED` y `REJECTED`.
- La función actual `book_appointment(...)` intenta usar `appointments.fecha`,
  `appointments.hora`, `appointments.estado`, `availability_blocks.fecha_inicio`
  y `availability_blocks.fecha_fin`. Esas columnas no corresponden a las
  columnas confirmadas. Por eso esta función no es compatible con el esquema
  actual.
- `get_working_shifts(p_tenant_id, p_day_of_week)` sí corresponde a las
  columnas actuales y devuelve los turnos activos.
- No aparecieron en el listado consultado las funciones actuales que espera la
  app: `get_available_slots`, `create_critical_booking`,
  `get_owned_booking_pets`, `get_owned_appointments`,
  `get_tenant_appointments` y `transition_appointment_status`.

## Cambio preparado

El archivo preparado para revisión es:

`supabase/migrations/20261103000000_booking_api_compatibility.sql`

El script:

- Añade `tenants.time_zone` y `appointments.owner_id` solo si no existen.
- Reemplaza la implementación de `book_appointment` conservando su firma, para
  que clientes que aún la llamen puedan crear una cita usando las columnas de
  intervalos actuales.
- Conserva `get_working_shifts` y lo adapta a las columnas confirmadas.
- Define la consulta de horarios disponibles, excluyendo intervalos bloqueados
  y citas `REQUESTED` o `CONFIRMED`.
- Define una reserva transaccional que vuelve a validar el horario, la duración
  del servicio y la pertenencia de la mascota al usuario autenticado; también
  usa una clave de idempotencia.
- Define las funciones de mascotas, citas del cliente y agenda de clínica que
  consume la app.
- Define transiciones de estado para que la clínica pueda confirmar, cancelar o
  completar una cita, validando su pertenencia a la clínica.
- No contiene `DROP TABLE`, `DROP COLUMN`, `DELETE` ni `TRUNCATE`; no está
  diseñado para borrar citas existentes.

Las reservas nuevas quedan como `REQUESTED`, que es el estado existente para
una cita pendiente de confirmación. La aplicación presenta ese estado como
“Pendiente de confirmación”. No se propone cambiar el enum ni añadir columnas
para los flujos futuros de asistencia, inasistencia, modificación o
notificación.

## Revisión y aplicación solicitadas

Como el proyecto observado está en producción y la persona que hizo las
consultas no es responsable de Supabase ni confirmó que exista un respaldo, no
se ha ejecutado ningún cambio. Antes de aplicar el archivo, se solicita al
encargado:

1. Confirmar un respaldo recuperable o probar primero en un entorno de
   desarrollo/staging.
2. Revisar el SQL junto con las políticas RLS, triggers, permisos y funciones
   existentes del proyecto.
3. Confirmar que `tenants.time_zone` y `appointments.owner_id` son compatibles
   con los datos actuales. El script usa `America/Mexico_City` como zona horaria
   por defecto cuando no existe el campo.
4. Si la revisión es satisfactoria, ejecutar el contenido completo del archivo
   anterior una sola vez en SQL Editor.
5. No ejecutar además `20261102000000_booking_availability_api.sql` para este
   cambio. El archivo fechado `20261103000000` es la propuesta de
   compatibilidad que debe revisarse.

Si SQL Editor devuelve un error, detenerse y revisar el mensaje; no ejecutar
fragmentos sueltos ni volver a ejecutar sin confirmar primero el estado de la
transacción.

## Verificación posterior

Después de aplicar el cambio, el encargado puede verificar firmas y permisos
con consultas de solo lectura:

```sql
select p.proname as funcion,
       pg_get_function_identity_arguments(p.oid) as argumentos,
       pg_get_function_result(p.oid) as resultado
from pg_proc p
join pg_namespace n on n.oid = p.pronamespace
where n.nspname = 'public'
  and p.proname in (
      'book_appointment', 'get_working_shifts', 'get_available_slots',
      'create_critical_booking', 'get_owned_booking_pets',
      'get_owned_appointments', 'get_tenant_appointments',
      'transition_appointment_status'
  )
order by p.proname, argumentos;
```

La prueba funcional debe hacerse después desde la aplicación, con una sesión
autenticada de cliente y una clínica/servicio/mascota de prueba:

1. Consultar una fecha futura y verificar que se ofrecen solo turnos activos,
   que duran lo configurado para el servicio y que no se traslapan con bloqueos
   ni citas pendientes/confirmadas.
2. Reservar un turno disponible. La app debe recibir un ID de cita.
3. Consultar `appointments` con una consulta de solo lectura y verificar que la
   fila tenga el `tenant_id`, `pet_id`, `service_id`, `owner_id`, `booked_by`,
   `starts_at`, `ends_at` y `status = 'REQUESTED'` esperados.
4. Intentar reservar de nuevo el mismo intervalo con otra clave: debe ser
   rechazado por falta de disponibilidad.
5. Iniciar sesión como clínica y confirmar que la cita aparece en su agenda.
   Probar confirmar y luego completar, comprobando el estado actualizado.

No ejecutar `get_available_slots` directamente desde SQL Editor para probar
disponibilidad: requiere el contexto de una sesión autenticada de cliente.

## Alcance y límites

- Este informe y el SQL no certifican que el cambio ya esté aplicado; Supabase
  no ha sido modificado por el asistente.
- El script debe revisarlo el responsable antes de ejecutarlo en producción.
- El flujo actual cubre solicitud, confirmación, cancelación por clínica y
  finalización. No implementa notificaciones, inasistencia ni modificación de
  citas.
- La reserva evita conflictos de horario con una validación transaccional. La
  disponibilidad mostrada antes de reservar puede cambiar; el servidor vuelve a
  validar al confirmar.
- La compilación Kotlin no se pudo validar en el entorno de trabajo porque no
  estaba disponible el Gradle wrapper (`gradlew.bat`).

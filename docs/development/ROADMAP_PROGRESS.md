# 📋 Roadmap de Desarrollo de Base de Datos y Dominio - PawMedic

Este documento detalla los avances, la arquitectura de datos, una guía para la incorporación del equipo y el resumen de lo implementado en cada fase del proyecto PawMedic.

---

## 👥 Guía para Compañeros de Equipo: ¿Cómo implementar / configurar Supabase en tu entorno local?

Si acabas de hacer `git pull` de los últimos cambios del repositorio, la capa de red y los modelos ya están listos en el código. Para que tu aplicación (Android o Desktop) pueda comunicarse con Supabase sin errores, debes seguir estos pasos:

### 1. Configurar el archivo de entorno (`.env`)
1. En la raíz del proyecto, busca el archivo `.env.example`.
2. Duplícalo y renómbralo exactamente como **`.env`** (este archivo está ignorado en Git por seguridad, por lo que nunca se subirá al repositorio).
3. Abre el archivo `.env` y añade las credenciales públicas de Supabase:
   ```env
   SUPABASE_URL=https://tu-proyecto.supabase.co
   SUPABASE_ANON_KEY=tu-anon-key-publica
   ```
   *(Si estás utilizando Supabase de forma local con la CLI, ejecuta `supabase status` en tu terminal para obtener la URL y la Anon Key).*

### 2. Sincronizar Gradle
* En Android Studio, haz clic en **Sync Project with Gradle Files** (o ejecuta `./gradlew clean build`) para que Android inyecte automáticamente estas variables mediante `BuildConfig` y configure las dependencias de KMP y Postgrest.

---

## 🏛️ Visión General de la Arquitectura

PawMedic utiliza una arquitectura **Kotlin Multiplatform (KMP)** organizada por capas (Clean Architecture) e inyección de dependencias:

1. **Backend & Base de Datos:** Supabase (PostgreSQL) con autenticación JWT, Row Level Security (RLS) y UUIDs.
2. **Capa de Red (`shared/core/network`):** Utiliza `supabase-kt` (Postgrest y Auth). Las credenciales se leen del entorno (`.env`).
3. **Capa de Modelos (`shared/core/model`):** Data Classes con `@Serializable` y `@SerialName` que mapean las tablas PostgreSQL.
4. **Capa de Datos (`shared/features/*` y `core/data`):** DataSources y Repositorios que ejecutan consultas Postgrest y funciones RPC respetando RLS.

---

## 🟢 Fase 1: Identidad, Autenticación y Multi-Tenancy (Completada)

### 📌 Qué se realizó:
* **Refactor de Usuarios (SQL):** Se unificaron las antiguas tablas `Cliente` y `Veterinario` en una única tabla `profiles` vinculada 1:1 con `auth.users` mediante UUIDs y un ENUM de roles (`USER`, `VETERINARY_BUSINESS`, `SUPERADMIN`). Se añadió un trigger automático (`handle_new_user`) para crear el perfil al registrarse.
* **Multi-Tenancy (SQL):** Creación de las tablas `tenants` (sucursales veterinarias) y `business_applications` (solicitudes de registro).
* **RLS Base (SQL):** Políticas de Row Level Security para asegurar que cada usuario solo lea y actualice su propio perfil.
* **Integración Kotlin:** Creación del modelo `UserProfile` y el repositorio `SupabaseProfileRepository` para consultar perfiles de forma segura con Postgrest.

---

## 🟢 Fase 2: Entidades Dependientes y RLS por Propietario (Completada)

### 📌 Qué se realizó:
* **Mascotas (SQL - Issue #42 & #43):** Creación de la tabla `pets` migrando la llave foránea a UUID (`owner_id` -> `profiles.id`). Se aplicaron políticas RLS estrictas (SELECT, INSERT, UPDATE, DELETE) para aislar las mascotas por dueño.
* **Servicios y Horarios (SQL - Issue #50 & #51):** Creación de las tablas `services` (servicios), `business_schedules` (horarios de atención) y `availability_blocks` (bloqueos de agenda) vinculadas al `tenant_id`.
* **RLS Servicios (SQL):** Lectura pública para cualquier usuario autenticado y escritura restringida exclusivamente al dueño del negocio (`tenant_id -> owner_id`).
* **Integración Kotlin:** Creación de los modelos `@Serializable` (`PetModel`, `ServiceModel`, `BusinessScheduleModel`, `AvailabilityBlockModel`) y las fuentes de datos remotas (`SupabasePetDataSource` y `SupabaseServiceDataSource`).

---

## 🟢 Fase 3: Transacciones, Citas y Concurrencia (Completada)

### 📌 Qué se realizó:
* **Tabla Citas (SQL):** Creación de la tabla `appointments` vinculando usuario (`owner_id`), mascota (`pet_id`), sucursal (`tenant_id`) y servicio (`service_id`).
* **Protección contra Doble Reserva (SQL):** Implementación de un índice único condicional (`unique_active_appointment`) para evitar duplicar citas activas en la misma sucursal, fecha y hora.
* **Función RPC Atómica (SQL):** Creación de la función PL/pgSQL `book_appointment(...)`, la cual valida de forma transaccional la propiedad de la mascota, los conflictos de horario y los bloqueos de disponibilidad antes de insertar la cita.
* **Integración Kotlin:** Implementación de `SupabaseAppointmentDataSource` para invocar la función RPC atómica y consultar citas bajo RLS.

---

## 🟢 Fase 4: Verificación y Pruebas (Completada)

### 📌 Qué se realizó:
* **Script de Fixtures (SQL):** Creación de plantillas de pruebas para verificar perfiles, inserción de tenants, mascotas, servicios y llamadas al RPC `book_appointment` directamente en el editor SQL de Supabase.
* **Validación Multiplataforma:** Pruebas de compilación exitosas en Gradle para los targets de Android y Desktop.

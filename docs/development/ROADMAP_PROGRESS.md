# 📋 Roadmap de Desarrollo de Base de Datos y Dominio - PawMedic

Este documento detalla los avances, arquitectura de datos, archivos creados/modificados y la función de cada componente implementado en las distintas fases del proyecto.

---

## 🏛️ Visión General de la Arquitectura

PawMedic utiliza una arquitectura **Kotlin Multiplatform (KMP)** organizada por capas de diseño limpio (Clean Architecture) e inyección de dependencias con Koin:

1. **Backend & Base de Datos:** Supabase (PostgreSQL) con autenticación basada en JWT, Row Level Security (RLS) y UUIDs como identificadores universales.
2. **Capa de Red (`shared/core/network`):** Utiliza el SDK de `supabase-kt` (Postgrest y GoTrue/Auth). Las credenciales se inyectan mediante el entorno (`.env` -> `BuildConfig` / `System.getenv`).
3. **Capa de Modelos (`shared/core/model`):** Contiene los Data Classes anotados con `@Serializable` y `@SerialName` que mapean automáticamente las tablas de PostgreSQL en objetos Kotlin.
4. **Capa de Datos y Características (`shared/features/*` y `shared/core/data`):** Contiene las fuentes de datos (DataSources) y repositorios que realizan consultas a Supabase con Postgrest respetando RLS.

---

## 🟢 Fase 1: Identidad, Autenticación y Multi-Tenancy (Completada)

### 📌 Objetivos Alcanzados:
* Eliminación del esquema legacy de usuarios con IDs enteros autoincrementables.
* Integración con `auth.users` de Supabase mediante una tabla `profiles` atada 1:1 por UUID.
* Creación del modelo *Multi-Tenant* mediante las tablas `tenants` (sucursales veterinarias) y `business_applications` (solicitudes de registro).
* Implementación de políticas RLS para aislar perfiles y lectura de sucursales.

### 📁 Archivos Modificados / Creados en Fase 1:

| Archivo / Ruta | Función Principal |
| :--- | :--- |
| `gradle/libs.versions.toml` | Declara las dependencias oficiales de `supabase-gotrue` y `supabase-postgrest`. |
| `shared/core/network/SupabaseClient.kt` | Función factoría `provideSupabaseClient` que inicializa el SDK de Supabase de manera segura. |
| `apps/mobile-android/build.gradle.kts` | Lee el archivo `.env` en tiempo de compilación para inyectar `SUPABASE_URL` y `SUPABASE_ANON_KEY` en `BuildConfig`. |
| `shared/core/model/.../UserProfile.kt` | Modelo serializable para la tabla `profiles` (`id`, `role`, `nombre`, `apellidos`, `num_tel`). |
| `shared/core/model/.../Tenant.kt` | Modelo serializable para la tabla `tenants` (`id`, `owner_id`, `nombre`, `domicilio`, `municipio`, etc.). |
| `shared/core/model/.../BusinessApplication.kt` | Modelo para las solicitudes de veterinarias (`applicant_id`, `nombre_negocio`, `estado`). |
| `core/.../AccountModels.kt` | Sincronización de los modelos centrales del core con `@SerialName`. |
| `core/.../SupabaseRepository.kt` | Implementación de `SupabaseAuthRepository` (login con Supabase Auth) y `SupabaseProfileRepository` (obtener perfil por UUID). |

---

## 🟢 Fase 2: Entidades Dependientes y RLS por Propietario (Completada)

### 📌 Objetivos Alcanzados (Issues #42, #43, #50, #51):
* Migración de la tabla `pets` para usar UUID `owner_id` enlazado al perfil del cliente.
* Creación de las tablas `services` (catálogo de servicios), `business_schedules` (horarios de atención) y `availability_blocks` (bloqueos de disponibilidad) asociadas al `tenant_id`.
* Configuración de Row Level Security (RLS) que restringe el acceso a mascotas solo al cliente dueño y permite lectura pública de servicios/horarios con escritura restringida al dueño del tenant.
* Creación de DataSources e integración de Postgrest en Kotlin para mascotas y servicios.

### 📁 Archivos Modificados / Creados en Fase 2:

| Archivo / Ruta | Función Principal |
| :--- | :--- |
| `shared/core/model/.../PetModel.kt` | Mapeo de la tabla `pets` (`id`, `owner_id`, `nombre`, `especie`, `raza`, `edad`, `alergias`, `sexo`, `peso`). |
| `shared/core/model/.../ServiceModel.kt` | Mapeo de la tabla `services` (`id`, `tenant_id`, `nombre`, `descripcion`, `precio_centavos`, `duracion_minutos`). |
| `shared/core/model/.../BusinessScheduleModel.kt` | Mapeo de la tabla `business_schedules` (`id`, `tenant_id`, `dia_semana`, `hora_apertura`, `hora_cierre`). |
| `shared/core/model/.../AvailabilityBlockModel.kt` | Mapeo de la tabla `availability_blocks` (`id`, `tenant_id`, `motivo`, `fecha_inicio`, `fecha_fin`). |
| `shared/features/pets/build.gradle.kts` | Configuración del módulo de mascotas para incluir Postgrest y `:shared:core:model`. |
| `shared/features/services/build.gradle.kts` | Configuración del módulo de servicios para incluir Postgrest y `:shared:core:model`. |
| `shared/features/pets/.../SupabasePetDataSource.kt` | Consultas Postgrest para listar mascotas del usuario autenticado e insertar mascotas. |
| `shared/features/services/.../SupabaseServiceDataSource.kt` | Consultas Postgrest para listar servicios, horarios, bloqueos de disponibilidad e insertar servicios. |

---

## 🟡 Fase 3: Transacciones, Citas y Concurrencia (Siguiente Paso)

### 🎯 Próximos Objetivos:
1. **Tabla de Citas (`appointments` / `Cita`):** Rediseño con UUIDs vinculando `owner_id`, `pet_id`, `tenant_id` y `service_id`.
2. **Reserva Atómica mediante Función RPC (SQL):** Creación de una función PL/pgSQL en PostgreSQL que valide en una sola transacción sin bloqueos/conflictos de doble reserva.
3. **RLS de Citas:** Reglas para que el cliente solo vea sus citas y la veterinaria vea únicamente las citas programadas en su tenant.
4. **Consumo de RPC en Kotlin:** Integración en `shared/features/appointments` llamando a `supabase.postgrest.rpc(...)`.

---

## 🟡 Fase 4: Verificación, Fixtures y Pruebas (Fase Final)

### 🎯 Próximos Objetivos:
1. **Script de Fixtures (SQL):** Datos de prueba con perfiles, mascotas y tenants de prueba.
2. **Pruebas de Validación RLS:** Verificación del aislamiento de datos desde el editor SQL y pruebas unitarias/de integración en Kotlin.

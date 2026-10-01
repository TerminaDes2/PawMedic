# Migración del prototipo `auth`

## Propósito

Este documento describe cómo migrar el prototipo histórico ubicado en
`features/auth/` hacia el módulo canónico `shared/features/auth/`. La
migración sirve como ejemplo de cómo debe evolucionar una funcionalidad del
proyecto: primero se conserva el comportamiento válido, después se separan
responsabilidades y finalmente se retira el código legado.

No se deben copiar archivos de forma mecánica ni mantener dos implementaciones
activas de autenticación. El destino oficial es `shared/features/auth/`.

## Situación actual

El prototipo histórico contiene:

```text
features/auth/
├── src/commonMain/
│   └── kotlin/com/pawsmedic/features/auth/
│       ├── domain/AuthState.kt
│       ├── presentation/AuthViewModel.kt
│       └── di/AuthModule.kt
└── src/commonTest/
    └── kotlin/com/pawsmedic/features/auth/AuthViewModelTest.kt
```

Su responsabilidad actual es limitada:

- representar estados de sesión (`SignedOut`, `Loading`, `SignedIn` y `Error`);
- solicitar inicio de sesión;
- intentar restaurar una sesión existente;
- cerrar sesión;
- conectar el `AuthViewModel` con un repositorio del `core` legado;
- probar el comportamiento básico del ViewModel.

El prototipo depende de `:core`, `AuthRepository` y modelos definidos en la
arquitectura anterior. Es útil para identificar comportamiento y casos de
prueba, pero no debe recibir nuevas funcionalidades.

## Destino canónico

La funcionalidad debe quedar organizada en el módulo que ya forma parte del
grafo oficial de Gradle:

```text
shared/features/auth/
└── src/
    ├── commonMain/
    │   └── kotlin/com/pawsmedic/features/auth/
    │       ├── domain/
    │       │   ├── model/
    │       │   ├── repository/
    │       │   └── usecase/
    │       ├── data/
    │       │   ├── datasource/
    │       │   ├── dto/
    │       │   ├── repository/
    │       │   └── di/
    │       ├── presentation-common/
    │       └── presentation/
    ├── androidMain/
    │   └── kotlin/.../presentation-android/
    ├── desktopMain/
    │   └── kotlin/.../presentation-desktop/
    └── commonTest/
```

El destino debe respetar la dirección de dependencias:

```text
presentación -> casos de uso -> contratos del dominio
                                      ^
                                      |
                         repositorios de data
                                      |
                               datasource Supabase
```

El dominio no conoce Supabase, HTTP, DTOs, Compose ni una plataforma
específica. La capa `data` implementa los contratos del dominio y traduce las
respuestas externas a modelos internos. La presentación coordina la
interacción de la interfaz, pero no autoriza operaciones protegidas.

## Mapeo de responsabilidades

| Prototipo | Destino | Tratamiento |
| --- | --- | --- |
| `domain/AuthState.kt` | `shared/features/auth/presentation-common/` o un modelo de estado de presentación | Conservar los estados útiles para la UI; separar estado de pantalla de modelos de dominio cuando sean conceptos distintos. |
| `presentation/AuthViewModel.kt` | `shared/features/auth/presentation/` | Conservar las intenciones y transiciones válidas, pero reemplazar la dependencia directa del repositorio por casos de uso. |
| `di/AuthModule.kt` | `shared/features/auth/data/di/` y configuración del módulo canónico | Registrar contratos, implementaciones y casos de uso sin depender del `:core` legado. |
| `AuthRepository` de `core` | `domain/repository/` | Convertirlo en un contrato propio de autenticación, con nombres y resultados alineados al dominio actual. |
| Implementación del repositorio legado | `data/repository/` | Reescribir o adaptar la implementación para usar el datasource y el cliente configurado por la aplicación. |
| Modelos de `core` | `domain/model/` o `shared/core/model/` | Ubicar cada modelo según su alcance: específico de auth en la feature; transversal en `shared/core/model/`. |
| `AuthViewModelTest.kt` | `shared/features/auth/src/commonTest/` | Trasladar escenarios válidos y agregar casos para los nuevos contratos, errores y restauración de sesión. |

Este mapeo es conceptual. Cada archivo debe revisarse antes de moverlo para
evitar trasladar acoplamientos, nombres heredados o responsabilidades
mezcladas.

## Fases de migración

### 1. Inventariar el comportamiento

Registrar qué hace realmente el prototipo, qué estados expone, qué errores
pueden producirse y qué pruebas lo describen. No se deben inventar
funcionalidades durante el traslado. Las operaciones iniciales son:

- iniciar sesión;
- restaurar sesión;
- cerrar sesión;
- representar carga, sesión activa, sesión cerrada y error.

También se deben identificar todos los consumidores del módulo histórico antes
de cambiar sus dependencias.

### 2. Definir los contratos del dominio

Crear en el módulo canónico los contratos que expresan lo que autenticación
necesita, sin mencionar el proveedor externo. Los contratos deben cubrir las
operaciones soportadas, sus entradas, resultados y errores esperados.

En esta fase se decide qué pertenece a `shared/core/auth` y qué es específico
de la feature. El ciclo de vida general de JWT o sesión puede exponerse como
puerto transversal en `shared/core/auth`; las intenciones de autenticación del
producto permanecen en `shared/features/auth`.

### 3. Definir modelos internos y casos de uso

Separar los modelos de dominio de los DTOs del proveedor. Después, expresar
cada operación como un caso de uso independiente. El ViewModel no debe conocer
detalles de Supabase ni ejecutar directamente operaciones de red.

Los casos de uso coordinan la intención completa y devuelven resultados
explícitos. Las reglas de autorización de recursos protegidos no se trasladan
al cliente: siguen siendo responsabilidad del JWT, las Edge Functions, las
constraints y RLS.

### 4. Implementar la capa de datos

Crear el datasource que use el cliente autenticado configurado por la
aplicación, los DTOs necesarios y el repositorio que implemente los contratos
del dominio. Las conversiones y errores del proveedor deben resolverse en esta
frontera.

No se deben incluir service-role keys, contraseñas, credenciales de usuario ni
configuración sensible en el módulo.

### 5. Adaptar la presentación

Trasladar el comportamiento del ViewModel al módulo canónico y conectarlo a
los casos de uso. La presentación común debe exponer estados e intenciones
reutilizables. Android y escritorio pueden adaptar la experiencia visual en
sus respectivos source sets, sin duplicar el dominio.

La UI puede ocultar o mostrar rutas según el estado y el rol, pero esto sólo
es navegación. Nunca debe considerarse un reemplazo de la autorización del
backend.

### 6. Migrar y ampliar las pruebas

Trasladar las pruebas del prototipo a `shared/features/auth/src/commonTest/`.
Las pruebas deben verificar como mínimo:

- estado inicial y restauración de sesión;
- transición a carga durante el inicio de sesión;
- sesión válida;
- error de autenticación;
- cierre de sesión;
- comportamiento ante errores del repositorio;
- que el ViewModel dependa de casos de uso o contratos sustituibles, no de una
  implementación concreta de Supabase.

Las pruebas de RLS, migraciones, RPCs y Edge Functions permanecen en
`supabase/tests/`; no deben simularse como pruebas de UI.

### 7. Cambiar consumidores y retirar el prototipo

Actualizar las aplicaciones y módulos que todavía dependan de
`features/auth/` para que consuman `shared/features/auth/`. Verificar que
`settings.gradle.kts` sólo incluya el módulo canónico y que no existan dos
registros de inyección de dependencias para autenticación.

Cuando no haya consumidores del prototipo, eliminarlo o conservarlo sólo si
existe una razón histórica documentada. En ningún caso debe permanecer como
una ruta alternativa que pueda compilarse o registrarse accidentalmente.

## Criterios de finalización

La migración se considera terminada cuando:

- el comportamiento necesario del prototipo está cubierto por el módulo
  canónico;
- las aplicaciones usan `shared/features/auth/`;
- ningún caso de uso o ViewModel canónico depende de `:core` legado;
- los DTOs y detalles de Supabase están confinados a `data`;
- las pruebas comunes pasan en el módulo canónico;
- no existen dos implementaciones activas de autenticación;
- el scaffold histórico fue retirado o quedó explícitamente marcado como no
  compilable y no mantenido.

## Resultado esperado

Después de la migración, `auth` se convierte en un ejemplo de referencia para
las demás features: se identifica el comportamiento existente, se definen
contratos, se separan dominio, datos y presentación, se trasladan las pruebas,
se conectan las aplicaciones al módulo canónico y finalmente se elimina el
scaffold antiguo.

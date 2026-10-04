# Estructura del proyecto

PawMedic es un monorepo Kotlin Multiplatform. La estructura siguiente define
los límites canónicos del proyecto: cada carpeta tiene una responsabilidad
concreta y no debe convertirse en un lugar genérico para código que podría
pertenecer a otra capa.

```text
pawsmedic/
├── apps/
│   ├── mobile-android/
│   ├── desktop-veterinary/
│   └── desktop-admin/
│
├── shared/
│   ├── core/
│   │   ├── common/
│   │   ├── model/
│   │   ├── auth/
│   │   ├── network/
│   │   ├── storage/
│   │   ├── design-system/
│   │   └── testing/
│   │
│   └── features/
│       ├── auth/
│       ├── pets/
│       ├── businesses/
│       ├── appointments/
│       ├── services/
│       ├── medical-records/
│       ├── products/
│       ├── reports/
│       └── platform-admin/
│
├── supabase/
│   ├── migrations/
│   ├── functions/
│   ├── tests/
│   └── seed.sql
│
├── docs/
│   ├── architecture/
│   ├── database/
│   ├── security/
│   ├── development/
│   ├── decisions/
│   └── diagrams/
│
└── build-logic/
```

## `apps/`: aplicaciones ejecutables

Las aplicaciones ensamblan módulos compartidos, configuran navegación,
inyección de dependencias, clientes de Supabase y parámetros de plataforma.
Son el lugar para el arranque de cada producto y para la composición de sus
pantallas; no son el lugar para implementar reglas de negocio reutilizables.

| Carpeta | Se desarrolla aquí | No se desarrolla aquí |
| --- | --- | --- |
| `apps/mobile-android/` | Cliente Android para usuarios (`USER`): registro de mascotas, búsqueda y solicitud de servicios, citas y consulta de información autorizada. Incluye `AndroidManifest`, arranque y wiring específico de Android. | Lógica de dominio exclusiva de una feature, acceso directo a tablas, políticas de autorización o funcionalidades de escritorio. |
| `apps/desktop-veterinary/` | Cliente Compose Desktop para negocios veterinarios aprobados (`VETERINARY_BUSINESS`): operación del negocio, agenda, servicios, productos y atención clínica según permisos. | Reglas clínicas o de tenancy duplicadas, flujo de usuario final Android o administración global de la plataforma. |
| `apps/desktop-admin/` | Cliente Compose Desktop para operadores `SUPERADMIN`: operaciones de plataforma, aprobación de negocios, auditoría y reportes administrativos. | Administración interna de una clínica, bypass de RLS, secretos de Supabase o lógica de otras features. |

Una aplicación puede decidir qué rutas y componentes mostrar según el rol,
pero esa decisión es navegación y experiencia de usuario. La autenticación,
las Edge Functions, las restricciones de base de datos y RLS siguen siendo la
autoridad de seguridad.

## `shared/core/`: capacidades transversales

Estos módulos deben ser pequeños, estables y reutilizables por varias
features. No deben contener flujos de una capacidad de negocio concreta.

| Carpeta | Se desarrolla aquí | No se desarrolla aquí |
| --- | --- | --- |
| `shared/core/common/` | Primitivas comunes: resultados, errores, fechas, identificadores y helpers de coroutines. | Casos de uso, DTOs de una feature o componentes visuales. |
| `shared/core/model/` | Modelos serializables y valores compartidos entre features, como sesión, roles e identificadores. | Modelos de persistencia acoplados a una tabla, llamadas de red o reglas de pantalla. |
| `shared/core/auth/` | Puertos y ciclo de vida de autenticación, sesión y JWT; contratos para restaurar o cerrar sesión. | Login de una pantalla concreta, autorización basada sólo en UI o decisiones de negocio. |
| `shared/core/network/` | Abstracciones de transporte HTTP/Supabase, configuración del cliente y errores de comunicación. | Reglas de autorización, consultas de una feature o claves de servicio. |
| `shared/core/storage/` | Puertos para caché local, preferencias y persistencia segura en el dispositivo. | Convertirse en la fuente de verdad de datos clínicos o almacenar secretos de backend. |
| `shared/core/design-system/` | Tema, tokens, componentes Compose reutilizables y primitivas de accesibilidad. | Pantallas completas, navegación o componentes que conocen una feature. |
| `shared/core/testing/` | Fakes, fixtures, builders y utilidades deterministas para pruebas compartidas. | Código usado por producción o simulaciones que oculten errores reales de integración. |

## `shared/features/`: capacidades verticales

Cada carpeta representa una capacidad funcional y contiene el dominio, los
adaptadores de datos y los contratos de presentación de esa capacidad. La
estructura interna esperada es:

```text
shared/features/<feature>/
└── src/
    ├── commonMain/       # dominio, contratos, datos y presentación común
    ├── androidMain/      # integración o UI exclusiva de Android, si aplica
    ├── desktopMain/      # integración o UI exclusiva de escritorio, si aplica
    └── commonTest/       # pruebas de la feature
```

En `commonMain`, `domain/` contiene modelos, interfaces de repositorio y casos
de uso; `data/` contiene DTOs, fuentes de datos y repositorios que implementan
los contratos; `presentation-common/` contiene estado e intenciones comunes;
`presentation-android/` y `presentation-desktop/` contienen UI específica de
plataforma cuando sea necesaria.

### Ejemplo de división: `shared/features/auth/`

El módulo `auth` concentra la capacidad de autenticación y sesión, pero no
pertenece a ninguna aplicación concreta. Android, escritorio veterinario y
escritorio administrativo pueden consumir el mismo módulo, aunque cada
aplicación presente la experiencia de acceso de una forma distinta.

Una organización esperada, a nivel conceptual, es la siguiente:

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
    │           └── ViewModel de autenticación
    ├── androidMain/
    │   └── kotlin/.../presentation-android/
    ├── desktopMain/
    │   └── kotlin/.../presentation-desktop/
    └── commonTest/
        └── kotlin/com/pawsmedic/features/auth/
```

El árbol anterior es una guía de responsabilidades y nombres; no es una
solicitud para implementar ahora las clases o archivos mostrados.

#### `auth/domain/`: qué significa el dominio

El **dominio** es el núcleo de la capacidad. Describe qué significa estar
autenticado y qué operaciones necesita el producto, sin saber si la sesión se
obtiene mediante Supabase, otro proveedor, Android o Compose Desktop.

En este ejemplo, `domain/model/` definiría conceptos como una sesión
autenticada, el identificador del usuario, el rol de perfil y el estado de
sesión. Estos modelos expresan significado del negocio y no deberían ser
copias de una respuesta JSON ni contener detalles de una tabla.

`domain/usecase/` definiría acciones orientadas al negocio, por ejemplo
iniciar sesión, cerrar sesión o restaurar una sesión existente. Un **caso de
uso** representa una intención completa y coordinada del sistema: valida las
condiciones que corresponden al cliente, solicita la operación necesaria y
devuelve un resultado que la presentación puede interpretar. No es una
pantalla, no dibuja controles y no debe conocer detalles de HTTP o Supabase.

`domain/repository/` declararía los **contratos** que el dominio necesita para
trabajar con autenticación. Un contrato es una promesa estable sobre qué
operaciones están disponibles, qué entradas reciben y qué resultados o errores
pueden producir. Es una interfaz entre capas: el dominio dice qué necesita y
la capa de datos decide cómo cumplirlo. El contrato no debe describir la
implementación concreta ni obligar al dominio a usar una librería específica.

#### `auth/data/`: cómo se cumplen los contratos

La capa `data` implementa los contratos del dominio y traduce entre el mundo
externo y los modelos internos.

* `datasource/` contendría la fuente que habla con el proveedor de identidad,
  el cliente de Supabase o el mecanismo de sesión configurado. Sólo esta
  frontera debería conocer detalles del transporte, nombres de campos remotos
  y respuestas del proveedor.
* `dto/` contendría los **DTOs** (Data Transfer Objects), es decir, formas
  de transportar datos entre el sistema y un servicio externo. Un DTO puede
  tener campos, formatos o valores nulos que no conviene propagar al dominio.
* `repository/` contendría la implementación del contrato del dominio.
  Coordina la fuente de datos, convierte DTOs a modelos de dominio y expone
  errores de manera explícita. Un **repositorio** es el punto de acceso que
  oculta dónde y cómo se obtienen o guardan los datos; no es la base de datos
  ni debe convertirse en una segunda capa de presentación.
* `di/` contendría el ensamblaje de dependencias del módulo: qué implementación
  concreta se entrega cuando el dominio solicita su contrato. La aplicación
  puede completar la configuración del entorno, pero no debe duplicar la
  lógica del repositorio.

La separación permite cambiar un proveedor o usar un fake en pruebas sin
reescribir los casos de uso. También evita que el dominio quede acoplado a
Supabase. La autenticación real sigue dependiendo de Auth, JWT, Edge Functions,
restricciones de base de datos y RLS cuando la operación accede a recursos
protegidos.

#### `auth/presentation-common/`: presentación común

La **presentación común** contiene el comportamiento que puede compartirse
entre Android y escritorio sin asumir un toolkit o tamaño de pantalla. Define
el estado observable de la autenticación, las intenciones del usuario y las
reglas para convertir el resultado de un caso de uso en estados como
inicializando, formulario listo, cargando, autenticado o error visible.

Aquí no se debería colocar un botón, un `Activity`, una ventana de escritorio
ni una navegación específica. Tampoco se debe considerar la visibilidad de
una pantalla como autorización: la presentación sólo refleja el resultado de
la sesión y del rol; el backend continúa validando el acceso.

Un **ViewModel** es el coordinador entre la UI y los casos de uso. Recibe
intenciones de la interfaz, conserva o expone el estado de pantalla, ejecuta
los casos de uso y publica resultados observables. Su responsabilidad es
coordinar y sobrevivir a cambios de configuración o recomposición según la
plataforma; no es un repositorio, no contiene consultas SQL y no debe decidir
por sí solo si un usuario tiene permiso para una operación protegida.

En el ejemplo, un ViewModel de autenticación podría coordinar el envío de las
credenciales y la restauración de sesión, mientras que el caso de uso y el
repositorio realizan el trabajo correspondiente. Esta división hace que el
mismo comportamiento se pruebe sin iniciar una ventana ni depender de un
dispositivo.

#### `androidMain/` y `desktopMain/`: experiencia por plataforma

Estas carpetas contienen sólo lo que no puede compartirse. `presentation-android/`
podría adaptar el estado común a pantallas, ciclo de vida y componentes propios
de Android. `presentation-desktop/` podría adaptar ese mismo estado a una
ventana de Compose Desktop, teclado, tamaños de ventana y navegación de
escritorio.

Ninguna de las dos debería reimplementar autenticación, crear otro repositorio
ni cambiar las reglas del dominio. Si una diferencia es de interacción o
disposición visual, pertenece a la plataforma; si es una regla sobre la
sesión, pertenece al dominio o a la infraestructura correspondiente.

#### `commonTest/`: qué se prueba

Las pruebas comunes verificarían los casos de uso, las transiciones del
ViewModel, las conversiones relevantes y los escenarios de error usando
repositorios o fuentes falsas. No deben depender de una cuenta real, de una
service-role key ni de una interfaz gráfica. Las pruebas de integración con
Supabase y las políticas RLS viven en `supabase/tests/`; las pruebas visuales
o específicas de plataforma pueden mantenerse junto a sus respectivos
source sets cuando el proyecto las necesite.

| Carpeta | Se desarrolla aquí | No se desarrolla aquí |
| --- | --- | --- |
| `auth/` | Inicio y cierre de sesión, restauración de sesión y estado autenticado. | Decidir permisos sólo en pantalla o implementar el proveedor de identidad dentro de una app. |
| `pets/` | Registro, actualización, listado y propiedad de mascotas. | Datos de usuarios, negocios o historias clínicas que sólo se relacionen con una mascota. |
| `businesses/` | Búsqueda y operación de negocios veterinarios, perfiles y pertenencia al tenant. | Aprobación global de negocios, que pertenece a `platform-admin/`. |
| `appointments/` | Solicitud, disponibilidad, agenda, estados y reglas del ciclo de una cita. | Crear consultas clínicas o gestionar horarios mediante SQL directo desde la UI. |
| `services/` | Catálogo y configuración de servicios veterinarios. | Productos de inventario o citas completas. |
| `medical-records/` | Expedientes, consultas y acceso autorizado a información clínica. | Relajar controles de acceso para facilitar la UI o guardar datos clínicos en caché sin una política explícita. |
| `products/` | Catálogo, inventario y operaciones de productos del negocio. | Pagos, autorización global o servicios veterinarios. |
| `reports/` | Consultas y modelos de reportes autorizados para los roles que los consumen. | Un almacén analítico independiente o exponer datos sin filtros de tenant. |
| `platform-admin/` | Solicitudes y aprobación de negocios, auditoría y operaciones exclusivas de `SUPERADMIN`. | Operaciones rutinarias de una clínica o controles de seguridad que deban sustituir RLS. |

Una feature puede depender de `shared/core`, pero no debe importar los
paquetes `data` o `presentation` de otra feature. La colaboración entre
features se realiza mediante contratos de dominio o casos de uso explícitos.
El flujo esperado es **UI → ViewModel → caso de uso → repositorio → Supabase /
Edge Function**.

La migración de `features/auth/` hacia este modelo está documentada paso a
paso en [Migración del prototipo auth](auth-migration.md). Ese documento debe
usarse como ejemplo para trasladar cualquier otro scaffold histórico a una
feature canónica: conservar el comportamiento válido, separar las capas,
migrar las pruebas, cambiar los consumidores y retirar la implementación
anterior.

## `supabase/`: backend y persistencia

| Carpeta o archivo | Se desarrolla aquí | No se desarrolla aquí |
| --- | --- | --- |
| `supabase/migrations/` | Cambios versionados y ordenados de esquema, constraints, funciones SQL, índices y políticas RLS. | Cambios manuales no versionados ni lógica de presentación. |
| `supabase/functions/` | Edge Functions autenticadas para comandos que requieren validación, transacciones, idempotencia o coordinación del backend. | Un sustituto de toda la lógica de dominio del cliente, endpoints anónimos sin autorización o secretos en el código. |
| `supabase/tests/` | Pruebas del backend: esquema, constraints, RLS, RPCs y Edge Functions según las herramientas configuradas. | Pruebas de ViewModels o UI de Kotlin. |
| `supabase/seed.sql` | Datos mínimos y reproducibles para desarrollo local y pruebas manuales. | Datos reales, credenciales, tokens o un mecanismo de despliegue a producción. |

La base de datos y las Edge Functions son parte de la frontera de seguridad:
el cliente nunca debe incluir una service-role key. Los cambios de esquema
deben entrar por migraciones y respetar el modelo multi-tenant.

## `docs/`: documentación del sistema

| Carpeta | Contenido | No contiene |
| --- | --- | --- |
| `docs/architecture/` | Estructura, límites de módulos, navegación, dependencias y decisiones de diseño técnico. | Código de producción ni instrucciones operativas de base de datos detalladas. |
| `docs/database/` | Modelo de datos, convenciones de persistencia y relación con migraciones. | Una segunda definición del esquema separada de `supabase/migrations/`. |
| `docs/security/` | Tenancy, roles, RLS, autenticación y amenazas relevantes. | Secretos, credenciales o autorizaciones implementadas sólo como documentación. |
| `docs/development/` | Configuración local, ejecución, pruebas y flujo de desarrollo. | Configuración local con valores sensibles versionados. |
| `docs/decisions/` | ADRs y decisiones que explican el contexto y las alternativas descartadas. | Tareas temporales o un backlog sin decisión técnica. |
| `docs/diagrams/` | Diagramas de contexto, flujos, navegación y dependencias. | La única fuente de verdad para contratos o políticas ejecutables. |

## `build-logic/`: convenciones de Gradle

Aquí se desarrollan plugins de convención y configuración Gradle compartida
para evitar repetir versiones, targets, compilación, pruebas y reglas de
módulos. No contiene código de negocio, código de aplicación, configuración de
Supabase ni plugins específicos de una feature salvo que la convención sea
realmente reutilizable.

## `PawMedic/features/`: scaffold histórico de features

La carpeta superior `features/` que existe actualmente en el repositorio no es
la misma que `shared/features/`. Contiene un scaffold anterior, pequeño y
parcial, con estos módulos:

```text
features/
├── auth/
│   ├── src/commonMain/.../domain/AuthState.kt
│   ├── src/commonMain/.../presentation/AuthViewModel.kt
│   ├── src/commonMain/.../di/AuthModule.kt
│   └── src/commonTest/.../AuthViewModelTest.kt
└── profile/
    ├── src/commonMain/.../domain/ProfileState.kt
    ├── src/commonMain/.../presentation/ProfileViewModel.kt
    ├── src/commonMain/.../di/ProfileModule.kt
    └── src/commonTest/.../ProfileViewModelTest.kt
```

En ese scaffold se desarrolló una primera aproximación a:

| Módulo | Qué contiene actualmente | Qué representa |
| --- | --- | --- |
| `features/auth/` | Estado de autenticación, un `AuthViewModel`, configuración de inyección de dependencias y pruebas del ViewModel. | Un prototipo inicial de inicio de sesión, restauración de sesión y cierre de sesión. |
| `features/profile/` | Estado de perfil, un `ProfileViewModel`, configuración de inyección de dependencias y pruebas del ViewModel. | Un prototipo inicial de carga de perfil de usuario. |

Este código usa dependencias del scaffold anterior, como `:core`,
`AuthRepository`, `ProfileRepository` y modelos ubicados en el `core` legado.
No contiene todavía la división completa por `domain`, `data` y
`presentation-common` definida para los módulos canónicos. Por ello, sirve
como referencia histórica o material de transición, no como plantilla que se
deba copiar para nuevas funcionalidades.

No se desarrolla funcionalidad nueva en `PawMedic/features/`. Las nuevas
features deben crearse en `shared/features/<nombre>/`, registrarse en
`settings.gradle.kts` y seguir los límites descritos en esta guía. La
migración de `features/auth/` está descrita en
[auth-migration.md](auth-migration.md) y debe servir como ejemplo de
referencia. Si el scaffold histórico se migra, la migración debe trasladar
sus responsabilidades al módulo canónico correspondiente y evitar mantener
dos implementaciones de autenticación o perfil activas en paralelo.

## Carpetas fuera del árbol canónico

`settings.gradle.kts` define como módulos canónicos únicamente las carpetas
mostradas arriba. Las carpetas superiores `core/`, `features/` y las
aplicaciones antiguas (`apps/androidApp/`, `apps/patientApp/` y
`apps/clinicApp/`) se conservan como material histórico del scaffold y no
forman parte del grafo actual de compilación. No se debe agregar funcionalidad
nueva allí ni tratarlas como una segunda arquitectura.

Los archivos de configuración de Gradle y del repositorio (`build.gradle.kts`,
`gradle/`, `gradle.properties`, `.env.example` y `settings.gradle.kts`) sirven
para construir y configurar el monorepo; no son módulos de producto ni deben
contener secretos.

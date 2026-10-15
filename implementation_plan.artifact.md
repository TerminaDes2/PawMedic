# Plan de Implementación: Autenticación Real con Supabase, Google Sign-In, Verificación OTP y Mejoras UI

Implementar la autenticación real con Supabase en la aplicación Android móvil de PawMedic, reemplazando datos de prueba por datos reales de Supabase. El flujo incluirá inicio de sesión por correo y contraseña (con validación estricta de credenciales), registro con confirmación de código por correo (OTP/2FA), inicio de sesión con Google (Google Sign-In via Credential Manager + Supabase Auth) y mejoras visuales en la interfaz UI manteniendo la identidad de la marca.

## User Review Required

> [!IMPORTANT]
> - **Google Cloud + Supabase Auth**: Se requiere que en Google Cloud Console hayas generado las credenciales OAuth 2.0 Client ID (tipo Web Application o Android Client ID) y registrado en Supabase (*Authentication -> Providers -> Google*).
> - **Verificación por Correo (OTP)**: Para la verificación en 2 pasos al registrarse, Supabase Auth enviará un correo con un código/token de verificación de 6 dígitos. La app solicitará al usuario ingresar este código para activar y validar su cuenta.

## Proposed Changes

### 1. Capa de Red y Autenticación con Supabase (`:shared:core:network` y `:shared:features:auth`)

#### [MODIFY] [SupabaseAuthDataSourceImpl.kt](file:///C:/Users/krist/AndroidStudioProjects/PawMedic/shared/features/auth/src/commonMain/kotlin/com/pawsmedic/features/auth/data/datasource/SupabaseAuthDataSourceImpl.kt)
- Agregar soporte para verificación OTP de correo (`verifyEmailOTP(email, code)`).
- Agregar soporte para inicio de sesión con Google usando ID Token (`signInWithGoogleIdToken(idToken, rawNonce)`).
- Asegurar el envío de metadata adicional (`full_name`, `phone`, `role`) en el registro con Supabase.

#### [MODIFY] [SupabaseAuthRepository.kt](file:///C:/Users/krist/AndroidStudioProjects/PawMedic/shared/features/auth/src/commonMain/kotlin/com/pawsmedic/features/auth/data/repository/SupabaseAuthRepository.kt)
- Exponer métodos de verificación de código OTP para el registro de usuarios.
- Exponer método para autenticación con Google ID Token.

#### [MODIFY] [AuthRepository.kt](file:///C:/Users/krist/AndroidStudioProjects/PawMedic/shared/features/auth/src/commonMain/kotlin/com/pawsmedic/features/auth/domain/repository/AuthRepository.kt)
- Definir contratos para `verifyOtp`, `resendOtp` y `signInWithGoogle`.

---

### 2. ViewModels y Estado de UI (`:shared:features:auth`)

#### [MODIFY] [LoginViewModel.kt](file:///C:/Users/krist/AndroidStudioProjects/PawMedic/shared/features/auth/src/commonMain/kotlin/com/pawsmedic/features/auth/presentation/login/LoginViewModel.kt)
- Incorporar validaciones estrictas de correo y contraseña (formato exacto, campos no vacíos, manejo detallado de errores de credenciales inválidas retornadas por Supabase Auth).
- Agregar método `loginWithGoogle(idToken)`.

#### [MODIFY] [RegisterViewModel.kt](file:///C:/Users/krist/AndroidStudioProjects/PawMedic/shared/features/auth/src/commonMain/kotlin/com/pawsmedic/features/auth/presentation/register/RegisterViewModel.kt)
- Actualizar flujo de registro para solicitar el envío de correo de confirmación OTP.
- Manejar estado `requiresOtpVerification` e implementación de `verifyOtp(code)` y `resendOtpCode()`.
- Agregar método `registerWithGoogle(idToken)`.

---

### 3. Pantallas y Componentes de Interfaz UI/UX (`:shared:features:auth` y `:shared:core:design-system`)

#### [NEW] [OtpVerificationScreen.kt](file:///C:/Users/krist/AndroidStudioProjects/PawMedic/shared/features/auth/src/commonMain/kotlin/com/pawsmedic/features/auth/presentation/otp/OtpVerificationScreen.kt)
- Pantalla elegante para ingresar el código de verificación enviado al correo.
- Contiene 6 casillas separadas para ingresar números, temporizador de re-envío y mensajes de estado.

#### [MODIFY] [LoginScreen.kt](file:///C:/Users/krist/AndroidStudioProjects/PawMedic/shared/features/auth/src/commonMain/kotlin/com/pawsmedic/features/auth/presentation/login/LoginScreen.kt)
- Rediseño mejorado respetando la paleta Teal/Mint y tipografía de PawMedic.
- Integración de botón social "Continuar con Google".
- Mensajes de error claros e indicación de progreso durante la solicitud.

#### [MODIFY] [RegisterScreen.kt](file:///C:/Users/krist/AndroidStudioProjects/PawMedic/shared/features/auth/src/commonMain/kotlin/com/pawsmedic/features/auth/presentation/register/RegisterScreen.kt)
- Rediseño visual mejorado con secciones organizadas.
- Integración de botón social "Continuar con Google".
- Transición fluida a la pantalla de Verificación OTP al completar los datos iniciales.

#### [NEW] [GoogleSignInHelper.android.kt](file:///C:/Users/krist/AndroidStudioProjects/PawMedic/apps/mobile-android/src/main/java/com/pawsmedic/mobile/auth/GoogleSignInHelper.kt)
- Integración con Android Credential Manager API para solicitar de forma nativa e interactiva la cuenta de Google del usuario y extraer el ID Token.

#### [MODIFY] [AuthNavigation.kt](file:///C:/Users/krist/AndroidStudioProjects/PawMedic/shared/features/auth/src/commonMain/kotlin/com/pawsmedic/features/auth/presentation/navigation/AuthNavigation.kt)
- Agregar pantalla `AuthScreenState.OtpVerification` en la navegación de Auth.

---

### 4. Limpieza de Datos Dummy y Reemplazo por Datos Reales

#### [MODIFY] [AuthModule.kt](file:///C:/Users/krist/AndroidStudioProjects/PawMedic/shared/features/auth/src/commonMain/kotlin/com/pawsmedic/features/auth/di/AuthModule.kt)
- Asegurar la inyección única y limpia del cliente real de Supabase Auth sin fallback a data de prueba.

---

## Verification Plan

### Automated Tests
- Ejecución de `gradle_build(":apps:mobile-android:assembleDebug")` para validar la compilación limpia del proyecto.

### Manual Verification
- Pruebas de registro con correo real e ingreso del código OTP enviado al correo.
- Pruebas de inicio de sesión con correo y contraseña incorrectos vs correctos (verificar validación exacta).
- Prueba de inicio de sesión / registro utilizando la cuenta de Google con Credential Manager.
- Verificación visual del nuevo diseño de interfaz en dispositivo o emulador Android.

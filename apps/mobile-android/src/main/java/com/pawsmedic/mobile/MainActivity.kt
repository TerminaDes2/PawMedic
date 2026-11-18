package com.pawsmedic.mobile

import android.os.Bundle
import android.content.res.Configuration
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalConfiguration
import com.pawsmedic.features.auth.data.datasource.SupabaseAuthDataSourceImpl
import com.pawsmedic.features.auth.data.repository.SupabaseAuthRepository
import com.pawsmedic.features.auth.domain.usecase.LoginUseCase
import com.pawsmedic.features.auth.domain.usecase.LoginWithGoogleUseCase
import com.pawsmedic.features.auth.domain.usecase.RegisterUseCase
import com.pawsmedic.features.auth.domain.usecase.ResendOtpUseCase
import com.pawsmedic.features.auth.domain.usecase.RestoreSessionUseCase
import com.pawsmedic.features.auth.domain.usecase.VerifyOtpUseCase
import com.pawsmedic.features.auth.presentation.login.LoginViewModel
import com.pawsmedic.features.auth.presentation.navigation.AuthNavHost
import com.pawsmedic.features.auth.presentation.register.RegisterViewModel
import com.pawsmedic.features.auth.presentation.splash.SplashViewModel
import com.pawsmedic.mobile.auth.GoogleSignInHelper
import com.pawsmedic.mobile.notifications.NotificationDeviceSync
import com.pawsmedic.features.pets.presentation.settings.LocalNotificationSettings
import com.pawsmedic.features.pets.presentation.settings.NotificationSettingsControl
import com.pawsmedic.shared.core.designsystem.theme.PawMedicTheme
import com.pawsmedic.shared.core.designsystem.theme.LocalThemePreference
import com.pawsmedic.shared.core.designsystem.theme.ThemePreference
import com.pawsmedic.shared.core.designsystem.theme.ThemePreferenceControl
import com.pawsmedic.shared.core.designsystem.language.LanguagePreferenceControl
import com.pawsmedic.shared.core.designsystem.language.LocalLanguagePreference
import com.pawsmedic.shared.core.designsystem.language.languageFromPreference
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.core.content.ContextCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        val supabaseClient = (application as PawMedicApplication).supabaseClient
        val dataSource = SupabaseAuthDataSourceImpl(supabaseClient)
        val repository = SupabaseAuthRepository(dataSource)

        val restoreSessionUseCase = RestoreSessionUseCase(repository)
        val loginUseCase = LoginUseCase(repository)
        val loginWithGoogleUseCase = LoginWithGoogleUseCase(repository)
        val registerUseCase = RegisterUseCase(repository)
        val verifyOtpUseCase = VerifyOtpUseCase(repository)
        val resendOtpUseCase = ResendOtpUseCase(repository)

        val googleSignInHelper = GoogleSignInHelper(
            context = this,
            serverClientId = BuildConfig.GOOGLE_WEB_CLIENT_ID
        )

        setContent {
            val scope = rememberCoroutineScope()
            var notificationsEnabled by remember { mutableStateOf(NotificationDeviceSync.enabled(this)) }
            var notificationsBusy by remember { mutableStateOf(false) }
            var notificationsError by remember { mutableStateOf<String?>(null) }
            val authStatus by supabaseClient.auth.sessionStatus.collectAsState()
            var notificationsAllowed by remember { mutableStateOf(NotificationManagerCompat.from(this).areNotificationsEnabled()) }
            DisposableEffect(this) {
                val observer = LifecycleEventObserver { _, event ->
                    if (event == Lifecycle.Event.ON_RESUME) {
                        notificationsAllowed = NotificationManagerCompat.from(this@MainActivity).areNotificationsEnabled()
                    }
                }
                lifecycle.addObserver(observer)
                onDispose { lifecycle.removeObserver(observer) }
            }
            val enableNotifications = {
                scope.launch {
                    notificationsBusy = true
                    notificationsError = null
                    try {
                        NotificationDeviceSync.enable(this@MainActivity)
                        notificationsEnabled = true
                    } catch (e: CancellationException) {
                        throw e
                    } catch (e: Exception) {
                        notificationsError = "No se pudieron activar las notificaciones. Comprueba tu conexión e inténtalo de nuevo."
                    } finally {
                        notificationsBusy = false
                    }
                }
                Unit
            }
            val permissionLauncher = rememberLauncherForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { granted ->
                notificationsAllowed = NotificationManagerCompat.from(this).areNotificationsEnabled()
                if (granted) enableNotifications()
                else notificationsError = "Permite las notificaciones en los ajustes de Android para activarlas."
            }
            val notificationControl = NotificationSettingsControl(
                enabled = notificationsEnabled && notificationsAllowed &&
                    NotificationDeviceSync.registeredUser(this) == (authStatus as? SessionStatus.Authenticated)?.session?.user?.id,
                busy = notificationsBusy,
                error = notificationsError,
                onChange = { enabled ->
                    if (enabled) {
                        if (Build.VERSION.SDK_INT >= 33 &&
                            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
                        ) {
                            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        } else if (!notificationsAllowed) {
                            notificationsError = "Permite las notificaciones de PawMedic en los ajustes de Android."
                        } else {
                            enableNotifications()
                        }
                    } else {
                        scope.launch {
                            notificationsBusy = true
                            notificationsError = null
                            try {
                                NotificationDeviceSync.disable(this@MainActivity)
                                notificationsEnabled = false
                            } catch (e: CancellationException) {
                                throw e
                            } catch (e: Exception) {
                                notificationsError = "No se pudieron desactivar los avisos. Comprueba tu conexión e inténtalo de nuevo."
                            } finally {
                                notificationsBusy = false
                            }
                        }
                    }
                }
            )
            LaunchedEffect(supabaseClient) {
                supabaseClient.auth.sessionStatus
                    .map { (it as? SessionStatus.Authenticated)?.session?.user?.id }
                    .distinctUntilChanged()
                    .collect { if (it != null) NotificationDeviceSync.schedule(this@MainActivity) }
            }
            val preferences = remember { getSharedPreferences("appearance", MODE_PRIVATE) }
            var language by remember {
                mutableStateOf(languageFromPreference(preferences.getString("language", null)))
            }
            val languageControl = remember(language) {
                LanguagePreferenceControl(language) { selected ->
                    preferences.edit().putString("language", selected.name).apply()
                    language = selected
                }
            }
            var themePreference by remember {
                mutableStateOf(
                    ThemePreference.entries.firstOrNull {
                        it.name == preferences.getString("theme", ThemePreference.SYSTEM.name)
                    } ?: ThemePreference.SYSTEM
                )
            }
            val themeControl = remember(themePreference) {
                ThemePreferenceControl(themePreference) { selected ->
                    preferences.edit().putString("theme", selected.name).apply()
                    themePreference = selected
                }
            }
            val systemDark = LocalConfiguration.current.uiMode and Configuration.UI_MODE_NIGHT_MASK ==
                Configuration.UI_MODE_NIGHT_YES
            val darkTheme = when (themePreference) {
                ThemePreference.SYSTEM -> systemDark
                ThemePreference.LIGHT -> false
                ThemePreference.DARK -> true
            }
            SideEffect {
                val bars = WindowCompat.getInsetsController(window, window.decorView)
                bars.isAppearanceLightStatusBars = !darkTheme
                bars.isAppearanceLightNavigationBars = !darkTheme
                @Suppress("DEPRECATION")
                window.statusBarColor = if (darkTheme) android.graphics.Color.rgb(16, 24, 28)
                    else android.graphics.Color.rgb(250, 250, 250)
                @Suppress("DEPRECATION")
                window.navigationBarColor = if (darkTheme) android.graphics.Color.rgb(16, 24, 28)
                    else android.graphics.Color.rgb(250, 250, 250)
            }

            val splashViewModel = remember { SplashViewModel(restoreSessionUseCase) }
            val loginViewModel = remember { LoginViewModel(loginUseCase, loginWithGoogleUseCase) }
            val registerViewModel = remember {
                RegisterViewModel(
                    registerUseCase = registerUseCase,
                    verifyOtpUseCase = verifyOtpUseCase,
                    resendOtpUseCase = resendOtpUseCase,
                    loginWithGoogleUseCase = loginWithGoogleUseCase,
                    recordLegalAcceptance = { termsVersion, privacyVersion ->
                        repository.recordLegalAcceptance(termsVersion, privacyVersion)
                    }
                )
            }

            val onGoogleLoginClick = {
                scope.launch {
                    val result = googleSignInHelper.signIn(language)
                    result.onSuccess { credentials ->
                        loginViewModel.loginWithGoogle(credentials.idToken, credentials.rawNonce)
                    }.onFailure { error ->
                        loginViewModel.onGoogleSignInError(
                            error.message ?: "No se pudo iniciar sesión con Google"
                        )
                    }
                }
                Unit
            }
            val onGoogleRegisterClick = {
                scope.launch {
                    val result = googleSignInHelper.signIn(language)
                    result.onSuccess { credentials ->
                        registerViewModel.registerWithGoogle(credentials.idToken, credentials.rawNonce)
                    }.onFailure { error ->
                        registerViewModel.onGoogleSignInError(
                            error.message ?: "No se pudo registrar con Google"
                        )
                    }
                }
                Unit
            }

            CompositionLocalProvider(
                LocalThemePreference provides themeControl,
                LocalLanguagePreference provides languageControl,
                LocalNotificationSettings provides notificationControl
            ) {
                PawMedicTheme(darkTheme = darkTheme) {
                    AuthNavHost(
                        splashViewModel = splashViewModel,
                        loginViewModel = loginViewModel,
                        registerViewModel = registerViewModel,
                        supabaseClient = supabaseClient,
                        onGoogleLoginClick = onGoogleLoginClick,
                        onGoogleRegisterClick = onGoogleRegisterClick,
                        beforeSignOut = {
                            try {
                                NotificationDeviceSync.disable(this@MainActivity)
                                notificationsEnabled = false
                            } catch (e: CancellationException) {
                                throw e
                            } catch (e: Exception) {
                                throw IllegalStateException("No se pudo desactivar el dispositivo. Comprueba tu conexión e inténtalo de nuevo.")
                            }
                        },
                        afterAccountDeletion = {
                            NotificationDeviceSync.clearAfterAccountDeletion(this@MainActivity)
                            notificationsEnabled = false
                        }
                    )
                }
            }
        }
    }
}

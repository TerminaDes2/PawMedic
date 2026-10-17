package com.pawsmedic.mobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import com.pawsmedic.core.network.provideSupabaseClient
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
import com.pawsmedic.shared.core.designsystem.theme.PawMedicTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val supabaseClient = provideSupabaseClient(
            url = BuildConfig.SUPABASE_URL,
            anonKey = BuildConfig.SUPABASE_ANON_KEY
        )
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

            val splashViewModel = remember { SplashViewModel(restoreSessionUseCase) }
            val loginViewModel = remember { LoginViewModel(loginUseCase, loginWithGoogleUseCase) }
            val registerViewModel = remember {
                RegisterViewModel(
                    registerUseCase = registerUseCase,
                    verifyOtpUseCase = verifyOtpUseCase,
                    resendOtpUseCase = resendOtpUseCase,
                    loginWithGoogleUseCase = loginWithGoogleUseCase
                )
            }

            val onGoogleLoginClick = {
                scope.launch {
                    val result = googleSignInHelper.signIn("Inicio de sesión")
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
                    val result = googleSignInHelper.signIn("Registro")
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

            PawMedicTheme {
                AuthNavHost(
                    splashViewModel = splashViewModel,
                    loginViewModel = loginViewModel,
                    registerViewModel = registerViewModel,
                    supabaseClient = supabaseClient,
                    onGoogleLoginClick = onGoogleLoginClick,
                    onGoogleRegisterClick = onGoogleRegisterClick
                )
            }
        }
    }
}

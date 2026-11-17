package com.pawsmedic.shared.core.designsystem.components

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import com.pawsmedic.shared.core.designsystem.language.AppLanguage
import com.pawsmedic.shared.core.designsystem.language.LocalLanguagePreference
import java.util.Locale

@Composable
internal actual fun LocalizedDatePickerContent(content: @Composable () -> Unit) {
    val language = LocalLanguagePreference.current?.selected
    if (language == null) {
        content()
        return
    }
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val localizedConfiguration = remember(configuration, language) {
        Configuration(configuration).apply {
            setLocale(Locale.forLanguageTag(if (language == AppLanguage.ENGLISH) "en" else "es"))
        }
    }
    val localizedContext = remember(context, localizedConfiguration) {
        context.createConfigurationContext(localizedConfiguration)
    }
    CompositionLocalProvider(
        LocalContext provides localizedContext,
        LocalConfiguration provides localizedConfiguration
    ) {
        key(language) { content() }
    }
}

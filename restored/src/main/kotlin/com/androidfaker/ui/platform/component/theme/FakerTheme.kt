package com.androidfaker.ui.platform.component.theme

import android.os.Build
import android.view.inputmethod.EditorInfo
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.*
import com.android1500.androidfaker.data.platform.datasource.disk.AppTheme
import com.android1500.androidfaker.data.platform.repository.SettingsRepository
import com.androidfaker.ui.platform.component.colorpicker.PaletteCatalog
import com.materialkolor.dynamicColorScheme
import com.materialkolor.dynamiccolor.ColorSpec
import com.materialkolor.scheme.DynamicScheme

@Composable
fun SettingsTheme(repository: SettingsRepository, content: @Composable () -> Unit) {
    // Repository flow getters allocate collectors, so retain each flow across recompositions.
    val theme by remember(repository) { repository.appThemeFlow }.collectAsState()
    val dynamic by remember(repository) { repository.isDynamicColorsEnabledFlow }.collectAsState()
    val seed by remember(repository) { repository.seedColorFlow }.collectAsState()
    val style by remember(repository) { repository.paletteStyleIndexFlow }.collectAsState()
    FakerTheme(theme, dynamic, seed, style, content)
}

internal fun AppTheme.isDark(systemDark: Boolean) = when (this) {
    AppTheme.DEFAULT -> systemDark
    AppTheme.DARK -> true
    AppTheme.LIGHT -> false
}

@Composable
fun FakerTheme(theme: AppTheme, dynamicColor: Boolean, seedColor: Int, paletteStyleIndex: Int,
    content: @Composable () -> Unit) {
    val dark = theme.isDark(isSystemInDarkTheme())
    val context = LocalContext.current
    val scheme = if (dynamicColor && Build.VERSION.SDK_INT >= 31) {
        if (dark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    } else {
        remember(seedColor, paletteStyleIndex, dark) {
            dynamicColorScheme(Color(seedColor), dark, style = PaletteCatalog.style(paletteStyleIndex),
                contrastLevel = 0.0, specVersion = ColorSpec.SpecVersion.SPEC_2021,
                platform = DynamicScheme.Platform.PHONE)
        }
    }
    MaterialTheme(colorScheme = scheme, typography = FakerTypography) {
        IncognitoInput(content)
    }
}

@OptIn(ExperimentalComposeUiApi::class)
internal fun noPersonalizedLearning(request: PlatformTextInputMethodRequest): PlatformTextInputMethodRequest =
    PlatformTextInputMethodRequest { attributes ->
        request.createInputConnection(attributes).also {
            attributes.imeOptions = attributes.imeOptions or EditorInfo.IME_FLAG_NO_PERSONALIZED_LEARNING
        }
    }

@OptIn(ExperimentalComposeUiApi::class)
private val noLearningInterceptor = PlatformTextInputInterceptor { request, next ->
    next.startInputMethod(noPersonalizedLearning(request))
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun IncognitoInput(content: @Composable () -> Unit) {
    InterceptPlatformTextInput(noLearningInterceptor, content)
}

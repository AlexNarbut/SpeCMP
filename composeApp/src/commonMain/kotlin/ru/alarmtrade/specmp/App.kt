package ru.alarmtrade.specmp

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview
import ru.alarmtrade.specmp.core.common.di.commonModule
import ru.alarmtrade.specmp.core.localization.LocalAppLocale
import ru.alarmtrade.specmp.core.localization.applyAppLocale
import ru.alarmtrade.specmp.core.localization.prepareAppLocale
import ru.alarmtrade.specmp.core.specialist.specialistModule
import ru.alarmtrade.specmp.core.ui.theme.AppTheme
import ru.alarmtrade.specmp.feature.authstate.di.authStateModule
import ru.alarmtrade.specmp.feature.login.di.loginModule
import ru.alarmtrade.specmp.feature.main.di.mainModule
import ru.alarmtrade.specmp.navigation.AppNavigation
import org.koin.compose.KoinApplication

@Preview
@Composable
fun App(
    onThemeChanged: @Composable (isDark: Boolean) -> Unit = {}
) {
    var appLocale by remember { mutableStateOf(prepareAppLocale()) }

    KoinApplication(application = {
        modules(commonModule, specialistModule, authStateModule, loginModule, mainModule)
    }) {
        CompositionLocalProvider(LocalAppLocale provides appLocale) {
            AppTheme(onThemeChanged) {
                AppNavigation(
                    onLocaleChanged = { locale ->
                        applyAppLocale(locale)
                        appLocale = locale
                    },
                )
            }
        }
    }
}

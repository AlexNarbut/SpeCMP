package ru.alarmtrade.specmp

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import ru.alarmtrade.specmp.core.common.di.commonModule
import ru.alarmtrade.specmp.core.ui.theme.AppTheme
import ru.alarmtrade.specmp.feature.login.di.loginModule
import ru.alarmtrade.specmp.feature.login.di.platformLoginModule
import ru.alarmtrade.specmp.navigation.AppNavigation
import org.koin.compose.KoinApplication

@Preview
@Composable
fun App(
    onThemeChanged: @Composable (isDark: Boolean) -> Unit = {}
) {
    KoinApplication(application = {
        modules(commonModule, loginModule, platformLoginModule)
    }) {
        AppTheme(onThemeChanged) {
            AppNavigation()
        }
    }
}

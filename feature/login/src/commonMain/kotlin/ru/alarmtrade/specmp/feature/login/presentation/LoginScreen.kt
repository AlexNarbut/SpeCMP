package ru.alarmtrade.specmp.feature.login.presentation

import androidx.compose.runtime.Composable
import ru.alarmtrade.specmp.core.localization.AppLocale
import ru.alarmtrade.specmp.feature.login.presentation.screen.authroot.AuthRootScreen

@Composable
fun LoginScreen(
    appLocale: AppLocale,
    onLocaleChanged: (AppLocale) -> Unit,
    onLoginSuccess: () -> Unit,
) {
    AuthRootScreen(
        appLocale = appLocale,
        onLocaleChanged = onLocaleChanged,
        onLoginSuccess = onLoginSuccess,
    )
}

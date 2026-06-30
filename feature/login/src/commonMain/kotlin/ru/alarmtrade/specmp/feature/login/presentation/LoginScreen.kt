package ru.alarmtrade.specmp.feature.login.presentation

import androidx.compose.runtime.Composable
import ru.alarmtrade.specmp.feature.login.presentation.screen.authroot.AuthRootScreen

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
) {
    AuthRootScreen(
        onLoginSuccess = onLoginSuccess,
    )
}

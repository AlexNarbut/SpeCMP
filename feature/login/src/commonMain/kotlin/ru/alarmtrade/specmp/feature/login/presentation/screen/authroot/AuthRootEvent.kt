package ru.alarmtrade.specmp.feature.login.presentation.screen.authroot

sealed interface AuthRootEvent {
    data object Authorized : AuthRootEvent
}

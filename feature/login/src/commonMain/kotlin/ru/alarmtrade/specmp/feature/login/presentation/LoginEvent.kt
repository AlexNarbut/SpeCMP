package ru.alarmtrade.specmp.feature.login.presentation

sealed interface LoginEvent {
    data object Authorized : LoginEvent
}

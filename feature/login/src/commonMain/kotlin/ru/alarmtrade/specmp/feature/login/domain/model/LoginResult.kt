package ru.alarmtrade.specmp.feature.login.domain.model

sealed interface LoginResult {
    data object Authorized : LoginResult
    data object ActivationRequired : LoginResult
}

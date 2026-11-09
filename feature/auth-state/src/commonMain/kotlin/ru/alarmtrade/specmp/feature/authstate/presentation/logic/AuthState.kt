package ru.alarmtrade.specmp.feature.authstate.presentation.logic

enum class AuthState {
    WAITING,
    NO_AUTH,
    AUTHED,
    CHECK_FAILED,
}

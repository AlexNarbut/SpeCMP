package ru.alarmtrade.specmp.feature.authstate.domain.model

enum class AuthState {
    WAITING,
    NO_AUTH,
    AUTHED,
    CHECK_FAILED,
}

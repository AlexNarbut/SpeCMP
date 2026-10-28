package ru.alarmtrade.specmp.feature.authstate.presentation.logic

/** Какую зону показывать: ожидание проверки сессии, вход, авторизованная зона или ошибка проверки (с «Повторить»). */
enum class AuthState {
    WAITING,
    NO_AUTH,
    AUTHED,
    CHECK_FAILED
}

package ru.alarmtrade.specmp.feature.authstate.presentation.logic

import androidx.compose.runtime.Immutable

/** [session] растёт при каждом входе — ключ `ViewModelScope`: VM прежнего аккаунта не достаются новому входу. */
@Immutable
data class AuthStateScreenState(val state: AuthState = AuthState.WAITING, val session: Int = 0)

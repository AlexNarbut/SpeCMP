package ru.alarmtrade.specmp.feature.authstate.presentation.logic

import androidx.compose.runtime.Immutable
import ru.alarmtrade.specmp.feature.authstate.domain.model.AuthState

/**
 * Состояние корня авторизации. [session] растёт при каждом входе в [AuthState.AUTHED] — ключ хранилища ViewModel
 * авторизованной зоны (`ViewModelScope`): VM прежнего аккаунта не достаются новому входу.
 */
@Immutable
data class AuthStateScreenState(val state: AuthState = AuthState.WAITING, val session: Int = 0)

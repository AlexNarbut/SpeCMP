package ru.alarmtrade.specmp.feature.device.interaction.ui.presentation.host

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.runningFold
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ru.alarmtrade.specmp.core.ui.effect.sideEffectFlow
import ru.alarmtrade.specmp.core.ui.viewmodel.BaseViewModel
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.DeviceSessionState
import ru.alarmtrade.specmp.feature.device.interaction.domain.session.DeviceSession

/**
 * VM обёртки flow работы с устройством: состояние сессии + последний подключённый блок
 * (по нему остаётся в композиции дочерний flow, пока связь потеряна) и выход из flow.
 */
class DeviceInteractionViewModel(
    private val deviceSession: DeviceSession,
    private val appScope: CoroutineScope,
) : BaseViewModel() {

    val state: StateFlow<DeviceInteractionState> = deviceSession.state
        .runningFold(DeviceInteractionState()) { previous, session ->
            DeviceInteractionState(
                session = session,
                device = (session as? DeviceSessionState.Ready)?.device ?: previous.device,
            )
        }
        .combine(deviceSession.transport) { state, transport -> state.copy(transport = transport ?: state.transport) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, DeviceInteractionState())

    private val _effect = sideEffectFlow<DeviceInteractionEffect>()
    val effect: SharedFlow<DeviceInteractionEffect> = _effect.asSharedFlow()

    private var exited = false

    /** Выход из всего flow: закрыть сессию, затем уйти. */
    fun onExit() {
        safeLaunch {
            executeWithProceedLock {
                exited = true
                deviceSession.close()
                _effect.tryEmit(DeviceInteractionEffect.Exit)
            }
        }
    }

    override fun onCleared() {
        // Запись ушла не через onExit (например, сброс навигации при выходе из аккаунта) —
        // сессию всё равно закрываем; viewModelScope уже отменён.
        if (!exited) appScope.launch { deviceSession.close() }
        super.onCleared()
    }
}

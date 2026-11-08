package ru.alarmtrade.specmp.feature.device.interaction.ui.presentation.host

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.runningFold
import kotlinx.coroutines.flow.stateIn
import ru.alarmtrade.specmp.core.ui.effect.sideEffectFlow
import ru.alarmtrade.specmp.core.ui.viewmodel.BaseViewModel
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.DeviceSessionState
import ru.alarmtrade.specmp.feature.device.interaction.domain.session.DeviceSession

/**
 * VM обёртки flow работы с устройством: состояние сессии + последний блок в рабочем режиме
 * (по нему остаётся в композиции дочерний flow, пока связь потеряна; загрузчик его сбрасывает), признак
 * «был загрузчик», ожидаемая перезагрузка и выход из flow.
 */
class DeviceInteractionViewModel(private val deviceSession: DeviceSession) : BaseViewModel() {

    val state: StateFlow<DeviceInteractionState> = deviceSession.state
        .runningFold(DeviceInteractionState()) { previous, session ->
            DeviceInteractionState(
                session = session,
                // Рабочий режим — блок для дочернего flow; загрузчик сбрасывает его: дочерний flow уходит из композиции
                // и после возврата в рабочий режим стартует заново. Обрыв/подключение блок не трогают.
                device = when (session) {
                    is DeviceSessionState.Ready -> session.device
                    is DeviceSessionState.BluetoothBoot, is DeviceSessionState.FirmwareBoot -> null
                    DeviceSessionState.Connecting, DeviceSessionState.Disconnected -> previous.device
                },
                wasBoot = when (session) {
                    is DeviceSessionState.Ready -> false
                    is DeviceSessionState.BluetoothBoot, is DeviceSessionState.FirmwareBoot -> true
                    DeviceSessionState.Connecting, DeviceSessionState.Disconnected -> previous.wasBoot
                },
            )
        }
        .combine(deviceSession.transport) { state, transport -> state.copy(transport = transport ?: state.transport) }
        .combine(deviceSession.reboot) { state, reboot -> state.copy(reboot = reboot) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, DeviceInteractionState())

    private val _effect = sideEffectFlow<DeviceInteractionEffect>()
    val effect: SharedFlow<DeviceInteractionEffect> = _effect.asSharedFlow()

    private val exitRequested = MutableStateFlow(false)

    /**
     * Выход из всего flow: закрыть сессию и сразу уйти. Закрытие идёт в скоупе сессии и не ждётся здесь —
     * следующее подключение (экран поиска) само дождётся его. Повторное нажатие не даёт второго Exit.
     */
    fun onExit() {
        if (!exitRequested.compareAndSet(expect = false, update = true)) return
        deviceSession.close()
        _effect.tryEmit(DeviceInteractionEffect.Exit)
    }

    override fun onCleared() {
        // Запись могла уйти не через onExit (например, сброс навигации при выходе из аккаунта) — закрываем
        // сессию всегда; повторное закрытие безопасно и сериализовано внутри сессии.
        deviceSession.close()
        super.onCleared()
    }
}

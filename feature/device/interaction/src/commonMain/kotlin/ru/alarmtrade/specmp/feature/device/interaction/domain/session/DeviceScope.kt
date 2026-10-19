package ru.alarmtrade.specmp.feature.device.interaction.domain.session

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.AlarmDevice
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.DeviceSessionState

/**
 * Работа экрана с подключённым устройством (замена базового `AlarmDeviceViewModel` старого приложения).
 * VM создаёт его со своим скоупом: `DeviceScope(viewModelScope, deviceSession)`.
 *
 * - [state] — состояние сессии;
 * - [launch] — работа с устройством, отменяется при потере связи (на дисконнекте не стартует);
 * - [onReady] — действие на каждое (пере)подключение.
 */
class DeviceScope(private val scope: CoroutineScope, deviceSession: DeviceSession) {
    val state: StateFlow<DeviceSessionState> = deviceSession.state

    private val deviceJob = SupervisorJob(scope.coroutineContext[Job])

    init {
        scope.launch {
            state.collect { if (it !is DeviceSessionState.Ready) deviceJob.cancelChildren() }
        }
    }

    /** Работа с устройством; без связи не запускается (возвращает null). */
    fun launch(block: suspend CoroutineScope.() -> Unit): Job? {
        if (state.value !is DeviceSessionState.Ready) return null
        return scope.launch(deviceJob, block = block)
    }

    /** [action] на каждое подключение (в т.ч. повторное); отменяется при потере связи. */
    fun onReady(action: suspend (AlarmDevice) -> Unit): Job = scope.launch {
        state.map { (it as? DeviceSessionState.Ready)?.device }
            .distinctUntilChanged()
            .collectLatest { device -> if (device != null) action(device) }
    }
}

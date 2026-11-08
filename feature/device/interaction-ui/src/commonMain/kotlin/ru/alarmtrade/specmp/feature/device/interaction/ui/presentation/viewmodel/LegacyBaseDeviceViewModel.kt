package ru.alarmtrade.specmp.feature.device.interaction.ui.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import ru.alarmtrade.specmp.core.ui.viewmodel.LegacyBaseViewModel
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.AlarmDevice
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.DeviceSessionState
import ru.alarmtrade.specmp.feature.device.interaction.domain.session.DeviceSession

/**
 * Базовый класс ViewModel, работающих с подключённым блоком (замена `AlarmDeviceViewModel` старого приложения).
 *
 * - [connectedDevice] / [connectedDeviceName] — блок на связи (`Ready`) как источник для `combine` в state;
 *   [currentDevice] — его снимок для интентов.
 * - [launchOnDevice] — работа с блоком: отменяется при потере связи, без связи не стартует. Работу с блоком
 *   не запускать через `safeLaunch` — она переживёт отключение и допишет в state данные прежнего блока.
 * - [onEachDeviceReady] / [onDeviceLost] — действие на каждое (пере)подключение / потерю связи; запускаются из
 *   `launchOnFirstStart { … }` на цепочке state наследника — init-стадия экрана (docs/patterns.md), не `init {}`.
 *
 * Необработанные исключения работы с блоком логируются, как у `safeLaunch`, а не роняют приложение.
 */
abstract class LegacyBaseDeviceViewModel(protected val deviceSession: DeviceSession) : LegacyBaseViewModel() {

    protected val connectedDevice: Flow<AlarmDevice?> = deviceSession.state
        .map { it.readyDevice() }
        .distinctUntilChanged()

    protected val connectedDeviceName: Flow<String?> = connectedDevice
        .map { it?.systemName }
        .distinctUntilChanged()

    protected val currentDevice: AlarmDevice?
        get() = deviceSession.state.value.readyDevice()

    private val deviceJob = SupervisorJob(viewModelScope.coroutineContext[Job])
    private val deviceErrors = CoroutineExceptionHandler { _, e -> logger.e(e) { "Unhandled device work error" } }

    init {
        viewModelScope.launch(deviceErrors) {
            deviceSession.state.collect { if (it !is DeviceSessionState.Ready) deviceJob.cancelChildren() }
        }
    }

    /** Работа с блоком; без связи не запускается (возвращает null). */
    protected fun launchOnDevice(block: suspend CoroutineScope.() -> Unit): Job? {
        if (currentDevice == null) return null
        return viewModelScope.launch(deviceJob + deviceErrors, block = block)
    }

    /**
     * [action] на каждую потерю связи с блоком (сессия вышла из `Ready`); работа [launchOnDevice] к этому моменту уже
     * отменена. Для экранов, которые на обрыве возвращаются к исходному состоянию процесса.
     */
    protected fun onDeviceLost(action: () -> Unit): Job = viewModelScope.launch(deviceErrors) {
        deviceSession.state
            .map { it is DeviceSessionState.Ready }
            .distinctUntilChanged()
            .collect { ready -> if (!ready) action() }
    }

    /** [action] на каждое подключение (в т.ч. повторное); отменяется при потере связи. */
    protected fun onEachDeviceReady(action: suspend (AlarmDevice) -> Unit): Job = viewModelScope.launch(deviceErrors) {
        connectedDevice.collectLatest { device -> if (device != null) action(device) }
    }
}

private fun DeviceSessionState.readyDevice(): AlarmDevice? = (this as? DeviceSessionState.Ready)?.device

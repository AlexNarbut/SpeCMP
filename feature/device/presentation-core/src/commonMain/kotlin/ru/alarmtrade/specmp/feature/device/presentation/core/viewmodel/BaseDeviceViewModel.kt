package ru.alarmtrade.specmp.feature.device.presentation.core.viewmodel

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import ru.alarmtrade.specmp.core.ui.viewmodel.BaseViewModel
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.AlarmDevice
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.DeviceSessionState
import ru.alarmtrade.specmp.feature.device.interaction.domain.session.DeviceSession

/** База VM, работающих с подключённым блоком; контракт — `feature/device/AGENTS.md`. */
abstract class BaseDeviceViewModel<S : Any, E : Any>(protected val deviceSession: DeviceSession, initialState: S) :
    BaseViewModel<S, E>(initialState) {

    protected val connectedDevice: Flow<AlarmDevice?> = deviceSession.state
        .map { it.readyDevice() }
        .distinctUntilChanged()

    protected val connectedDeviceName: Flow<String?> = connectedDevice
        .map { it?.systemName }
        .distinctUntilChanged()

    protected val currentDevice: AlarmDevice?
        get() = deviceSession.state.value.readyDevice()

    private val deviceJob = SupervisorJob(viewModelScope.coroutineContext[Job])

    // Стартует с первой работы с блоком: без неё отменять нечего, а наследнику не нужно звать super.onStart().
    private val deviceLossWatch: Job by lazy {
        safeLaunch {
            deviceSession.state.collect { if (it !is DeviceSessionState.Ready) deviceJob.cancelChildren() }
        }
    }

    /** Отменяется при потере связи; без связи не запускается (возвращает null). */
    protected fun launchOnDevice(block: suspend CoroutineScope.() -> Unit): Job? {
        if (currentDevice == null) return null
        deviceLossWatch.start()
        return safeLaunch(deviceJob, block)
    }

    /** На каждую потерю связи; работа [launchOnDevice] к этому моменту уже отменена. Вызывать из `onStart()`. */
    protected fun onDeviceLost(action: () -> Unit): Job = safeLaunch {
        deviceSession.state
            .map { it is DeviceSessionState.Ready }
            .distinctUntilChanged()
            .collect { ready -> if (!ready) action() }
    }

    /** На каждое (пере)подключение; отменяется при потере связи. Вызывать из `onStart()`. */
    protected fun onEachDeviceReady(action: suspend (AlarmDevice) -> Unit): Job = safeLaunch {
        connectedDevice.collectLatest { device -> if (device != null) action(device) }
    }
}

private fun DeviceSessionState.readyDevice(): AlarmDevice? = (this as? DeviceSessionState.Ready)?.device

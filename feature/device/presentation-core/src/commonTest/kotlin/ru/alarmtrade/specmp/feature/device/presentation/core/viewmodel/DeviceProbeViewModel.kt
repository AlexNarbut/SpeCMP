package ru.alarmtrade.specmp.feature.device.presentation.core.viewmodel

import ru.alarmtrade.specmp.feature.device.interaction.domain.service.DeviceSession

internal class DeviceProbeViewModel(deviceSession: DeviceSession) :
    BaseDeviceViewModel<DeviceProbeState, Nothing>(deviceSession, DeviceProbeState()) {

    override suspend fun IntentScope.onStart() {
        onEachDeviceReady { device -> reduce { copy(events = events + "ready:${device.systemId}") } }
        onDeviceLost { reduce { copy(events = events + "lost") } }
    }

    fun onWork(work: suspend () -> Unit) {
        intent {
            reduce { copy(launched = launchOnDevice { work() } != null) }
        }
    }

    fun onKeyedWork(work: suspend () -> Unit) {
        intent {
            reduce { copy(launched = launchOnDevice(KEY) { work() } != null) }
        }
    }

    fun onCancelKeyed() {
        cancelLaunched(KEY)
    }

    fun onReadCurrent() {
        intent {
            reduce { copy(current = currentDevice) }
        }
    }

    private companion object {
        const val KEY = "probe"
    }
}

package ru.alarmtrade.specmp.feature.device.presentation.core.viewmodel

import ru.alarmtrade.specmp.feature.device.interaction.domain.session.DeviceSession

internal class DeviceProbeViewModel(deviceSession: DeviceSession) :
    BaseDeviceViewModel<DeviceProbeState, Nothing>(deviceSession, DeviceProbeState()) {

    override suspend fun onStart() {
        onEachDeviceReady { device -> reduce { copy(events = events + "ready:${device.systemId}") } }
        onDeviceLost { reduce { copy(events = events + "lost") } }
    }

    fun onWork(work: suspend () -> Unit) {
        reduce { copy(launched = launchOnDevice { work() } != null) }
    }

    fun onReadCurrent() {
        reduce { copy(current = currentDevice) }
    }
}

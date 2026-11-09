package ru.alarmtrade.specmp.feature.device.selection.data.transport

import ru.alarmtrade.specmp.core.specialist.DeviceConnectionType
import ru.alarmtrade.specmp.core.specialist.supportedConnectionTypes
import ru.alarmtrade.specmp.feature.device.selection.domain.model.DeviceTransport

internal fun supportedDeviceTransports(): Set<DeviceTransport> {
    val connectionTypes = supportedConnectionTypes()
    return buildSet {
        if (DeviceConnectionType.BLUETOOTH in connectionTypes) add(DeviceTransport.BLE)
        if (DeviceConnectionType.USB in connectionTypes) add(DeviceTransport.USB)
        if (DeviceConnectionType.SERIAL in connectionTypes) add(DeviceTransport.SERIAL)
    }
}

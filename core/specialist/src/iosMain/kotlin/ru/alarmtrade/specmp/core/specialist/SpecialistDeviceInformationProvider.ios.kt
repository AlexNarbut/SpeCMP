package ru.alarmtrade.specmp.core.specialist

import platform.UIKit.UIDevice
import ru.alarmtrade.common.app.domain.AppDeviceInformation
import ru.alarmtrade.common.app.domain.DeviceType

actual fun createSpecialistDeviceInformation(): AppDeviceInformation {
    val device = UIDevice.currentDevice
    val model = device.model
    val osVersion = "${device.systemName} ${device.systemVersion}"
    val deviceId = device.identifierForVendor?.UUIDString ?: "$model:$osVersion"

    return AppDeviceInformation(
        deviceId = deviceId,
        deviceType = DeviceType.IOS,
        deviceDescription = "$model, $osVersion, SpeCMP"
    )
}

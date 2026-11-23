package ru.alarmtrade.specmp.core.specialist

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.toKString
import platform.UIKit.UIDevice
import platform.posix.uname
import platform.posix.utsname
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
        deviceDescription = "$model, $osVersion",
        deviceName = hardwareModel() ?: model,
        osName = device.systemName,
        osVersion = device.systemVersion,
    )
}

// UIDevice.model — только «iPhone»; точную модель (`iPhone17,2`) отдаёт uname.
@OptIn(ExperimentalForeignApi::class)
private fun hardwareModel(): String? = memScoped {
    val info = alloc<utsname>()
    if (uname(info.ptr) != 0) return@memScoped null
    info.machine.toKString().takeIf { it.isNotBlank() }
}

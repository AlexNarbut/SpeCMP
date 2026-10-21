package ru.alarmtrade.specmp.core.specialist

import ru.alarmtrade.common.app.domain.AppDeviceInformation
import ru.alarmtrade.common.app.domain.DeviceType

actual fun createSpecialistDeviceInformation(): AppDeviceInformation {
    val osName = System.getProperty("os.name").orEmpty()
    val osVersion = System.getProperty("os.version").orEmpty()
    val userName = System.getProperty("user.name").orEmpty()
    val javaVendor = System.getProperty("java.vendor").orEmpty()
    val javaVersion = System.getProperty("java.version").orEmpty()

    val deviceType = when {
        osName.contains("mac", ignoreCase = true) -> DeviceType.MACOS
        osName.contains("win", ignoreCase = true) -> DeviceType.WINDOWS
        else -> DeviceType.LINUX
    }

    val deviceId = listOf(osName, osVersion, userName)
        .joinToString(separator = ":")
        .ifBlank { "SpeCMP JVM" }

    return AppDeviceInformation(
        deviceId = deviceId,
        deviceType = deviceType,
        deviceDescription = "$osName $osVersion, JVM $javaVersion $javaVendor, SpeCMP"
    )
}

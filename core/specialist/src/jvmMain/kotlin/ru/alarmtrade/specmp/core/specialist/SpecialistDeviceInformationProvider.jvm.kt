package ru.alarmtrade.specmp.core.specialist

import java.util.concurrent.TimeUnit
import ru.alarmtrade.common.app.domain.AppDeviceInformation
import ru.alarmtrade.common.app.domain.DeviceType
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.core.common.safeRun

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
        deviceDescription = "$osName $osVersion, JVM $javaVersion $javaVendor",
        deviceName = hardwareModel(deviceType) ?: System.getProperty("os.arch").orEmpty(),
        osName = when (deviceType) {
            DeviceType.MACOS -> "macOS"
            DeviceType.WINDOWS -> "Windows"
            else -> "Linux"
        },
        osVersion = osVersion,
    )
}

// JVM не отдаёт модель компьютера; на macOS её знает sysctl (`MacBookAir10,1`), как в старом приложении.
private fun hardwareModel(deviceType: DeviceType): String? {
    if (deviceType != DeviceType.MACOS) return null
    val response = safeRun {
        val process = ProcessBuilder("sysctl", "-n", "hw.model").redirectErrorStream(true).start()
        if (!process.waitFor(SYSCTL_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
            process.destroy()
            return null
        }
        process.inputStream.bufferedReader().readText().trim().takeIf { process.exitValue() == 0 && it.isNotEmpty() }
    }
    return (response as? Response.Success)?.value
}

private const val SYSCTL_TIMEOUT_SECONDS = 2L

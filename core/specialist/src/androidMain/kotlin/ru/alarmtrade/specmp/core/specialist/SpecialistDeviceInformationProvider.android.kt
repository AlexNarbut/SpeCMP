package ru.alarmtrade.specmp.core.specialist

import android.os.Build
import ru.alarmtrade.common.app.domain.AppDeviceInformation
import ru.alarmtrade.common.app.domain.DeviceType

actual fun createSpecialistDeviceInformation(): AppDeviceInformation {
    val model = listOfNotNull(
        Build.MANUFACTURER,
        Build.MODEL,
    )
        .joinToString(separator = " ")
        .trim()
        .ifBlank { "Android device" }

    val osVersion = "Android ${Build.VERSION.RELEASE} (SDK ${Build.VERSION.SDK_INT})"
    val fingerprint = Build.FINGERPRINT.ifBlank { model }

    return AppDeviceInformation(
        deviceId = fingerprint,
        deviceType = DeviceType.ANDROID,
        deviceDescription = "$model, $osVersion",
        deviceName = Build.MODEL.orEmpty().ifBlank { model },
        osName = "Android",
        osVersion = Build.VERSION.RELEASE.orEmpty(),
    )
}

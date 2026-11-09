package ru.alarmtrade.specmp.feature.device.firmware.domain.model

import ru.alarmtrade.specmp.feature.device.interaction.domain.model.CanInfo

/** [version] — числом, как его принимает сервер; `null` — только по коду. [profileName] — та же версия для показа. */
data class FirmwareCan(val code: String, val version: Int? = null, val profileName: String? = null)

/** Только единая CAN-прошивка под CAN-код; заводские CAN-настройки и блок без кода — `null` (ищем по всем версиям). */
fun CanInfo?.toFirmwareCan(): FirmwareCan? = this?.takeIf { !it.isFactory && it.code.isNotBlank() }?.let {
    FirmwareCan(it.code, it.version, it.firmwareVersion.takeIf { name -> name.isNotBlank() })
}

package ru.alarmtrade.specmp.feature.device.firmware.domain.model

import ru.alarmtrade.specmp.feature.device.interaction.domain.model.CanInfo

/**
 * CAN-код и версия CAN-профиля, под которые собрана единая CAN-прошивка: фильтр серверных запросов прошивок
 * основной программы. [version] — числом, как его принимает сервер; `null` — только по коду. [profileName] — та
 * же версия для показа («1.03»).
 */
data class FirmwareCan(val code: String, val version: Int? = null, val profileName: String? = null)

/**
 * CAN блока для запросов прошивок: только единая CAN-прошивка под CAN-код автомобиля. Заводские CAN-настройки
 * (блок ещё не прошит под автомобиль) и блок без кода — `null`: подставлять нечего, ищем по всем версиям.
 */
fun CanInfo?.toFirmwareCan(): FirmwareCan? = this?.takeIf { !it.isFactory && it.code.isNotBlank() }?.let {
    FirmwareCan(it.code, it.version, it.firmwareVersion.takeIf { name -> name.isNotBlank() })
}

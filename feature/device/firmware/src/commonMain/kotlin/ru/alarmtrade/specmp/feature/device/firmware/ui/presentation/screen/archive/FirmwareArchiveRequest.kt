package ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.archive

import kotlinx.serialization.Serializable
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.FirmwareChannel
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.update.UpdateProfile

/**
 * Запрос на открытие архива прошивок (точка входа [FirmwareArchiveEntry]): какой модуль, канал и что установлено.
 * Сериализуется: это ключ маршрута nav-графа.
 *
 * @property installedVersion установленная версия модуля — по ней версии помечаются «Установлена» /
 *   «Старее установленной».
 * @property deviceTitle подзаголовок («Название  ID» блока или имя Bluetooth-модуля, по [profile]).
 */
@Serializable
data class FirmwareArchiveRequest(
    val moduleId: Long,
    val channel: FirmwareChannel,
    val installedVersion: String?,
    val deviceTitle: String,
    val profile: UpdateProfile
)

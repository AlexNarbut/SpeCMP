package ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.archive

import kotlinx.serialization.Serializable
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.FirmwareChannel
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.update.UpdateProfile

/**
 * Параметры архива, которые экран обновления передаёт при открытии. Сериализуются: это ключ маршрута nav-графа.
 *
 * @property installedVersion установленная версия модуля — по ней версии помечаются «Установлена» /
 *   «Старее установленной».
 * @property deviceTitle подзаголовок («Название  ID» блока или имя Bluetooth-модуля, по [profile]).
 */
@Serializable
data class FirmwareArchiveArgs(
    val moduleId: Long,
    val channel: FirmwareChannel,
    val installedVersion: String?,
    val deviceTitle: String,
    val profile: UpdateProfile
)

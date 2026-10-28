package ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.archive

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.FirmwareChannel
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.update.UpdateProfile

/**
 * Запрос на открытие архива прошивок (точка входа [FirmwareArchiveEntry]): какой модуль, канал и что установлено.
 * Сериализуется: это ключ маршрута nav-графа.
 *
 * @property moduleId модуль, версии которого показываем; `null` — основной модуль блока [deviceName] (архив открыт
 *   не из загрузчика, например из карточки автомобиля).
 * @property installedVersion установленная версия модуля — по ней версии помечаются «Установлена» /
 *   «Старее установленной».
 * @property deviceTitle подзаголовок («Название  ID» блока или имя Bluetooth-модуля, по [profile]).
 * @property deviceName модель блока: каталог автомобилей для CAN-фильтра (`null` — каталог с выбором типа) и поиск
 *   основного модуля, если [moduleId] не задан.
 * @property canCode CAN-фильтр при открытии (`null` — все версии); у Bluetooth-модуля фильтра нет.
 * @property viewOnly просмотр версий без выбора для установки.
 */
@Serializable
@SerialName("device.firmware.FirmwareArchiveRequest")
data class FirmwareArchiveRequest(
    val moduleId: Long?,
    val channel: FirmwareChannel,
    val installedVersion: String?,
    val deviceTitle: String,
    val profile: UpdateProfile,
    val deviceName: String? = null,
    val canCode: String? = null,
    val viewOnly: Boolean = false
) {
    /** CAN-фильтр имеет смысл только для основной программы блока. */
    val canFilterEnabled: Boolean get() = profile == UpdateProfile.Main
}

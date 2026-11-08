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
 * @property canCode CAN-фильтр при открытии (`null` — по CAN блока [deviceCanCode], если он есть, иначе все версии);
 *   у Bluetooth-модуля фильтра нет.
 * @property viewOnly просмотр версий без выбора для установки.
 * @property deviceCanCode CAN-код единой CAN-прошивки подключённого блока: версии под него подсвечиваются, «Вернуть
 *   CAN блока» возвращает фильтр к нему.
 * @property deviceCanVersion версия CAN-профиля блока: уточняет фильтр, только пока в нём код блока; по ней (с кодом)
 *   сборка считается «Установлена».
 * @property deviceCanProfile та же версия CAN-профиля для показа («1.03»).
 * @property deviceCanFactory у блока заводские CAN-настройки (кода нет): подсказка выбрать автомобиль.
 * @property noUnit вход с главного экрана без блока: подсистема [moduleId] выбрана в списке систем, система —
 *   [deviceName], подсистема — [deviceTitle]; канал меняется на самом экране, у версий «Скачать» и «Поделиться»
 *   вместо выбора.
 * @property canFilterAllowed CAN-фильтр есть: только у основной программы сигнализации (у остальных подсистем и
 *   Bluetooth-модуля прошивки к CAN-коду не привязаны).
 * @property moduleName имя прошивки подсистемы («DX4G») — начало имени файла при «Скачать»; `null` — [deviceName].
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
    val viewOnly: Boolean = false,
    val deviceCanCode: String? = null,
    val deviceCanVersion: Int? = null,
    val deviceCanProfile: String? = null,
    val deviceCanFactory: Boolean = false,
    val noUnit: Boolean = false,
    val canFilterAllowed: Boolean = true,
    val moduleName: String? = null,
)

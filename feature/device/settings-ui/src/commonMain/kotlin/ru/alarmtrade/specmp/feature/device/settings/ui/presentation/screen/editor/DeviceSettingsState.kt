package ru.alarmtrade.specmp.feature.device.settings.ui.presentation.screen.editor

import androidx.compose.runtime.Immutable
import ru.alarmtrade.specmp.feature.device.settings.domain.model.Setting
import ru.alarmtrade.specmp.feature.device.settings.domain.model.SettingNode
import ru.alarmtrade.specmp.feature.device.settings.domain.model.SettingValue
import ru.alarmtrade.specmp.feature.device.settings.domain.model.SettingsSearchHit
import ru.alarmtrade.specmp.feature.device.settings.domain.model.SettingsSection
import ru.alarmtrade.specmp.feature.vehicle.domain.model.CarSummary

/** Состояние редактора настроек: меню, изменения, окно редактирования, диалоги, запись. */
@Immutable
internal data class DeviceSettingsState(
    val header: SettingsHeader? = null,
    val menu: SettingsMenu = SettingsMenu.Loading,
    /** Изменённые и не записанные настройки (id) — подсветка строк и панель «Изменено: N». */
    val changedIds: Set<Int> = emptySet(),
    /** Сколько настроек изменено внутри раздела (id раздела → число) — счётчики в строках, дереве, крошках. */
    val sectionChanges: Map<Int, Int> = emptyMap(),
    val searchQuery: String = "",
    val searchHits: List<SettingsSearchHit> = emptyList(),
    val editing: EditingSetting? = null,
    val dialog: SettingsDialog? = null,
    /** Идёт запись в устройство — блокирующий прогресс. */
    val isWriting: Boolean = false,
    /** Идёт сброс блока к заводским настройкам — блокирующий прогресс. */
    val isResetting: Boolean = false,
    /** Источник поддерживает сброс к заводским (пункт меню «⋯»). */
    val canResetToFactory: Boolean = false,
    /** Ширина колонки дерева, заданная пользователем (dp); `null` — по умолчанию раскладки. */
    val treeWidthDp: Int? = null,
    /** Автомобили по id (подпись к CAN-коду в строке и в окне). */
    val cars: Map<Int, CarSummary> = emptyMap()
) {
    val changedCount: Int get() = changedIds.size
}

/** Шапка: модель и номер блока; [singleCanCode] — код единой CAN-прошивки, если она есть. */
internal data class SettingsHeader(val deviceName: String, val deviceId: Long, val singleCanCode: String?)

internal sealed interface SettingsMenu {
    data object Loading : SettingsMenu
    data object Error : SettingsMenu
    data class Ready(val root: SettingsSection) : SettingsMenu
}

/** Открытое окно редактирования: [original] — значение «как было» (до правок), [draft] — набираемое. */
internal data class EditingSetting(
    val setting: Setting,
    val original: SettingValue,
    val draft: SettingValue,
    /** Поиск автомобиля по CAN-коду в окне CAN-кода. */
    val carSearch: CarSearch = CarSearch.Idle
)

internal sealed interface CarSearch {
    data object Idle : CarSearch
    data object Loading : CarSearch

    /** Найдено (пусто — для кода автомобиля нет, код всё равно можно применить). */
    data class Found(val cars: List<CarSummary>) : CarSearch

    /** Нет связи с сервером — код можно применить без автомобиля. */
    data object Failed : CarSearch
}

internal sealed interface SettingsDialog {
    data class Description(val node: SettingNode) : SettingsDialog

    /** Выход с незаписанными изменениями — подтверждение; [target] — куда после выхода. */
    data class ExitConfirm(val target: ExitTarget) : SettingsDialog

    /** CAN-код на блоке с единой CAN-прошивкой меняется прошивкой CAN — предложение перейти к обновлению. */
    data object SingleCanFirmware : SettingsDialog
    data object WriteError : SettingsDialog
    data object ResetConfirm : SettingsDialog
    data object ResetError : SettingsDialog
}

/** One-shot события экрана. */
internal sealed interface DeviceSettingsEffect {
    data class Written(val count: Int) : DeviceSettingsEffect
    data object SetValueFailed : DeviceSettingsEffect
    data object ResetDone : DeviceSettingsEffect
    data class Exit(val target: ExitTarget) : DeviceSettingsEffect
}

/** Куда уходим из настроек: назад (корень монтажа) или в «Проверку прошивок» (единая CAN-прошивка). */
internal enum class ExitTarget { BACK, FIRMWARE_UPDATE }

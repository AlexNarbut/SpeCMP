package ru.alarmtrade.specmp.feature.device.settings.ui.presentation.screen.editor

import androidx.compose.runtime.Immutable
import ru.alarmtrade.specmp.feature.device.settings.domain.model.Setting
import ru.alarmtrade.specmp.feature.device.settings.domain.model.SettingNode
import ru.alarmtrade.specmp.feature.device.settings.domain.model.SettingValue
import ru.alarmtrade.specmp.feature.device.settings.domain.model.SettingsSearchHit
import ru.alarmtrade.specmp.feature.device.settings.domain.model.SettingsSection

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
    /** Ширина колонки дерева, заданная пользователем (dp); `null` — по умолчанию раскладки. */
    val treeWidthDp: Int? = null
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
internal data class EditingSetting(val setting: Setting, val original: SettingValue, val draft: SettingValue)

internal sealed interface SettingsDialog {
    data class Description(val node: SettingNode) : SettingsDialog
    data object ExitConfirm : SettingsDialog
    data object WriteError : SettingsDialog
}

/** One-shot события экрана. */
internal sealed interface DeviceSettingsEffect {
    data class Written(val count: Int) : DeviceSettingsEffect
    data object SetValueFailed : DeviceSettingsEffect
    data object Exit : DeviceSettingsEffect
}

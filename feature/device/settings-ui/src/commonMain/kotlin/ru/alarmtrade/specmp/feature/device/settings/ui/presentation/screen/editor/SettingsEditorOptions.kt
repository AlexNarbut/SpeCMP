package ru.alarmtrade.specmp.feature.device.settings.ui.presentation.screen.editor

import ru.alarmtrade.specmp.feature.device.settings.domain.model.SettingsSource
import ru.alarmtrade.specmp.feature.device.settings.domain.model.settings

/** [focusSettingId] — после чтения меню открыть раздел настройки; [openLoadMenu] — сразу выбор «Загрузить из…». */
internal data class SettingsEditorOptions(
    val loadOnOpen: SettingsSource? = null,
    val canWriteToDevice: Boolean = false,
    val focusSettingId: Int? = null,
    val openLoadMenu: Boolean = false,
)

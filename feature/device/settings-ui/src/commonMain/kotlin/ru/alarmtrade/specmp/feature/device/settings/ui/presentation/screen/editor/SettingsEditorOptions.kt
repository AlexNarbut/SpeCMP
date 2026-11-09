package ru.alarmtrade.specmp.feature.device.settings.ui.presentation.screen.editor

import ru.alarmtrade.specmp.feature.device.settings.domain.model.SettingsSource
import ru.alarmtrade.specmp.feature.device.settings.domain.model.settings

internal data class SettingsEditorOptions(val loadOnOpen: SettingsSource? = null, val canWriteToDevice: Boolean = false)

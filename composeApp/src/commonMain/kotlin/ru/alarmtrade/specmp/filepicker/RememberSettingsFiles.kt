package ru.alarmtrade.specmp.filepicker

import androidx.compose.runtime.Composable
import ru.alarmtrade.specmp.feature.device.settings.ui.presentation.platform.SettingsFilePicker
import ru.alarmtrade.specmp.feature.device.settings.ui.presentation.platform.SettingsFileSaver

/** Выбор файла настроек `.dxs` в файловом менеджере платформы (см. контракт [SettingsFilePicker]). */
@Composable
expect fun rememberSettingsFilePicker(): SettingsFilePicker

/** Сохранение файла настроек через системный диалог (см. контракт [SettingsFileSaver]). */
@Composable
expect fun rememberSettingsFileSaver(): SettingsFileSaver

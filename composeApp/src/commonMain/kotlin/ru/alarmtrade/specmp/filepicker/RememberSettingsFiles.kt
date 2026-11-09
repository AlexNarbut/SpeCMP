package ru.alarmtrade.specmp.filepicker

import androidx.compose.runtime.Composable
import ru.alarmtrade.specmp.feature.device.presentation.core.platform.SettingsFilePicker
import ru.alarmtrade.specmp.feature.device.presentation.core.platform.SettingsFileSaver

@Composable
expect fun rememberSettingsFilePicker(): SettingsFilePicker

@Composable
expect fun rememberSettingsFileSaver(): SettingsFileSaver

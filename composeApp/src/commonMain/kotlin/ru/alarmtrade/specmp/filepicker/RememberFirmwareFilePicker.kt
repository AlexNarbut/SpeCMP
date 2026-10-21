package ru.alarmtrade.specmp.filepicker

import androidx.compose.runtime.Composable
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.platform.FirmwareFilePicker

/** Выбор файла прошивки в файловом менеджере платформы (см. контракт [FirmwareFilePicker]). */
@Composable
expect fun rememberFirmwareFilePicker(): FirmwareFilePicker

package ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.platform

import androidx.compose.runtime.Composable

/** Выбор файла прошивки в файловом менеджере платформы (см. контракт [FirmwareFilePicker]). */
@Composable
expect fun rememberFirmwareFilePicker(): FirmwareFilePicker

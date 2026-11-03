package ru.alarmtrade.specmp.filepicker

import androidx.compose.runtime.Composable
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.platform.FirmwareFileExporter

/** Файл прошивки из архива без блока: сохранить, поделиться, открыть папку (см. контракт [FirmwareFileExporter]). */
@Composable
expect fun rememberFirmwareFileExporter(): FirmwareFileExporter

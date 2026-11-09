package ru.alarmtrade.specmp.filepicker

import androidx.compose.runtime.Composable
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.platform.FirmwareFileExporter

@Composable
expect fun rememberFirmwareFileExporter(): FirmwareFileExporter

package ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.platform

import androidx.compose.runtime.Composable
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope

/** Android копирует выбранный документ в [appScope], переживая уход экрана. */
@Composable
expect fun rememberFirmwareFilePicker(appScope: CoroutineScope, ioDispatcher: CoroutineDispatcher): FirmwareFilePicker

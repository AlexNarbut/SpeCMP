package ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.platform

import androidx.compose.runtime.Composable
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope

/**
 * Выбор файла прошивки в файловом менеджере платформы (см. контракт [FirmwareFilePicker]). [appScope] и
 * [ioDispatcher] передаёт корень композиции: Android копирует выбранный документ в нём, переживая уход экрана.
 */
@Composable
expect fun rememberFirmwareFilePicker(appScope: CoroutineScope, ioDispatcher: CoroutineDispatcher): FirmwareFilePicker

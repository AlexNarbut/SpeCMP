package ru.alarmtrade.specmp.flasher.filepicker

import androidx.compose.runtime.Composable
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.platform.FirmwareFilePicker

/**
 * Выбор файла прошивки в файловом менеджере платформы (контракт [FirmwareFilePicker]). Повторяет реализацию
 * `composeApp/filepicker` (Android — SAF + копия в `cacheDir`, desktop — `JFileChooser` без копии): корни приложений
 * друг от друга не зависят.
 */
@Composable
expect fun rememberFirmwareFilePicker(): FirmwareFilePicker

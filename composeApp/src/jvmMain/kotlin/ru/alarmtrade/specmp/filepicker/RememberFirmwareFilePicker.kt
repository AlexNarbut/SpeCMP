package ru.alarmtrade.specmp.filepicker

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import javax.swing.JFileChooser
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.platform.FirmwareFileLimits
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.platform.FirmwareFilePickResult
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.platform.FirmwareFilePicker
import specmp.composeapp.generated.resources.Res
import specmp.composeapp.generated.resources.composeapp_picker_title_file

@Composable
actual fun rememberFirmwareFilePicker(): FirmwareFilePicker {
    val title = stringResource(Res.string.composeapp_picker_title_file)
    return remember(title) { DesktopFirmwareFilePicker(title) }
}

private class DesktopFirmwareFilePicker(private val title: String) : FirmwareFilePicker {

    // Диалог модальный и синхронный: результат кладётся в буфер сразу после закрытия.
    private val channel = Channel<FirmwareFilePickResult>(Channel.UNLIMITED)
    override val results: Flow<FirmwareFilePickResult> = channel.receiveAsFlow()

    override fun launch() {
        val chooser = JFileChooser().apply {
            dialogTitle = title
            fileSelectionMode = JFileChooser.FILES_ONLY
            isMultiSelectionEnabled = false
        }
        val file = if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) chooser.selectedFile else null
        // Desktop отдаёт SDK оригинал без копии (файл только читается и не удаляется).
        val result = when {
            file == null -> FirmwareFilePickResult.Cancelled
            !file.isFile || !file.canRead() -> FirmwareFilePickResult.Failed
            file.length() > FirmwareFileLimits.MAX_BYTES -> FirmwareFilePickResult.TooLarge
            else -> FirmwareFilePickResult.Picked(file.name, file.absolutePath)
        }
        channel.trySend(result)
    }
}

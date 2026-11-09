package ru.alarmtrade.specmp.filepicker

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import co.touchlab.kermit.Logger
import java.io.File
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.common.file.safeFileName
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.app_picker_title_file
import ru.alarmtrade.specmp.feature.device.presentation.core.platform.SettingsFilePickResult
import ru.alarmtrade.specmp.feature.device.presentation.core.platform.SettingsFilePicker
import ru.alarmtrade.specmp.feature.device.presentation.core.platform.SettingsFileSaveResult
import ru.alarmtrade.specmp.feature.device.presentation.core.platform.SettingsFileSaver

@Composable
actual fun rememberSettingsFilePicker(): SettingsFilePicker {
    val title = stringResource(Strings.string.app_picker_title_file)
    return remember(title) { DesktopSettingsFilePicker(title) }
}

@Composable
actual fun rememberSettingsFileSaver(): SettingsFileSaver = remember { DesktopSettingsFileSaver() }

private class DesktopSettingsFilePicker(private val title: String) : SettingsFilePicker {

    // Диалог модальный и синхронный: результат кладётся в буфер сразу после закрытия.
    private val channel = Channel<SettingsFilePickResult>(Channel.UNLIMITED)
    override val results: Flow<SettingsFilePickResult> = channel.receiveAsFlow()

    override fun launch() {
        val chooser = JFileChooser().apply {
            dialogTitle = title
            fileSelectionMode = JFileChooser.FILES_ONLY
            isMultiSelectionEnabled = false
            fileFilter = FileNameExtensionFilter(DXS_FILTER, DXS_EXTENSION)
        }
        val file = if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) chooser.selectedFile else null
        // Desktop отдаёт SDK сам файл: «Сохранить» в редакторе перезаписывает его.
        val result = when {
            file == null -> SettingsFilePickResult.Cancelled
            !file.isFile || !file.canRead() -> SettingsFilePickResult.Failed
            else -> SettingsFilePickResult.Picked(file.name, file.absolutePath, inPlace = true)
        }
        channel.trySend(result)
    }
}

private class DesktopSettingsFileSaver : SettingsFileSaver {
    // Граница платформенного I/O: любой сбой копирования — Failed.
    @Suppress("TooGenericExceptionCaught")
    override suspend fun save(sourcePath: String, fileName: String): SettingsFileSaveResult {
        val chooser = JFileChooser().apply {
            fileSelectionMode = JFileChooser.FILES_ONLY
            // Имя по умолчанию в текущем каталоге диалога.
            selectedFile = File(currentDirectory, safeFileName(fileName))
            fileFilter = FileNameExtensionFilter(DXS_FILTER, DXS_EXTENSION)
        }
        if (chooser.showSaveDialog(null) != JFileChooser.APPROVE_OPTION) return SettingsFileSaveResult.Cancelled
        val picked = chooser.selectedFile ?: return SettingsFileSaveResult.Cancelled
        val target = if (picked.extension.equals(DXS_EXTENSION, ignoreCase = true)) {
            picked
        } else {
            File(picked.parentFile, "${picked.name}.$DXS_EXTENSION")
        }
        return try {
            File(sourcePath).copyTo(target, overwrite = true)
            SettingsFileSaveResult.Saved(target.name)
        } catch (e: Exception) {
            logger.w(e) { "Settings file save failed" }
            SettingsFileSaveResult.Failed
        }
    }
}

private val logger = Logger.withTag("SettingsFiles")

private const val DXS_EXTENSION = "dxs"
private const val DXS_FILTER = "*.dxs"

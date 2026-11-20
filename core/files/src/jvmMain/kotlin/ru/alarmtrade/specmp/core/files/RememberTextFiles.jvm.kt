package ru.alarmtrade.specmp.core.files

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import co.touchlab.kermit.Logger
import java.io.File
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.common.AppDispatchers
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.app_picker_title_file

private val logger = Logger.withTag("TextFiles")

@Composable
actual fun rememberTextFilePicker(dispatchers: AppDispatchers): TextFilePicker {
    val title = stringResource(Strings.string.app_picker_title_file)
    return remember(title, dispatchers) {
        object : TextFilePicker {
            // Граница платформенного I/O: любой сбой чтения — Failed.
            @Suppress("TooGenericExceptionCaught")
            override suspend fun pick(type: TextFileType): TextFilePickResult {
                val file = withContext(dispatchers.main) {
                    val chooser = fileChooser(type.extension).apply { dialogTitle = title }
                    chooser.selectedFile.takeIf { chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION }
                } ?: return TextFilePickResult.Cancelled
                return withContext(dispatchers.io) {
                    try {
                        when {
                            !file.isFile || !file.canRead() -> TextFilePickResult.Failed
                            file.length() > type.maxBytes -> TextFilePickResult.TooLarge
                            else -> decodeTextFile(file.name, file.readBytes(), type.maxBytes)
                        }
                    } catch (e: Exception) {
                        logger.w(e) { "Text file read failed" }
                        TextFilePickResult.Failed
                    }
                }
            }
        }
    }
}

@Composable
actual fun rememberTextFileSaver(dispatchers: AppDispatchers): TextFileSaver = remember(dispatchers) {
    object : TextFileSaver {
        override suspend fun save(suggestedName: String, type: TextFileType, text: String): TextFileSaveResult =
            saveWithDialog(dispatchers, suggestedName, type.extension, text.encodeToByteArray()).toTextResult()
    }
}

internal suspend fun saveWithDialog(
    dispatchers: AppDispatchers,
    suggestedName: String,
    extension: String,
    bytes: ByteArray,
): BinaryFileSaveResult = saveWithDialog(dispatchers, suggestedName, extension) { picked ->
    File(picked.parentFile, fileNameWithExtension(picked.name, extension)).apply { writeBytes(bytes) }
}

/** Диалог сохранения → [write] в выбранный файл (возвращает записанный файл) на `io`. */
// Граница платформенного I/O: любой сбой записи — Failed.
@Suppress("TooGenericExceptionCaught")
internal suspend fun saveWithDialog(
    dispatchers: AppDispatchers,
    suggestedName: String,
    extension: String,
    write: (picked: File) -> File,
): BinaryFileSaveResult {
    val picked = withContext(dispatchers.main) {
        val chooser = fileChooser(extension)
        chooser.selectedFile = File(chooser.currentDirectory, fileNameWithExtension(suggestedName, extension))
        chooser.selectedFile.takeIf { chooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION }
    } ?: return BinaryFileSaveResult.Cancelled
    return withContext(dispatchers.io) {
        try {
            BinaryFileSaveResult.Saved(write(picked).name)
        } catch (e: Exception) {
            logger.w(e) { "File save failed" }
            BinaryFileSaveResult.Failed
        }
    }
}

internal fun fileChooser(extension: String) = JFileChooser().apply {
    fileSelectionMode = JFileChooser.FILES_ONLY
    isMultiSelectionEnabled = false
    fileFilter = FileNameExtensionFilter("*.$extension", extension)
}

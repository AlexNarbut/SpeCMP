package ru.alarmtrade.specmp.core.files

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import co.touchlab.kermit.Logger
import java.awt.Desktop
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import javax.swing.JFileChooser
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.common.AppDispatchers
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.app_picker_title_file

private val logger = Logger.withTag("BinaryFiles")

@Composable
actual fun rememberBinaryFileExporter(dispatchers: AppDispatchers): BinaryFileExporter = remember(dispatchers) {
    object : BinaryFileExporter {
        override val canShare: Boolean = false

        override suspend fun save(fileName: String, type: BinaryFileType, bytes: ByteArray): BinaryFileSaveResult =
            saveWithDialog(dispatchers, fileName, type.extension, bytes)

        override suspend fun share(fileName: String, type: BinaryFileType, bytes: ByteArray): Boolean = false

        override suspend fun open(fileName: String, type: BinaryFileType, bytes: ByteArray): BinaryFileOpenResult =
            withContext(dispatchers.io) { openWithDesktop(fileNameWithExtension(fileName, type.extension), bytes) }

        override suspend fun saveFile(fileName: String, type: BinaryFileType, path: String): BinaryFileSaveResult =
            saveWithDialog(dispatchers, fileName, type.extension) { target -> copyFileTo(path, target, type) }

        override suspend fun shareFile(fileName: String, type: BinaryFileType, path: String): Boolean = false
    }
}

/** Файл выбирается на месте, без копии: путь — сам выбранный файл. */
@Composable
actual fun rememberBinaryFilePicker(dispatchers: AppDispatchers): BinaryFilePicker {
    val title = stringResource(Strings.string.app_picker_title_file)
    return remember(title, dispatchers) {
        object : BinaryFilePicker {
            override suspend fun pick(type: BinaryFileType): BinaryFilePickResult {
                val file = withContext(dispatchers.main) {
                    val chooser = fileChooser(type.extension).apply { dialogTitle = title }
                    if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) chooser.selectedFile else null
                } ?: return BinaryFilePickResult.Cancelled
                val readable = withContext(dispatchers.io) { file.isFile && file.canRead() }
                return if (readable) {
                    BinaryFilePickResult.Picked(file.name, file.absolutePath)
                } else {
                    BinaryFilePickResult.Failed
                }
            }
        }
    }
}

/** Копия [sourcePath] в [target] (с расширением типа); сбой копирования не оставляет недописанный файл. */
internal fun copyFileTo(sourcePath: String, target: File, type: BinaryFileType): File {
    val file = File(target.parentFile, fileNameWithExtension(target.name, type.extension))
    try {
        Files.copy(File(sourcePath).toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING)
    } catch (e: IOException) {
        file.delete()
        throw e
    }
    return file
}

@Composable
actual fun rememberHtmlPdfRenderer(dispatchers: AppDispatchers): HtmlPdfRenderer =
    remember(dispatchers) { DesktopHtmlPdfRenderer(dispatchers) }

@Composable
actual fun rememberPdfPageRasterizer(dispatchers: AppDispatchers): PdfPageRasterizer =
    remember(dispatchers) { DesktopPdfPageRasterizer(dispatchers) }

// Граница платформенного I/O: нет обработчика типа — NoApp, прочее — Failed.
@Suppress("TooGenericExceptionCaught")
private fun openWithDesktop(fileName: String, bytes: ByteArray): BinaryFileOpenResult {
    if (!Desktop.isDesktopSupported()) return BinaryFileOpenResult.NoApp
    val desktop = Desktop.getDesktop().takeIf { it.isSupported(Desktop.Action.OPEN) }
        ?: return BinaryFileOpenResult.NoApp
    val file = try {
        val directory = Files.createTempDirectory(OPEN_DIR_PREFIX).toFile().apply { deleteOnExit() }
        File(directory, fileName).apply {
            writeBytes(bytes)
            deleteOnExit()
        }
    } catch (e: Exception) {
        logger.w(e) { "Temp file write failed" }
        return BinaryFileOpenResult.Failed
    }
    return try {
        desktop.open(file)
        BinaryFileOpenResult.Opened
    } catch (e: IOException) {
        logger.w(e) { "No app to open file" }
        BinaryFileOpenResult.NoApp
    } catch (e: Exception) {
        logger.w(e) { "File open failed" }
        BinaryFileOpenResult.Failed
    }
}

private const val OPEN_DIR_PREFIX = "specmp-open"

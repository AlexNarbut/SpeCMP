package ru.alarmtrade.specmp.core.files

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import co.touchlab.kermit.Logger
import java.awt.Desktop
import java.io.File
import java.io.IOException
import java.nio.file.Files
import kotlinx.coroutines.withContext
import ru.alarmtrade.specmp.core.common.AppDispatchers

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
    }
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
    val desktop = Desktop.getDesktop().takeIf { Desktop.isDesktopSupported() && it.isSupported(Desktop.Action.OPEN) }
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

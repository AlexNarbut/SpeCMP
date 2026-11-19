package ru.alarmtrade.specmp.core.files

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import co.touchlab.kermit.Logger
import java.io.File
import java.util.UUID
import kotlinx.coroutines.withContext
import ru.alarmtrade.specmp.core.common.AppDispatchers

private val logger = Logger.withTag("BinaryFiles")

/** «Поделиться» и «Открыть» — копия в `cache/file-export/` через `ImageCaptureFileProvider` модуля. */
@Composable
actual fun rememberBinaryFileExporter(dispatchers: AppDispatchers): BinaryFileExporter {
    val writer = rememberDocumentWriter(dispatchers)
    val appContext = LocalContext.current.applicationContext
    return remember(writer, appContext) {
        object : BinaryFileExporter {
            override val canShare: Boolean = true

            override suspend fun save(fileName: String, type: BinaryFileType, bytes: ByteArray): BinaryFileSaveResult =
                writer.write(fileName, type.extension, type.mimeType, bytes)

            override suspend fun share(fileName: String, type: BinaryFileType, bytes: ByteArray): Boolean {
                val uri = withContext(dispatchers.io) { appContext.exportCopy(fileName, type, bytes) } ?: return false
                val send = Intent(Intent.ACTION_SEND)
                    .setType(type.mimeType)
                    .putExtra(Intent.EXTRA_STREAM, uri)
                    .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                return withContext(dispatchers.main) {
                    appContext.start(Intent.createChooser(send, null)) == BinaryFileOpenResult.Opened
                }
            }

            override suspend fun open(fileName: String, type: BinaryFileType, bytes: ByteArray): BinaryFileOpenResult {
                val uri = withContext(dispatchers.io) { appContext.exportCopy(fileName, type, bytes) }
                    ?: return BinaryFileOpenResult.Failed
                val view = Intent(Intent.ACTION_VIEW)
                    .setDataAndType(uri, type.mimeType)
                    .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                return withContext(dispatchers.main) { appContext.start(view) }
            }
        }
    }
}

@Composable
actual fun rememberHtmlPdfRenderer(dispatchers: AppDispatchers): HtmlPdfRenderer {
    val appContext = LocalContext.current.applicationContext
    return remember(appContext, dispatchers) { AndroidHtmlPdfRenderer(appContext, dispatchers) }
}

@Composable
actual fun rememberPdfPageRasterizer(dispatchers: AppDispatchers): PdfPageRasterizer {
    val appContext = LocalContext.current.applicationContext
    return remember(appContext, dispatchers) { AndroidPdfPageRasterizer(appContext, dispatchers) }
}

// Граница файлов: сбой записи копии — null.
@Suppress("TooGenericExceptionCaught")
private fun Context.exportCopy(fileName: String, type: BinaryFileType, bytes: ByteArray): Uri? = try {
    // Прежние копии чистим перед записью: получатель уже прочитал их по гранту, система вправе вычистить cache и сама.
    val root = File(cacheDir, EXPORT_DIR).apply {
        deleteRecursively()
        mkdirs()
    }
    val directory = File(root, UUID.randomUUID().toString()).apply { mkdirs() }
    val file = File(directory, fileNameWithExtension(fileName, type.extension)).apply { writeBytes(bytes) }
    FileProvider.getUriForFile(this, packageName + FILE_PROVIDER_AUTHORITY_SUFFIX, file)
} catch (e: Exception) {
    logger.w(e) { "Export copy failed" }
    null
}

// ACTION_VIEW без chooser: нет обработчика — ActivityNotFoundException (resolveActivity на API 30+ без <queries>
// ненадёжен).
@Suppress("TooGenericExceptionCaught")
private fun Context.start(intent: Intent): BinaryFileOpenResult = try {
    startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    BinaryFileOpenResult.Opened
} catch (e: ActivityNotFoundException) {
    logger.w(e) { "No app for ${intent.action}" }
    BinaryFileOpenResult.NoApp
} catch (e: Exception) {
    logger.w(e) { "Start ${intent.action} failed" }
    BinaryFileOpenResult.Failed
}

private const val EXPORT_DIR = "file-export"

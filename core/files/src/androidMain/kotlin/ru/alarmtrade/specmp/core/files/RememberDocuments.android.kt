package ru.alarmtrade.specmp.core.files

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import co.touchlab.kermit.Logger
import java.io.File
import java.util.UUID
import kotlinx.coroutines.withContext
import ru.alarmtrade.specmp.core.common.AppDispatchers
import ru.alarmtrade.specmp.core.common.file.queryNameAndSize

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
                return send(uri, type)
            }

            override suspend fun open(fileName: String, type: BinaryFileType, bytes: ByteArray): BinaryFileOpenResult {
                val uri = withContext(dispatchers.io) { appContext.exportCopy(fileName, type, bytes) }
                    ?: return BinaryFileOpenResult.Failed
                val view = Intent(Intent.ACTION_VIEW)
                    .setDataAndType(uri, type.mimeType)
                    .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                return withContext(dispatchers.main) { appContext.start(view) }
            }

            override suspend fun saveFile(fileName: String, type: BinaryFileType, path: String): BinaryFileSaveResult =
                writer.write(fileName, type.extension, type.mimeType) { output ->
                    File(path).inputStream().use { it.copyTo(output) }
                }

            override suspend fun shareFile(fileName: String, type: BinaryFileType, path: String): Boolean {
                val uri = withContext(dispatchers.io) {
                    appContext.exportCopy(fileName, type) { target -> File(path).copyTo(target, overwrite = true) }
                } ?: return false
                return send(uri, type)
            }

            private suspend fun send(uri: Uri, type: BinaryFileType): Boolean {
                val send = Intent(Intent.ACTION_SEND)
                    .setType(type.mimeType)
                    .putExtra(Intent.EXTRA_STREAM, uri)
                    .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                return withContext(dispatchers.main) {
                    appContext.start(Intent.createChooser(send, null)) == BinaryFileOpenResult.Opened
                }
            }
        }
    }
}

/** Выбранный файл копируется потоком в `cache/file-import/` (прежние копии удаляются): путь SAF-документа не файл. */
@Composable
actual fun rememberBinaryFilePicker(dispatchers: AppDispatchers): BinaryFilePicker {
    val appContext = LocalContext.current.applicationContext
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument(), importRequester::onResult)
    importRequester.Bind(launcher)
    return remember(appContext, dispatchers) {
        object : BinaryFilePicker {
            // Граница платформенного I/O: любой сбой копирования — Failed.
            @Suppress("TooGenericExceptionCaught")
            override suspend fun pick(type: BinaryFileType): BinaryFilePickResult {
                val pending = importRequester.request(type.pickMimeTypes.toTypedArray(), dispatchers.main)
                    ?: return BinaryFilePickResult.Failed
                val uri = pending.await() ?: return BinaryFilePickResult.Cancelled
                return withContext(dispatchers.io) {
                    try {
                        appContext.importCopy(uri, type)
                    } catch (e: Exception) {
                        logger.w(e) { "File import copy failed" }
                        BinaryFilePickResult.Failed
                    }
                }
            }
        }
    }
}

private val importRequester = ResultRequester<Array<String>, Uri>("File picker")

private fun Context.importCopy(uri: Uri, type: BinaryFileType): BinaryFilePickResult {
    val resolver = contentResolver
    val name = pickedFileName(resolver.queryNameAndSize(uri).name, type)
    val root = File(cacheDir, IMPORT_DIR).apply {
        deleteRecursively()
        mkdirs()
    }
    val file = File(File(root, UUID.randomUUID().toString()).apply { mkdirs() }, name)
    val input = resolver.openInputStream(uri) ?: return BinaryFilePickResult.Failed
    input.use { source -> file.outputStream().use { source.copyTo(it) } }
    return BinaryFilePickResult.Picked(name, file.absolutePath)
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

private fun Context.exportCopy(fileName: String, type: BinaryFileType, bytes: ByteArray): Uri? =
    exportCopy(fileName, type) { it.writeBytes(bytes) }

// Граница файлов: сбой записи копии — null.
@Suppress("TooGenericExceptionCaught")
private fun Context.exportCopy(fileName: String, type: BinaryFileType, write: (File) -> Unit): Uri? = try {
    // Прежние копии чистим перед записью: получатель уже прочитал их по гранту, система вправе вычистить cache и сама.
    val root = File(cacheDir, EXPORT_DIR).apply {
        deleteRecursively()
        mkdirs()
    }
    val directory = File(root, UUID.randomUUID().toString()).apply { mkdirs() }
    val file = File(directory, fileNameWithExtension(fileName, type.extension)).also(write)
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
private const val IMPORT_DIR = "file-import"

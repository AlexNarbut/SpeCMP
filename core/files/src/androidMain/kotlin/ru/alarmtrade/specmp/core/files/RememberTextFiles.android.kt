package ru.alarmtrade.specmp.core.files

import android.app.Activity
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import co.touchlab.kermit.Logger
import java.io.ByteArrayOutputStream
import java.io.InputStream
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import ru.alarmtrade.specmp.core.common.AppDispatchers
import ru.alarmtrade.specmp.core.common.file.queryNameAndSize
import ru.alarmtrade.specmp.core.common.file.safeFileName

private val logger = Logger.withTag("TextFiles")

private val pickRequester = ResultRequester<Array<String>>()
private val saveRequester = ResultRequester<CreateRequest>()

@Composable
actual fun rememberTextFilePicker(dispatchers: AppDispatchers): TextFilePicker {
    val appContext = LocalContext.current.applicationContext
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument(), pickRequester::onResult)
    pickRequester.Bind(launcher)
    return remember(appContext, dispatchers) {
        object : TextFilePicker {
            // Граница платформенного I/O: любой сбой чтения — Failed.
            @Suppress("TooGenericExceptionCaught")
            override suspend fun pick(type: TextFileType): TextFilePickResult {
                val pending = pickRequester.request(type.pickMimeTypes.toTypedArray(), dispatchers.main)
                    ?: return TextFilePickResult.Failed
                val uri = pending.await() ?: return TextFilePickResult.Cancelled
                return withContext(dispatchers.io) {
                    try {
                        appContext.contentResolver.readText(uri, type)
                    } catch (e: Exception) {
                        logger.w(e) { "Text file read failed" }
                        TextFilePickResult.Failed
                    }
                }
            }
        }
    }
}

/** Режим `wt` — с обрезкой: перезапись длинного файла коротким не оставляет хвост. */
@Composable
actual fun rememberTextFileSaver(dispatchers: AppDispatchers): TextFileSaver {
    val appContext = LocalContext.current.applicationContext
    val launcher = rememberLauncherForActivityResult(CreateDocumentOfType(), saveRequester::onResult)
    saveRequester.Bind(launcher)
    return remember(appContext, dispatchers) {
        object : TextFileSaver {
            // Граница платформенного I/O: любой сбой записи — Failed.
            @Suppress("TooGenericExceptionCaught")
            override suspend fun save(suggestedName: String, type: TextFileType, text: String): TextFileSaveResult {
                val fileName = fileNameWithExtension(suggestedName, type.extension)
                val pending = saveRequester.request(CreateRequest(fileName, type.mimeType), dispatchers.main)
                    ?: return TextFileSaveResult.Failed
                val uri = pending.await() ?: return TextFileSaveResult.Cancelled
                return withContext(dispatchers.io) {
                    try {
                        val resolver = appContext.contentResolver
                        resolver.openOutputStream(uri, "wt")?.use { it.write(text.encodeToByteArray()) }
                            ?: error("No output stream")
                        TextFileSaveResult.Saved(resolver.queryNameAndSize(uri).name ?: fileName)
                    } catch (e: Exception) {
                        logger.w(e) { "Text file save failed" }
                        TextFileSaveResult.Failed
                    }
                }
            }
        }
    }
}

/**
 * Ожидание — на уровне процесса, лаунчер — текущей композиции: после поворота экрана registry доставит результат
 * новому лаунчеру, и он завершит запрос, начатый через старый.
 */
private class ResultRequester<I : Any> {
    private var launcher: ActivityResultLauncher<I>? = null
    private var pending: CompletableDeferred<Uri?>? = null

    @Composable
    fun Bind(current: ActivityResultLauncher<I>) {
        DisposableEffect(current) {
            launcher = current
            onDispose { if (launcher === current) launcher = null }
        }
    }

    // Нет системного приложения для документов — ActivityNotFoundException: запрос не начат.
    @Suppress("TooGenericExceptionCaught")
    suspend fun request(input: I, main: CoroutineDispatcher): CompletableDeferred<Uri?>? = withContext(main) {
        val current = launcher ?: return@withContext null
        val deferred = CompletableDeferred<Uri?>()
        pending?.complete(null)
        pending = deferred
        try {
            current.launch(input)
            deferred
        } catch (e: Exception) {
            logger.w(e) { "Document picker launch failed" }
            pending = null
            null
        }
    }

    fun onResult(uri: Uri?) {
        pending?.complete(uri)
        pending = null
    }
}

private class CreateRequest(val fileName: String, val mimeType: String)

/** `CreateDocument` фиксирует MIME в конструкторе, а тип приходит в [TextFileSaver.save]. */
private class CreateDocumentOfType : ActivityResultContract<CreateRequest, Uri?>() {
    override fun createIntent(context: Context, input: CreateRequest): Intent = Intent(Intent.ACTION_CREATE_DOCUMENT)
        .addCategory(Intent.CATEGORY_OPENABLE)
        .setType(input.mimeType)
        .putExtra(Intent.EXTRA_TITLE, input.fileName)

    override fun parseResult(resultCode: Int, intent: Intent?): Uri? =
        intent.takeIf { resultCode == Activity.RESULT_OK }?.data
}

private fun ContentResolver.readText(uri: Uri, type: TextFileType): TextFilePickResult {
    val meta = queryNameAndSize(uri)
    if ((meta.sizeBytes ?: 0L) > type.maxBytes) return TextFilePickResult.TooLarge
    val name = safeFileName(meta.name.orEmpty(), fallback = fileNameWithExtension("", type.extension))
    val bytes = openInputStream(uri)?.use { it.readAtMost(type.maxBytes + 1) } ?: return TextFilePickResult.Failed
    return decodeTextFile(name, bytes, type.maxBytes)
}

/** Провайдер мог не отдать SIZE: читаем не больше лимита, лишний байт — признак слишком большого файла. */
private fun InputStream.readAtMost(limit: Long): ByteArray {
    val output = ByteArrayOutputStream()
    val buffer = ByteArray(BUFFER_SIZE)
    var total = 0L
    while (total < limit) {
        val read = read(buffer, 0, minOf(buffer.size.toLong(), limit - total).toInt())
        if (read < 0) break
        output.write(buffer, 0, read)
        total += read
    }
    return output.toByteArray()
}

private const val BUFFER_SIZE = 8 * 1024

package ru.alarmtrade.specmp.filepicker

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import co.touchlab.kermit.Logger
import java.io.File
import java.util.UUID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.compose.koinInject
import ru.alarmtrade.specmp.core.common.AppDispatchers
import ru.alarmtrade.specmp.core.common.di.appScopeQualifier
import ru.alarmtrade.specmp.feature.supportchat.domain.model.SupportChatPickedFile
import ru.alarmtrade.specmp.feature.supportchat.presentation.platform.SupportChatAttachmentLimits
import ru.alarmtrade.specmp.feature.supportchat.presentation.platform.SupportChatFilePicker
import ru.alarmtrade.specmp.feature.supportchat.presentation.platform.SupportChatPickKind
import ru.alarmtrade.specmp.feature.supportchat.presentation.platform.SupportChatPickedFileResult

/**
 * Буфер результатов выбора — на уровне процесса, а не в `remember`:
 * - пока открыт системный пикер, процесс могут убить; registry доставит результат
 *   лаунчеру уже в новом процессе — он попадёт сюда и дождётся, пока экран подпишется;
 * - копирование большого файла может пережить пересоздание Activity — результат не
 *   должен уйти в канал старой композиции.
 * Подписчик один — экран чата (`SupportChatEntry`).
 */
private object PickerResults {
    val channel = Channel<SupportChatPickedFileResult>(Channel.UNLIMITED)
}

/**
 * Картинки — системный Photo Picker (`PickVisualMedia`), документы — SAF (`OpenDocument`).
 * `GetContent` не годится: часть галерей возвращает выбор в `clipData`, а не в
 * `intent.data`, — успешный выбор приходил как `null`, то есть как «тихая» отмена.
 */
@Composable
actual fun rememberSupportChatFilePicker(): SupportChatFilePicker {
    val appContext = LocalContext.current.applicationContext
    val dispatchers = koinInject<AppDispatchers>()
    val appScope = koinInject<CoroutineScope>(qualifier = appScopeQualifier)

    fun deliver(uri: Uri?, kind: SupportChatPickKind) {
        if (uri == null) {
            PickerResults.channel.trySend(SupportChatPickedFileResult.Cancelled)
            return
        }
        // Копирование — вне main (большой файл не должен давать ANR).
        appScope.launch {
            val result = withContext(dispatchers.io) { uri.copyToAttachments(appContext, kind) }
            PickerResults.channel.trySend(result)
        }
    }

    val imageLauncher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        deliver(uri, SupportChatPickKind.Image)
    }
    val documentLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        deliver(uri, SupportChatPickKind.Document)
    }

    return remember(imageLauncher, documentLauncher) {
        object : SupportChatFilePicker {
            override val results: Flow<SupportChatPickedFileResult> = PickerResults.channel.receiveAsFlow()

            override fun launchImage() {
                imageLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            }

            override fun launchDocument() {
                documentLauncher.launch(arrayOf(ANY_MIME))
            }
        }
    }
}

/**
 * Копия в собственный каталог приложения (`filesDir`, а не `cacheDir`): она нужна
 * outbox'у после рестарта, а кэш система вправе вычистить. Удаляет копию
 * [rememberSupportChatAttachmentFiles], когда сообщение подтверждено или удалено.
 *
 * Каждая копия — в своём подкаталоге `<uuid>/<исходное имя>`: SDK берёт имя файла
 * для сервера из пути, и префикс в имени увидела бы поддержка.
 */
// Граница платформенного I/O: любой сбой копирования логируется и превращается в Failed
@Suppress("TooGenericExceptionCaught")
private fun Uri.copyToAttachments(context: Context, kind: SupportChatPickKind): SupportChatPickedFileResult {
    val resolver = context.contentResolver
    val metadata = resolver.queryMetadata(this)
    if ((metadata.sizeBytes ?: 0L) > SupportChatAttachmentLimits.MAX_BYTES) {
        return SupportChatPickedFileResult.TooLarge
    }

    val name = safeFileName(metadata.name ?: DEFAULT_NAME)
    val target = try {
        val directory = File(supportChatAttachmentsDir(context), UUID.randomUUID().toString()).apply { mkdirs() }
        File(directory, name)
    } catch (e: Exception) {
        logger.w(e) { "Failed to create attachment copy for '$name'" }
        return SupportChatPickedFileResult.Failed
    }

    return try {
        val copied = resolver.openInputStream(this)?.use { input ->
            target.outputStream().use { output -> input.copyTo(output) }
        } ?: return SupportChatPickedFileResult.Failed.also {
            logger.w { "Cannot open input stream for picked uri: $this" }
            target.deleteWithDirectory()
        }
        // Провайдер мог не отдать SIZE — проверяем фактический размер.
        if (copied > SupportChatAttachmentLimits.MAX_BYTES) {
            target.deleteWithDirectory()
            return SupportChatPickedFileResult.TooLarge
        }
        SupportChatPickedFileResult.Picked(
            SupportChatPickedFile(
                id = UUID.randomUUID().toString(),
                name = name,
                path = target.absolutePath,
                mimeType = resolver.getType(this),
                sizeBytes = copied
            ),
            kind
        )
    } catch (e: Exception) {
        logger.w(e) { "Failed to copy picked attachment '$name'" }
        target.deleteWithDirectory()
        SupportChatPickedFileResult.Failed
    }
}

/** Удаляет копию вместе с её персональным подкаталогом `<uuid>/`. */
internal fun File.deleteWithDirectory() {
    delete()
    parentFile?.delete()
}

private val logger = Logger.withTag("SupportChatFilePicker")

internal fun supportChatAttachmentsDir(context: Context): File = File(context.filesDir, ATTACHMENTS_DIR)

private fun ContentResolver.queryMetadata(uri: Uri): PickedFileMetadata {
    val cursor = query(uri, null, null, null, null) ?: return PickedFileMetadata()
    return cursor.use {
        if (it.moveToFirst()) {
            val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            val sizeIndex = it.getColumnIndex(OpenableColumns.SIZE)
            PickedFileMetadata(
                name = nameIndex.takeIf { index -> index != -1 }?.let(it::getString),
                sizeBytes = sizeIndex
                    .takeIf { index -> index != -1 && !it.isNull(index) }
                    ?.let(it::getLong)
            )
        } else {
            PickedFileMetadata()
        }
    }
}

private data class PickedFileMetadata(val name: String? = null, val sizeBytes: Long? = null)

private const val ANY_MIME = "*/*"
private const val DEFAULT_NAME = "attachment"
private const val ATTACHMENTS_DIR = "support-chat-attachments"

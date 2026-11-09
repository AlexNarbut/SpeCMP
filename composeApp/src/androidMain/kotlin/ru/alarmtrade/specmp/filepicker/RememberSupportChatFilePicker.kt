package ru.alarmtrade.specmp.filepicker

import android.content.Context
import android.net.Uri
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
import ru.alarmtrade.specmp.core.common.file.queryNameAndSize
import ru.alarmtrade.specmp.core.common.file.safeFileName
import ru.alarmtrade.specmp.feature.supportchat.domain.model.SupportChatPickedFile
import ru.alarmtrade.specmp.feature.supportchat.presentation.platform.SupportChatAttachmentLimits
import ru.alarmtrade.specmp.feature.supportchat.presentation.platform.SupportChatFilePicker
import ru.alarmtrade.specmp.feature.supportchat.presentation.platform.SupportChatPickKind
import ru.alarmtrade.specmp.feature.supportchat.presentation.platform.SupportChatPickedFileResult

/**
 * Буфер — на уровне процесса: пока открыт пикер, процесс могут убить (registry доставит результат в новый), а
 * копирование большого файла может пережить пересоздание Activity.
 */
private object PickerResults {
    val channel = Channel<SupportChatPickedFileResult>(Channel.UNLIMITED)
}

/** `GetContent` не годится: часть галерей отдаёт выбор в `clipData`, и он приходил как `null` (тихая отмена). */
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
 * Копия в `filesDir`, не в `cacheDir`: нужна outbox'у после рестарта. Своя папка `<uuid>/` на копию: SDK берёт имя
 * файла для сервера из пути.
 */
// Граница платформенного I/O: любой сбой копирования логируется и превращается в Failed
@Suppress("TooGenericExceptionCaught")
private fun Uri.copyToAttachments(context: Context, kind: SupportChatPickKind): SupportChatPickedFileResult {
    val resolver = context.contentResolver
    val metadata = resolver.queryNameAndSize(this)
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
                sizeBytes = copied,
            ),
            kind,
        )
    } catch (e: Exception) {
        logger.w(e) { "Failed to copy picked attachment '$name'" }
        target.deleteWithDirectory()
        SupportChatPickedFileResult.Failed
    }
}

internal fun File.deleteWithDirectory() {
    delete()
    parentFile?.delete()
}

private val logger = Logger.withTag("SupportChatFilePicker")

internal fun supportChatAttachmentsDir(context: Context): File = File(context.filesDir, ATTACHMENTS_DIR)

private const val ANY_MIME = "*/*"
private const val DEFAULT_NAME = "attachment"
private const val ATTACHMENTS_DIR = "support-chat-attachments"

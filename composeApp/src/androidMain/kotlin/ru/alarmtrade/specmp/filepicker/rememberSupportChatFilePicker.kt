package ru.alarmtrade.specmp.filepicker

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.compose.koinInject
import ru.alarmtrade.specmp.core.common.AppDispatchers
import ru.alarmtrade.specmp.core.common.di.appScopeQualifier
import ru.alarmtrade.specmp.feature.supportchat.domain.model.SupportChatPickedFile
import ru.alarmtrade.specmp.feature.supportchat.presentation.platform.SupportChatAttachmentLimits
import ru.alarmtrade.specmp.feature.supportchat.presentation.platform.SupportChatFilePicker
import ru.alarmtrade.specmp.feature.supportchat.presentation.platform.SupportChatPickedFileResult
import java.io.File
import java.util.UUID

/**
 * Колбэки ожидающего выбора живут вне композиции: пока открыта галерея, Activity
 * может пересоздаться (поворот, «не сохранять действия»), и `remember` потерял бы
 * колбэк — выбранный файл молча пропал бы. Процесс при этом жив, per-entry VM
 * тоже, поэтому колбэк (ссылка на интент VM) остаётся валидным. Очищаются сразу
 * после доставки результата.
 */
private object PendingPickerCallbacks {
    var image: ((SupportChatPickedFileResult) -> Unit)? = null
    var document: ((SupportChatPickedFileResult) -> Unit)? = null
}

@Composable
actual fun rememberSupportChatFilePicker(): SupportChatFilePicker {
    val appContext = LocalContext.current.applicationContext
    val dispatchers = koinInject<AppDispatchers>()
    val appScope = koinInject<CoroutineScope>(qualifier = appScopeQualifier)

    fun deliver(uri: Uri?, take: () -> ((SupportChatPickedFileResult) -> Unit)?) {
        val callback = take() ?: return
        if (uri == null) {
            callback(SupportChatPickedFileResult.Cancelled)
            return
        }
        // Копирование — вне main (большой файл не должен давать ANR); колбэк — на main.
        appScope.launch {
            val result = withContext(dispatchers.io) { uri.copyToAttachments(appContext) }
            withContext(dispatchers.main) { callback(result) }
        }
    }

    val imageLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        deliver(uri) { PendingPickerCallbacks.image.also { PendingPickerCallbacks.image = null } }
    }
    val documentLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        deliver(uri) { PendingPickerCallbacks.document.also { PendingPickerCallbacks.document = null } }
    }

    return remember(imageLauncher, documentLauncher) {
        object : SupportChatFilePicker {
            override fun launchImage(onPicked: (SupportChatPickedFileResult) -> Unit) {
                PendingPickerCallbacks.image = onPicked
                imageLauncher.launch(IMAGE_MIME)
            }

            override fun launchDocument(onPicked: (SupportChatPickedFileResult) -> Unit) {
                PendingPickerCallbacks.document = onPicked
                documentLauncher.launch(ANY_MIME)
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
private fun Uri.copyToAttachments(context: Context): SupportChatPickedFileResult {
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
        return SupportChatPickedFileResult.Failed
    }

    return try {
        val copied = resolver.openInputStream(this)?.use { input ->
            target.outputStream().use { output -> input.copyTo(output) }
        } ?: return SupportChatPickedFileResult.Failed.also { target.deleteWithDirectory() }
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
            )
        )
    } catch (e: Exception) {
        target.deleteWithDirectory()
        SupportChatPickedFileResult.Failed
    }
}

/** Удаляет копию вместе с её персональным подкаталогом `<uuid>/`. */
internal fun File.deleteWithDirectory() {
    delete()
    parentFile?.delete()
}

internal fun supportChatAttachmentsDir(context: Context): File {
    return File(context.filesDir, ATTACHMENTS_DIR)
}

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
                    ?.let(it::getLong),
            )
        } else {
            PickedFileMetadata()
        }
    }
}

private data class PickedFileMetadata(
    val name: String? = null,
    val sizeBytes: Long? = null,
)

private const val IMAGE_MIME = "image/*"
private const val ANY_MIME = "*/*"
private const val DEFAULT_NAME = "attachment"
private const val ATTACHMENTS_DIR = "support-chat-attachments"

package ru.alarmtrade.specmp.filepicker

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import co.touchlab.kermit.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.alarmtrade.specmp.feature.supportchat.presentation.platform.SupportChatFilePicker
import ru.alarmtrade.specmp.feature.supportchat.presentation.platform.SupportChatPickKind
import ru.alarmtrade.specmp.feature.supportchat.presentation.platform.SupportChatPickedFileResult
import ru.alarmtrade.specmp.feature.supportchat.domain.model.SupportChatPickedFile
import java.io.File
import java.util.UUID

@Composable
actual fun rememberSupportChatFilePicker(): SupportChatFilePicker {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val picker = remember { AndroidSupportChatFilePicker() }

    // Pending-результат registry переживает смерть процесса: после пересоздания
    // он придёт в этот же лаунчер, а picker буферизует его до подписчика (Channel).
    // Поэтому результат нельзя роутить через remember-лямбду — она не переживает
    // смерть процесса, и выбранный файл молча терялся.
    //
    // Изображения — системный Photo Picker (PickVisualMedia), документы — SAF
    // (OpenDocument). GetContent здесь непригоден: он читает результат только из
    // intent.data, а часть галерей возвращает выбор в clipData — uri приходил null
    // при успешном выборе.
    val imageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
    ) { uri ->
        picker.onResult(uri, context.applicationContext, scope, SupportChatPickKind.Image)
    }

    val documentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { uri ->
        picker.onResult(uri, context.applicationContext, scope, SupportChatPickKind.Document)
    }

    return remember(picker, imageLauncher, documentLauncher) {
        object : SupportChatFilePicker {
            override val results: Flow<SupportChatPickedFileResult> = picker.results

            override fun launchImage() {
                imageLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                )
            }

            override fun launchDocument() {
                documentLauncher.launch(arrayOf("*/*"))
            }
        }
    }
}

private class AndroidSupportChatFilePicker {

    private val channel = Channel<SupportChatPickedFileResult>(Channel.UNLIMITED)
    val results: Flow<SupportChatPickedFileResult> = channel.receiveAsFlow()

    fun onResult(
        uri: Uri?,
        context: Context,
        scope: CoroutineScope,
        kind: SupportChatPickKind,
    ) {
        if (uri == null) {
            channel.trySend(SupportChatPickedFileResult.Cancelled)
            return
        }
        // Копирование content:// во временный файл — IO, не на main-потоке.
        scope.launch {
            val file = withContext(Dispatchers.IO) { uri.toPickedFile(context) }
            channel.trySend(
                file?.let { SupportChatPickedFileResult.Picked(it, kind) }
                    ?: SupportChatPickedFileResult.Cancelled,
            )
        }
    }
}

private fun Uri.toPickedFile(context: Context): SupportChatPickedFile? {
    val resolver = context.contentResolver
    val metadata = resolver.queryMetadata(this)
    val name = metadata.name ?: "attachment"
    val mimeType = resolver.getType(this)
    val extension = name.substringAfterLast('.', "").takeIf { it.isNotBlank() }
    val tempFile = try {
        File.createTempFile(
            "picked_${UUID.randomUUID()}_",
            extension?.let { ".$it" },
            context.cacheDir,
        )
    } catch (e: Exception) {
        logger.w(e) { "Failed to create temp file for picked attachment '$name'" }
        return null
    }

    return try {
        resolver.openInputStream(this)?.use { input ->
            tempFile.outputStream().use { output ->
                input.copyTo(output)
            }
        } ?: run {
            logger.w { "Cannot open input stream for picked uri: $this" }
            tempFile.delete()
            return null
        }
        SupportChatPickedFile(
            id = UUID.randomUUID().toString(),
            name = name,
            path = tempFile.absolutePath,
            mimeType = mimeType,
            sizeBytes = metadata.sizeBytes ?: tempFile.length(),
        )
    } catch (e: Exception) {
        logger.w(e) { "Failed to copy picked attachment '$name' to temp file" }
        tempFile.delete()
        null
    }
}

private val logger = Logger.withTag("SupportChatFilePicker")

private fun android.content.ContentResolver.queryMetadata(uri: Uri): PickedFileMetadata {
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

package ru.alarmtrade.specmp.filepicker

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import ru.alarmtrade.specmp.feature.supportchat.presentation.platform.SupportChatFilePicker
import ru.alarmtrade.specmp.feature.supportchat.presentation.platform.SupportChatPickedFileResult
import ru.alarmtrade.specmp.feature.supportchat.domain.model.SupportChatPickedFile
import java.io.File
import java.util.UUID

@Composable
actual fun rememberSupportChatFilePicker(): SupportChatFilePicker {
    val context = LocalContext.current

    var imageCallback by remember { mutableStateOf<((SupportChatPickedFileResult) -> Unit)?>(null) }
    var documentCallback by remember { mutableStateOf<((SupportChatPickedFileResult) -> Unit)?>(null) }

    val imageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
    ) { uri ->
        imageCallback?.invoke(uri?.toPickedFile(context).toPickerResult())
        imageCallback = null
    }

    val documentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
    ) { uri ->
        documentCallback?.invoke(uri?.toPickedFile(context).toPickerResult())
        documentCallback = null
    }

    return remember(imageLauncher, documentLauncher) {
        object : SupportChatFilePicker {
            override fun launchImage(onPicked: (SupportChatPickedFileResult) -> Unit) {
                imageCallback = onPicked
                imageLauncher.launch("image/*")
            }

            override fun launchDocument(onPicked: (SupportChatPickedFileResult) -> Unit) {
                documentCallback = onPicked
                documentLauncher.launch("*/*")
            }
        }
    }
}

private fun SupportChatPickedFile?.toPickerResult(): SupportChatPickedFileResult {
    return this?.let(SupportChatPickedFileResult::Picked) ?: SupportChatPickedFileResult.Cancelled
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
        return null
    }

    return try {
        resolver.openInputStream(this)?.use { input ->
            tempFile.outputStream().use { output ->
                input.copyTo(output)
            }
        } ?: return null
        SupportChatPickedFile(
            id = UUID.randomUUID().toString(),
            name = name,
            path = tempFile.absolutePath,
            mimeType = mimeType,
            sizeBytes = metadata.sizeBytes ?: tempFile.length(),
        )
    } catch (e: Exception) {
        tempFile.delete()
        null
    }
}

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

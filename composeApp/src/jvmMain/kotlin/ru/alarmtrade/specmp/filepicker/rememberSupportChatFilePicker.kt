package ru.alarmtrade.specmp.filepicker

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.feature.supportchat.presentation.platform.SupportChatFilePicker
import ru.alarmtrade.specmp.feature.supportchat.presentation.platform.SupportChatPickKind
import ru.alarmtrade.specmp.feature.supportchat.presentation.platform.SupportChatPickedFileResult
import ru.alarmtrade.specmp.feature.supportchat.domain.model.SupportChatPickedFile
import specmp.composeapp.generated.resources.Res
import specmp.composeapp.generated.resources.support_chat_picker_label_images
import specmp.composeapp.generated.resources.support_chat_picker_title_file
import specmp.composeapp.generated.resources.support_chat_picker_title_image
import java.io.File
import java.util.UUID
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter

@Composable
actual fun rememberSupportChatFilePicker(): SupportChatFilePicker {
    val imageTitle = stringResource(Res.string.support_chat_picker_title_image)
    val imagesLabel = stringResource(Res.string.support_chat_picker_label_images)
    val fileTitle = stringResource(Res.string.support_chat_picker_title_file)
    return remember(imageTitle, imagesLabel, fileTitle) {
        DesktopSupportChatFilePicker(
            imageTitle = imageTitle,
            imagesLabel = imagesLabel,
            fileTitle = fileTitle,
        )
    }
}

private class DesktopSupportChatFilePicker(
    private val imageTitle: String,
    private val imagesLabel: String,
    private val fileTitle: String,
) : SupportChatFilePicker {

    private val channel = Channel<SupportChatPickedFileResult>(Channel.UNLIMITED)
    override val results: Flow<SupportChatPickedFileResult> = channel.receiveAsFlow()

    override fun launchImage() {
        val file = showDialog(
            title = imageTitle,
            filter = FileNameExtensionFilter(
                imagesLabel,
                "jpg", "jpeg", "png", "gif", "webp", "bmp",
            ),
        )
        channel.trySend(file?.toPickedFile().toPickerResult(SupportChatPickKind.Image))
    }

    override fun launchDocument() {
        val file = showDialog(
            title = fileTitle,
            filter = null,
        )
        channel.trySend(file?.toPickedFile().toPickerResult(SupportChatPickKind.Document))
    }

    private fun showDialog(
        title: String,
        filter: FileNameExtensionFilter?,
    ): File? {
        val chooser = JFileChooser().apply {
            dialogTitle = title
            fileSelectionMode = JFileChooser.FILES_ONLY
            isMultiSelectionEnabled = false
            filter?.let { fileFilter = it }
        }
        val result = chooser.showOpenDialog(null)
        return if (result == JFileChooser.APPROVE_OPTION) chooser.selectedFile else null
    }

    private fun SupportChatPickedFile?.toPickerResult(
        kind: SupportChatPickKind,
    ): SupportChatPickedFileResult {
        return this?.let { SupportChatPickedFileResult.Picked(it, kind) }
            ?: SupportChatPickedFileResult.Cancelled
    }

    private fun File.toPickedFile(): SupportChatPickedFile {
        val extension = name.substringAfterLast('.', "").lowercase()
        val mimeType = when (extension) {
            "jpg", "jpeg" -> "image/jpeg"
            "png" -> "image/png"
            "gif" -> "image/gif"
            "webp" -> "image/webp"
            "bmp" -> "image/bmp"
            "pdf" -> "application/pdf"
            "txt" -> "text/plain"
            "doc" -> "application/msword"
            "docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
            else -> null
        }
        return SupportChatPickedFile(
            id = UUID.randomUUID().toString(),
            name = name,
            path = absolutePath,
            mimeType = mimeType,
            sizeBytes = length(),
        )
    }
}

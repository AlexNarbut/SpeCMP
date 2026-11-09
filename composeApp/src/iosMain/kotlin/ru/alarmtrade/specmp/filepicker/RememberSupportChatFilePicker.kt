@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package ru.alarmtrade.specmp.filepicker

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.uikit.LocalUIViewController
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import platform.Foundation.NSApplicationSupportDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSFileSize
import platform.Foundation.NSNumber
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSURL
import platform.Foundation.NSUUID
import platform.Foundation.NSUserDomainMask
import platform.PhotosUI.PHPickerConfiguration
import platform.PhotosUI.PHPickerFilter
import platform.PhotosUI.PHPickerResult
import platform.PhotosUI.PHPickerViewController
import platform.PhotosUI.PHPickerViewControllerDelegateProtocol
import platform.UIKit.UIDocumentPickerDelegateProtocol
import platform.UIKit.UIDocumentPickerMode
import platform.UIKit.UIDocumentPickerViewController
import platform.UIKit.UIViewController
import platform.darwin.DISPATCH_QUEUE_PRIORITY_DEFAULT
import platform.darwin.NSObject
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_global_queue
import platform.darwin.dispatch_get_main_queue
import ru.alarmtrade.specmp.core.common.file.safeFileName
import ru.alarmtrade.specmp.feature.supportchat.domain.model.SupportChatPickedFile
import ru.alarmtrade.specmp.feature.supportchat.presentation.platform.SupportChatAttachmentLimits
import ru.alarmtrade.specmp.feature.supportchat.presentation.platform.SupportChatFilePicker
import ru.alarmtrade.specmp.feature.supportchat.presentation.platform.SupportChatPickKind
import ru.alarmtrade.specmp.feature.supportchat.presentation.platform.SupportChatPickedFileResult

@Composable
actual fun rememberSupportChatFilePicker(): SupportChatFilePicker {
    val viewController = LocalUIViewController.current
    return remember(viewController) {
        IosSupportChatFilePicker(viewController)
    }
}

private class IosSupportChatFilePicker(private val viewController: UIViewController) : SupportChatFilePicker {

    private var activeDelegate: NSObject? = null

    // Делегаты отдают результат на main; буфер — до подписки экрана.
    private val channel = Channel<SupportChatPickedFileResult>(Channel.UNLIMITED)
    override val results: Flow<SupportChatPickedFileResult> = channel.receiveAsFlow()
    private val onPicked: (SupportChatPickedFileResult) -> Unit = { channel.trySend(it) }

    override fun launchImage() {
        val configuration = PHPickerConfiguration().apply {
            filter = PHPickerFilter.imagesFilter()
            selectionLimit = 1
        }
        val picker = PHPickerViewController(configuration)
        val delegate = PhotoPickerDelegate(
            onPicked = onPicked,
            onFinished = { activeDelegate = null },
        )
        activeDelegate = delegate
        picker.delegate = delegate
        viewController.presentViewController(
            viewControllerToPresent = picker,
            animated = true,
            completion = null,
        )
    }

    override fun launchDocument() {
        launchPicker(documentTypes = listOf(DATA_UTI))
    }

    private fun launchPicker(documentTypes: List<String>) {
        val picker = UIDocumentPickerViewController(
            documentTypes = documentTypes,
            inMode = UIDocumentPickerMode.UIDocumentPickerModeOpen,
        ).apply {
            allowsMultipleSelection = false
        }
        val delegate = DocumentPickerDelegate(
            onPicked = onPicked,
            onFinished = { activeDelegate = null },
        )
        activeDelegate = delegate
        picker.delegate = delegate
        viewController.presentViewController(
            viewControllerToPresent = picker,
            animated = true,
            completion = null,
        )
    }

    private companion object {
        const val DATA_UTI = "public.data"
    }
}

private const val IMAGE_UTI = "public.image"

private class PhotoPickerDelegate(
    private val onPicked: (SupportChatPickedFileResult) -> Unit,
    private val onFinished: () -> Unit,
) : NSObject(),
    PHPickerViewControllerDelegateProtocol {

    override fun picker(picker: PHPickerViewController, didFinishPicking: List<*>) {
        picker.dismissViewControllerAnimated(true, null)
        val result = didFinishPicking.firstOrNull() as? PHPickerResult
        if (result == null) {
            onPicked(SupportChatPickedFileResult.Cancelled)
            onFinished()
            return
        }
        // Колбэк itemProvider приходит на фоновой очереди; временный url валиден
        // только внутри него — копируем здесь, результат отдаём на main.
        result.itemProvider.loadFileRepresentationForTypeIdentifier(IMAGE_UTI) { url, _ ->
            val picked = url?.copyToAttachments(SupportChatPickKind.Image) ?: SupportChatPickedFileResult.Failed
            dispatch_async(dispatch_get_main_queue()) {
                onPicked(picked)
                onFinished()
            }
        }
    }
}

private class DocumentPickerDelegate(
    private val onPicked: (SupportChatPickedFileResult) -> Unit,
    private val onFinished: () -> Unit,
) : NSObject(),
    UIDocumentPickerDelegateProtocol {

    override fun documentPicker(controller: UIDocumentPickerViewController, didPickDocumentsAtURLs: List<*>) {
        val url = didPickDocumentsAtURLs.firstOrNull() as? NSURL
        if (url == null) {
            onPicked(SupportChatPickedFileResult.Cancelled)
            onFinished()
            return
        }
        dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT.toLong(), 0u)) {
            val picked = url.copyToAttachments(SupportChatPickKind.Document)
            dispatch_async(dispatch_get_main_queue()) {
                onPicked(picked)
                onFinished()
            }
        }
    }

    override fun documentPickerWasCancelled(controller: UIDocumentPickerViewController) {
        onPicked(SupportChatPickedFileResult.Cancelled)
        onFinished()
    }
}

/** Копия в `Application Support`, не в tmp: нужна outbox'у после рестарта. */
private fun NSURL.copyToAttachments(kind: SupportChatPickKind): SupportChatPickedFileResult {
    val canAccess = startAccessingSecurityScopedResource()
    return try {
        val fileManager = NSFileManager.defaultManager
        val sourceSize = path?.let { sourcePath ->
            (fileManager.attributesOfItemAtPath(sourcePath, error = null)?.get(NSFileSize) as? NSNumber)?.longLongValue
        }
        if ((sourceSize ?: 0L) > SupportChatAttachmentLimits.MAX_BYTES) return SupportChatPickedFileResult.TooLarge

        val root = supportChatAttachmentsDirectory() ?: return SupportChatPickedFileResult.Failed
        val originalName = safeFileName(lastPathComponent ?: DEFAULT_NAME)
        // Своя папка на копию: SDK берёт имя для сервера из пути — без префиксов в имени.
        val directory = "$root/${NSUUID().UUIDString}"
        val destinationPath = "$directory/$originalName"
        val created = fileManager.createDirectoryAtPath(
            path = directory,
            withIntermediateDirectories = true,
            attributes = null,
            error = null,
        )
        if (!created || !fileManager.copyItemAtURL(this, NSURL.fileURLWithPath(destinationPath), error = null)) {
            fileManager.removeItemAtPath(directory, error = null)
            return SupportChatPickedFileResult.Failed
        }

        val sizeBytes = (
            fileManager.attributesOfItemAtPath(destinationPath, error = null)
                ?.get(NSFileSize) as? NSNumber
            )
            ?.longLongValue
        if ((sizeBytes ?: 0L) > SupportChatAttachmentLimits.MAX_BYTES) {
            fileManager.removeItemAtPath(directory, error = null)
            return SupportChatPickedFileResult.TooLarge
        }

        SupportChatPickedFileResult.Picked(
            SupportChatPickedFile(
                id = NSUUID().UUIDString,
                name = originalName,
                path = destinationPath,
                mimeType = mimeTypeForName(originalName),
                sizeBytes = sizeBytes,
            ),
            kind,
        )
    } finally {
        if (canAccess) stopAccessingSecurityScopedResource()
    }
}

internal fun supportChatAttachmentsDirectory(): String? {
    val base = NSSearchPathForDirectoriesInDomains(NSApplicationSupportDirectory, NSUserDomainMask, true)
        .firstOrNull() as? String ?: return null
    val directory = "$base/$ATTACHMENTS_DIR"
    val created = NSFileManager.defaultManager.createDirectoryAtPath(
        path = directory,
        withIntermediateDirectories = true,
        attributes = null,
        error = null,
    )
    return directory.takeIf { created }
}

private const val DEFAULT_NAME = "attachment"
private const val ATTACHMENTS_DIR = "support-chat-attachments"

private fun mimeTypeForName(name: String): String? = when (name.substringAfterLast('.', "").lowercase()) {
    "jpg", "jpeg" -> "image/jpeg"
    "png" -> "image/png"
    "gif" -> "image/gif"
    "webp" -> "image/webp"
    "heic" -> "image/heic"
    "pdf" -> "application/pdf"
    "txt" -> "text/plain"
    "doc" -> "application/msword"
    "docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
    else -> null
}

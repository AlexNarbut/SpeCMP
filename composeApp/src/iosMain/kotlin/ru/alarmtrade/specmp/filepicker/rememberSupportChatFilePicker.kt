@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package ru.alarmtrade.specmp.filepicker

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.uikit.LocalUIViewController
import platform.Foundation.NSFileManager
import platform.Foundation.NSFileSize
import platform.Foundation.NSNumber
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSURL
import platform.Foundation.NSUUID
import platform.PhotosUI.PHPickerConfiguration
import platform.PhotosUI.PHPickerFilter
import platform.PhotosUI.PHPickerResult
import platform.PhotosUI.PHPickerViewController
import platform.PhotosUI.PHPickerViewControllerDelegateProtocol
import platform.UIKit.UIDocumentPickerDelegateProtocol
import platform.UIKit.UIDocumentPickerMode
import platform.UIKit.UIDocumentPickerViewController
import platform.UIKit.UIViewController
import platform.darwin.NSObject
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue
import ru.alarmtrade.specmp.feature.supportchat.presentation.platform.SupportChatFilePicker
import ru.alarmtrade.specmp.feature.supportchat.presentation.platform.SupportChatPickedFileResult
import ru.alarmtrade.specmp.feature.supportchat.domain.model.SupportChatPickedFile

@Composable
actual fun rememberSupportChatFilePicker(): SupportChatFilePicker {
    val viewController = LocalUIViewController.current
    return remember(viewController) {
        IosSupportChatFilePicker(viewController)
    }
}

private class IosSupportChatFilePicker(
    private val viewController: UIViewController,
) : SupportChatFilePicker {

    private var activeDelegate: NSObject? = null

    override fun launchImage(onPicked: (SupportChatPickedFileResult) -> Unit) {
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

    override fun launchDocument(onPicked: (SupportChatPickedFileResult) -> Unit) {
        launchPicker(
            documentTypes = listOf(DATA_UTI),
            onPicked = onPicked,
        )
    }

    private fun launchPicker(
        documentTypes: List<String>,
        onPicked: (SupportChatPickedFileResult) -> Unit,
    ) {
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

private fun SupportChatPickedFile?.toPickerResult(): SupportChatPickedFileResult {
    return this?.let(SupportChatPickedFileResult::Picked) ?: SupportChatPickedFileResult.Cancelled
}

private const val IMAGE_UTI = "public.image"

private class PhotoPickerDelegate(
    private val onPicked: (SupportChatPickedFileResult) -> Unit,
    private val onFinished: () -> Unit,
) : NSObject(), PHPickerViewControllerDelegateProtocol {

    override fun picker(picker: PHPickerViewController, didFinishPicking: List<*>) {
        picker.dismissViewControllerAnimated(true, null)
        val result = didFinishPicking.firstOrNull() as? PHPickerResult
        if (result == null) {
            onPicked(SupportChatPickedFileResult.Cancelled)
            onFinished()
            return
        }
        result.itemProvider.loadFileRepresentationForTypeIdentifier(IMAGE_UTI) { url, _ ->
            val picked = url?.copyToTemporaryFile().toPickerResult()
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
) : NSObject(), UIDocumentPickerDelegateProtocol {

    override fun documentPicker(
        controller: UIDocumentPickerViewController,
        didPickDocumentsAtURLs: List<*>,
    ) {
        val url = didPickDocumentsAtURLs.firstOrNull() as? NSURL
        onPicked(url?.copyToTemporaryFile().toPickerResult())
        onFinished()
    }

    override fun documentPickerWasCancelled(controller: UIDocumentPickerViewController) {
        onPicked(SupportChatPickedFileResult.Cancelled)
        onFinished()
    }
}

private fun NSURL.copyToTemporaryFile(): SupportChatPickedFile? {
    val canAccess = startAccessingSecurityScopedResource()
    return try {
        val originalName = lastPathComponent ?: "attachment"
        val safeName = originalName.replace('/', '_')
        val destinationPath = "${NSTemporaryDirectory()}${NSUUID().UUIDString}_$safeName"
        val destinationUrl = NSURL.fileURLWithPath(destinationPath)
        val fileManager = NSFileManager.defaultManager

        if (!fileManager.copyItemAtURL(this, destinationUrl, error = null)) return null

        val sizeBytes = (fileManager.attributesOfItemAtPath(destinationPath, error = null)
            ?.get(NSFileSize) as? NSNumber)
            ?.longLongValue

        SupportChatPickedFile(
            id = NSUUID().UUIDString,
            name = originalName,
            path = destinationPath,
            mimeType = mimeTypeForName(originalName),
            sizeBytes = sizeBytes,
        )
    } finally {
        if (canAccess) stopAccessingSecurityScopedResource()
    }
}

private fun mimeTypeForName(name: String): String? {
    return when (name.substringAfterLast('.', "").lowercase()) {
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
}

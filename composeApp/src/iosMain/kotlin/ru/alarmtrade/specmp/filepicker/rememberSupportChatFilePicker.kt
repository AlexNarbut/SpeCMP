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
import platform.UIKit.UIDocumentPickerDelegateProtocol
import platform.UIKit.UIDocumentPickerMode
import platform.UIKit.UIDocumentPickerViewController
import platform.UIKit.UIViewController
import platform.darwin.NSObject
import ru.alarmtrade.specmp.feature.supportchat.domain.picker.SupportChatFilePicker
import ru.alarmtrade.specmp.feature.supportchat.domain.picker.SupportChatPickedFile

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

    private var activeDelegate: DocumentPickerDelegate? = null

    override fun launchImage(onPicked: (SupportChatPickedFile?) -> Unit) {
        launchPicker(
            documentTypes = listOf(IMAGE_UTI),
            onPicked = onPicked,
        )
    }

    override fun launchDocument(onPicked: (SupportChatPickedFile?) -> Unit) {
        launchPicker(
            documentTypes = listOf(DATA_UTI),
            onPicked = onPicked,
        )
    }

    private fun launchPicker(
        documentTypes: List<String>,
        onPicked: (SupportChatPickedFile?) -> Unit,
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
        const val IMAGE_UTI = "public.image"
        const val DATA_UTI = "public.data"
    }
}

private class DocumentPickerDelegate(
    private val onPicked: (SupportChatPickedFile?) -> Unit,
    private val onFinished: () -> Unit,
) : NSObject(), UIDocumentPickerDelegateProtocol {

    override fun documentPicker(
        controller: UIDocumentPickerViewController,
        didPickDocumentsAtURLs: List<*>,
    ) {
        val url = didPickDocumentsAtURLs.firstOrNull() as? NSURL
        onPicked(url?.copyToTemporaryFile())
        onFinished()
    }

    override fun documentPickerWasCancelled(controller: UIDocumentPickerViewController) {
        onPicked(null)
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

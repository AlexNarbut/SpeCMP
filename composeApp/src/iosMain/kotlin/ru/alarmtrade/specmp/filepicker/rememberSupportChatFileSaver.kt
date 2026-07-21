@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package ru.alarmtrade.specmp.filepicker

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.Foundation.NSData
import platform.Foundation.NSFileManager
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSUUID
import platform.Foundation.NSURL
import platform.Foundation.create
import platform.Foundation.writeToURL
import platform.UIKit.UIApplication
import platform.UIKit.UIDocumentPickerDelegateProtocol
import platform.UIKit.UIDocumentPickerViewController
import platform.UIKit.UIViewController
import platform.UIKit.UIWindowScene
import platform.darwin.NSObject
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue
import ru.alarmtrade.specmp.feature.supportchat.domain.picker.SupportChatFileSaver

@Composable
actual fun rememberSupportChatFileSaver(): SupportChatFileSaver {
    return remember {
        object : SupportChatFileSaver {
            override suspend fun saveToDownloads(fileName: String, bytes: ByteArray): String? {
                if (bytes.isEmpty()) return null
                return try {
                    val tempUrl = writeTempFile(fileName, bytes) ?: return null
                    try {
                        presentExportPicker(tempUrl)
                    } finally {
                        NSFileManager.defaultManager.removeItemAtURL(tempUrl, error = null)
                    }
                } catch (e: Throwable) {
                    null
                }
            }
        }
    }
}

private var activeExportDelegate: ExportDelegate? = null

private class ExportDelegate(
    private val onResult: (NSURL?) -> Unit,
) : NSObject(), UIDocumentPickerDelegateProtocol {

    override fun documentPicker(
        controller: UIDocumentPickerViewController,
        didPickDocumentsAtURLs: List<*>,
    ) {
        onResult(didPickDocumentsAtURLs.firstOrNull() as? NSURL)
    }

    override fun documentPickerWasCancelled(controller: UIDocumentPickerViewController) {
        onResult(null)
    }
}

private suspend fun presentExportPicker(fileUrl: NSURL): String? = suspendCancellableCoroutine { cont ->
    dispatch_async(dispatch_get_main_queue()) {
        val presenter = topViewController()
        if (presenter == null) {
            cont.resumeWith(Result.success(null))
            return@dispatch_async
        }
        val delegate = ExportDelegate { pickedUrl ->
            activeExportDelegate = null
            if (cont.isActive) cont.resumeWith(Result.success(pickedUrl?.path))
        }
        activeExportDelegate = delegate
        val picker = UIDocumentPickerViewController(forExportingURLs = listOf(fileUrl), asCopy = true)
        picker.delegate = delegate
        presenter.presentViewController(picker, animated = true, completion = null)
    }
}

private fun topViewController(): UIViewController? {
    val scene = UIApplication.sharedApplication.connectedScenes.firstOrNull() as? UIWindowScene
    var controller = scene?.keyWindow?.rootViewController
    while (controller?.presentedViewController != null) {
        controller = controller.presentedViewController
    }
    return controller
}

private fun writeTempFile(fileName: String, bytes: ByteArray): NSURL? {
    val fileUrl = NSURL.fileURLWithPath(NSTemporaryDirectory() + NSUUID().UUIDString + "-" + fileName)
    val data = bytes.usePinned { NSData.create(bytes = it.addressOf(0), length = bytes.size.toULong()) }
    return if (data.writeToURL(fileUrl, atomically = true)) fileUrl else null
}

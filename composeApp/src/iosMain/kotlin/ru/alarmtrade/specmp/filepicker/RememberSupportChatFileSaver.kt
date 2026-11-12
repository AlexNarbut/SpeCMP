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
import platform.Foundation.NSURL
import platform.Foundation.NSUUID
import platform.Foundation.create
import platform.Foundation.writeToURL
import platform.UIKit.UIApplication
import platform.UIKit.UIDocumentPickerDelegateProtocol
import platform.UIKit.UIDocumentPickerViewController
import platform.UIKit.UISceneActivationStateForegroundActive
import platform.UIKit.UIViewController
import platform.UIKit.UIWindow
import platform.UIKit.UIWindowScene
import platform.darwin.NSObject
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue
import ru.alarmtrade.specmp.core.common.file.safeFileName
import ru.alarmtrade.specmp.feature.supportchat.presentation.platform.SupportChatFileSaveResult
import ru.alarmtrade.specmp.feature.supportchat.presentation.platform.SupportChatFileSaver

@Composable
actual fun rememberSupportChatFileSaver(): SupportChatFileSaver {
    return remember {
        object : SupportChatFileSaver {
            override suspend fun saveToDownloads(fileName: String, bytes: ByteArray): SupportChatFileSaveResult {
                if (bytes.isEmpty()) return SupportChatFileSaveResult.Failed
                return try {
                    val tempUrl = writeTempFile(fileName, bytes) ?: return SupportChatFileSaveResult.Failed
                    try {
                        presentExportPicker(tempUrl)
                    } finally {
                        NSFileManager.defaultManager.removeItemAtURL(tempUrl, error = null)
                    }
                } catch (ignored: Throwable) {
                    SupportChatFileSaveResult.Failed
                }
            }
        }
    }
}

private class ExportDelegate(private val onResult: (NSURL?) -> Unit) :
    NSObject(),
    UIDocumentPickerDelegateProtocol {

    override fun documentPicker(controller: UIDocumentPickerViewController, didPickDocumentsAtURLs: List<*>) {
        onResult(didPickDocumentsAtURLs.firstOrNull() as? NSURL)
    }

    override fun documentPickerWasCancelled(controller: UIDocumentPickerViewController) {
        onResult(null)
    }
}

/**
 * Делегат пикера — weak-ссылка: его держит корутина (захват в `invokeOnCancellation`). При отмене пикер
 * закрывается — иначе `finally` удалил бы временный файл, пока пользователь выбирает место.
 */
private suspend fun presentExportPicker(fileUrl: NSURL): SupportChatFileSaveResult =
    suspendCancellableCoroutine { cont ->
        dispatch_async(dispatch_get_main_queue()) {
            val presenter = topViewController()
            if (presenter == null) {
                cont.resumeWith(Result.success(SupportChatFileSaveResult.Failed))
                return@dispatch_async
            }
            val delegate = ExportDelegate { pickedUrl ->
                if (cont.isActive) {
                    val result = when {
                        pickedUrl == null -> SupportChatFileSaveResult.Cancelled
                        pickedUrl.path != null -> SupportChatFileSaveResult.Saved(pickedUrl.path!!)
                        else -> SupportChatFileSaveResult.Failed
                    }
                    cont.resumeWith(Result.success(result))
                }
            }
            val picker = UIDocumentPickerViewController(forExportingURLs = listOf(fileUrl), asCopy = true)
            picker.delegate = delegate
            cont.invokeOnCancellation {
                dispatch_async(dispatch_get_main_queue()) {
                    // Захват delegate здесь и удерживает его до закрытия пикера.
                    if (picker.delegate === delegate) picker.delegate = null
                    picker.dismissViewControllerAnimated(true, null)
                }
            }
            presenter.presentViewController(picker, animated = true, completion = null)
        }
    }

internal fun topViewController(): UIViewController? {
    val scenes = UIApplication.sharedApplication.connectedScenes.filterIsInstance<UIWindowScene>()
    val scene =
        scenes.firstOrNull { it.activationState == UISceneActivationStateForegroundActive } ?: scenes.firstOrNull()
    var controller = scene?.keyWindow?.rootViewController ?: scene?.windows?.firstOrNull()?.let {
        (it as? UIWindow)?.rootViewController
    }
    // Закрывающийся контроллер новую презентацию не примет — показываем поверх того, что останется.
    while (controller?.presentedViewController?.isBeingDismissed() == false) {
        controller = controller.presentedViewController
    }
    return controller
}

private fun writeTempFile(fileName: String, bytes: ByteArray): NSURL? {
    // Имя с сервера: без санитизации `/` в имени ломает путь временного файла.
    val fileUrl = NSURL.fileURLWithPath(NSTemporaryDirectory() + NSUUID().UUIDString + "-" + safeFileName(fileName))
    val data = bytes.usePinned { NSData.create(bytes = it.addressOf(0), length = bytes.size.toULong()) }
    return if (data.writeToURL(fileUrl, atomically = true)) fileUrl else null
}

@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class, kotlinx.cinterop.BetaInteropApi::class)

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
import platform.UIKit.UIDocumentInteractionController
import platform.UIKit.UIDocumentInteractionControllerDelegateProtocol
import platform.UIKit.UIViewController
import platform.darwin.NSObject
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue
import ru.alarmtrade.specmp.feature.supportchat.presentation.platform.SupportChatFileOpenResult
import ru.alarmtrade.specmp.feature.supportchat.presentation.platform.SupportChatFileOpener

/**
 * iOS: предпросмотр QuickLook через `UIDocumentInteractionController`; если тип
 * не поддерживается предпросмотром — меню «Открыть в…» (выбор приложения).
 * Разрешения не нужны.
 */
@Composable
actual fun rememberSupportChatFileOpener(): SupportChatFileOpener {
    return remember {
        object : SupportChatFileOpener {
            override suspend fun openBytes(fileName: String, bytes: ByteArray): SupportChatFileOpenResult {
                val url = writeOpenFile(fileName, bytes) ?: return SupportChatFileOpenResult.Failed
                return present(url)
            }

            override suspend fun openLocal(path: String, fileName: String): SupportChatFileOpenResult {
                if (!NSFileManager.defaultManager.fileExistsAtPath(path)) return SupportChatFileOpenResult.Failed
                return present(NSURL.fileURLWithPath(path))
            }
        }
    }
}

/**
 * Контроллер и делегат должны жить, пока открыт предпросмотр (у контроллера делегат
 * weak), — держим последний открытый; новый заменяет предыдущий.
 */
private object ActiveDocumentPreview {
    var controller: UIDocumentInteractionController? = null
    var delegate: PreviewDelegate? = null
}

private class PreviewDelegate(
    private val presenter: UIViewController,
) : NSObject(), UIDocumentInteractionControllerDelegateProtocol {
    override fun documentInteractionControllerViewControllerForPreview(
        controller: UIDocumentInteractionController,
    ): UIViewController = presenter
}

private suspend fun present(url: NSURL): SupportChatFileOpenResult = suspendCancellableCoroutine { cont ->
    dispatch_async(dispatch_get_main_queue()) {
        val presenter = topViewController()
        if (presenter == null) {
            cont.resumeWith(Result.success(SupportChatFileOpenResult.Failed))
            return@dispatch_async
        }
        val controller = UIDocumentInteractionController.interactionControllerWithURL(url)
        val delegate = PreviewDelegate(presenter)
        controller.delegate = delegate
        ActiveDocumentPreview.controller = controller
        ActiveDocumentPreview.delegate = delegate

        val result = when {
            controller.presentPreviewAnimated(true) -> SupportChatFileOpenResult.Opened
            controller.presentOpenInMenuFromRect(presenter.view.bounds, inView = presenter.view, animated = true) ->
                SupportChatFileOpenResult.Opened
            else -> SupportChatFileOpenResult.NoApp
        }
        cont.resumeWith(Result.success(result))
    }
}

private fun writeOpenFile(fileName: String, bytes: ByteArray): NSURL? {
    val directory = "${NSTemporaryDirectory()}$OPEN_DIR/${NSUUID().UUIDString}"
    val created = NSFileManager.defaultManager.createDirectoryAtPath(
        path = directory,
        withIntermediateDirectories = true,
        attributes = null,
        error = null,
    )
    if (!created) return null
    val url = NSURL.fileURLWithPath("$directory/${safeFileName(fileName)}")
    val data = bytes.usePinned { NSData.create(bytes = it.addressOf(0), length = bytes.size.toULong()) }
    return if (data.writeToURL(url, atomically = true)) url else null
}

private const val OPEN_DIR = "support-chat-open"

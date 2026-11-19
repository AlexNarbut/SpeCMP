@file:OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)

package ru.alarmtrade.specmp.core.files

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.withContext
import platform.Foundation.NSFileManager
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSURL
import platform.Foundation.NSUUID
import platform.Foundation.writeToFile
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIDocumentInteractionController
import platform.UIKit.UIDocumentInteractionControllerDelegateProtocol
import platform.UIKit.UIViewController
import platform.UIKit.popoverPresentationController
import platform.darwin.NSObject
import ru.alarmtrade.specmp.core.common.AppDispatchers

@Composable
actual fun rememberBinaryFileExporter(dispatchers: AppDispatchers): BinaryFileExporter = remember(dispatchers) {
    object : BinaryFileExporter {
        override val canShare: Boolean = true

        override suspend fun save(fileName: String, type: BinaryFileType, bytes: ByteArray): BinaryFileSaveResult =
            exportWithPicker(dispatchers, fileName, type.extension, bytes)

        override suspend fun share(fileName: String, type: BinaryFileType, bytes: ByteArray): Boolean {
            val url = withContext(dispatchers.io) { tempCopy(fileName, type, bytes) } ?: return false
            return withContext(dispatchers.main) { presentShare(url) }
        }

        override suspend fun open(fileName: String, type: BinaryFileType, bytes: ByteArray): BinaryFileOpenResult {
            val url =
                withContext(dispatchers.io) { tempCopy(fileName, type, bytes) } ?: return BinaryFileOpenResult.Failed
            return withContext(dispatchers.main) { presentPreview(url) }
        }
    }
}

@Composable
actual fun rememberHtmlPdfRenderer(dispatchers: AppDispatchers): HtmlPdfRenderer =
    remember(dispatchers) { IosHtmlPdfRenderer(dispatchers) }

@Composable
actual fun rememberPdfPageRasterizer(dispatchers: AppDispatchers): PdfPageRasterizer =
    remember(dispatchers) { IosPdfPageRasterizer(dispatchers) }

/** Копии живут до следующего вызова: «Поделиться» и предпросмотр читают файл после возврата из функции. */
private fun tempCopy(fileName: String, type: BinaryFileType, bytes: ByteArray): NSURL? {
    val fileManager = NSFileManager.defaultManager
    val root = NSTemporaryDirectory().trimEnd('/') + "/" + SHARE_DIR
    fileManager.removeItemAtPath(root, null)
    val directory = root + "/" + NSUUID().UUIDString
    if (!fileManager.createDirectoryAtPath(directory, true, null, null)) return null
    val path = directory + "/" + fileNameWithExtension(fileName, type.extension)
    return if (bytes.toNSData().writeToFile(path, true)) NSURL.fileURLWithPath(path) else null
}

private fun presentShare(url: NSURL): Boolean {
    val presenter = topViewController() ?: return false
    val controller = UIActivityViewController(activityItems = listOf(url), applicationActivities = null)
    // iPad: меню — поповер, ему нужен якорь.
    controller.popoverPresentationController?.sourceView = presenter.view
    presenter.presentViewController(controller, animated = true, completion = null)
    return true
}

private fun presentPreview(url: NSURL): BinaryFileOpenResult {
    val presenter = topViewController() ?: return BinaryFileOpenResult.Failed
    val controller = UIDocumentInteractionController.interactionControllerWithURL(url)
    val delegate = PreviewDelegate(presenter)
    controller.delegate = delegate
    ActivePreview.controller = controller
    ActivePreview.delegate = delegate
    return if (controller.presentPreviewAnimated(true)) BinaryFileOpenResult.Opened else BinaryFileOpenResult.NoApp
}

/** Делегат у контроллера weak: держим последний открытый предпросмотр, новый заменяет его. */
private object ActivePreview {
    var controller: UIDocumentInteractionController? = null
    var delegate: PreviewDelegate? = null
}

private class PreviewDelegate(private val presenter: UIViewController) :
    NSObject(),
    UIDocumentInteractionControllerDelegateProtocol {
    override fun documentInteractionControllerViewControllerForPreview(
        controller: UIDocumentInteractionController,
    ): UIViewController = presenter
}

private const val SHARE_DIR = "file-share"

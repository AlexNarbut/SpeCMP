@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.uikit.LocalUIViewController
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
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
import platform.darwin.DISPATCH_QUEUE_PRIORITY_DEFAULT
import platform.darwin.NSObject
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_global_queue
import platform.darwin.dispatch_get_main_queue
import ru.alarmtrade.specmp.core.common.file.safeFileName

@Composable
actual fun rememberFirmwareFilePicker(appScope: CoroutineScope, ioDispatcher: CoroutineDispatcher): FirmwareFilePicker {
    val viewController = LocalUIViewController.current
    return remember(viewController) { IosFirmwareFilePicker(viewController) }
}

private class IosFirmwareFilePicker(private val viewController: UIViewController) : FirmwareFilePicker {

    private var activeDelegate: NSObject? = null

    // Делегат отдаёт результат на main; буфер — до подписки экрана.
    private val channel = Channel<FirmwareFilePickResult>(Channel.UNLIMITED)
    override val results: Flow<FirmwareFilePickResult> = channel.receiveAsFlow()

    override fun launch() {
        val picker = UIDocumentPickerViewController(
            documentTypes = listOf(DATA_UTI),
            inMode = UIDocumentPickerMode.UIDocumentPickerModeOpen,
        ).apply { allowsMultipleSelection = false }
        val delegate = FirmwareDocumentPickerDelegate(
            onPicked = { channel.trySend(it) },
            onFinished = { activeDelegate = null },
        )
        activeDelegate = delegate
        picker.delegate = delegate
        viewController.presentViewController(viewControllerToPresent = picker, animated = true, completion = null)
    }

    private companion object {
        const val DATA_UTI = "public.data"
    }
}

private class FirmwareDocumentPickerDelegate(
    private val onPicked: (FirmwareFilePickResult) -> Unit,
    private val onFinished: () -> Unit,
) : NSObject(),
    UIDocumentPickerDelegateProtocol {

    override fun documentPicker(controller: UIDocumentPickerViewController, didPickDocumentsAtURLs: List<*>) {
        val url = didPickDocumentsAtURLs.firstOrNull() as? NSURL
        if (url == null) {
            onPicked(FirmwareFilePickResult.Cancelled)
            onFinished()
            return
        }
        // Копирование не должно вешать main-поток.
        dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT.toLong(), 0u)) {
            val picked = url.copyToTemporary()
            dispatch_async(dispatch_get_main_queue()) {
                onPicked(picked)
                onFinished()
            }
        }
    }

    override fun documentPickerWasCancelled(controller: UIDocumentPickerViewController) {
        onPicked(FirmwareFilePickResult.Cancelled)
        onFinished()
    }
}

/** Копия в `tmp/firmware-files/<uuid>/<имя>`: прошлые копии стираются перед новой, размер проверяется до и после. */
private fun NSURL.copyToTemporary(): FirmwareFilePickResult {
    val canAccess = startAccessingSecurityScopedResource()
    return try {
        val fileManager = NSFileManager.defaultManager
        val sourceSize = path?.let { sourcePath ->
            (fileManager.attributesOfItemAtPath(sourcePath, error = null)?.get(NSFileSize) as? NSNumber)?.longLongValue
        }
        if ((sourceSize ?: 0L) > FirmwareFileLimits.MAX_BYTES) return FirmwareFilePickResult.TooLarge

        val name = safeFileName(lastPathComponent ?: DEFAULT_NAME, fallback = DEFAULT_NAME)
        val root = NSTemporaryDirectory().trimEnd('/') + "/" + FIRMWARE_DIR
        fileManager.removeItemAtPath(root, error = null)
        val directory = "$root/${NSUUID().UUIDString}"
        val destination = "$directory/$name"
        val created = fileManager.createDirectoryAtPath(
            path = directory,
            withIntermediateDirectories = true,
            attributes = null,
            error = null,
        )
        if (!created || !fileManager.copyItemAtURL(this, NSURL.fileURLWithPath(destination), error = null)) {
            fileManager.removeItemAtPath(root, error = null)
            return FirmwareFilePickResult.Failed
        }
        val copiedSize = (fileManager.attributesOfItemAtPath(destination, error = null)?.get(NSFileSize) as? NSNumber)
            ?.longLongValue
        if ((copiedSize ?: 0L) > FirmwareFileLimits.MAX_BYTES) {
            fileManager.removeItemAtPath(root, error = null)
            return FirmwareFilePickResult.TooLarge
        }
        FirmwareFilePickResult.Picked(name, destination)
    } finally {
        if (canAccess) stopAccessingSecurityScopedResource()
    }
}

private const val DEFAULT_NAME = "firmware"
private const val FIRMWARE_DIR = "firmware-files"

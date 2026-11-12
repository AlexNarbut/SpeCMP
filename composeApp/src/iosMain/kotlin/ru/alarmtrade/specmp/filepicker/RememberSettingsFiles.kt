@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package ru.alarmtrade.specmp.filepicker

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.uikit.LocalUIViewController
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.Foundation.NSFileManager
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
import ru.alarmtrade.specmp.feature.device.presentation.core.platform.SettingsFilePickResult
import ru.alarmtrade.specmp.feature.device.presentation.core.platform.SettingsFilePicker
import ru.alarmtrade.specmp.feature.device.presentation.core.platform.SettingsFileSaveResult
import ru.alarmtrade.specmp.feature.device.presentation.core.platform.SettingsFileSaver

@Composable
actual fun rememberSettingsFilePicker(): SettingsFilePicker {
    val viewController = LocalUIViewController.current
    return remember(viewController) { IosSettingsFilePicker(viewController) }
}

@Composable
actual fun rememberSettingsFileSaver(): SettingsFileSaver = remember { IosSettingsFileSaver() }

private class IosSettingsFilePicker(private val viewController: UIViewController) : SettingsFilePicker {

    private var activeDelegate: NSObject? = null

    // Делегат отдаёт результат на main; буфер — до подписки экрана.
    private val channel = Channel<SettingsFilePickResult>(Channel.UNLIMITED)
    override val results: Flow<SettingsFilePickResult> = channel.receiveAsFlow()

    override fun launch() {
        val picker = UIDocumentPickerViewController(
            documentTypes = listOf(DATA_UTI),
            inMode = UIDocumentPickerMode.UIDocumentPickerModeOpen,
        ).apply { allowsMultipleSelection = false }
        val delegate = SettingsPickerDelegate(
            onPicked = { channel.trySend(it) },
            onFinished = { activeDelegate = null },
        )
        activeDelegate = delegate
        picker.delegate = delegate
        viewController.presentViewController(viewControllerToPresent = picker, animated = true, completion = null)
    }
}

private class SettingsPickerDelegate(
    private val onPicked: (SettingsFilePickResult) -> Unit,
    private val onFinished: () -> Unit,
) : NSObject(),
    UIDocumentPickerDelegateProtocol {

    override fun documentPicker(controller: UIDocumentPickerViewController, didPickDocumentsAtURLs: List<*>) {
        val url = didPickDocumentsAtURLs.firstOrNull() as? NSURL
        if (url == null) {
            onPicked(SettingsFilePickResult.Cancelled)
            onFinished()
            return
        }
        dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT.toLong(), 0u)) {
            val picked = url.copySettingsToTemporary()
            dispatch_async(dispatch_get_main_queue()) {
                onPicked(picked)
                onFinished()
            }
        }
    }

    override fun documentPickerWasCancelled(controller: UIDocumentPickerViewController) {
        onPicked(SettingsFilePickResult.Cancelled)
        onFinished()
    }
}

/** Копия в `tmp/settings-picked/<uuid>/<имя>`: прошлые копии стираются перед новой. */
private fun NSURL.copySettingsToTemporary(): SettingsFilePickResult {
    val canAccess = startAccessingSecurityScopedResource()
    return try {
        val fileManager = NSFileManager.defaultManager
        val name = safeFileName(lastPathComponent ?: DEFAULT_NAME)
        val root = NSTemporaryDirectory().trimEnd('/') + "/" + PICKED_DIR
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
            SettingsFilePickResult.Failed
        } else {
            SettingsFilePickResult.Picked(name, destination, inPlace = false)
        }
    } finally {
        if (canAccess) stopAccessingSecurityScopedResource()
    }
}

private class IosSettingsFileSaver : SettingsFileSaver {
    override suspend fun save(sourcePath: String, fileName: String): SettingsFileSaveResult {
        val fileManager = NSFileManager.defaultManager
        val directory = NSTemporaryDirectory().trimEnd('/') + "/" + EXPORT_DIR + "/" + NSUUID().UUIDString
        val target = "$directory/${safeFileName(fileName)}"
        val prepared = fileManager.createDirectoryAtPath(
            path = directory,
            withIntermediateDirectories = true,
            attributes = null,
            error = null,
        ) && fileManager.copyItemAtPath(sourcePath, toPath = target, error = null)
        if (!prepared) {
            fileManager.removeItemAtPath(directory, error = null)
            return SettingsFileSaveResult.Failed
        }
        return try {
            presentExport(NSURL.fileURLWithPath(target), safeFileName(fileName))
        } finally {
            fileManager.removeItemAtPath(directory, error = null)
        }
    }
}

private class SettingsExportDelegate(private val onResult: (NSURL?) -> Unit) :
    NSObject(),
    UIDocumentPickerDelegateProtocol {

    override fun documentPicker(controller: UIDocumentPickerViewController, didPickDocumentsAtURLs: List<*>) {
        onResult(didPickDocumentsAtURLs.firstOrNull() as? NSURL)
    }

    override fun documentPickerWasCancelled(controller: UIDocumentPickerViewController) {
        onResult(null)
    }
}

/** Делегат пикера — weak-ссылка: его держит корутина (захват в `invokeOnCancellation`). */
private suspend fun presentExport(fileUrl: NSURL, fileName: String): SettingsFileSaveResult =
    suspendCancellableCoroutine { cont ->
        dispatch_async(dispatch_get_main_queue()) {
            val presenter = topViewController()
            if (presenter == null) {
                cont.resumeWith(Result.success(SettingsFileSaveResult.Failed))
                return@dispatch_async
            }
            val delegate = SettingsExportDelegate { pickedUrl ->
                if (cont.isActive) {
                    val result = if (pickedUrl == null) {
                        SettingsFileSaveResult.Cancelled
                    } else {
                        SettingsFileSaveResult.Saved(pickedUrl.lastPathComponent ?: fileName)
                    }
                    cont.resumeWith(Result.success(result))
                }
            }
            val picker = UIDocumentPickerViewController(forExportingURLs = listOf(fileUrl), asCopy = true)
            picker.delegate = delegate
            cont.invokeOnCancellation {
                dispatch_async(dispatch_get_main_queue()) {
                    if (picker.delegate === delegate) picker.delegate = null
                    picker.dismissViewControllerAnimated(true, null)
                }
            }
            presenter.presentViewController(picker, animated = true, completion = null)
            // UIKit молча отказывает в показе, пока идёт другая презентация/закрытие: без проверки корутина
            // висит, «Сохранить» остаётся занятым и ничего не происходит.
            if (picker.presentingViewController == null && cont.isActive) {
                picker.delegate = null
                cont.resumeWith(Result.success(SettingsFileSaveResult.Failed))
            }
        }
    }

private const val DATA_UTI = "public.data"
private const val DEFAULT_NAME = "settings.dxs"
private const val PICKED_DIR = "settings-picked"
private const val EXPORT_DIR = "settings-export"

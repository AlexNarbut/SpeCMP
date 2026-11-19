@file:OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)

package ru.alarmtrade.specmp.core.files

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import platform.Foundation.NSData
import platform.Foundation.NSFileManager
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSURL
import platform.Foundation.NSUUID
import platform.Foundation.dataWithContentsOfURL
import platform.Foundation.writeToFile
import platform.UIKit.UIApplication
import platform.UIKit.UIDocumentPickerDelegateProtocol
import platform.UIKit.UIDocumentPickerMode
import platform.UIKit.UIDocumentPickerViewController
import platform.UIKit.UISceneActivationStateForegroundActive
import platform.UIKit.UIViewController
import platform.UIKit.UIWindow
import platform.UIKit.UIWindowScene
import platform.darwin.NSObject
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue
import ru.alarmtrade.specmp.core.common.AppDispatchers
import ru.alarmtrade.specmp.core.common.file.safeFileName

@Composable
actual fun rememberTextFilePicker(dispatchers: AppDispatchers): TextFilePicker = remember(dispatchers) {
    object : TextFilePicker {
        override suspend fun pick(type: TextFileType): TextFilePickResult {
            val outcome = withContext(dispatchers.main) {
                presentDocumentPicker {
                    UIDocumentPickerViewController(
                        documentTypes = listOf(type.uti),
                        inMode = UIDocumentPickerMode.UIDocumentPickerModeImport,
                    ).apply { allowsMultipleSelection = false }
                }
            }
            return when (outcome) {
                PickerOutcome.NotPresented -> TextFilePickResult.Failed
                PickerOutcome.Cancelled -> TextFilePickResult.Cancelled
                is PickerOutcome.Picked -> withContext(dispatchers.io) { outcome.url.readText(type) }
            }
        }
    }
}

@Composable
actual fun rememberTextFileSaver(dispatchers: AppDispatchers): TextFileSaver = remember(dispatchers) {
    object : TextFileSaver {
        override suspend fun save(suggestedName: String, type: TextFileType, text: String): TextFileSaveResult =
            exportWithPicker(dispatchers, suggestedName, type.extension, text.encodeToByteArray()).toTextResult()
    }
}

/** Копия во временной папке → экспорт-пикер (место выбирает пользователь) → копия удаляется. */
internal suspend fun exportWithPicker(
    dispatchers: AppDispatchers,
    suggestedName: String,
    extension: String,
    bytes: ByteArray,
): BinaryFileSaveResult {
    val fileName = fileNameWithExtension(suggestedName, extension)
    val directory = NSTemporaryDirectory().trimEnd('/') + "/" + EXPORT_DIR + "/" + NSUUID().UUIDString
    val path = "$directory/$fileName"
    val fileManager = NSFileManager.defaultManager
    return try {
        val written = withContext(dispatchers.io) {
            fileManager.createDirectoryAtPath(directory, true, null, null) && bytes.toNSData().writeToFile(path, true)
        }
        if (!written) return BinaryFileSaveResult.Failed
        val outcome = withContext(dispatchers.main) {
            presentDocumentPicker {
                UIDocumentPickerViewController(forExportingURLs = listOf(NSURL.fileURLWithPath(path)), asCopy = true)
            }
        }
        when (outcome) {
            PickerOutcome.NotPresented -> BinaryFileSaveResult.Failed
            PickerOutcome.Cancelled -> BinaryFileSaveResult.Cancelled
            is PickerOutcome.Picked -> BinaryFileSaveResult.Saved(outcome.url.lastPathComponent ?: fileName)
        }
    } finally {
        fileManager.removeItemAtPath(directory, null)
    }
}

private sealed interface PickerOutcome {
    class Picked(val url: NSURL) : PickerOutcome

    data object Cancelled : PickerOutcome

    data object NotPresented : PickerOutcome
}

/** Вызывать на main. Делегат пикера — weak-ссылка: его держит корутина (захват в `invokeOnCancellation`). */
private suspend fun presentDocumentPicker(create: () -> UIDocumentPickerViewController): PickerOutcome =
    suspendCancellableCoroutine { cont ->
        val presenter = topViewController()
        if (presenter == null) {
            cont.resumeWith(Result.success(PickerOutcome.NotPresented))
            return@suspendCancellableCoroutine
        }
        val picker = create()
        val delegate = PickerDelegate { url ->
            val outcome = url?.let(PickerOutcome::Picked) ?: PickerOutcome.Cancelled
            if (cont.isActive) cont.resumeWith(Result.success(outcome))
        }
        picker.delegate = delegate
        cont.invokeOnCancellation {
            dispatch_async(dispatch_get_main_queue()) {
                if (picker.delegate === delegate) picker.delegate = null
                picker.dismissViewControllerAnimated(true, null)
            }
        }
        presenter.presentViewController(picker, animated = true, completion = null)
        // UIKit молча отказывает в показе, пока идёт другая презентация/закрытие: без проверки корутина зависнет.
        if (picker.presentingViewController == null && cont.isActive) {
            picker.delegate = null
            cont.resumeWith(Result.success(PickerOutcome.NotPresented))
        }
    }

private class PickerDelegate(private val onResult: (NSURL?) -> Unit) :
    NSObject(),
    UIDocumentPickerDelegateProtocol {

    override fun documentPicker(controller: UIDocumentPickerViewController, didPickDocumentsAtURLs: List<*>) {
        onResult(didPickDocumentsAtURLs.firstOrNull() as? NSURL)
    }

    override fun documentPickerWasCancelled(controller: UIDocumentPickerViewController) {
        onResult(null)
    }
}

private fun NSURL.readText(type: TextFileType): TextFilePickResult {
    val canAccess = startAccessingSecurityScopedResource()
    return try {
        val data = NSData.dataWithContentsOfURL(this) ?: return TextFilePickResult.Failed
        if (data.length.toLong() > type.maxBytes) return TextFilePickResult.TooLarge
        val bytes = data.toByteArray()
        val name = safeFileName(lastPathComponent.orEmpty(), fallback = fileNameWithExtension("", type.extension))
        decodeTextFile(name, bytes, type.maxBytes)
    } finally {
        if (canAccess) stopAccessingSecurityScopedResource()
    }
}

internal fun topViewController(): UIViewController? {
    val scenes = UIApplication.sharedApplication.connectedScenes.filterIsInstance<UIWindowScene>()
    val scene =
        scenes.firstOrNull { it.activationState == UISceneActivationStateForegroundActive } ?: scenes.firstOrNull()
    // keyWindow может быть закрывающимся окном Compose-диалога: показанное из него исчезнет вместе с окном.
    val window = scene?.keyWindow?.takeIf { !it.hidden }
        ?: scene?.windows?.filterIsInstance<UIWindow>()?.lastOrNull { !it.hidden }
    var controller = window?.rootViewController
    // Закрывающийся контроллер новую презентацию не примет — показываем поверх того, что останется.
    while (controller?.presentedViewController?.isBeingDismissed() == false) {
        controller = controller.presentedViewController
    }
    // Контроллер вне иерархии окон презентацию «примет» молча: ничего не появится, колбэк не придёт.
    return controller?.takeIf { it.view.window != null }
}

private const val EXPORT_DIR = "file-export"

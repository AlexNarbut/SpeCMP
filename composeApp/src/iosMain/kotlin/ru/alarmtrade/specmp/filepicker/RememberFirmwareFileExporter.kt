@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class, kotlinx.cinterop.BetaInteropApi::class)

package ru.alarmtrade.specmp.filepicker

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.Foundation.NSURL
import platform.UIKit.UIActivityViewController
import platform.UIKit.popoverPresentationController
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.platform.FirmwareFileExporter
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.platform.FirmwareSaveResult
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.platform.FirmwareSaveTarget

@Composable
actual fun rememberFirmwareFileExporter(): FirmwareFileExporter {
    val saver = rememberSupportChatFileSaver()
    return remember(saver) {
        object : FirmwareFileExporter {
            override val target = FirmwareSaveTarget.Chosen
            override val canShare = true
            override val canOpenFolder = false

            override suspend fun save(fileName: String, bytes: ByteArray): FirmwareSaveResult =
                saver.saveToDownloads(fileName, bytes).toFirmware { null }

            override suspend fun share(fileName: String, bytes: ByteArray): Boolean {
                val url = writeTempFile(SHARE_DIR, fileName, bytes) ?: return false
                return presentShare(url)
            }

            override suspend fun openFolder(path: String): Boolean = false
        }
    }
}

private suspend fun presentShare(url: NSURL): Boolean = suspendCancellableCoroutine { cont ->
    dispatch_async(dispatch_get_main_queue()) {
        val presenter = topViewController()
        if (presenter == null) {
            cont.resumeWith(Result.success(false))
            return@dispatch_async
        }
        val controller = UIActivityViewController(activityItems = listOf(url), applicationActivities = null)
        // iPad: меню — поповер, ему нужен якорь.
        controller.popoverPresentationController?.sourceView = presenter.view
        presenter.presentViewController(controller, animated = true, completion = null)
        cont.resumeWith(Result.success(true))
    }
}

private const val SHARE_DIR = "firmware-share"

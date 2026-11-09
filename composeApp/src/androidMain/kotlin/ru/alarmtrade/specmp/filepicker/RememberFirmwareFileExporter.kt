package ru.alarmtrade.specmp.filepicker

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import java.io.File
import java.util.UUID
import ru.alarmtrade.specmp.core.common.file.safeFileName
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.platform.FirmwareFileExporter
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.platform.FirmwareSaveResult
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.platform.FirmwareSaveTarget

/**
 * «Загрузки/Pandora Spec» через MediaStore (API < 29 — с разрешением); «Поделиться» — копия в `cacheDir` через
 * FileProvider (пути — в `support_chat_file_paths.xml` androidApp).
 */
@Composable
actual fun rememberFirmwareFileExporter(): FirmwareFileExporter {
    val appContext = LocalContext.current.applicationContext
    val saver = rememberSupportChatFileSaver()
    return remember(appContext, saver) {
        object : FirmwareFileExporter {
            override val target = FirmwareSaveTarget.Downloads
            override val canShare = true
            override val canOpenFolder = false

            override suspend fun save(fileName: String, bytes: ByteArray): FirmwareSaveResult =
                saver.saveToDownloads(fileName, bytes).toFirmware { path -> path.substringBeforeLast('/', "") }

            override suspend fun share(fileName: String, bytes: ByteArray): Boolean = appContext.share(fileName, bytes)

            override suspend fun openFolder(path: String): Boolean = false
        }
    }
}

private fun Context.share(fileName: String, bytes: ByteArray): Boolean = try {
    // Старые копии чистим перед записью: система вправе вычистить cacheDir и сама.
    val root = File(cacheDir, SHARE_DIR).apply {
        deleteRecursively()
        mkdirs()
    }
    val directory = File(root, UUID.randomUUID().toString()).apply { mkdirs() }
    val file = File(directory, safeFileName(fileName)).apply { writeBytes(bytes) }
    val uri = FileProvider.getUriForFile(this, "$packageName$FILE_PROVIDER_SUFFIX", file)
    val send = Intent(Intent.ACTION_SEND)
        .setType(BINARY_MIME)
        .putExtra(Intent.EXTRA_STREAM, uri)
        .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    startActivity(Intent.createChooser(send, null).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    true
} catch (ignored: Exception) {
    false
}

/** Совпадает с `android:authorities="${applicationId}.supportchat.files"` в манифесте androidApp. */
private const val FILE_PROVIDER_SUFFIX = ".supportchat.files"
private const val SHARE_DIR = "firmware-share"
private const val BINARY_MIME = "application/octet-stream"

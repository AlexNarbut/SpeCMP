package ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.platform

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import co.touchlab.kermit.Logger
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.alarmtrade.specmp.core.common.file.copyToCache
import ru.alarmtrade.specmp.core.common.file.queryNameAndSize
import ru.alarmtrade.specmp.core.common.file.safeFileName

/**
 * Буфер результатов — на уровне процесса: пока открыт системный пикер, процесс могут убить, и registry доставит
 * результат лаунчеру уже в новом процессе; он дождётся подписки экрана обновления.
 */
private object FirmwarePickerResults {
    val channel = Channel<FirmwareFilePickResult>(Channel.UNLIMITED)
}

private val logger = Logger.withTag("FirmwareFilePicker")

/** SAF (`OpenDocument`): документ копируется во временный файл в `cacheDir` — SDK принимает путь, не `content://`. */
@Composable
actual fun rememberFirmwareFilePicker(appScope: CoroutineScope, ioDispatcher: CoroutineDispatcher): FirmwareFilePicker {
    val appContext = LocalContext.current.applicationContext

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri == null) {
            FirmwarePickerResults.channel.trySend(FirmwareFilePickResult.Cancelled)
        } else {
            appScope.launch {
                val result = withContext(ioDispatcher) { uri.copyToCache(appContext) }
                FirmwarePickerResults.channel.trySend(result)
            }
        }
    }
    return remember(launcher) {
        object : FirmwareFilePicker {
            override val results: Flow<FirmwareFilePickResult> = FirmwarePickerResults.channel.receiveAsFlow()

            override fun launch() = launcher.launch(arrayOf(ANY_MIME))
        }
    }
}

/** Прошлые копии стираются перед новой: прошивка нужна только на время обновления. */
// Граница платформенного I/O: любой сбой копирования логируется и превращается в Failed
@Suppress("TooGenericExceptionCaught")
private fun Uri.copyToCache(context: Context): FirmwareFilePickResult {
    val meta = context.contentResolver.queryNameAndSize(this)
    if ((meta.sizeBytes ?: 0L) > FirmwareFileLimits.MAX_BYTES) return FirmwareFilePickResult.TooLarge

    val fileName = safeFileName(meta.name ?: DEFAULT_NAME, fallback = DEFAULT_NAME)
    return try {
        val copy = context.copyToCache(this, FIRMWARE_DIR, fileName)
        when {
            copy == null -> {
                logger.w { "Cannot open input stream for picked uri: $this" }
                FirmwareFilePickResult.Failed
            }

            // Провайдер мог не отдать SIZE — проверяем фактический размер.
            copy.bytes > FirmwareFileLimits.MAX_BYTES -> {
                copy.file.parentFile?.deleteRecursively()
                FirmwareFilePickResult.TooLarge
            }

            else -> FirmwareFilePickResult.Picked(fileName, copy.file.absolutePath)
        }
    } catch (e: Exception) {
        logger.w(e) { "Failed to copy picked firmware '$fileName'" }
        FirmwareFilePickResult.Failed
    }
}

private const val ANY_MIME = "*/*"
private const val DEFAULT_NAME = "firmware"
private const val FIRMWARE_DIR = "firmware-files"

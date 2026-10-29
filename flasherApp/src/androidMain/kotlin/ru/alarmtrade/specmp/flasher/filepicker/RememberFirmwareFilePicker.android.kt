package ru.alarmtrade.specmp.flasher.filepicker

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import co.touchlab.kermit.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.compose.koinInject
import ru.alarmtrade.specmp.core.common.AppDispatchers
import ru.alarmtrade.specmp.core.common.di.appScopeQualifier
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.platform.FirmwareFileLimits
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.platform.FirmwareFilePickResult
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.platform.FirmwareFilePicker

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
actual fun rememberFirmwareFilePicker(): FirmwareFilePicker {
    val appContext = LocalContext.current.applicationContext
    val dispatchers = koinInject<AppDispatchers>()
    val appScope = koinInject<CoroutineScope>(qualifier = appScopeQualifier)

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri == null) {
            FirmwarePickerResults.channel.trySend(FirmwareFilePickResult.Cancelled)
        } else {
            // Копирование — вне main-потока.
            appScope.launch {
                val result = withContext(dispatchers.io) { uri.copyToCache(appContext) }
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

/**
 * Копия в `cacheDir/firmware-files/<uuid>/<имя>`: прошлые копии стираются перед новой (прошивка нужна только на время
 * обновления). Размер проверяется до копирования, а если провайдер его не отдал — по факту.
 */
// Граница платформенного I/O: любой сбой копирования логируется и превращается в Failed
@Suppress("TooGenericExceptionCaught")
private fun Uri.copyToCache(context: Context): FirmwareFilePickResult {
    val meta = context.contentResolver.queryNameAndSize(this)
    if ((meta.sizeBytes ?: 0L) > FirmwareFileLimits.MAX_BYTES) return FirmwareFilePickResult.TooLarge

    val fileName = safeFileName(meta.name ?: DEFAULT_NAME)
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

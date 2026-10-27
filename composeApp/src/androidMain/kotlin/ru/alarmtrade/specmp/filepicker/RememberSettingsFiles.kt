package ru.alarmtrade.specmp.filepicker

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import co.touchlab.kermit.Logger
import java.io.File
import java.util.UUID
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.compose.koinInject
import ru.alarmtrade.specmp.core.common.AppDispatchers
import ru.alarmtrade.specmp.core.common.di.appScopeQualifier
import ru.alarmtrade.specmp.feature.device.settings.ui.presentation.platform.SettingsFilePickResult
import ru.alarmtrade.specmp.feature.device.settings.ui.presentation.platform.SettingsFilePicker
import ru.alarmtrade.specmp.feature.device.settings.ui.presentation.platform.SettingsFileSaveResult
import ru.alarmtrade.specmp.feature.device.settings.ui.presentation.platform.SettingsFileSaver

/**
 * Буфер результатов — на уровне процесса: пока открыт системный пикер, процесс могут убить, и registry доставит
 * результат лаунчеру уже в новом процессе; он дождётся подписки экрана.
 */
private object SettingsPickerResults {
    val channel = Channel<SettingsFilePickResult>(Channel.UNLIMITED)
}

private val logger = Logger.withTag("SettingsFiles")

/** SAF (`OpenDocument`): документ копируется во временный файл в `cacheDir` — SDK принимает путь, не `content://`. */
@Composable
actual fun rememberSettingsFilePicker(): SettingsFilePicker {
    val appContext = LocalContext.current.applicationContext
    val dispatchers = koinInject<AppDispatchers>()
    val appScope = koinInject<CoroutineScope>(qualifier = appScopeQualifier)

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri == null) {
            SettingsPickerResults.channel.trySend(SettingsFilePickResult.Cancelled)
        } else {
            appScope.launch {
                val result = withContext(dispatchers.io) { uri.copyToCache(appContext) }
                SettingsPickerResults.channel.trySend(result)
            }
        }
    }
    return remember(launcher) {
        object : SettingsFilePicker {
            override val results: Flow<SettingsFilePickResult> = SettingsPickerResults.channel.receiveAsFlow()

            override fun launch() = launcher.launch(arrayOf(ANY_MIME))
        }
    }
}

/**
 * SAF (`CreateDocument`): пользователь выбирает место и имя, готовый `.dxs` копируется в документ целиком (режим
 * `wt` — с обрезкой: перезапись длинного файла коротким не оставляет хвост). Лаунчер живёт в композиции,
 * сейвер ждёт результат через [SettingsSaveRequester].
 */
@Composable
actual fun rememberSettingsFileSaver(): SettingsFileSaver {
    val appContext = LocalContext.current.applicationContext
    val dispatchers = koinInject<AppDispatchers>()
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument(DXS_MIME)) { uri ->
        SettingsSaveRequester.onResult(uri)
    }
    DisposableEffect(launcher) {
        SettingsSaveRequester.launcher = launcher
        onDispose { if (SettingsSaveRequester.launcher === launcher) SettingsSaveRequester.launcher = null }
    }
    return remember(appContext, dispatchers) {
        object : SettingsFileSaver {
            // Граница платформенного I/O: любой сбой записи — Failed.
            @Suppress("TooGenericExceptionCaught")
            override suspend fun save(sourcePath: String, fileName: String): SettingsFileSaveResult {
                val uri = SettingsSaveRequester.request(safeFileName(fileName), dispatchers)
                    ?: return SettingsFileSaveResult.Cancelled
                return withContext(dispatchers.io) {
                    try {
                        val resolver = appContext.contentResolver
                        resolver.openOutputStream(uri, "wt")?.use { output ->
                            File(sourcePath).inputStream().use { it.copyTo(output) }
                        } ?: error("No output stream")
                        SettingsFileSaveResult.Saved(resolver.displayName(uri) ?: fileName)
                    } catch (e: Exception) {
                        logger.w(e) { "Settings file save failed" }
                        SettingsFileSaveResult.Failed
                    }
                }
            }
        }
    }
}

private object SettingsSaveRequester {
    var launcher: ActivityResultLauncher<String>? = null
    private var pending: CompletableDeferred<Uri?>? = null

    suspend fun request(fileName: String, dispatchers: AppDispatchers): Uri? {
        val deferred = CompletableDeferred<Uri?>()
        val started = withContext(dispatchers.main) {
            val current = launcher ?: return@withContext false
            pending = deferred
            current.launch(fileName)
            true
        }
        return if (started) deferred.await() else null
    }

    fun onResult(uri: Uri?) {
        pending?.complete(uri)
        pending = null
    }
}

/** Копия в `cacheDir/settings-picked/<uuid>/<имя>`: прошлые копии стираются перед новой. */
// Граница платформенного I/O: любой сбой копирования логируется и превращается в Failed
@Suppress("TooGenericExceptionCaught")
private fun Uri.copyToCache(context: Context): SettingsFilePickResult {
    val resolver = context.contentResolver
    val fileName = safeFileName(resolver.displayName(this) ?: DEFAULT_NAME)
    val root = File(context.cacheDir, PICKED_DIR)
    val directory = File(root, UUID.randomUUID().toString())
    return try {
        root.deleteRecursively()
        directory.mkdirs()
        val target = File(directory, fileName)
        val copied = resolver.openInputStream(this)?.use { input ->
            target.outputStream().use { output -> input.copyTo(output) }
        }
        if (copied == null) {
            directory.deleteRecursively()
            SettingsFilePickResult.Failed
        } else {
            SettingsFilePickResult.Picked(fileName, target.absolutePath, inPlace = false)
        }
    } catch (e: Exception) {
        logger.w(e) { "Failed to copy picked settings file '$fileName'" }
        directory.deleteRecursively()
        SettingsFilePickResult.Failed
    }
}

private fun android.content.ContentResolver.displayName(uri: Uri): String? =
    query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
        if (cursor.moveToFirst()) cursor.getString(0) else null
    }

private const val ANY_MIME = "*/*"
private const val DXS_MIME = "application/octet-stream"
private const val DEFAULT_NAME = "settings.dxs"
private const val PICKED_DIR = "settings-picked"

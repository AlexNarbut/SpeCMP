package ru.alarmtrade.specmp.filepicker

import android.Manifest
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import java.io.File
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import org.koin.compose.koinInject
import ru.alarmtrade.specmp.core.common.AppDispatchers
import ru.alarmtrade.specmp.feature.supportchat.presentation.platform.SupportChatFileSaveResult
import ru.alarmtrade.specmp.feature.supportchat.presentation.platform.SupportChatFileSaver

/**
 * Сейвер передаётся в конструктор per-entry VM, а VM переживает пересоздание
 * Activity — поэтому только `applicationContext`, иначе VM удерживала бы старую Activity.
 *
 * Android 7–9 (API 24–28): запись в общие «Загрузки» требует `WRITE_EXTERNAL_STORAGE`
 * (в манифесте с `maxSdkVersion="28"`) — запрашиваем в момент сохранения. Лаунчер
 * живёт в композиции, сейвер берёт текущий через [StoragePermissionRequester].
 * С API 29 — MediaStore, разрешение не нужно.
 */
@Composable
actual fun rememberSupportChatFileSaver(): SupportChatFileSaver {
    val appContext = LocalContext.current.applicationContext
    val mainDispatcher = koinInject<AppDispatchers>().main
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        StoragePermissionRequester.onResult(granted)
    }
    DisposableEffect(permissionLauncher) {
        StoragePermissionRequester.launcher = permissionLauncher
        onDispose {
            if (StoragePermissionRequester.launcher === permissionLauncher) StoragePermissionRequester.launcher = null
        }
    }
    return remember(appContext, mainDispatcher) {
        object : SupportChatFileSaver {
            override suspend fun saveToDownloads(fileName: String, bytes: ByteArray): SupportChatFileSaveResult {
                if (!StoragePermissionRequester.ensureGranted(appContext, mainDispatcher)) {
                    return SupportChatFileSaveResult.PermissionDenied
                }
                val path = saveToDownloads(appContext, safeFileName(fileName), bytes)
                return if (path != null) {
                    SupportChatFileSaveResult.Saved(path)
                } else {
                    // Ошибка записи или отсутствие прав (Android < 29 без
                    // WRITE_EXTERNAL_STORAGE) — отмены как исхода на Android нет.
                    SupportChatFileSaveResult.Failed
                }
            }
        }
    }
}

private fun saveToDownloads(context: Context, fileName: String, bytes: ByteArray): String? = try {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        saveViaMediaStore(context, fileName, bytes)
    } else {
        saveToPublicDownloads(fileName, bytes)
    }
} catch (ignored: Exception) {
    null
}

/**
 * Запись через `IS_PENDING`: пока файл не дописан, он не виден другим приложениям;
 * при ошибке запись удаляется, а не остаётся пустым файлом в «Загрузках».
 * Путь в результате — фактическое имя из MediaStore (система уникализирует дубли).
 */
@RequiresApi(Build.VERSION_CODES.Q)
private fun saveViaMediaStore(context: Context, fileName: String, bytes: ByteArray): String? {
    val relativePath = "${Environment.DIRECTORY_DOWNLOADS}/$DOWNLOADS_SUBDIR"
    val values = ContentValues().apply {
        put(MediaStore.Downloads.DISPLAY_NAME, fileName)
        put(MediaStore.Downloads.RELATIVE_PATH, relativePath)
        put(MediaStore.Downloads.IS_PENDING, 1)
    }
    val resolver = context.contentResolver
    val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values) ?: return null
    return try {
        resolver.openOutputStream(uri)?.use { it.write(bytes) } ?: error("No output stream")
        resolver.update(uri, ContentValues().apply { put(MediaStore.Downloads.IS_PENDING, 0) }, null, null)
        "$relativePath/${resolver.displayName(uri) ?: fileName}"
    } catch (ignored: Exception) {
        resolver.delete(uri, null, null)
        null
    }
}

private fun ContentResolver.displayName(uri: Uri): String? =
    query(uri, arrayOf(MediaStore.Downloads.DISPLAY_NAME), null, null, null)?.use { cursor ->
        if (cursor.moveToFirst()) cursor.getString(0) else null
    }

// API < 29: требуется WRITE_EXTERNAL_STORAGE; без него вернём null — ошибка уйдёт в снекбар.
private fun saveToPublicDownloads(fileName: String, bytes: ByteArray): String? {
    val dir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), DOWNLOADS_SUBDIR)
    if (!dir.exists() && !dir.mkdirs()) return null
    val file = File(dir, uniqueFileName(fileName) { File(dir, it).exists() })
    file.writeBytes(bytes)
    return file.absolutePath
}

private const val DOWNLOADS_SUBDIR = "Pandora Spec"

/**
 * Запрос `WRITE_EXTERNAL_STORAGE` на API < 29 из suspend-кода сейвера. Лаунчер
 * обновляет композиция (переживает пересоздание Activity), результат ждём через
 * [CompletableDeferred]. На API 29+ и при уже выданном разрешении — сразу `true`.
 */
private object StoragePermissionRequester {
    var launcher: ActivityResultLauncher<String>? = null
    private var pending: CompletableDeferred<Boolean>? = null

    suspend fun ensureGranted(context: Context, mainDispatcher: CoroutineDispatcher): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) return true
        val permission = Manifest.permission.WRITE_EXTERNAL_STORAGE
        if (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED) return true
        val deferred = CompletableDeferred<Boolean>()
        val started = withContext(mainDispatcher) {
            val current = launcher ?: return@withContext false
            pending = deferred
            current.launch(permission)
            true
        }
        return started && deferred.await()
    }

    fun onResult(granted: Boolean) {
        pending?.complete(granted)
        pending = null
    }
}

package ru.alarmtrade.specmp.filepicker

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import ru.alarmtrade.specmp.feature.supportchat.domain.picker.SupportChatFileSaver
import java.io.File

@Composable
actual fun rememberSupportChatFileSaver(): SupportChatFileSaver {
    val context = LocalContext.current
    return remember(context) {
        object : SupportChatFileSaver {
            override suspend fun saveToDownloads(fileName: String, bytes: ByteArray): String? {
                return saveToDownloads(context, fileName, bytes)
            }
        }
    }
}

private fun saveToDownloads(context: Context, fileName: String, bytes: ByteArray): String? {
    return try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            saveViaMediaStore(context, fileName, bytes)
        } else {
            saveToPublicDownloads(fileName, bytes)
        }
    } catch (e: Exception) {
        null
    }
}

private fun saveViaMediaStore(context: Context, fileName: String, bytes: ByteArray): String? {
    val relativePath = "${Environment.DIRECTORY_DOWNLOADS}/Pandora Spec"
    val values = ContentValues().apply {
        put(MediaStore.Downloads.DISPLAY_NAME, fileName)
        put(MediaStore.Downloads.RELATIVE_PATH, relativePath)
    }
    val resolver = context.contentResolver
    val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values) ?: return null
    resolver.openOutputStream(uri)?.use { it.write(bytes) } ?: return null
    return "$relativePath/$fileName"
}

// API < 29: требуется WRITE_EXTERNAL_STORAGE; без него вернём null — ошибка уйдёт в снекбар.
private fun saveToPublicDownloads(fileName: String, bytes: ByteArray): String? {
    val dir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "Pandora Spec")
    if (!dir.exists() && !dir.mkdirs()) return null
    val file = File(dir, fileName)
    file.writeBytes(bytes)
    return file.absolutePath
}

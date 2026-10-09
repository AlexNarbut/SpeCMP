package ru.alarmtrade.specmp.filepicker

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.webkit.MimeTypeMap
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import ru.alarmtrade.specmp.feature.supportchat.presentation.platform.SupportChatFileOpenResult
import ru.alarmtrade.specmp.feature.supportchat.presentation.platform.SupportChatFileOpener
import java.io.File
import java.util.UUID

/**
 * Открытие во внешнем приложении через `FileProvider` (`<provider>` и пути —
 * в манифесте `androidApp`) и `ACTION_VIEW` с выбором приложения. Разрешения не
 * нужны: доступ к файлу выдаётся URI-грантом `FLAG_GRANT_READ_URI_PERMISSION`.
 * Только `applicationContext` — VM, которой передан порт, переживает Activity.
 */
@Composable
actual fun rememberSupportChatFileOpener(): SupportChatFileOpener {
    val appContext = LocalContext.current.applicationContext
    return remember(appContext) {
        object : SupportChatFileOpener {
            override suspend fun openBytes(fileName: String, bytes: ByteArray): SupportChatFileOpenResult {
                val file = try {
                    // Временная копия в cacheDir: система вправе её вычистить, повторное
                    // открытие скачает файл заново. Старые копии чистим перед записью.
                    val root = File(appContext.cacheDir, OPEN_DIR).apply { deleteRecursively(); mkdirs() }
                    val directory = File(root, UUID.randomUUID().toString()).apply { mkdirs() }
                    File(directory, safeFileName(fileName)).apply { writeBytes(bytes) }
                } catch (e: Exception) {
                    return SupportChatFileOpenResult.Failed
                }
                return appContext.openInExternalApp(file)
            }

            override suspend fun openLocal(path: String, fileName: String): SupportChatFileOpenResult {
                val file = File(path)
                if (!file.isFile) return SupportChatFileOpenResult.Failed
                return appContext.openInExternalApp(file)
            }
        }
    }
}

private fun Context.openInExternalApp(file: File): SupportChatFileOpenResult {
    return try {
        val uri = FileProvider.getUriForFile(this, "$packageName$FILE_PROVIDER_SUFFIX", file)
        val mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(file.extension.lowercase()) ?: ANY_MIME
        // Без chooser: при нескольких приложениях система сама предложит выбор, а
        // отсутствие обработчика — ActivityNotFoundException (resolveActivity на API 30+
        // без <queries> ненадёжен).
        startActivity(
            Intent(Intent.ACTION_VIEW)
                .setDataAndType(uri, mimeType)
                .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
        )
        SupportChatFileOpenResult.Opened
    } catch (e: ActivityNotFoundException) {
        SupportChatFileOpenResult.NoApp
    } catch (e: Exception) {
        SupportChatFileOpenResult.Failed
    }
}

/** Совпадает с `android:authorities="${applicationId}.supportchat.files"` в манифесте androidApp. */
private const val FILE_PROVIDER_SUFFIX = ".supportchat.files"
private const val OPEN_DIR = "support-chat-open"
private const val ANY_MIME = "*/*"

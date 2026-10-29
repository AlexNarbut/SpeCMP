package ru.alarmtrade.specmp.flasher.filepicker

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import java.io.File
import java.util.UUID

/** Имя и размер документа от провайдера; любое может отсутствовать. */
internal data class DocumentMeta(val name: String? = null, val sizeBytes: Long? = null)

internal fun ContentResolver.queryNameAndSize(uri: Uri): DocumentMeta {
    val cursor = query(uri, null, null, null, null) ?: return DocumentMeta()
    return cursor.use {
        if (!it.moveToFirst()) return@use DocumentMeta()
        val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        val sizeIndex = it.getColumnIndex(OpenableColumns.SIZE)
        DocumentMeta(
            name = nameIndex.takeIf { index -> index != -1 }?.let(it::getString),
            sizeBytes = sizeIndex.takeIf { index -> index != -1 && !it.isNull(index) }?.let(it::getLong)
        )
    }
}

/** Копия документа во временном каталоге и её фактический размер. */
internal class CachedCopy(val file: File, val bytes: Long)

/**
 * Копия документа в `cacheDir/<dirName>/<uuid>/<fileName>`: прошлые копии каталога стираются (SDK нужен путь только на
 * время операции). `null` — провайдер не открыл поток. Ошибку I/O пробрасывает (каталог копии при этом удалён) —
 * вызывающий переводит её в свой `Failed`.
 */
// Граница платформенного I/O: любой сбой (I/O, SecurityException провайдера) — каталог копии удаляется, ошибка дальше
@Suppress("TooGenericExceptionCaught")
internal fun Context.copyToCache(uri: Uri, dirName: String, fileName: String): CachedCopy? {
    val root = File(cacheDir, dirName)
    val directory = File(root, UUID.randomUUID().toString())
    try {
        root.deleteRecursively()
        directory.mkdirs()
        val target = File(directory, fileName)
        val copied = contentResolver.openInputStream(uri)?.use { input ->
            target.outputStream().use { output -> input.copyTo(output) }
        }
        if (copied == null) directory.deleteRecursively()
        return copied?.let { CachedCopy(target, it) }
    } catch (e: Exception) {
        directory.deleteRecursively()
        throw e
    }
}

/**
 * Имена выбранных файлов приходят извне (системный пикер) и
 * используются в путях файловой системы — поэтому перед записью:
 * - только basename: `../`, абсолютные пути и разделители каталогов срезаются;
 * - запрещённые в Windows/Android/iOS символы и управляющие коды → `_`;
 * - пустое/точечное имя → [FALLBACK_NAME]; длина ограничена [MAX_NAME_LENGTH].
 */
internal fun safeFileName(name: String): String {
    val baseName = name.substringAfterLast('/').substringAfterLast('\\')
    val cleaned = buildString {
        baseName.forEach { char ->
            append(if (char in FORBIDDEN_CHARS || char.code < 0x20) '_' else char)
        }
    }.trim().trim('.')
    if (cleaned.isEmpty()) return FALLBACK_NAME
    if (cleaned.length <= MAX_NAME_LENGTH) return cleaned

    val extension = cleaned.substringAfterLast('.', "").take(MAX_EXTENSION_LENGTH)
    val stem = cleaned.removeSuffix(".$extension").take(MAX_NAME_LENGTH - extension.length - 1)
    return if (extension.isEmpty()) cleaned.take(MAX_NAME_LENGTH) else "$stem.$extension"
}

private const val FALLBACK_NAME = "firmware"
private const val MAX_NAME_LENGTH = 120
private const val MAX_EXTENSION_LENGTH = 16
private val FORBIDDEN_CHARS = setOf('/', '\\', ':', '*', '?', '"', '<', '>', '|')

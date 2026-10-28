package ru.alarmtrade.specmp.filepicker

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

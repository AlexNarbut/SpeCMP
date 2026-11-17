package ru.alarmtrade.specmp.core.files

import ru.alarmtrade.specmp.core.common.file.safeFileName

internal fun decodeTextFile(name: String, bytes: ByteArray, maxBytes: Long): TextFilePickResult {
    if (bytes.size > maxBytes) return TextFilePickResult.TooLarge
    val start = if (bytes.hasUtf8Bom()) UTF8_BOM.size else 0
    return try {
        TextFilePickResult.Picked(name, bytes.decodeToString(start, bytes.size, throwOnInvalidSequence = true))
    } catch (_: CharacterCodingException) {
        TextFilePickResult.Failed
    }
}

/** Безопасный basename с расширением [extension]: `theme` → `theme.json`, `theme.JSON` не меняется. */
internal fun fileNameWithExtension(name: String, extension: String): String {
    val fallback = "$DEFAULT_STEM.$extension"
    val safe = safeFileName(name, fallback = fallback)
    return if (safe.substringAfterLast('.', "").equals(extension, ignoreCase = true)) safe else "$safe.$extension"
}

private fun ByteArray.hasUtf8Bom(): Boolean = size >= UTF8_BOM.size && UTF8_BOM.indices.all { this[it] == UTF8_BOM[it] }

private val UTF8_BOM = byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte())
private const val DEFAULT_STEM = "file"

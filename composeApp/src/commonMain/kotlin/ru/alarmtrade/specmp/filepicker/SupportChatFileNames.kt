package ru.alarmtrade.specmp.filepicker

/**
 * Имена файлов вложений чата приходят извне (сервер, системный пикер) и
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

/**
 * Имя, не занятое в целевом каталоге: `report.pdf` → `report (1).pdf` → …
 * Существующий файл пользователя не перезаписывается молча.
 */
internal fun uniqueFileName(name: String, exists: (String) -> Boolean): String {
    if (!exists(name)) return name
    val extension = name.substringAfterLast('.', "")
    val stem = if (extension.isEmpty()) name else name.removeSuffix(".$extension")
    var index = 1
    while (true) {
        val candidate = if (extension.isEmpty()) "$stem ($index)" else "$stem ($index).$extension"
        if (!exists(candidate)) return candidate
        index++
    }
}

private const val FALLBACK_NAME = "attachment"
private const val MAX_NAME_LENGTH = 120
private const val MAX_EXTENSION_LENGTH = 16
private val FORBIDDEN_CHARS = setOf('/', '\\', ':', '*', '?', '"', '<', '>', '|')

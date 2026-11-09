package ru.alarmtrade.specmp.core.common.file

/** Только basename; запрещённые символы и управляющие коды → `_`; пустое/точечное имя → [fallback]. */
fun safeFileName(name: String, fallback: String = DEFAULT_FALLBACK_NAME): String {
    val baseName = name.substringAfterLast('/').substringAfterLast('\\')
    val cleaned = buildString {
        baseName.forEach { char ->
            append(if (char in FORBIDDEN_CHARS || char.code < 0x20) '_' else char)
        }
    }.trim().trim('.')
    if (cleaned.isEmpty()) return fallback
    if (cleaned.length <= MAX_NAME_LENGTH) return cleaned

    val extension = cleaned.substringAfterLast('.', "").take(MAX_EXTENSION_LENGTH)
    val stem = cleaned.removeSuffix(".$extension").take(MAX_NAME_LENGTH - extension.length - 1)
    return if (extension.isEmpty()) cleaned.take(MAX_NAME_LENGTH) else "$stem.$extension"
}

/** `report.pdf` → `report (1).pdf` → …: существующий файл не перезаписывается. */
fun uniqueFileName(name: String, exists: (String) -> Boolean): String {
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

private const val DEFAULT_FALLBACK_NAME = "attachment"
private const val MAX_NAME_LENGTH = 120
private const val MAX_EXTENSION_LENGTH = 16
private val FORBIDDEN_CHARS = setOf('/', '\\', ':', '*', '?', '"', '<', '>', '|')

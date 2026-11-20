package ru.alarmtrade.specmp.feature.mountpassport.presentation.model

import ru.alarmtrade.specmp.feature.mountpassport.domain.model.MountPassportSummary

/** Поле, по которому нашёлся паспорт, если его нет в заголовке строки (автомобиль, госномер, ID). */
internal enum class PassportMatchField { VIN, CLIENT, PHONE }

/**
 * Где подсветить запрос в показанном тексте. Ищет сам SDK; здесь — только место совпадения: без регистра, пробелов
 * и разделителей номеров, «ё» = «е», латинские двойники кириллицы в госномерах — одна буква.
 */
internal fun highlightRange(text: String, query: String): IntRange? {
    val needle = query.mapNotNull(::normalized).joinToString("")
    if (needle.isEmpty()) return null
    val positions = text.indices.filter { normalized(text[it]) != null }
    val flat = positions.map { requireNotNull(normalized(text[it])) }.joinToString("")
    val at = flat.indexOf(needle)
    if (at < 0) return null
    return positions[at]..positions[at + needle.length - 1]
}

internal fun extraMatch(item: MountPassportSummary, query: String): PassportMatchField? {
    if (query.isBlank()) return null
    val inTitle = listOfNotNull(item.carTitle, item.plate, item.systemId).any { highlightRange(it, query) != null }
    if (inTitle) return null
    return when {
        item.vin.matches(query) -> PassportMatchField.VIN
        item.clientName.matches(query) -> PassportMatchField.CLIENT
        item.clientPhone.matches(query) -> PassportMatchField.PHONE
        else -> null
    }
}

private fun String?.matches(query: String): Boolean = this != null && highlightRange(this, query) != null

private fun normalized(char: Char): Char? {
    if (char in SEPARATORS) return null
    val lower = char.lowercaseChar()
    return LOOKALIKES[lower] ?: lower
}

private const val SEPARATORS = " -()+.\t"

private val LOOKALIKES: Map<Char, Char> = mapOf(
    'ё' to 'е',
    'a' to 'а',
    'b' to 'в',
    'e' to 'е',
    'k' to 'к',
    'm' to 'м',
    'h' to 'н',
    'o' to 'о',
    'p' to 'р',
    'c' to 'с',
    't' to 'т',
    'y' to 'у',
    'x' to 'х',
)

package ru.alarmtrade.specmp.feature.vehicle.ui.presentation.model

import ru.alarmtrade.specmp.feature.vehicle.domain.model.CarBrand
import ru.alarmtrade.specmp.feature.vehicle.ui.presentation.screen.select.ComboSearch

// Локальный поиск по каталогу: сервер отдаёт уровни целиком и текстового поиска не имеет.

private val whitespace = Regex("\\s+")

/** Название содержит запрос (без учёта регистра); пустой запрос — подходит всё. */
internal fun matchesQuery(name: String, query: String): Boolean {
    val q = query.trim()
    return q.isEmpty() || name.contains(q, ignoreCase = true)
}

/**
 * «Марка модель» на уровне марок: запрос начинается с полного имени марки и пробела (самое длинное совпадение —
 * «Land Rover», а не «Land»), либо первое слово — начало имени ровно одной марки. Иначе — `null`.
 */
internal fun detectCombo(query: String, brands: List<CarBrand>): ComboSearch? {
    val q = query.trim().lowercase()
    if (q.isEmpty()) return null
    brands
        .filter { q.startsWith(it.name.lowercase() + " ") }
        .maxByOrNull { it.name.length }
        ?.let { return ComboSearch(it, q.substring(it.name.length).trim()) }
    val tokens = q.split(whitespace)
    if (tokens.size < 2) return null
    val single = brands.singleOrNull { it.name.lowercase().startsWith(tokens.first()) } ?: return null
    return ComboSearch(single, tokens.drop(1).joinToString(" "))
}

/** Запрос из одних цифр — похоже на CAN-код. */
internal fun looksLikeCanCode(query: String): Boolean = query.trim().let { it.isNotEmpty() && it.all(Char::isDigit) }

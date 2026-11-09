package ru.alarmtrade.specmp.feature.vehicle.ui.presentation.model

import ru.alarmtrade.specmp.feature.vehicle.domain.model.CarBrand
import ru.alarmtrade.specmp.feature.vehicle.ui.presentation.screen.select.ComboSearch

// Локальный поиск по каталогу: сервер отдаёт уровни целиком и текстового поиска не имеет.

private val whitespace = Regex("\\s+")

internal fun matchesQuery(name: String, query: String): Boolean {
    val q = query.trim()
    return q.isEmpty() || name.contains(q, ignoreCase = true)
}

/**
 * Самое длинное совпадение имени марки и пробел («Land Rover», а не «Land»), либо первое слово — начало имени ровно
 * одной марки.
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

internal fun looksLikeCanCode(query: String): Boolean = query.trim().let { it.isNotEmpty() && it.all(Char::isDigit) }

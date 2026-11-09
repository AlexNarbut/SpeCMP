package ru.alarmtrade.specmp.core.common

/**
 * Неизменённые элементы — экземпляры из [previous]: Compose пропускает их по ссылке; ничего не изменилось — сам
 * [previous].
 */
fun <T, K> List<T>.reusing(previous: List<T>?, key: (T) -> K): List<T> {
    if (previous.isNullOrEmpty()) return this
    val old = previous.associateBy(key)
    val result = map { item -> old[key(item)]?.takeIf { it == item } ?: item }
    val unchanged = result.size == previous.size && result.indices.all { result[it] === previous[it] }
    return if (unchanged) previous else result
}

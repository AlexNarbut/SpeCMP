package ru.alarmtrade.specmp.core.common

/**
 * Перечитанный список с экземплярами из [previous] для неизменённых элементов (тот же [key] и `==`); если не
 * изменилось ничего — сам [previous]. Сохраняет идентичность: Compose пропускает неизменённые элементы по ссылке,
 * а не перерисовывает весь список после перечитки (docs/compose-best-prac.md, §3.4).
 */
fun <T, K> List<T>.reusing(previous: List<T>?, key: (T) -> K): List<T> {
    if (previous.isNullOrEmpty()) return this
    val old = previous.associateBy(key)
    val result = map { item -> old[key(item)]?.takeIf { it == item } ?: item }
    val unchanged = result.size == previous.size && result.indices.all { result[it] === previous[it] }
    return if (unchanged) previous else result
}

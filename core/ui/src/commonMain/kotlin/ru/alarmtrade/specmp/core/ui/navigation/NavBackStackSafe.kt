package ru.alarmtrade.specmp.core.ui.navigation

import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey

/** Текущий (верхний) маршрут back stack. */
val <T : NavKey> NavBackStack<T>.currentRoute: T?
    get() = lastOrNull()

/** Переход без дублей: не добавляет маршрут, если он уже наверху (защита от двойного клика). */
fun <T : NavKey> NavBackStack<T>.navigateSafe(route: T) {
    if (currentRoute != route) add(route)
}

/** Переход со снятием всего, что лежит поверх уже существующего экземпляра маршрута. */
fun <T : NavKey> NavBackStack<T>.navigateClearingTop(route: T) {
    val index = indexOf(route)
    if (index >= 0) {
        while (size > index) removeAt(size - 1)
    }
    add(route)
}

/** Заменить текущий верхний маршрут. */
fun <T : NavKey> NavBackStack<T>.replaceCurrent(route: T) {
    if (isNotEmpty()) removeAt(size - 1)
    add(route)
}

/** Сбросить стек к единственному маршруту. */
fun <T : NavKey> NavBackStack<T>.resetTo(route: T) {
    clear()
    add(route)
}

/** Безопасный pop: снимает верхний маршрут только если в стеке больше одного. Возвращает, был ли pop. */
fun <T : NavKey> NavBackStack<T>.popSafe(): Boolean {
    if (size <= 1) return false
    removeAt(size - 1)
    return true
}

/** Pop до указанного маршрута (inclusive — включая сам маршрут). */
fun <T : NavKey> NavBackStack<T>.popSafe(route: T, inclusive: Boolean): Boolean {
    val index = indexOf(route)
    if (index < 0) return false
    val target = if (inclusive) index else index + 1
    if (target >= size) return false
    while (size > target) removeAt(size - 1)
    return true
}

/** Pop до последнего маршрута типа [R] (inclusive — включая сам маршрут). */
inline fun <T : NavKey, reified R : T> NavBackStack<T>.popSafe(inclusive: Boolean): Boolean {
    val index = indexOfLast { it is R }
    if (index < 0) return false
    val target = if (inclusive) index else index + 1
    if (target >= size) return false
    while (size > target) removeAt(size - 1)
    return true
}

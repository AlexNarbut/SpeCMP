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

/** Заменить текущий верхний маршрут. */
fun <T : NavKey> NavBackStack<T>.replaceCurrent(route: T) {
    if (isNotEmpty()) removeAt(size - 1)
    add(route)
}

/** Безопасный pop: снимает верхний маршрут только если в стеке больше одного. Возвращает, был ли pop. */
fun <T : NavKey> NavBackStack<T>.popSafe(): Boolean {
    if (size <= 1) return false
    removeAt(size - 1)
    return true
}

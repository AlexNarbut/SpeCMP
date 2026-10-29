package ru.alarmtrade.specmp.core.common

import kotlinx.coroutines.flow.MutableStateFlow

/**
 * Атомарный условный переход: [change] применяется, только если [condition] истинно для текущего значения
 * (как `update`, но с проверкой внутри той же CAS-попытки). Ответ — был ли переход.
 */
inline fun <T> MutableStateFlow<T>.updateIf(condition: (T) -> Boolean, change: (T) -> T): Boolean {
    while (true) {
        val current = value
        if (!condition(current)) return false
        if (compareAndSet(current, change(current))) return true
    }
}

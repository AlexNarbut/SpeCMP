package ru.alarmtrade.specmp.core.common

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Новые первыми, без дублей по [key], не больше [max]. Правки — под замком: `Settings` не атомарно, а замок общий
 * только у одного экземпляра (Koin `single`).
 */
class RecentList<T, K>(
    private val max: Int,
    private val key: (T) -> K,
    private val read: () -> List<T>,
    private val write: (List<T>) -> Unit,
) {
    private val lock = Mutex()

    fun all(): List<T> = read()

    suspend fun push(item: T): List<T> = update { items ->
        (listOf(item) + items.filter { key(it) != key(item) }).take(max)
    }

    suspend fun remove(itemKey: K): List<T> = update { items -> items.filter { key(it) != itemKey } }

    private suspend fun update(change: (List<T>) -> List<T>): List<T> = lock.withLock { change(read()).also(write) }
}

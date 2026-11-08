package ru.alarmtrade.specmp.core.common

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Список «недавних»: новые первыми, без дублей по [key], не больше [max]. Правки — read-modify-write под замком:
 * хранилище (`Settings`) не атомарно, и две быстрые правки без замка потеряли бы одну.
 *
 * Хранение — у владельца ([read] / [write], обычно JSON в именованном `Settings`); экземпляр держит app-scoped
 * хранилище (Koin `single`), чтобы замок был общим для всех экранов.
 */
class RecentList<T, K>(
    private val max: Int,
    private val key: (T) -> K,
    private val read: () -> List<T>,
    private val write: (List<T>) -> Unit,
) {
    private val lock = Mutex()

    fun all(): List<T> = read()

    /** Поднять [item] наверх. Ответ — новый список. */
    suspend fun push(item: T): List<T> = update { items ->
        (listOf(item) + items.filter { key(it) != key(item) }).take(max)
    }

    /** Убрать элемент с ключом [itemKey] (нет — ничего). Ответ — новый список. */
    suspend fun remove(itemKey: K): List<T> = update { items -> items.filter { key(it) != itemKey } }

    private suspend fun update(change: (List<T>) -> List<T>): List<T> = lock.withLock { change(read()).also(write) }
}

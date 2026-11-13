package ru.alarmtrade.specmp.feature.techrequests.domain.list

import ru.alarmtrade.specmp.feature.techrequests.domain.model.TechRequest

/**
 * Загруженные заявки: уникальны по `id` (повторная доставка заменяет — страницы сдвигаются, обновления статуса
 * приходят отдельной порцией), порядок — новые сверху (`createdAt`, затем `id`).
 */
class TechRequestList private constructor(val items: List<TechRequest>) {

    val active: List<TechRequest> = items.filter { it.status.isActive }
    val finished: List<TechRequest> = items.filterNot { it.status.isActive }

    fun merge(portion: List<TechRequest>): TechRequestList {
        if (portion.isEmpty()) return this
        val byId = LinkedHashMap<Long, TechRequest>(items.size + portion.size)
        items.forEach { byId[it.id] = it }
        portion.forEach { byId[it.id] = it }
        return TechRequestList(byId.values.sortedWith(NEWEST_FIRST))
    }

    fun find(id: Long): TechRequest? = items.firstOrNull { it.id == id }

    override fun equals(other: Any?): Boolean = other is TechRequestList && other.items == items

    override fun hashCode(): Int = items.hashCode()

    companion object {
        val Empty = TechRequestList(emptyList())

        fun of(requests: List<TechRequest>): TechRequestList = Empty.merge(requests)

        private val NEWEST_FIRST = compareByDescending<TechRequest> { it.createdAt }.thenByDescending { it.id }
    }
}

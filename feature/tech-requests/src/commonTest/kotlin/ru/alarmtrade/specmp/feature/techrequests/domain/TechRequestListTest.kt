package ru.alarmtrade.specmp.feature.techrequests.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame
import kotlinx.datetime.Instant
import ru.alarmtrade.specmp.feature.techrequests.domain.list.TechRequestList
import ru.alarmtrade.specmp.feature.techrequests.domain.model.TechRequest
import ru.alarmtrade.specmp.feature.techrequests.domain.model.TechRequestStatus

internal fun techRequest(
    id: Long,
    status: TechRequestStatus = TechRequestStatus.OPEN,
    createdAtSeconds: Long = id,
    issue: String = "issue $id",
) = TechRequest(id, status, "Иван", "+79001234567", issue, Instant.fromEpochSeconds(createdAtSeconds))

class TechRequestListTest {

    @Test
    fun newestFirstAndUniqueById() {
        val list = TechRequestList.of(
            listOf(techRequest(1), techRequest(3)),
        ).merge(listOf(techRequest(2), techRequest(3)))

        assertEquals(listOf(3L, 2L, 1L), list.items.map { it.id })
    }

    @Test
    fun laterPortionReplacesStatus() {
        val list = TechRequestList.of(listOf(techRequest(1), techRequest(2)))
            .merge(listOf(techRequest(1, TechRequestStatus.FINISHED_BY_USER)))

        assertEquals(TechRequestStatus.FINISHED_BY_USER, list.find(1)?.status)
        assertEquals(listOf(2L), list.active.map { it.id })
        assertEquals(listOf(1L), list.finished.map { it.id })
    }

    @Test
    fun sameTimeIsOrderedById() {
        val list = TechRequestList.of(
            listOf(techRequest(5, createdAtSeconds = 10), techRequest(7, createdAtSeconds = 10)),
        )

        assertEquals(listOf(7L, 5L), list.items.map { it.id })
    }

    @Test
    fun emptyPortionKeepsInstance() {
        val list = TechRequestList.of(listOf(techRequest(1)))

        assertSame(list, list.merge(emptyList()))
    }
}

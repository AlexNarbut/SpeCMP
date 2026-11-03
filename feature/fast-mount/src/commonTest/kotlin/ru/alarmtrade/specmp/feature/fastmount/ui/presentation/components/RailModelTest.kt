package ru.alarmtrade.specmp.feature.fastmount.ui.presentation.components

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class RailModelTest {

    private val path = railPath(plan = null, usb = false)

    private fun statuses(position: RailPosition, cloneNeeded: Boolean?) =
        railModel(path, position, cloneNeeded).rows.associate { it.key to it.status }

    @Test
    fun `usb path has no slave and system check`() {
        assertEquals(
            listOf(RailKey.PREP, RailKey.WRITE, RailKey.MOUNT, RailKey.BYPASS, RailKey.CLONE, RailKey.REVIEW),
            railPath(plan = null, usb = true)
        )
    }

    @Test
    fun `clone is optional until the bypass requires it`() {
        val model = railModel(path, RailPosition(RailKey.MOUNT), cloneNeeded = null)
        val rows = model.rows.associateBy { it.key }
        assertEquals(RailStatus.DONE, rows.getValue(RailKey.WRITE).status)
        assertEquals(RailStatus.CURRENT, rows.getValue(RailKey.MOUNT).status)
        assertEquals(RailStatus.OPTIONAL, rows.getValue(RailKey.CLONE).status)
        assertNull(rows.getValue(RailKey.CLONE).number)
        // Подготовка, запись, монтаж, обход, Slave, проверка, отзыв — 7 шагов, монтаж — третий.
        assertEquals(3, model.currentNumber)
        assertEquals(7, model.total)
    }

    @Test
    fun `clone becomes a numbered step when required and skipped when not`() {
        val required = railModel(path, RailPosition(RailKey.CLONE), cloneNeeded = true)
        assertEquals(RailStatus.CURRENT, required.rows.first { it.key == RailKey.CLONE }.status)
        assertEquals(8, required.total)

        assertEquals(RailStatus.SKIPPED, statuses(RailPosition(RailKey.SLAVE), cloneNeeded = false)[RailKey.CLONE])
    }

    @Test
    fun `reboot marks the finished step done and the next one pulsing`() {
        val result = statuses(RailPosition(RailKey.MOUNT, rebootFrom = RailKey.MOUNT), cloneNeeded = null)
        assertEquals(RailStatus.DONE, result[RailKey.MOUNT])
        assertEquals(RailStatus.NEXT, result[RailKey.BYPASS])

        val toClone = statuses(RailPosition(RailKey.BYPASS, rebootFrom = RailKey.BYPASS), cloneNeeded = true)
        assertEquals(RailStatus.NEXT, toClone[RailKey.CLONE])
    }

    @Test
    fun `done marks every step done`() {
        val model = railModel(path, RailPosition(null), cloneNeeded = false)
        assertTrue(model.done)
        assertTrue(model.rows.filter { it.key != RailKey.CLONE }.all { it.status == RailStatus.DONE })
    }
}

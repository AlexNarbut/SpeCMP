package ru.alarmtrade.specmp.core.common

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotSame
import kotlin.test.assertSame

class ListReuseTest {

    private data class Item(val id: Int, val name: String)

    @Test
    fun unchangedListReturnsPreviousInstance() {
        val previous = listOf(Item(1, "a"), Item(2, "b"))
        val reread = listOf(Item(1, "a"), Item(2, "b"))

        assertSame(previous, reread.reusing(previous) { it.id })
    }

    @Test
    fun unchangedItemsKeepInstancesChangedAreNew() {
        val first = Item(1, "a")
        val previous = listOf(first, Item(2, "b"))
        val renamed = Item(2, "B")

        val result = listOf(Item(1, "a"), renamed, Item(3, "c")).reusing(previous) { it.id }

        assertSame(first, result[0])
        assertSame(renamed, result[1])
        assertEquals(Item(3, "c"), result[2])
    }

    @Test
    fun reorderKeepsInstancesButNotList() {
        val a = Item(1, "a")
        val b = Item(2, "b")
        val previous = listOf(a, b)

        val result = listOf(Item(2, "b"), Item(1, "a")).reusing(previous) { it.id }

        assertNotSame(previous, result)
        assertSame(b, result[0])
        assertSame(a, result[1])
    }

    @Test
    fun noPreviousReturnsReread() {
        val reread = listOf(Item(1, "a"))

        assertSame(reread, reread.reusing(null) { it.id })
    }
}

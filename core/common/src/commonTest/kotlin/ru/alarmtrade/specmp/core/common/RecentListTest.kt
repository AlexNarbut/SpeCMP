package ru.alarmtrade.specmp.core.common

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext

class RecentListTest {

    private var stored = emptyList<String>()
    private val recent = RecentList<String, String>(max = 3, key = { it }, read = { stored }, write = { stored = it })

    @Test
    fun `push moves the item to the top without duplicates and keeps max`() = runTest {
        listOf("a", "b", "c", "b", "d").forEach { recent.push(it) }

        assertEquals(listOf("d", "b", "c"), recent.all())
    }

    @Test
    fun `remove drops the item and returns the new list`() = runTest {
        recent.push("a")
        recent.push("b")

        assertEquals(listOf("a"), recent.remove("b"))
        assertEquals(listOf("a"), recent.remove("missing"))
    }

    @Test
    fun `parallel pushes are not lost`() = runTest {
        var shared = emptyList<Int>()
        val list = RecentList<Int, Int>(max = PARALLEL_CALLS, key = { it }, read = { shared }, write = { shared = it })

        withContext(Dispatchers.Default) {
            repeat(PARALLEL_CALLS) { index -> launch { list.push(index) } }
        }

        assertEquals((0 until PARALLEL_CALLS).toSet(), list.all().toSet())
    }

    private companion object {
        const val PARALLEL_CALLS = 32
    }
}

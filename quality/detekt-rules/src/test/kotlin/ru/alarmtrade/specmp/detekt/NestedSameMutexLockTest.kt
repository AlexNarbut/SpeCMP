package ru.alarmtrade.specmp.detekt

import dev.detekt.api.Config
import dev.detekt.test.lint
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class NestedSameMutexLockTest {
    private val rule = NestedSameMutexLock(Config.empty)

    @Test
    fun `reports nested withLock of the same mutex and member functions locking it`() {
        val code = """
            class Store {
                private val mutex = Mutex()

                suspend fun a() = mutex.withLock {
                    listOf(1).forEach { this.mutex.withLock { } }
                }

                suspend fun b() = mutex.withLock {
                    save()
                    this.save()
                }

                private suspend fun save() = mutex.withLock { }
            }
        """.trimIndent()

        assertEquals(3, rule.lint(code).size)
    }

    @Test
    fun `does not report different mutexes, other coroutines and unlocked helpers`() {
        val code = """
            class Store(private val other: Store2) {
                private val mutex = Mutex()
                private val second = Mutex()

                suspend fun a() = mutex.withLock {
                    second.withLock { }
                    saveLocked()
                    other.save()
                    scope.launch { mutex.withLock { } }
                }

                suspend fun b() {
                    mutex.withLock { }
                    mutex.withLock { }
                }

                private fun saveLocked() = Unit
            }

            class Store2 {
                private val mutex = Mutex()
                suspend fun save() = mutex.withLock { }
            }
        """.trimIndent()

        assertEquals(0, rule.lint(code).size)
    }
}

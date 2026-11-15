package ru.alarmtrade.specmp.detekt

import dev.detekt.api.Config
import dev.detekt.test.lint
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class NestedSameMutexLockBypassTest {
    private val rule = NestedSameMutexLock(Config.empty)

    @Test
    fun `bypass via labeled this`() {
        val code = """
            class Foo {
                private val mutex = Mutex()

                suspend fun a() {
                    mutex.withLock { this@Foo.mutex.withLock { } }
                }
            }
        """.trimIndent()

        assertEquals(1, rule.lint(code).size)
    }

    @Test
    fun `bypass via lock call`() {
        val code = """
            class Foo {
                private val mutex = Mutex()

                suspend fun a() {
                    mutex.withLock { mutex.lock() }
                }
            }
        """.trimIndent()

        assertEquals(1, rule.lint(code).size)
    }

    @Test
    fun `bypass via two-level member call chain`() {
        val code = """
            class Foo {
                private val mutex = Mutex()

                suspend fun a() {
                    mutex.withLock { b() }
                }

                private suspend fun b() = c()

                private suspend fun c() {
                    mutex.withLock { }
                }
            }
        """.trimIndent()

        assertEquals(1, rule.lint(code).size)
    }

    @Test
    fun `bypass via local alias of the mutex`() {
        val code = """
            class Foo {
                private val mutex = Mutex()

                suspend fun a() {
                    val lock = mutex
                    mutex.withLock { lock.withLock { } }
                }
            }
        """.trimIndent()

        assertEquals(1, rule.lint(code).size)
    }

    @Test
    fun `bypass via withContext and scope functions`() {
        val code = """
            class Foo {
                private val mutex = Mutex()

                suspend fun a() {
                    mutex.withLock {
                        withContext(Dispatchers.IO) { run { mutex.withLock { } } }
                    }
                }
            }
        """.trimIndent()

        assertEquals(1, rule.lint(code).size)
    }

    @Test
    fun `does not report other mutexes behind labels and aliases`() {
        val code = """
            class Foo {
                private val mutex = Mutex()
                private val other = Mutex()

                suspend fun a() {
                    val second = other
                    mutex.withLock {
                        this@Foo.other.withLock { }
                        second.withLock { }
                        other.lock()
                        b()
                    }
                }

                private suspend fun b() = c()

                private suspend fun c() {
                    other.withLock { }
                }
            }
        """.trimIndent()

        assertEquals(0, rule.lint(code).size)
    }
}

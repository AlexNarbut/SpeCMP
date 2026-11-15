package ru.alarmtrade.specmp.detekt

import dev.detekt.api.Config
import dev.detekt.test.lint
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class NestedCollectBypassTest {
    private val rule = NestedCollect(Config.empty)

    @Test
    fun `bypass via onEach of the outer flow`() {
        val code = """
            suspend fun a() {
                outer.onEach { inner.collect { } }.collect()
            }
        """.trimIndent()

        assertEquals(1, rule.lint(code).size)
    }

    @Test
    fun `bypass via lambda passed in parentheses`() {
        val code = """
            suspend fun a() {
                outer.collect({ inner.collectLatest { } })
            }
        """.trimIndent()

        assertEquals(1, rule.lint(code).size)
    }

    @Test
    fun `bypass via private helper function`() {
        val code = """
            class Foo {
                suspend fun a() {
                    outer.collect { value -> handle(value) }
                }

                private suspend fun handle(value: Int) {
                    inner.collect { }
                }
            }
        """.trimIndent()

        assertEquals(1, rule.lint(code).size)
    }

    @Test
    fun `bypass via function reference`() {
        val code = """
            suspend fun a() {
                outer.collect(::handle)
            }

            private suspend fun handle(value: Int) {
                inner.collect { }
            }
        """.trimIndent()

        assertEquals(1, rule.lint(code).size)
    }

    @Test
    fun `bypass via collectIndexed`() {
        val code = """
            suspend fun a() {
                outer.collectIndexed { _, _ -> inner.collect { } }
                outer.collect { inner.collectIndexed { _, _ -> } }
            }
        """.trimIndent()

        assertEquals(2, rule.lint(code).size)
    }

    @Test
    fun `does not report helpers and references that do not collect`() {
        val code = """
            class Foo {
                suspend fun a() {
                    outer.collect { value -> handle(value) }
                    outer.collect(::handle)
                    outer.onEach { handle(it) }.collect()
                }

                private fun handle(value: Int) = println(value)
            }
        """.trimIndent()

        assertEquals(0, rule.lint(code).size)
    }
}

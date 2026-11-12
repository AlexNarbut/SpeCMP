package ru.alarmtrade.specmp.detekt

import dev.detekt.api.Config
import dev.detekt.test.lint
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class DomainSentinelDefaultTest {
    private val rule = DomainSentinelDefault(Config.empty)

    @Test
    fun `reports sentinel defaults in domain model`() {
        val code = """
            package ru.alarmtrade.specmp.feature.foo.domain.model

            data class Foo(
                val url: String = "",
                val id: Int = -1,
                val seq: Long = -1L,
                val name: String = "unknown",
            ) {
                val title: String = "N/A"
            }
        """.trimIndent()

        assertEquals(5, rule.lint(code).size)
    }

    @Test
    fun `does not report null, real defaults, locals and other packages`() {
        val domain = """
            package ru.alarmtrade.specmp.feature.foo.domain.model

            data class Foo(val url: String? = null, val count: Int = 0, val name: String = "Default", val n: Int = -2) {
                fun f() {
                    val empty = ""
                }
            }

            class Bar(id: Int) {
                fun g(x: Int = -1) = x
            }
        """.trimIndent()
        val presentation = """
            package ru.alarmtrade.specmp.feature.foo.presentation

            data class FooState(val query: String = "", val selected: Int = -1)
        """.trimIndent()

        assertEquals(0, rule.lint(domain).size)
        assertEquals(0, rule.lint(presentation).size)
    }
}

package ru.alarmtrade.specmp.detekt

import dev.detekt.api.Config
import dev.detekt.test.lint
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class DomainSentinelDefaultBypassTest {
    private val rule = DomainSentinelDefault(Config.empty)

    @Test
    fun `bypass via parenthesized sentinel`() {
        val code = """
            package ru.alarmtrade.specmp.feature.foo.domain.model

            data class Foo(val id: Int = (-1), val name: String = (""))
        """.trimIndent()

        assertEquals(2, rule.lint(code).size)
    }

    @Test
    fun `bypass via property getter`() {
        val code = """
            package ru.alarmtrade.specmp.feature.foo.domain.model

            class Foo {
                val name: String get() = "unknown"
                val id: Long
                    get() {
                        return -1L
                    }
            }
        """.trimIndent()

        assertEquals(2, rule.lint(code).size)
    }

    @Test
    fun `bypass via secondary constructor delegation`() {
        val code = """
            package ru.alarmtrade.specmp.feature.foo.domain.model

            data class Foo(val id: Int, val name: String) {
                constructor() : this(-1, "N/A")
            }
        """.trimIndent()

        assertEquals(2, rule.lint(code).size)
    }

    @Test
    fun `bypass via raw string and letter case`() {
        val code = """
            package ru.alarmtrade.specmp.feature.foo.domain.model

            data class Foo(val name: String = ${"\"\"\"\"\"\""}, val state: String = "Unknown", val note: String = "NONE")
        """.trimIndent()

        assertEquals(3, rule.lint(code).size)
    }

    @Test
    fun `bypass via nested model class and companion constant`() {
        val code = """
            package ru.alarmtrade.specmp.feature.foo.domain.model.device

            data class Device(val info: Info) {
                data class Info(val serial: String = "-")

                companion object {
                    val DEFAULT_ID: Int = -1
                }
            }
        """.trimIndent()

        assertEquals(2, rule.lint(code).size)
    }

    @Test
    fun `does not report real values in getters, delegation and other packages`() {
        val code = """
            package ru.alarmtrade.specmp.feature.foo.domain.model

            data class Foo(val id: Int?, val name: String?) {
                constructor(id: Int) : this(id, null)

                val title: String get() = name ?: "Foo"
                val offset: Int get() = (1)
            }
        """.trimIndent()

        assertEquals(0, rule.lint(code).size)
    }
}

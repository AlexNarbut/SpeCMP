package ru.alarmtrade.specmp.detekt

import dev.detekt.api.Config
import dev.detekt.test.lint
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class LoggerTagLiteralTest {
    private val rule = LoggerTagLiteral(Config.empty)

    @Test
    fun `reports literal tags inside classes and objects`() {
        val code = """
            class Foo {
                private val logger = Logger.withTag("Foo")
            }

            object Bar {
                private val logger = Logger.withTag("Bar")
            }
        """.trimIndent()

        assertEquals(2, rule.lint(code).size)
    }

    @Test
    fun `does not report class name tags, constants and top-level loggers`() {
        val code = """
            private val logger = Logger.withTag("FilePicker")

            class Foo {
                private val logger = Logger.withTag(this::class.simpleName.orEmpty())
                private val other = Logger.withTag(LOG_TAG)
            }
        """.trimIndent()

        assertEquals(0, rule.lint(code).size)
    }
}

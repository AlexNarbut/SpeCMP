package ru.alarmtrade.specmp.detekt

import dev.detekt.api.Config
import dev.detekt.test.lint
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class LoggerTagLiteralBypassTest {
    private val rule = LoggerTagLiteral(Config.empty)

    @Test
    fun `bypass via constant`() {
        val code = """
            private const val TAG = "Foo"

            class Foo {
                private val logger = Logger.withTag(TAG)
            }

            class Bar {
                private val logger = Logger.withTag(OWN_TAG)

                private companion object {
                    const val OWN_TAG = "Bar"
                }
            }
        """.trimIndent()

        assertEquals(2, rule.lint(code).size)
    }

    @Test
    fun `bypass via named argument and concatenation`() {
        val code = """
            class Foo {
                private val a = Logger.withTag(tag = "Foo")
                private val b = Logger.withTag("Foo" + "Impl")
            }
        """.trimIndent()

        assertEquals(2, rule.lint(code).size)
    }

    @Test
    fun `bypass via Logger constructor tag`() {
        val code = """
            class Foo {
                private val a = Logger(config, "Foo")
                private val b = Logger(config = config, tag = "Foo")
            }
        """.trimIndent()

        assertEquals(2, rule.lint(code).size)
    }

    @Test
    fun `bypass via companion object and lambda`() {
        val code = """
            class Foo {
                private val lazyLogger by lazy { Logger.withTag("Foo") }

                companion object {
                    val logger = co.touchlab.kermit.Logger.withTag("Foo")
                }
            }
        """.trimIndent()

        assertEquals(2, rule.lint(code).size)
    }

    @Test
    fun `bypass via import alias of Logger`() {
        val code = """
            import co.touchlab.kermit.Logger as Log

            object Foo {
                private val logger = Log.withTag("Foo")
            }
        """.trimIndent()

        assertEquals(1, rule.lint(code).size)
    }

    @Test
    fun `does not report class-name tags and top-level loggers`() {
        val code = """
            private const val TAG = "File"
            private val fileLogger = Logger.withTag(TAG)

            class Foo {
                private val tag = this::class.simpleName.orEmpty()
                private val a = Logger.withTag(tag)
                private val b = Logger(config)
                private val c = Logger(config, tag)
            }
        """.trimIndent()

        assertEquals(0, rule.lint(code).size)
    }
}

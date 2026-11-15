package ru.alarmtrade.specmp.detekt

import dev.detekt.api.Config
import dev.detekt.test.lint
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ForbiddenPrintlnBypassTest {
    private val rule = ForbiddenPrintln(Config.empty)

    @Test
    fun `bypass via fully qualified kotlin io call`() {
        val code = """
            fun a() {
                kotlin.io.println("x")
                kotlin.io.print("y")
            }
        """.trimIndent()

        assertEquals(2, rule.lint(code).size)
    }

    @Test
    fun `bypass via System out and err streams`() {
        val code = """
            fun a() {
                System.out.println("x")
                System.err.print("y")
                java.lang.System.out.println("z")
            }
        """.trimIndent()

        assertEquals(3, rule.lint(code).size)
    }

    @Test
    fun `bypass via import alias`() {
        val code = """
            import kotlin.io.println as log

            fun a() = log("x")
        """.trimIndent()

        assertEquals(1, rule.lint(code).size)
    }

    @Test
    fun `bypass via callable reference`() {
        val code = """
            fun a(items: List<Int>) {
                items.forEach(::println)
                items.forEach(kotlin.io::print)
            }
        """.trimIndent()

        assertEquals(2, rule.lint(code).size)
    }

    @Test
    fun `bypass via scope function receiver`() {
        val code = """
            fun a() {
                System.out.apply { println("x") }
                with(System.err) { print("y") }
            }
        """.trimIndent()

        assertEquals(2, rule.lint(code).size)
    }

    @Test
    fun `does not report own print functions and references`() {
        val code = """
            class Printer {
                fun println(text: String) = Unit
            }

            fun a(printer: Printer, items: List<String>) {
                items.forEach(printer::println)
                printer.println("x")
            }
        """.trimIndent()

        assertEquals(0, rule.lint(code).size)
    }
}

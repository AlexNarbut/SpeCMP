package ru.alarmtrade.specmp.detekt

import dev.detekt.api.Config
import dev.detekt.test.lint
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ForbiddenPrintlnTest {
    private val rule = ForbiddenPrintln(Config.empty)

    @Test
    fun `reports receiver-less println and print`() {
        val code = """
            fun log(x: Int) {
                println("x = ${'$'}x")
                print(x)
                println()
            }
        """.trimIndent()

        assertEquals(3, rule.lint(code).size)
    }

    @Test
    fun `does not report calls with receiver or other names`() {
        val code = """
            class Printer {
                fun println(text: String) = Unit
                fun print(text: String) = Unit
            }

            fun log(printer: Printer, out: java.io.PrintStream) {
                printer.println("a")
                printer.print("b")
                out.println("c")
                System.out.println("d")
                printLine("e")
            }

            fun printLine(text: String) = Unit
        """.trimIndent()

        assertEquals(0, rule.lint(code).size)
    }
}

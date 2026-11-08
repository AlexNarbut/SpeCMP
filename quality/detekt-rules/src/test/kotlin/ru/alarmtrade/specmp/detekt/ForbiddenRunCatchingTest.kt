package ru.alarmtrade.specmp.detekt

import dev.detekt.api.Config
import dev.detekt.test.lint
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ForbiddenRunCatchingTest {
    private val rule = ForbiddenRunCatching(Config.empty)

    @Test
    fun `reports runCatching with and without receiver, nested and by reference`() {
        val code = """
            fun load(text: String?): Int? {
                val a = runCatching { 1 }.getOrNull()
                val b = "x".runCatching { length }.getOrNull()
                val c = text?.let { runCatching { it.toInt() }.getOrNull() }
                val d = ::runCatching
                return a ?: b ?: c
            }
        """.trimIndent()

        assertEquals(4, rule.lint(code).size)
    }

    @Test
    fun `does not report safeRun or similarly named calls`() {
        val code = """
            fun <T> safeRun(block: () -> T): Result<T> = Result.success(block())
            fun runCatchingSafely() = Unit
            fun load() {
                safeRun { 1 }
                runCatchingSafely()
                val runCatching = 1
            }
        """.trimIndent()

        assertEquals(0, rule.lint(code).size)
    }
}

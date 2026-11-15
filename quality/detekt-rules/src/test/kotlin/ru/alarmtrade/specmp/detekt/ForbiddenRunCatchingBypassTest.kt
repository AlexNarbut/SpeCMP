package ru.alarmtrade.specmp.detekt

import dev.detekt.api.Config
import dev.detekt.test.lint
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ForbiddenRunCatchingBypassTest {
    private val rule = ForbiddenRunCatching(Config.empty)

    @Test
    fun `bypass via import alias`() {
        val code = """
            import kotlin.runCatching as attempt

            fun a() = attempt { 1 }
        """.trimIndent()

        assertEquals(1, rule.lint(code).size)
    }

    @Test
    fun `bypass via fully qualified call`() {
        val code = """
            fun a() = kotlin.runCatching { 1 }
        """.trimIndent()

        assertEquals(1, rule.lint(code).size)
    }

    @Test
    fun `bypass via receiver form and safe call`() {
        val code = """
            fun a(s: String?) {
                s.runCatching { length }
                s?.runCatching { length }
            }
        """.trimIndent()

        assertEquals(2, rule.lint(code).size)
    }

    @Test
    fun `bypass via nested lambda in default parameter`() {
        val code = """
            fun a(block: () -> Result<Int> = { listOf(1).let { runCatching { it.first() } } }) = block()
        """.trimIndent()

        assertEquals(1, rule.lint(code).size)
    }

    @Test
    fun `bypass via bound callable reference`() {
        val code = """
            fun a(s: String) = s.let(String::runCatching)
        """.trimIndent()

        assertEquals(1, rule.lint(code).size)
    }

    @Test
    fun `does not report safeRun and look-alike names`() {
        val code = """
            import ru.alarmtrade.specmp.core.common.safeRun as runSafely

            fun a() = runSafely { 1 }
            fun b() = runCatchingLater()
        """.trimIndent()

        assertEquals(0, rule.lint(code).size)
    }
}

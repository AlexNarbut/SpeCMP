package ru.alarmtrade.specmp.detekt

import dev.detekt.api.Config
import dev.detekt.test.lintWithContext
import dev.detekt.test.utils.createEnvironment
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class LoopWithoutCancellationBypassTest {
    private val env = createEnvironment()
    private val rule = LoopWithoutCancellation(Config.empty)

    private val prelude = """
        import kotlinx.coroutines.CoroutineScope
        import kotlinx.coroutines.delay
        import kotlinx.coroutines.flow.flow
        import kotlinx.coroutines.launch

        fun busy(): Boolean = false
    """.trimIndent()

    @Test
    fun `bypass via do while`() {
        val code = prelude + "\n" + """
            suspend fun a() {
                do {
                    busy()
                } while (true)
            }
        """.trimIndent()

        assertEquals(1, rule.lintWithContext(env, code).size)
    }

    @Test
    fun `bypass via parenthesized or negated constant condition`() {
        val code = prelude + "\n" + """
            suspend fun a() {
                while ((true)) { busy() }
            }

            suspend fun b() {
                while (!false) { busy() }
            }
        """.trimIndent()

        assertEquals(2, rule.lintWithContext(env, code).size)
    }

    @Test
    fun `bypass via labeled loop in launch`() {
        val code = prelude + "\n" + """
            fun a(scope: CoroutineScope) {
                scope.launch {
                    outer@ while (true) {
                        if (busy()) continue@outer
                    }
                }
            }
        """.trimIndent()

        assertEquals(1, rule.lintWithContext(env, code).size)
    }

    @Test
    fun `bypass via suspend call in a never invoked lambda`() {
        val code = prelude + "\n" + """
            suspend fun a() {
                while (true) {
                    val later: suspend () -> Unit = { delay(1) }
                    busy()
                }
            }
        """.trimIndent()

        assertEquals(1, rule.lintWithContext(env, code).size)
    }

    @Test
    fun `bypass via inline scope function around loop in flow builder`() {
        val code = prelude + "\n" + """
            fun a() = flow<Int> {
                run {
                    while (true) {
                        listOf(1).forEach { busy() }
                    }
                }
            }
        """.trimIndent()

        assertEquals(1, rule.lintWithContext(env, code).size)
    }

    @Test
    fun `does not report loops with non-constant or false conditions`() {
        val code = prelude + "\n" + """
            var running = true

            suspend fun a() {
                while (running) { busy() }
                do { busy() } while (false)
                while (!running) { busy() }
            }
        """.trimIndent()

        assertEquals(0, rule.lintWithContext(env, code).size)
    }
}

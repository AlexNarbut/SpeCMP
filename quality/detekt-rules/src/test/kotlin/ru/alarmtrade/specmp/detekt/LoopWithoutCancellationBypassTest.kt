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
                    delay(1)
                } while (true)
            }
        """.trimIndent()

        assertEquals(1, rule.lintWithContext(env, code).size)
    }

    @Test
    fun `bypass via parenthesized, negated or computed constant condition`() {
        val code = prelude + "\n" + """
            const val FOREVER = true

            suspend fun a() {
                while ((true)) { delay(1) }
                while (!false) { delay(1) }
                while (1 == 1) { delay(1) }
                while (FOREVER) { delay(1) }
                while (true && true) { delay(1) }
            }
        """.trimIndent()

        assertEquals(5, rule.lintWithContext(env, code).size)
    }

    @Test
    fun `bypass via own isActive property`() {
        val code = prelude + "\n" + """
            class Poller {
                var isActive = true
                private val state = State()

                suspend fun poll() {
                    while (isActive) { delay(1) }
                    while (state.isActive) { delay(1) }
                    while (busy() && this.isActive) { delay(1) }
                }
            }

            class State {
                val isActive: Boolean get() = true
            }
        """.trimIndent()

        assertEquals(3, rule.lintWithContext(env, code).size)
    }

    @Test
    fun `bypass via labeled loop in launch`() {
        val code = prelude + "\n" + """
            fun a(scope: CoroutineScope) {
                scope.launch {
                    outer@ while (true) {
                        delay(1)
                        if (busy()) continue@outer
                    }
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
                        listOf(1).forEach { emit(it) }
                    }
                }
            }
        """.trimIndent()

        assertEquals(1, rule.lintWithContext(env, code).size)
    }

    @Test
    fun `bypass via yield in body`() {
        val code = prelude + "\n" + """
            suspend fun a() {
                while (true) {
                    kotlinx.coroutines.yield()
                    busy()
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

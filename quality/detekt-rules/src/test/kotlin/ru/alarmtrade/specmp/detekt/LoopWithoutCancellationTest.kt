package ru.alarmtrade.specmp.detekt

import dev.detekt.api.Config
import dev.detekt.test.lintWithContext
import dev.detekt.test.utils.createEnvironment
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class LoopWithoutCancellationTest {
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
    fun `reports busy loops in suspend functions and coroutine lambdas`() {
        val code = prelude + "\n" + """
            suspend fun spin() {
                while (true) {
                    if (busy()) break
                }
            }

            fun CoroutineScope.start() {
                launch {
                    while (true) {
                        listOf(1).forEach { busy() }
                    }
                }
            }
        """.trimIndent()

        assertEquals(2, rule.lintWithContext(env, code).size)
    }

    @Test
    fun `does not report loops with suspension points and loops outside coroutines`() {
        val code = prelude + "\n" + """
            suspend fun tick() {
                while (true) {
                    delay(1000)
                }
            }

            fun ticks() = flow {
                while (true) {
                    emit(1)
                    listOf(1).forEach { delay(1) }
                }
            }

            fun name(): Int {
                var i = 0
                while (true) {
                    if (busy()) return i
                    i++
                }
            }
        """.trimIndent()

        assertEquals(0, rule.lintWithContext(env, code).size)
    }
}

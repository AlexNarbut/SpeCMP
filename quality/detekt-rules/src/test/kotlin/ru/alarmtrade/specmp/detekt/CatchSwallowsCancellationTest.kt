package ru.alarmtrade.specmp.detekt

import dev.detekt.api.Config
import dev.detekt.test.lintWithContext
import dev.detekt.test.utils.createEnvironment
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class CatchSwallowsCancellationTest {
    private val env = createEnvironment()
    private val rule = CatchSwallowsCancellation(Config.empty)

    private val prelude = """
        import kotlin.coroutines.cancellation.CancellationException
        import kotlinx.coroutines.CoroutineScope
        import kotlinx.coroutines.delay
        import kotlinx.coroutines.ensureActive
        import kotlinx.coroutines.launch
        import kotlinx.coroutines.currentCoroutineContext

        suspend fun load(): Int = 1
        fun parse(): Int = 1
    """.trimIndent()

    @Test
    fun `reports broad catch around suspend calls in coroutines`() {
        val code = prelude + "\n" + """
            suspend fun a(): Int? = try { load() } catch (e: Exception) { null }

            suspend fun b(): Int? = try { load() } catch (_: Throwable) { null }

            suspend fun c(): Int? = try { load() } catch (e: IllegalStateException) { null }

            fun d(scope: CoroutineScope) {
                scope.launch {
                    listOf(1).forEach {
                        try { delay(1) } catch (e: RuntimeException) { println(e) }
                    }
                }
            }
        """.trimIndent()

        assertEquals(4, rule.lintWithContext(env, code).size)
    }

    @Test
    fun `does not report rethrown cancellation, narrow catches and non-suspend code`() {
        val code = prelude + "\n" + """
            suspend fun a(): Int? = try {
                load()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                null
            }

            suspend fun b(): Int? = try {
                load()
            } catch (e: Throwable) {
                if (e is CancellationException) throw e
                null
            }

            suspend fun c(): Int? = try {
                load()
            } catch (e: Exception) {
                currentCoroutineContext().ensureActive()
                null
            }

            suspend fun d(): Int = try {
                load()
            } catch (e: Exception) {
                throw e
            }

            suspend fun e(): Int? = try { load() } catch (e: IllegalArgumentException) { null }

            suspend fun f(): Int? = try { parse() } catch (e: Exception) { null }

            fun g(): Int? = try { parse() } catch (e: Exception) { null }

            fun h(scope: CoroutineScope) {
                val callback = {
                    try { parse() } catch (e: Exception) { null }
                }
            }
        """.trimIndent()

        assertEquals(0, rule.lintWithContext(env, code).size)
    }
}

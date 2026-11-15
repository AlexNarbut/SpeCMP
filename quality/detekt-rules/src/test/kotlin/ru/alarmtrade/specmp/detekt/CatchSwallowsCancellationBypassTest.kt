package ru.alarmtrade.specmp.detekt

import dev.detekt.api.Config
import dev.detekt.test.lintWithContext
import dev.detekt.test.utils.createEnvironment
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class CatchSwallowsCancellationBypassTest {
    private val env = createEnvironment()
    private val rule = CatchSwallowsCancellation(Config.empty)

    private val prelude = """
        import kotlin.coroutines.cancellation.CancellationException
        import kotlinx.coroutines.CoroutineScope
        import kotlinx.coroutines.delay
        import kotlinx.coroutines.launch

        suspend fun load(): Int = 1
    """.trimIndent()

    @Test
    fun `bypass via import alias of Exception`() {
        val code = prelude + "\n" + """
            import kotlin.Exception as Failure

            suspend fun a(): Int? = try { load() } catch (e: Failure) { null }
        """.trimIndent()

        assertEquals(1, rule.lintWithContext(env, code).size)
    }

    @Test
    fun `bypass via typealias of Throwable`() {
        val code = prelude + "\n" + """
            typealias Problem = Throwable

            suspend fun a(): Int? = try { load() } catch (e: Problem) { null }
        """.trimIndent()

        assertEquals(1, rule.lintWithContext(env, code).size)
    }

    @Test
    fun `bypass via earlier catch of a look-alike CancellationException class`() {
        val code = prelude + "\n" + """
            class FakeCancellationException : Exception()

            suspend fun a(): Int? = try {
                load()
            } catch (e: FakeCancellationException) {
                null
            } catch (e: Exception) {
                null
            }
        """.trimIndent()

        assertEquals(1, rule.lintWithContext(env, code).size)
    }

    @Test
    fun `bypass via is-check of CancellationException without rethrow`() {
        val code = prelude + "\n" + """
            suspend fun a(): Int? = try {
                load()
            } catch (e: Exception) {
                if (e is CancellationException) println(e)
                null
            }
        """.trimIndent()

        assertEquals(1, rule.lintWithContext(env, code).size)
    }

    @Test
    fun `bypass via try inside inline scope functions of a launched coroutine`() {
        val code = prelude + "\n" + """
            fun a(scope: CoroutineScope) {
                scope.launch {
                    run { 1 }.let {
                        try { delay(1) } catch (e: java.lang.Exception) { println(e) }
                    }
                }
            }
        """.trimIndent()

        assertEquals(1, rule.lintWithContext(env, code).size)
    }

    @Test
    fun `does not report cancellation rethrown from when branch or real CancellationException catch`() {
        val code = prelude + "\n" + """
            suspend fun a(): Int? = try {
                load()
            } catch (e: Exception) {
                when (e) {
                    is CancellationException -> throw e
                    else -> null
                }
            }

            suspend fun b(): Int? = try {
                load()
            } catch (e: java.util.concurrent.CancellationException) {
                throw e
            } catch (e: Exception) {
                null
            }
        """.trimIndent()

        assertEquals(0, rule.lintWithContext(env, code).size)
    }
}

package ru.alarmtrade.specmp.detekt

import dev.detekt.api.Config
import dev.detekt.test.lintWithContext
import dev.detekt.test.utils.createEnvironment
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class UnawaitedAsyncTest {
    private val env = createEnvironment()
    private val rule = UnawaitedAsync(Config.empty)

    private val prelude = """
        import kotlinx.coroutines.async
        import kotlinx.coroutines.awaitAll
        import kotlinx.coroutines.coroutineScope

        suspend fun load(): Int = 1
    """.trimIndent()

    @Test
    fun `reports unused async results`() {
        val code = prelude + "\n" + """
            suspend fun a() = coroutineScope {
                async { load() }
                val unused = async { load() }
                load()
            }
        """.trimIndent()

        assertEquals(2, rule.lintWithContext(env, code).size)
    }

    @Test
    fun `does not report awaited and returned deferreds`() {
        val code = prelude + "\n" + """
            suspend fun a(): Int = coroutineScope {
                val first = async { load() }
                val all = listOf(1, 2).map { async { load() } }.awaitAll()
                first.await() + all.sum()
            }

            suspend fun b() = coroutineScope { async { load() } }

            class Other {
                fun async(block: () -> Unit) = Unit
                fun c() {
                    async { }
                }
            }
        """.trimIndent()

        assertEquals(0, rule.lintWithContext(env, code).size)
    }
}

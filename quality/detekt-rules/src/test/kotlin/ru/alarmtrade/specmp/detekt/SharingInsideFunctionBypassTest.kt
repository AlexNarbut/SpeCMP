package ru.alarmtrade.specmp.detekt

import dev.detekt.api.Config
import dev.detekt.test.lint
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class SharingInsideFunctionBypassTest {
    private val rule = SharingInsideFunction(Config.empty)

    @Test
    fun `bypass via import alias`() {
        val code = """
            import kotlinx.coroutines.flow.stateIn as hot

            class Provider(private val scope: CoroutineScope, private val source: Flow<Int>) {
                fun state() = source.hot(scope, SharingStarted.Eagerly, 0)
            }
        """.trimIndent()

        assertEquals(1, rule.lint(code).size)
    }

    @Test
    fun `bypass via implicit receiver in scope function`() {
        val code = """
            class Provider(private val scope: CoroutineScope, private val source: Flow<Int>) {
                fun state() = with(source) { stateIn(scope, SharingStarted.Eagerly, 0) }
                fun shared() = source.run { shareIn(scope, SharingStarted.Lazily) }
            }
        """.trimIndent()

        assertEquals(2, rule.lint(code).size)
    }

    @Test
    fun `bypass via lambda stored in a property`() {
        val code = """
            class Provider(private val scope: CoroutineScope, private val source: Flow<Int>) {
                private val makeState = { source.stateIn(scope, SharingStarted.Eagerly, 0) }
                private val makeShared: (Int) -> Flow<Int> = { replay -> source.shareIn(scope, SharingStarted.Lazily, replay) }
            }
        """.trimIndent()

        assertEquals(2, rule.lint(code).size)
    }

    @Test
    fun `bypass via extension function and safe call`() {
        val code = """
            fun Flow<Int>.cached(scope: CoroutineScope) = stateIn(scope, SharingStarted.Eagerly, 0)

            fun shared(source: Flow<Int>?, scope: CoroutineScope) = source?.shareIn(scope, SharingStarted.Lazily)
        """.trimIndent()

        assertEquals(2, rule.lint(code).size)
    }

    @Test
    fun `bypass via anonymous function and local function in initializer`() {
        val code = """
            class Provider(private val scope: CoroutineScope, private val source: Flow<Int>) {
                private val make = fun() = source.stateIn(scope, SharingStarted.Eagerly, 0)
                private val state = run {
                    fun build() = source.shareIn(scope, SharingStarted.Lazily)
                    build()
                }
            }
        """.trimIndent()

        assertEquals(2, rule.lint(code).size)
    }
}

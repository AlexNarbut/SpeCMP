package ru.alarmtrade.specmp.detekt

import dev.detekt.api.Config
import dev.detekt.test.lint
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class SharingInsideFunctionTest {
    private val rule = SharingInsideFunction(Config.empty)

    @Test
    fun `reports shareIn and stateIn in functions and getters`() {
        val code = """
            class Provider(private val scope: CoroutineScope, private val source: Flow<Int>) {
                fun state(): StateFlow<Int> = source.stateIn(scope, SharingStarted.Eagerly, 0)

                fun shared(): Flow<Int> {
                    val shared = source.map { it + 1 }.shareIn(scope, SharingStarted.Lazily)
                    return shared
                }

                val getter: StateFlow<Int>
                    get() = source.stateIn(scope, SharingStarted.Eagerly, 0)

                suspend fun suspending() = source.stateIn(scope)
            }
        """.trimIndent()

        assertEquals(4, rule.lint(code).size)
    }

    @Test
    fun `does not report property initializers`() {
        val code = """
            class Provider(private val scope: CoroutineScope, private val source: Flow<Int>) {
                private val state: StateFlow<Int> = source
                    .flatMapLatest { value -> flowOf(value) }
                    .stateIn(scope, SharingStarted.WhileSubscribed(), 0)

                val shared = source.shareIn(scope, SharingStarted.Lazily, replay = 1)

                private val lazyState by lazy { source.stateIn(scope, SharingStarted.Eagerly, 0) }
            }

            val topLevel = flowOf(1).stateIn(GlobalScope, SharingStarted.Eagerly, 0)
        """.trimIndent()

        assertEquals(0, rule.lint(code).size)
    }
}

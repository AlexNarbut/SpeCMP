package ru.alarmtrade.specmp.detekt

import dev.detekt.api.Config
import dev.detekt.test.lintWithContext
import dev.detekt.test.utils.createEnvironment
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class StateFlowValueMutationBypassTest {
    private val env = createEnvironment()
    private val rule = StateFlowValueMutation(Config.empty)

    @Test
    fun `bypass via implicit receiver in scope functions`() {
        val code = """
            import kotlinx.coroutines.flow.MutableStateFlow

            class Counter {
                private val state = MutableStateFlow(0)

                fun mutate() {
                    with(state) { value = 1 }
                    state.apply { value += 1 }
                    state.run { value++ }
                }
            }
        """.trimIndent()

        assertEquals(3, rule.lintWithContext(env, code).size)
    }

    @Test
    fun `bypass via extension function on MutableStateFlow`() {
        val code = """
            import kotlinx.coroutines.flow.MutableStateFlow

            fun MutableStateFlow<Int>.bump() {
                value++
                this.value -= 1
            }
        """.trimIndent()

        assertEquals(2, rule.lintWithContext(env, code).size)
    }

    @Test
    fun `bypass via import alias of the type`() {
        val code = """
            import kotlinx.coroutines.flow.MutableStateFlow as Box

            class Counter {
                private val state: Box<Int> = Box(0)

                fun mutate() {
                    state.value = 1
                }
            }
        """.trimIndent()

        assertEquals(1, rule.lintWithContext(env, code).size)
    }

    @Test
    fun `bypass via cast and read-only declared type`() {
        val code = """
            import kotlinx.coroutines.flow.MutableStateFlow
            import kotlinx.coroutines.flow.StateFlow

            class Counter {
                private val state: StateFlow<Int> = MutableStateFlow(0)

                fun mutate() {
                    (state as MutableStateFlow<Int>).value = 1
                    (state as? MutableStateFlow<Int>)?.value = 2
                }
            }
        """.trimIndent()

        assertEquals(2, rule.lintWithContext(env, code).size)
    }

    @Test
    fun `bypass via nested property chain and lambda`() {
        val code = """
            import kotlinx.coroutines.flow.MutableStateFlow

            class Holder { val flow = MutableStateFlow(0) }

            class Counter(private val holder: Holder) {
                private val reset: () -> Unit = { holder.flow.value = 0 }
            }
        """.trimIndent()

        assertEquals(1, rule.lintWithContext(env, code).size)
    }

    @Test
    fun `does not report value of own receiver that is not a StateFlow`() {
        val code = """
            import kotlinx.coroutines.flow.MutableStateFlow

            class Holder { var value = 0 }

            fun Holder.bump(state: MutableStateFlow<Int>) {
                value++
                with(state) { println(value) }
            }
        """.trimIndent()

        assertEquals(0, rule.lintWithContext(env, code).size)
    }
}

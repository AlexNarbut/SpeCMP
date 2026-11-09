package ru.alarmtrade.specmp.detekt

import dev.detekt.api.Config
import dev.detekt.test.lintWithContext
import dev.detekt.test.utils.createEnvironment
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class StateFlowValueMutationTest {
    private val env = createEnvironment()
    private val rule = StateFlowValueMutation(Config.empty)

    @Test
    fun `reports assignment, compound assignment and increments on MutableStateFlow value`() {
        val code = """
            import kotlinx.coroutines.flow.MutableStateFlow

            class Counter {
                private val state = MutableStateFlow(0)
                private val nullable: MutableStateFlow<Int>? = null

                fun mutate() {
                    state.value = 2
                    state.value += 1
                    state.value -= 1
                    state.value *= 2
                    state.value /= 2
                    state.value %= 2
                    state.value++
                    state.value--
                    ++state.value
                    --state.value
                    (state).value = 3
                    nullable?.value = 4
                    this.state.value = 5
                }
            }
        """.trimIndent()

        assertEquals(13, rule.lintWithContext(env, code).size)
    }

    @Test
    fun `reports subtypes of MutableStateFlow`() {
        val code = """
            import kotlinx.coroutines.flow.MutableStateFlow

            interface CounterFlow : MutableStateFlow<Int>

            fun bump(flow: CounterFlow) {
                flow.value += 1
            }
        """.trimIndent()

        assertEquals(1, rule.lintWithContext(env, code).size)
    }

    @Test
    fun `does not report reads, update and other value holders`() {
        val code = """
            import kotlinx.coroutines.flow.MutableStateFlow
            import kotlinx.coroutines.flow.update

            class Holder {
                var value: Int = 0
            }

            class Counter {
                private val state = MutableStateFlow(0)
                private val holder = Holder()

                fun mutate(): Int {
                    state.update { it + 1 }
                    holder.value = 1
                    holder.value += 1
                    holder.value++
                    var local = state.value
                    local += 1
                    return state.value + local
                }
            }
        """.trimIndent()

        assertEquals(0, rule.lintWithContext(env, code).size)
    }

    @Test
    fun `does not report Compose MutableState`() {
        val code = """
            import androidx.compose.runtime.mutableStateOf

            fun ui() {
                val counter = mutableStateOf(0)
                counter.value = 1
                counter.value += 1
                counter.value++
            }
        """.trimIndent()

        assertEquals(0, rule.lintWithContext(env, code, COMPOSE_RUNTIME_STUB).size)
    }

    private companion object {
        val COMPOSE_RUNTIME_STUB = """
            package androidx.compose.runtime

            interface State<out T> {
                val value: T
            }

            interface MutableState<T> : State<T> {
                override var value: T
            }

            fun <T> mutableStateOf(value: T): MutableState<T> = TODO()
        """.trimIndent()
    }
}

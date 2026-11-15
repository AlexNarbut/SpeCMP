package ru.alarmtrade.specmp.detekt

import dev.detekt.api.Config
import dev.detekt.test.lint
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ReduceOutsideIntentBypassTest {
    private val rule = ReduceOutsideIntent(Config.empty)

    @Test
    fun `bypass via nested helper class inside the ViewModel`() {
        val code = """
            class FooViewModel : BaseViewModel<FooState, FooEffect>(FooState()) {
                private inner class Helper {
                    fun apply() = reduce { copy(loading = true) }
                }
            }
        """.trimIndent()

        assertEquals(1, rule.lint(code).size)
    }

    @Test
    fun `bypass via anonymous object inside the ViewModel`() {
        val code = """
            class FooViewModel : BaseViewModel<FooState, FooEffect>(FooState()) {
                private val listener = object : Listener {
                    override fun onEvent() {
                        postSideEffect(FooEffect.Close)
                    }
                }
            }
        """.trimIndent()

        assertEquals(1, rule.lint(code).size)
    }

    @Test
    fun `bypass via import alias of reduce`() {
        val code = """
            import ru.alarmtrade.specmp.core.ui.viewmodel.reduce as set

            class FooViewModel : BaseViewModel<FooState, FooEffect>(FooState()) {
                fun onInput(text: String) {
                    set { copy(text = text) }
                }
            }
        """.trimIndent()

        assertEquals(1, rule.lint(code).size)
    }

    @Test
    fun `bypass via callable reference passed outside intent`() {
        val code = """
            class FooViewModel : BaseViewModel<FooState, FooEffect>(FooState()) {
                fun onEffects(effects: List<FooEffect>) {
                    effects.forEach(::postSideEffect)
                }
            }
        """.trimIndent()

        assertEquals(1, rule.lint(code).size)
    }

    @Test
    fun `bypass via own intent function shadowing the base launcher`() {
        val code = """
            class FooViewModel : BaseViewModel<FooState, FooEffect>(FooState()) {
                private fun intent(block: () -> Unit) = block()

                fun onInput(text: String) {
                    intent { reduce { copy(text = text) } }
                }
            }
        """.trimIndent()

        assertEquals(1, rule.lint(code).size)
    }

    @Test
    fun `bypass via local function and scope functions`() {
        val code = """
            class FooViewModel : BaseViewModel<FooState, FooEffect>(FooState()) {
                fun onInput(text: String) {
                    fun apply() = reduce { copy(text = text) }
                    text.let { this.postSideEffect(FooEffect.Typed(it)) }
                    run { apply() }
                }
            }
        """.trimIndent()

        assertEquals(2, rule.lint(code).size)
    }

    @Test
    fun `does not report lambdas of own IntentScope launchers, subIntent and base classes`() {
        val code = """
            class FooViewModel : BaseViewModel<FooState, FooEffect>(FooState()) {
                private fun work(block: suspend IntentScope.() -> Unit) = intent { block() }
                private fun <T> load(onReady: IntentScope.(T) -> Unit) = Unit

                fun onSave() {
                    work { reduce { copy(saving = true) } }
                    load<Int> { postSideEffect(FooEffect.Close) }
                }

                private suspend fun close() = subIntent { postSideEffect(FooEffect.Close) }
            }

            abstract class BaseDeviceViewModel<S : Any, E : Any> : BaseViewModel<S, E>(TODO()) {
                protected fun launchOnDevice(block: suspend IntentScope.() -> Unit) = intent(block)
            }
        """.trimIndent()

        assertEquals(0, rule.lint(code).size)
    }

    @Test
    fun `bypass via helper with qualified IntentScope receiver`() {
        val code = """
            class FooViewModel : BaseViewModel<FooState, FooEffect>(FooState()) {
                private fun BaseViewModel<FooState, FooEffect>.IntentScope.close() = postSideEffect(FooEffect.Close)
            }
        """.trimIndent()

        assertEquals(2, rule.lint(code).size)
    }

    @Test
    fun `bypass via onStart lookalike without override`() {
        val code = """
            class FooViewModel : BaseViewModel<FooState, FooEffect>(FooState()) {
                private suspend fun IntentScope.onStart(text: String) {
                    reduce { copy(text = text) }
                }
            }
        """.trimIndent()

        assertEquals(2, rule.lint(code).size)
    }

    @Test
    fun `bypass via IntentScope helper in a nested class of the ViewModel`() {
        val code = """
            class FooViewModel : BaseViewModel<FooState, FooEffect>(FooState()) {
                private inner class Steps {
                    suspend fun IntentScope.next() = Unit
                }
            }
        """.trimIndent()

        assertEquals(1, rule.lint(code).size)
    }
}

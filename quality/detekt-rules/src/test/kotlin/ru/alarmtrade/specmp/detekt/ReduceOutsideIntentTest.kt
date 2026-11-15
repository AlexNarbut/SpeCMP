package ru.alarmtrade.specmp.detekt

import dev.detekt.api.Config
import dev.detekt.test.lint
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ReduceOutsideIntentTest {
    private val rule = ReduceOutsideIntent(Config.empty)

    @Test
    fun `reports reduce and postSideEffect outside intent`() {
        val code = """
            class FooViewModel : BaseViewModel<FooState, FooEffect>(FooState()) {
                fun onInput(text: String) {
                    reduce { copy(text = text) }
                }

                fun onSave() {
                    reduce { copy(loading = true) }
                    intent { repository.save() }
                    postSideEffect(FooEffect.Close)
                }

                fun onItems(items: List<Int>) {
                    items.forEach { item -> reduce { copy(last = item) } }
                }

                private suspend fun refresh() {
                    reduce { copy(loading = false) }
                }

                private fun close() {
                    postSideEffect(FooEffect.Close)
                }

                fun onLater() {
                    intent { schedule { reduce { copy() } } }
                    select { query }.map { reduce { copy() } }
                }
            }
        """.trimIndent()

        val findings = rule.lint(code)

        assertEquals(7, findings.size)
        assertEquals(true, findings.all { "intent" in it.message })
    }

    @Test
    fun `does not report calls inside launchers, subIntent and onStart`() {
        val code = """
            class FooViewModel : BaseDeviceViewModel<FooState, FooEffect>(session, FooState()) {
                override suspend fun IntentScope.onStart() {
                    reduce { copy(loading = true) }
                }

                fun onSave() {
                    intent {
                        reduce { copy(loading = true) }
                        repository.save().onSuccess { postSideEffect(FooEffect.Close) }
                        items.forEach { reduce { copy(last = it) } }
                    }
                }

                fun onLoad() {
                    intent(dispatchers.io) { reduce { copy(loading = false) } }
                    launchReplacing(LOAD) { reduce { copy(loading = true) } }
                    launchOnDevice(block = { postSideEffect(FooEffect.Close) })
                    onDeviceLost { reduce { copy(device = null) } }
                    onEachDeviceReady { device -> reduce { copy(device = device) } }
                }

                private suspend fun refresh() = subIntent {
                    reduce { copy(loading = false) }
                }

                private suspend fun close() {
                    subIntent { postSideEffect(FooEffect.Close) }
                }
            }

            class FooRenderer {
                fun render() {
                    reduce { copy() }
                    postSideEffect(Close)
                }
            }

            fun Flow<Int>.sum() = reduce { a, b -> a + b }
        """.trimIndent()

        assertEquals(0, rule.lint(code).size)
    }

    @Test
    fun `reports helpers with IntentScope receiver except onStart`() {
        val code = """
            class FooViewModel : BaseViewModel<FooState, FooEffect>(FooState()) {
                override suspend fun IntentScope.onStart() = Unit

                private suspend fun IntentScope.refresh() {
                    reduce { copy(loading = false) }
                }

                private fun IntentScope.close() = Unit
            }
        """.trimIndent()

        val findings = rule.lint(code)

        assertEquals(3, findings.size)
        assertEquals(2, findings.count { "subIntent { … }" in it.message })
    }
}

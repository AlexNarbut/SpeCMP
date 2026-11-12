package ru.alarmtrade.specmp.detekt

import dev.detekt.api.Config
import dev.detekt.test.lint
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ReduceOutsideIntentTest {
    private val rule = ReduceOutsideIntent(Config.empty)

    @Test
    fun `reports reduce and postSideEffect outside intent or safeLaunch in the same function`() {
        val code = """
            class FooViewModel : BaseViewModel<FooState, FooEffect>(FooState()) {
                fun onSave() {
                    reduce { copy(loading = true) }
                    intent { repository.save() }
                    postSideEffect(FooEffect.Close)
                }

                fun onLoad() {
                    if (currentState.loading) reduce { copy(loading = false) }
                    safeLaunch(dispatchers.io) { repository.load() }
                }

                fun onItems(items: List<Int>) {
                    items.forEach { item -> reduce { copy(last = item) } }
                    intent(dispatchers.io) { repository.save() }
                }
            }
        """.trimIndent()

        val findings = rule.lint(code)

        assertEquals(4, findings.size)
        assertEquals(true, findings.all { "intent" in it.message })
    }

    @Test
    fun `does not report calls inside launchers and functions without intent or safeLaunch`() {
        val code = """
            class FooViewModel : BaseViewModel<FooState, FooEffect>(FooState()) {
                fun onSave() {
                    intent {
                        reduce { copy(loading = true) }
                        repository.save().onSuccess { postSideEffect(FooEffect.Close) }
                    }
                }

                fun onLoad() {
                    safeLaunch(dispatchers.io) { reduce { copy(loading = false) } }
                    launchReplacing(LOAD) { reduce { copy(loading = true) } }
                }

                fun onSelect(id: Int) {
                    reduce { copy(selected = id) }
                    postSideEffect(FooEffect.Close)
                    launchReplacing(LOAD) { repository.load(id) }
                }

                private suspend fun refresh() {
                    safeLaunch { repository.load() }
                    reduce { copy(loading = false) }
                }

                fun onNested() {
                    intent { items.forEach { reduce { copy(last = it) } } }
                }
            }

            class FooRenderer {
                fun render() {
                    reduce { copy() }
                    intent { }
                }
            }
        """.trimIndent()

        assertEquals(0, rule.lint(code).size)
    }
}

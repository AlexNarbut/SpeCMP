package ru.alarmtrade.specmp.detekt

import dev.detekt.api.Config
import dev.detekt.test.lint
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class SideEffectAwaitsUiBypassTest {
    private val rule = SideEffectAwaitsUi(Config.empty)

    @Test
    fun `bypass via import alias of collectSideEffect and showSnackbar`() {
        val code = """
            import ru.alarmtrade.specmp.core.ui.effect.collectSideEffect as onEffect
            import ru.alarmtrade.specmp.core.ui.snackbar.showSnackbar as toast

            @Composable
            fun Screen(vm: Vm, snackbar: CmpSnackbarHostState) {
                vm.sideEffect.onEffect { snackbar.toast("saved") }
            }
        """.trimIndent()

        assertEquals(1, rule.lint(code).size)
    }

    @Test
    fun `bypass via async awaited in place`() {
        val code = """
            @Composable
            fun Screen(vm: Vm, snackbar: CmpSnackbarHostState) {
                val scope = rememberCoroutineScope()
                vm.sideEffect.collectSideEffect { scope.async { snackbar.showSnackbar("saved") }.await() }
            }
        """.trimIndent()

        assertEquals(1, rule.lint(code).size)
    }

    @Test
    fun `bypass via helper function of the same file`() {
        val code = """
            @Composable
            fun Screen(vm: Vm, snackbar: CmpSnackbarHostState) {
                vm.sideEffect.collectSideEffect { effect -> notify(snackbar, effect) }
            }

            private suspend fun notify(snackbar: CmpSnackbarHostState, effect: Effect) {
                snackbar.showSnackbar(effect.toString())
            }
        """.trimIndent()

        assertEquals(1, rule.lint(code).size)
    }

    @Test
    fun `bypass via callable reference`() {
        val code = """
            @Composable
            fun Screen(vm: Vm, snackbar: CmpSnackbarHostState) {
                vm.sideEffect.collectSideEffect { effect -> effect.message.let(snackbar::showSnackbar) }
            }
        """.trimIndent()

        assertEquals(1, rule.lint(code).size)
    }

    @Test
    fun `bypass via scope functions and coroutineScope`() {
        val code = """
            @Composable
            fun Screen(vm: Vm, snackbar: CmpSnackbarHostState) {
                vm.sideEffect.collectSideEffect { effect ->
                    with(snackbar) { showSnackbar("saved") }
                    coroutineScope { snackbar.showSnackbar("done") }
                }
            }
        """.trimIndent()

        assertEquals(2, rule.lint(code).size)
    }

    @Test
    fun `does not report launched helper and fire-and-forget async`() {
        val code = """
            @Composable
            fun Screen(vm: Vm, snackbar: CmpSnackbarHostState) {
                val scope = rememberCoroutineScope()
                vm.sideEffect.collectSideEffect { effect ->
                    scope.launch { notify(snackbar, effect) }
                    scope.async { snackbar.showSnackbar("x") }
                    post(snackbar)
                }
            }

            private suspend fun notify(snackbar: CmpSnackbarHostState, effect: Effect) {
                snackbar.showSnackbar(effect.toString())
            }

            private fun post(snackbar: CmpSnackbarHostState) {
                scope.launch { snackbar.showSnackbar("later") }
            }
        """.trimIndent()

        assertEquals(0, rule.lint(code).size)
    }
}

package ru.alarmtrade.specmp.detekt

import dev.detekt.api.Config
import dev.detekt.test.lint
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class SideEffectAwaitsUiTest {
    private val rule = SideEffectAwaitsUi(Config.empty)

    @Test
    fun `reports snackbar awaited directly in collectSideEffect`() {
        val code = """
            @Composable
            fun Screen(vm: Vm, snackbar: CmpSnackbarHostState) {
                vm.sideEffect.collectSideEffect { effect ->
                    when (effect) {
                        Effect.Saved -> snackbar.showSnackbar("saved")
                        is Effect.Failed -> {
                            val result = snackbar.showSnackbar("failed", actionLabel = "retry")
                            if (result == CmpSnackbarResult.ActionPerformed) vm.onRetry()
                        }
                        is Effect.Done -> effect.text.let { showResult(snackbar, it) }
                    }
                }
            }
        """.trimIndent()

        assertEquals(3, rule.lint(code).size)
    }

    @Test
    fun `does not report launched snackbar and snackbar outside collectSideEffect`() {
        val code = """
            @Composable
            fun Screen(vm: Vm, snackbar: CmpSnackbarHostState, onBack: () -> Unit) {
                val scope = rememberCoroutineScope()
                vm.sideEffect.collectSideEffect { effect ->
                    when (effect) {
                        Effect.Close -> onBack()
                        Effect.Saved -> scope.launch { snackbar.showSnackbar("saved") }
                        is Effect.Done -> scope.launch {
                            if (snackbar.showSnackbar("done") == CmpSnackbarResult.ActionPerformed) vm.onRetry()
                        }
                    }
                }
                LaunchedEffect(Unit) { snackbar.showSnackbar("hello") }
                vm.sideEffect.collectSideEffect {
                    fun local() = snackbar.showSnackbar("local")
                }
            }

            suspend fun showResult(snackbar: CmpSnackbarHostState) {
                snackbar.showSnackbar("result")
            }
        """.trimIndent()

        assertEquals(0, rule.lint(code).size)
    }
}

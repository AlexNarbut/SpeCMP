package ru.alarmtrade.specmp.detekt

import dev.detekt.api.Config
import dev.detekt.test.lint
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ViewModelCallInEffectTest {
    private val rule = ViewModelCallInEffect(Config.empty)

    @Test
    fun `reports direct view model calls in effects`() {
        val code = """
            @Composable
            fun Screen(id: Int) {
                val viewModel = koinViewModel<FooViewModel>()
                LaunchedEffect(id) { viewModel.onLoad(id) }
                LaunchedEffect(Unit) {
                    if (ready) vm.onStart()
                }
                DisposableEffect(Unit) {
                    onDispose { fooViewModel.onLeave() }
                }
            }
        """.trimIndent()

        assertEquals(3, rule.lint(code).size)
    }

    @Test
    fun `does not report result bridges, references and calls outside effects`() {
        val code = """
            @Composable
            fun Screen() {
                val viewModel = koinViewModel<FooViewModel>()
                LaunchedEffect(picker) { picker.results.collect { viewModel.onPicked(it) } }
                LaunchedEffect(picker) { picker.results.collect(viewModel::onPicked) }
                LaunchedEffect(nearEnd) { if (nearEnd) actions.onEndReached() }
                CmpButton(onClick = { viewModel.onSave() })
            }
        """.trimIndent()

        assertEquals(0, rule.lint(code).size)
    }
}

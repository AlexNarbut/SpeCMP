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

    @Test
    fun `bypass via safe call on a nullable view model`() {
        val code = """
            @Composable
            fun Screen(viewModel: FooViewModel?) {
                LaunchedEffect(Unit) { viewModel?.onLoad() }
            }
        """.trimIndent()

        assertEquals(1, rule.lint(code).size)
    }

    @Test
    fun `bypass via with on the view model`() {
        val code = """
            @Composable
            fun Screen(viewModel: FooViewModel) {
                LaunchedEffect(Unit) {
                    with(viewModel) { onLoad() }
                }
            }
        """.trimIndent()

        assertEquals(1, rule.lint(code).size)
    }

    @Test
    fun `bypass via function reference invoked inside the effect`() {
        val code = """
            @Composable
            fun Screen(viewModel: FooViewModel) {
                LaunchedEffect(Unit) {
                    val load = viewModel::onLoad
                    load()
                }
            }
        """.trimIndent()

        assertEquals(1, rule.lint(code).size)
    }

    @Test
    fun `bypass via nested launch inside the effect`() {
        val code = """
            @Composable
            fun Screen(viewModel: FooViewModel) {
                LaunchedEffect(Unit) {
                    launch { this@Screen.viewModel.onLoad() }
                }
            }
        """.trimIndent()

        assertEquals(1, rule.lint(code).size)
    }

    @Test
    fun `bypass via import alias of LaunchedEffect`() {
        val code = """
            import androidx.compose.runtime.LaunchedEffect as OnEnter

            @Composable
            fun Screen(viewModel: FooViewModel) {
                OnEnter(Unit) { viewModel.onLoad() }
            }
        """.trimIndent()

        assertEquals(1, rule.lint(code).size)
    }
}

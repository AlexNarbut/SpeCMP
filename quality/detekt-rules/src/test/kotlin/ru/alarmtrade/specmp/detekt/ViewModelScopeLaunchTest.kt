package ru.alarmtrade.specmp.detekt

import dev.detekt.api.Config
import dev.detekt.test.lint
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ViewModelScopeLaunchTest {
    private val rule = ViewModelScopeLaunch(Config.empty)

    @Test
    fun `reports launch, async and launchIn on viewModelScope with any receiver form`() {
        val code = """
            class FooViewModel : BaseViewModel<FooState, Nothing>(FooState()) {
                fun load(flow: Flow<Int>) {
                    viewModelScope.launch { work() }
                    viewModelScope.launch(dispatchers.io) { work() }
                    val deferred = viewModelScope.async { work() }
                    this.viewModelScope.launch { work() }
                    this@FooViewModel.viewModelScope.async { work() }
                    viewModelScope?.launch { work() }
                    flow.onEach { }.launchIn(viewModelScope)
                    flow.launchIn(this.viewModelScope)
                }
            }
        """.trimIndent()

        val findings = rule.lint(code)

        assertEquals(8, findings.size)
        assertEquals(true, findings.all { "intent" in it.message })
    }

    @Test
    fun `does not report intent, stateIn, shareIn and other scopes`() {
        val code = """
            class FooViewModel : BaseViewModel<FooState, Nothing>(FooState()) {
                val state = flow.stateIn(viewModelScope, SharingStarted.Eagerly, 0)
                val shared = flow.shareIn(scope = viewModelScope, started = SharingStarted.Lazily)
                fun load(scope: CoroutineScope, flow: Flow<Int>) {
                    intent { work() }
                    intent(dispatchers.io) { work() }
                    scope.launch { work() }
                    appScope.async { work() }
                    flow.launchIn(scope)
                    launch { work() }
                    other.viewModelScope.launch { work() }
                    val machine = Machine(scope = viewModelScope)
                }
            }
        """.trimIndent()

        assertEquals(0, rule.lint(code).size)
    }

    @Test
    fun `bypass via with and scope functions on viewModelScope`() {
        val code = """
            class FooViewModel : BaseViewModel<FooState, Nothing>(FooState()) {
                fun load(flow: Flow<Int>) {
                    with(viewModelScope) { launch { work() } }
                    viewModelScope.run { items.forEach { async { work() } } }
                    viewModelScope.let { it.launch { work() } }
                    viewModelScope.apply { flow.launchIn(this) }
                }
            }
        """.trimIndent()

        assertEquals(4, rule.lint(code).size)
    }

    @Test
    fun `bypass via context added to viewModelScope`() {
        val code = """
            class FooViewModel : BaseViewModel<FooState, Nothing>(FooState()) {
                fun load(flow: Flow<Int>) {
                    (viewModelScope + dispatchers.io).launch { work() }
                    viewModelScope.plus(dispatchers.io).async { work() }
                    flow.launchIn(viewModelScope + dispatchers.io)
                }
            }
        """.trimIndent()

        assertEquals(3, rule.lint(code).size)
    }

    @Test
    fun `bypass via scope built from the viewModelScope context`() {
        val code = """
            class FooViewModel : BaseViewModel<FooState, Nothing>(FooState()) {
                fun load() {
                    CoroutineScope(viewModelScope.coroutineContext + dispatchers.io).launch { work() }
                }
            }
        """.trimIndent()

        assertEquals(1, rule.lint(code).size)
    }

    @Test
    fun `bypass via import alias of viewModelScope`() {
        val code = """
            import androidx.lifecycle.viewModelScope as vmScope

            class FooViewModel : BaseViewModel<FooState, Nothing>(FooState()) {
                fun load() {
                    vmScope.launch { work() }
                }
            }
        """.trimIndent()

        assertEquals(1, rule.lint(code).size)
    }

    @Test
    fun `does not report launches whose scope was changed inside viewModelScope lambdas`() {
        val code = """
            class FooViewModel : BaseViewModel<FooState, Nothing>(FooState()) {
                fun load() {
                    intent { coroutineScope { launch { work() } } }
                    viewModelScope.coroutineContext.let { context -> scope.launch(context) { work() } }
                    with(otherScope) { launch { work() } }
                }
            }
        """.trimIndent()

        assertEquals(0, rule.lint(code).size)
    }
}

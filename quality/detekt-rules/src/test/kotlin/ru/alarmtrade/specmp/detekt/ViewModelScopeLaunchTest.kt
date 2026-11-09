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
        assertEquals(true, findings.all { "safeLaunch" in it.message })
    }

    @Test
    fun `does not report safeLaunch, stateIn, shareIn and other scopes`() {
        val code = """
            class FooViewModel : BaseViewModel<FooState, Nothing>(FooState()) {
                val state = flow.stateIn(viewModelScope, SharingStarted.Eagerly, 0)
                val shared = flow.shareIn(scope = viewModelScope, started = SharingStarted.Lazily)
                fun load(scope: CoroutineScope, flow: Flow<Int>) {
                    safeLaunch { work() }
                    safeLaunch(dispatchers.io) { work() }
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
}

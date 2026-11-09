package ru.alarmtrade.specmp.detekt

import dev.detekt.api.Config
import dev.detekt.test.lintWithContext
import dev.detekt.test.utils.createEnvironment
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ViewModelPublicApiTest {
    private val env = createEnvironment()
    private val rule = ViewModelPublicApi(Config.empty)

    private val prelude = """
        package ru.alarmtrade.specmp.core.ui.viewmodel

        import kotlinx.coroutines.Job
        import kotlinx.coroutines.flow.MutableStateFlow
        import kotlinx.coroutines.flow.StateFlow

        abstract class ViewModel

        abstract class BaseViewModel<S : Any, E : Any>(initialState: S) : ViewModel() {
            val state: StateFlow<S> = MutableStateFlow(initialState)
            fun safeLaunch(block: suspend () -> Unit): Job = Job()
            protected fun intent(block: suspend () -> Unit): Job = safeLaunch(block)
            protected open suspend fun onStart() = Unit
            open fun onCleared() = Unit
        }

        class Machine {
            fun retry() = Unit
        }
    """.trimIndent()

    @Test
    fun `reports public properties, suspend functions and functions with a result`() {
        val code = prelude + "\n" + """
            class FooViewModel(val repo: String, internal var flag: Boolean) : BaseViewModel<Int, Nothing>(0) {
                var themeController: String? = null
                internal val extra = 1
                fun widgetFor(id: String): String? = null
                fun consume(count: Int): Boolean = count > 0
                suspend fun load() = Unit
                fun onSave() = safeLaunch { }
                fun onRefresh() = intent { }
                fun retry() = machine?.retry()
                private val machine: Machine? = null
            }

            abstract class AbstractScreenViewModel : BaseViewModel<Int, Nothing>(0)

            class BarViewModel : AbstractScreenViewModel() {
                fun count(): Int = 0
            }
        """.trimIndent()

        assertEquals(11, rule.lintWithContext(env, code).size)
    }

    @Test
    fun `does not report Unit intents, hidden members, overrides and other classes`() {
        val code = prelude + "\n" + """
            class FooViewModel(private val repo: String, plain: Int) : BaseViewModel<Int, Nothing>(0) {
                private val machine: Machine? = null
                protected val shared = 1
                fun onSave() {
                    safeLaunch { }
                }
                fun onClick(): Unit = Unit
                fun retry() {
                    machine?.retry()
                }
                fun onUnit() = Unit
                internal fun onInternal() {}
                private fun compute(): Int = 0
                protected fun helper(): Int = 1
                override suspend fun onStart() = Unit
                override fun onCleared() = Unit
            }

            class PlainViewModel : ViewModel() {
                val bootReady = MutableStateFlow(false)
            }

            class Helper {
                val value = 1
                suspend fun load(): Int = 0
            }
        """.trimIndent()

        assertEquals(0, rule.lintWithContext(env, code).size)
    }
}

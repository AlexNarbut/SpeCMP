package ru.alarmtrade.specmp.detekt

import dev.detekt.api.Config
import dev.detekt.test.lint
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class SafeRunInViewModelBypassTest {
    private val rule = SafeRunInViewModel(Config.empty)

    @Test
    fun `bypass via import alias`() {
        val code = """
            import ru.alarmtrade.specmp.core.common.safeRun as guarded

            class FooViewModel(private val repo: Repo) : BaseViewModel<FooState, Nothing>(FooState()) {
                private suspend fun load() = guarded { repo.load() }
            }
        """.trimIndent()

        assertEquals(1, rule.lint(code).size)
    }

    @Test
    fun `bypass via fully qualified name`() {
        val code = """
            class FooViewModel(private val repo: Repo) : BaseViewModel<FooState, Nothing>(FooState()) {
                private suspend fun load() = ru.alarmtrade.specmp.core.common.safeResponseRun { repo.load() }
            }
        """.trimIndent()

        assertEquals(1, rule.lint(code).size)
    }

    @Test
    fun `bypass via top-level extension function on the ViewModel`() {
        val code = """
            class FooViewModel(val repo: Repo) : BaseViewModel<FooState, Nothing>(FooState())

            private suspend fun FooViewModel.load() = safeRun { repo.load() }
        """.trimIndent()

        assertEquals(1, rule.lint(code).size)
    }

    @Test
    fun `bypass via companion object and anonymous object`() {
        val code = """
            class FooViewModel(private val repo: Repo) : BaseViewModel<FooState, Nothing>(FooState()) {
                private val loader = object : Loader {
                    override suspend fun load() = safeRun { repo.load() }
                }

                companion object {
                    suspend fun parse(raw: String) = safeRun { raw.toInt() }
                }
            }
        """.trimIndent()

        assertEquals(2, rule.lint(code).size)
    }

    @Test
    fun `bypass via lambda property and scope function`() {
        val code = """
            class FooViewModel(private val repo: Repo) : BaseViewModel<FooState, Nothing>(FooState()) {
                private val load: suspend () -> Response<Int> = { safeRun { repo.load() } }

                fun onSave() {
                    intent { repo.run { safeResponseRun { save() } } }
                }
            }
        """.trimIndent()

        assertEquals(2, rule.lint(code).size)
    }
}

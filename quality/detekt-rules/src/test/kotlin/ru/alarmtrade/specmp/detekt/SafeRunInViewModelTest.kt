package ru.alarmtrade.specmp.detekt

import dev.detekt.api.Config
import dev.detekt.test.lint
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class SafeRunInViewModelTest {
    private val rule = SafeRunInViewModel(Config.empty)

    @Test
    fun `reports safeRun in a ViewModel method`() {
        val code = """
            class FooViewModel(private val repo: Repo) : BaseViewModel<FooState, Nothing>(FooState()) {
                private suspend fun load() = safeRun { repo.load() }
            }
        """.trimIndent()

        val findings = rule.lint(code)

        assertEquals(1, findings.size)
        assertEquals(true, "ResponseExt" in findings.single().message)
    }

    @Test
    fun `reports safeRun inside an intent lambda`() {
        val code = """
            class FooViewModel(private val repo: Repo) : BaseViewModel<FooState, Nothing>(FooState()) {
                fun save() {
                    intent {
                        val result = safeRun(log = false) { repo.save() }
                    }
                }
            }
        """.trimIndent()

        assertEquals(1, rule.lint(code).size)
    }

    @Test
    fun `reports safeResponseRun`() {
        val code = """
            class FooViewModel(private val repo: Repo) : BaseViewModel<FooState, Nothing>(FooState()) {
                private suspend fun load() = safeResponseRun { repo.load() }
            }
        """.trimIndent()

        assertEquals(1, rule.lint(code).size)
    }

    @Test
    fun `reports safeRun in a BaseViewModel subclass with another name suffix`() {
        val code = """
            class AppShell(private val repo: Repo) : BaseViewModel<FooState, Nothing>(FooState()) {
                private suspend fun load() = safeRun { repo.load() }
            }
        """.trimIndent()

        assertEquals(1, rule.lint(code).size)
    }

    @Test
    fun `reports safeRun in a nested declaration of a ViewModel`() {
        val code = """
            class FooViewModel : BaseViewModel<FooState, Nothing>(FooState()) {
                private class Loader(private val repo: Repo) {
                    suspend fun load() = safeRun { repo.load() }
                }
            }
        """.trimIndent()

        assertEquals(1, rule.lint(code).size)
    }

    @Test
    fun `does not report safeRun in a repository`() {
        val code = """
            internal class FooRepositoryImpl(private val api: FooApi) : FooRepository {
                override suspend fun load() = safeRun { api.load() }
            }
        """.trimIndent()

        assertEquals(0, rule.lint(code).size)
    }

    @Test
    fun `does not report safeRun in a top-level function`() {
        val code = """
            suspend fun loadFoo(api: FooApi) = safeRun { api.load() }
        """.trimIndent()

        assertEquals(0, rule.lint(code).size)
    }

    @Test
    fun `respects Suppress`() {
        val code = """
            class FooViewModel(private val repo: Repo) : BaseViewModel<FooState, Nothing>(FooState()) {
                @Suppress("SafeRunInViewModel")
                private suspend fun load() = safeRun { repo.load() }
            }
        """.trimIndent()

        assertEquals(0, rule.lint(code).size)
    }

    @Test
    fun `skips direct subclasses of legacy bases`() {
        val code = """
            class FooViewModel(private val repo: Repo) : LegacyBaseViewModel() {
                fun load() { safeRun { repo.load() } }
            }
        """.trimIndent()

        assertEquals(0, rule.lint(code).size)
    }
}

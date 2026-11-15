package ru.alarmtrade.specmp.detekt

import dev.detekt.api.Config
import dev.detekt.test.lint
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ViewModelInitBlockTest {
    private val rule = ViewModelInitBlock(Config.empty)

    @Test
    fun `reports init in a ViewModel`() {
        val code = """
            class FooViewModel : BaseViewModel<FooState, Nothing>(FooState()) {
                init { load() }
            }
        """.trimIndent()

        val findings = rule.lint(code)

        assertEquals(1, findings.size)
        assertEquals(true, "onStart" in findings.single().message)
    }

    @Test
    fun `reports init in a BaseViewModel subclass with another name suffix`() {
        val code = """
            class AppShell(private val repo: Repo) : BaseViewModel<FooState, Nothing>(FooState()) {
                init { repo.start() }
            }
        """.trimIndent()

        assertEquals(1, rule.lint(code).size)
    }

    @Test
    fun `reports init in a generic BaseViewModel subclass`() {
        val code = """
            class AppShell(private val repo: Repo) : BaseViewModel<ShellState, Nothing>(ShellState()) {
                init { repo.start() }
            }
        """.trimIndent()

        assertEquals(1, rule.lint(code).size)
    }

    @Test
    fun `does not report init in a regular class`() {
        val code = """
            class Controller(private val repo: Repo) {
                init { repo.start() }
            }
        """.trimIndent()

        assertEquals(0, rule.lint(code).size)
    }

    @Test
    fun `does not report init in a companion object or nested class of a ViewModel`() {
        val code = """
            class FooViewModel : BaseViewModel<FooState, Nothing>(FooState()) {
                companion object {
                    init { register() }
                }

                class Helper {
                    init { prepare() }
                }
            }
        """.trimIndent()

        assertEquals(0, rule.lint(code).size)
    }

    @Test
    fun `does not report a ViewModel without init`() {
        val code = """
            class FooViewModel : BaseViewModel<FooState, Nothing>(FooState()) {
                override suspend fun onStart() = load()
            }
        """.trimIndent()

        assertEquals(0, rule.lint(code).size)
    }

    @Test
    fun `respects Suppress`() {
        val code = """
            @Suppress("ViewModelInitBlock")
            class FooViewModel : BaseViewModel<FooState, Nothing>(FooState()) {
                init { load() }
            }
        """.trimIndent()

        assertEquals(0, rule.lint(code).size)
    }

    @Test
    fun `bypass via init of an object literal property`() {
        val code = """
            class FooViewModel : BaseViewModel<FooState, Nothing>(FooState()) {
                private val starter = object {
                    init { load() }
                }
            }
        """.trimIndent()

        assertEquals(1, rule.lint(code).size)
    }

    @Test
    fun `bypass via secondary constructor body`() {
        val code = """
            class FooViewModel(repo: Repo) : BaseViewModel<FooState, Nothing>(FooState()) {
                constructor(repo: Repo, eager: Boolean) : this(repo) {
                    repo.start()
                }
            }
        """.trimIndent()

        assertEquals(1, rule.lint(code).size)
    }

    @Test
    fun `bypass via coroutine launched from a property initializer`() {
        val code = """
            class FooViewModel : BaseViewModel<FooState, Nothing>(FooState()) {
                private val loading = intent { load() }
            }
        """.trimIndent()

        assertEquals(1, rule.lint(code).size)
    }

    @Test
    fun `bypass via scope function in a property initializer`() {
        val code = """
            class FooViewModel : BaseViewModel<FooState, Nothing>(FooState()) {
                private val started = run {
                    intent { load() }
                    true
                }
            }
        """.trimIndent()

        assertEquals(1, rule.lint(code).size)
    }

    @Test
    fun `does not report deferred launches in lambdas and lazy delegates`() {
        val code = """
            class FooViewModel : BaseViewModel<FooState, Nothing>(FooState()) {
                private val retry = { intent { load() } }
                private val watch by lazy { intent { observe() } }

                constructor(eager: Boolean) : this()

                fun onRetry() {
                    val job = launchReplacing(KEY) { load() }
                }
            }
        """.trimIndent()

        assertEquals(0, rule.lint(code).size)
    }
}

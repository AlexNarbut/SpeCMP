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
    fun `skips legacy bases and their direct subclasses`() {
        val code = """
            abstract class LegacyBaseViewModel : ViewModel() {
                init { observe() }
            }
            class FooViewModel : LegacyBaseDeviceViewModel(session) {
                init { load() }
            }
        """.trimIndent()

        assertEquals(0, rule.lint(code).size)
    }
}

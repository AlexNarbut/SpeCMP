package ru.alarmtrade.specmp.detekt

import dev.detekt.api.Config
import dev.detekt.test.lint
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ViewModelFileContentsTest {
    private val rule = ViewModelFileContents(Config.empty)

    @Test
    fun `reports a public data class next to a ViewModel`() {
        val code = """
            class FooViewModel : BaseViewModel<FooState, Nothing>(FooState())

            data class FooState(val value: Int)
        """.trimIndent()

        val findings = rule.lint(code)

        assertEquals(1, findings.size)
        assertEquals(
            "`FooState` вынеси в свой файл FooState.kt: файл ViewModel содержит только ViewModel",
            findings.single().message,
        )
    }

    @Test
    fun `reports a private data class next to a ViewModel with the nesting hint`() {
        val code = """
            class FooViewModel : BaseViewModel<FooState, Nothing>(FooState())

            private data class Helper(val value: Int)
        """.trimIndent()

        val findings = rule.lint(code)

        assertEquals(1, findings.size)
        assertEquals(
            "`Helper` перенеси внутрь ViewModel как private вложенный тип: файл ViewModel содержит только ViewModel",
            findings.single().message,
        )
    }

    @Test
    fun `reports declarations next to a BaseViewModel subclass with another name suffix`() {
        val code = """
            class AppShell : BaseViewModel<FooState, Nothing>(FooState())

            enum class Phase { A, B }
            interface Listener
            object Defaults
            typealias Callback = () -> Unit
        """.trimIndent()

        assertEquals(4, rule.lint(code).size)
    }

    @Test
    fun `reports declarations next to a generic BaseViewModel subclass with another name suffix`() {
        val code = """
            class AppShell : BaseViewModel<ShellState, ShellEffect>(ShellState())

            data class ShellState(val value: Int = 0)
        """.trimIndent()

        assertEquals(1, rule.lint(code).size)
    }

    @Test
    fun `does not report top-level functions and properties`() {
        val code = """
            class FooViewModel : BaseViewModel<FooState, Nothing>(FooState())

            private const val LIMIT = 3

            private fun Int.clamp(): Int = coerceAtMost(LIMIT)
        """.trimIndent()

        assertEquals(0, rule.lint(code).size)
    }

    @Test
    fun `does not report a file without a ViewModel`() {
        val code = """
            data class FooState(val value: Int)

            sealed interface FooEffect

            enum class Phase { A, B }
        """.trimIndent()

        assertEquals(0, rule.lint(code).size)
    }

    @Test
    fun `does not report types nested in a ViewModel`() {
        val code = """
            class FooViewModel : BaseViewModel<FooState, Nothing>(FooState()) {
                private data class Helper(val value: Int)
            }
        """.trimIndent()

        assertEquals(0, rule.lint(code).size)
    }

    @Test
    fun `respects file Suppress`() {
        val code = """
            @file:Suppress("ViewModelFileContents")

            class FooViewModel : BaseViewModel<FooState, Nothing>(FooState())

            data class FooState(val value: Int)
        """.trimIndent()

        assertEquals(0, rule.lint(code).size)
    }

    @Test
    fun `skips a file whose ViewModel is on a legacy base`() {
        val code = """
            class FooViewModel : LegacyBaseViewModel()
            data class FooState(val count: Int = 0)
        """.trimIndent()

        assertEquals(0, rule.lint(code).size)
    }
}

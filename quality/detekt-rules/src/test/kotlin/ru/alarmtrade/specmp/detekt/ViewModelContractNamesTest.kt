package ru.alarmtrade.specmp.detekt

import dev.detekt.api.Config
import dev.detekt.test.lint
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ViewModelContractNamesTest {
    private val rule = ViewModelContractNames(Config.empty)

    private fun findings(generics: String) = rule.lint("class FooViewModel : BaseViewModel<$generics>(initial)")

    @Test
    fun `accepts State and Effect`() {
        assertEquals(0, findings("FooState, FooEffect").size)
    }

    @Test
    fun `accepts Nothing as the effect`() {
        assertEquals(0, findings("FooState, Nothing").size)
    }

    @Test
    fun `accepts longer names with the right suffixes`() {
        assertEquals(0, findings("FooUiState, FooSideEffect").size)
    }

    @Test
    fun `accepts qualified and nullable names`() {
        assertEquals(0, findings("org.foo.FooState?, org.foo.FooEffect").size)
    }

    @Test
    fun `reports a state type without the State suffix`() {
        val result = findings("FooUi, FooEffect")

        assertEquals(1, result.size)
        assertEquals(
            "первый дженерик BaseViewModel — тип состояния, имя должно оканчиваться на State (docs/patterns.md): `FooUi`",
            result.single().message,
        )
    }

    @Test
    fun `reports an effect type without the Effect suffix`() {
        val result = findings("FooState, FooEvent")

        assertEquals(1, result.size)
        assertEquals(true, "`FooEvent`" in result.single().message)
    }

    @Test
    fun `reports both arguments`() {
        assertEquals(2, findings("Foo, Bar").size)
    }

    @Test
    fun `does not check other bases`() {
        assertEquals(0, rule.lint("class FooViewModel : ViewModel()").size)
    }

    @Test
    fun `respects Suppress`() {
        val code = """
            @Suppress("ViewModelContractNames")
            class FooViewModel : BaseViewModel<Foo, Bar>(initial)
        """.trimIndent()

        assertEquals(0, rule.lint(code).size)
    }
}

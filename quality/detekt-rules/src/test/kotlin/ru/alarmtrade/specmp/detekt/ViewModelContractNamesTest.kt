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
    fun `checks BaseDeviceViewModel generics too`() {
        val code = "class FooViewModel(session: DeviceSession) : BaseDeviceViewModel<Foo, FooEffect>(session, initial)"

        assertEquals(1, rule.lint(code).size)
    }

    @Test
    fun `skips own type parameters of an intermediate base`() {
        val code = "abstract class BaseDeviceViewModel<S : Any, E : Any>(initial: S) : BaseViewModel<S, E>(initial)"

        assertEquals(0, rule.lint(code).size)
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

    @Test
    fun `bypass via intermediate generic base view model`() {
        val code = """
            abstract class ScreenViewModel<S : Any, E : Any>(initial: S) : BaseViewModel<S, E>(initial)

            class FooViewModel : ScreenViewModel<FooData, FooEvent>(FooData())
        """.trimIndent()

        assertEquals(2, rule.lint(code).size)
    }

    @Test
    fun `bypass via import alias of the base class`() {
        val code = """
            import ru.alarmtrade.specmp.core.ui.viewmodel.BaseViewModel as Base

            class FooViewModel : Base<FooData, Nothing>(FooData())
        """.trimIndent()

        assertEquals(1, rule.lint(code).size)
    }

    @Test
    fun `bypass via import alias that renames the type argument`() {
        val code = """
            import ru.alarmtrade.specmp.feature.foo.FooData as FooState
            import ru.alarmtrade.specmp.feature.foo.FooEvent as FooEffect

            class FooViewModel : BaseViewModel<FooState, FooEffect>(FooState())
        """.trimIndent()

        assertEquals(2, rule.lint(code).size)
    }

    @Test
    fun `bypass via object declaration`() {
        assertEquals(1, rule.lint("object FooViewModel : BaseViewModel<FooData, Nothing>(FooData())").size)
    }

    @Test
    fun `bypass via anonymous object`() {
        val code = """
            val viewModel = object : BaseDeviceViewModel<FooData, FooEvent>(session, FooData()) {}
        """.trimIndent()

        assertEquals(2, rule.lint(code).size)
    }

    @Test
    fun `does not report generic bases that are not contracts`() {
        val code = """
            class FooViewModel : BaseViewModel<FooState, Nothing>(FooState()), KeyedViewModel<String>
        """.trimIndent()

        assertEquals(0, rule.lint(code).size)
    }
}

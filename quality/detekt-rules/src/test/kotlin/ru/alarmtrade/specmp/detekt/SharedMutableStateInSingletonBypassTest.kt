package ru.alarmtrade.specmp.detekt

import dev.detekt.api.Config
import dev.detekt.test.lint
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class SharedMutableStateInSingletonBypassTest {
    private val rule = SharedMutableStateInSingleton(Config.empty)

    @Test
    fun `bypass via companion object of a data service`() {
        val code = """
            package ru.alarmtrade.specmp.feature.recipes.data.repository

            internal class RecipeRepositoryImpl : RecipeRepository {
                companion object {
                    private var cached: String? = null
                    private val ids = mutableSetOf<String>()
                }
            }
        """.trimIndent()

        assertEquals(2, rule.lint(code).size)
    }

    @Test
    fun `bypass via nested object of a class owning a scope`() {
        val code = """
            package ru.alarmtrade.specmp.feature.sleep.presentation

            class WidgetProvider(private val scope: CoroutineScope) {
                private object Cache {
                    var last: Int = 0
                }
            }
        """.trimIndent()

        assertEquals(1, rule.lint(code).size)
    }

    @Test
    fun `bypass via fully qualified scope type`() {
        val code = """
            package ru.alarmtrade.specmp.feature.sleep.presentation

            class WidgetProvider(private val scope: kotlinx.coroutines.CoroutineScope) {
                private var flag = false
            }
        """.trimIndent()

        assertEquals(1, rule.lint(code).size)
    }

    @Test
    fun `bypass via MainScope factory`() {
        val code = """
            package ru.alarmtrade.specmp.feature.sleep.presentation

            class WidgetProvider {
                private val scope = MainScope()
                private val pending: MutableList<Int> = ArrayList()
            }
        """.trimIndent()

        assertEquals(1, rule.lint(code).size)
    }

    @Test
    fun `bypass via mutable collection hidden behind a read-only type`() {
        val code = """
            package ru.alarmtrade.specmp.feature.recipes.data.repository

            internal class RecipeRepositoryImpl : RecipeRepository {
                private val ids: Set<String> = hashSetOf()
                private val names: Collection<String> = java.util.ArrayList()
            }
        """.trimIndent()

        assertEquals(2, rule.lint(code).size)
    }

    @Test
    fun `bypass via lateinit and nullable scope in object singleton`() {
        val code = """
            package ru.alarmtrade.specmp.feature.sleep.presentation

            object Bridge {
                private val scope: CoroutineScope? = null
                lateinit var listener: () -> Unit
            }
        """.trimIndent()

        assertEquals(1, rule.lint(code).size)
    }
}

package ru.alarmtrade.specmp.detekt

import dev.detekt.api.Config
import dev.detekt.test.lint
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class SharedMutableStateInSingletonTest {
    private val rule = SharedMutableStateInSingleton(Config.empty)

    @Test
    fun `reports var and mutable collections in classes owning a scope`() {
        val code = """
            package ru.alarmtrade.specmp.feature.sleep.presentation

            class WidgetProvider(private val scope: CoroutineScope, var flag: Boolean) {
                private var cache: String? = null
                private lateinit var name: String
                private val ids = mutableSetOf<String>()
                private val map: HashMap<String, Int> = HashMap()
            }

            object Bridge {
                private val scope = CoroutineScope(SupervisorJob())
                private var current: Job? = null
            }
        """.trimIndent()

        assertEquals(6, rule.lint(code).size)
    }

    @Test
    fun `reports var in data services by name`() {
        val code = """
            package ru.alarmtrade.specmp.feature.recipes.data.repository

            internal class RecipeRepositoryImpl : RecipeRepository {
                private var cached: String? = null
            }

            internal class ContentSyncCoordinatorImpl {
                private val pending = mutableListOf<Int>()
            }

            class AuthSession {
                var token: String? = null
            }
        """.trimIndent()

        assertEquals(3, rule.lint(code).size)
    }

    @Test
    fun `does not report MutableStateFlow, atomics, locals and non-shared classes`() {
        val code = """
            package ru.alarmtrade.specmp.feature.recipes.data.repository

            internal class RecipeRepositoryImpl(private val scope: CoroutineScope) : RecipeRepository {
                private val cached = MutableStateFlow<String?>(null)
                private val started = MutableStateFlow(false)
                private val ids: Set<String> = emptySet()

                fun build(): List<Int> {
                    var total = 0
                    val items = mutableListOf<Int>()
                    items += total
                    return items
                }

                companion object {
                    var counter = 0
                }
            }

            class RecipeMapper {
                var scratch = 0
            }

            data class RecipeDto(var title: String)
        """.trimIndent()

        assertEquals(0, rule.lint(code).size)
    }

    @Test
    fun `does not report service names outside data`() {
        val code = """
            package ru.alarmtrade.specmp.feature.recipes.presentation

            class RecipesController {
                private var selected: String? = null
            }
        """.trimIndent()

        assertEquals(0, rule.lint(code).size)
    }
}

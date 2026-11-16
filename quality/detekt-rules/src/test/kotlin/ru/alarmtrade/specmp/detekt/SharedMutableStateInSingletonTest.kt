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
                    const val COUNTER = 0
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
    fun `reports service names in any package`() {
        val code = """
            package ru.alarmtrade.specmp.feature.recipes.presentation

            class RecipesController {
                private var selected: String? = null
            }

            class DownloadManager {
                private var attempts = 0
            }

            class SyncStarter {
                private val started = mutableListOf<String>()
            }

            internal class ChatNotifier(private val title: String) {
                private var unread = 0
            }

            class PermissionsProvider {
                private var cached: Boolean? = null
            }

            class LocaleStore(var locale: String)

            internal class ChatDataSource {
                private val pending = mutableMapOf<String, Int>()
            }
        """.trimIndent()

        assertEquals(7, rule.lint(code).size)
    }

    @Test
    fun `does not report Impl outside data and service interfaces`() {
        val code = """
            package ru.alarmtrade.specmp.feature.recipes.presentation

            class RecipeFormatterImpl {
                private var buffer = ""
            }

            interface RecipesManager {
                var selected: String?
            }
        """.trimIndent()

        assertEquals(0, rule.lint(code).size)
    }
}

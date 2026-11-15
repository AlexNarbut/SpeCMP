package ru.alarmtrade.specmp.detekt

import dev.detekt.api.Config
import dev.detekt.test.lintWithContext
import dev.detekt.test.utils.createEnvironment
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class UpdateLambdaSideEffectsTest {
    private val env = createEnvironment()
    private val rule = UpdateLambdaSideEffects(Config.empty)

    private val prelude = """
        import kotlinx.coroutines.CoroutineScope
        import kotlinx.coroutines.flow.MutableSharedFlow
        import kotlinx.coroutines.flow.MutableStateFlow
        import kotlinx.coroutines.flow.getAndUpdate
        import kotlinx.coroutines.flow.update
        import kotlinx.coroutines.flow.updateAndGet
        import kotlinx.coroutines.launch

        data class State(val items: List<Int> = emptyList(), val count: Int = 0)

        interface ItemsRepository {
            fun peek(): Int
            suspend fun load(): Int
        }

        interface Settings {
            fun putString(key: String, value: String)
            fun getString(key: String): String
        }
    """.trimIndent()

    @Test
    fun `reports side effects inside update lambdas`() {
        val code = prelude + "\n" + """
            class Holder(private val repository: ItemsRepository, private val scope: CoroutineScope) {
                private val state = MutableStateFlow(State())
                private val effects = MutableSharedFlow<Int>(extraBufferCapacity = 1)

                suspend fun mutate() {
                    state.update { it.copy(count = repository.load()) }
                    state.update { it.copy(count = repository.peek()) }
                    state.getAndUpdate {
                        effects.tryEmit(1)
                        it
                    }
                    state.updateAndGet {
                        scope.launch { }
                        it
                    }
                    state.update { current -> current.copy(items = current.items.map { repository.peek() }) }
                }
            }
        """.trimIndent()

        assertEquals(5, rule.lintWithContext(env, code).size)
    }

    @Test
    fun `reports storage writes, directly and through own helpers`() {
        val code = prelude + "\n" + """
            class HistoryRepositoryImpl(private val settings: Settings) {
                private val history = MutableStateFlow(emptyList<String>())

                fun push(query: String) {
                    history.update { (listOf(query) + it).also { list -> persist(list) } }
                    history.update { current -> current.also { settings.putString("k", it.joinToString()) } }
                }

                private fun persist(list: List<String>) {
                    settings.putString("k", list.joinToString())
                }
            }
        """.trimIndent()

        assertEquals(2, rule.lintWithContext(env, code).size)
    }

    @Test
    fun `does not report pure own helpers and storage reads`() {
        val code = prelude + "\n" + """
            class ThemeController(private val settings: Settings) {
                private val mode = MutableStateFlow("light")
                private val current = MutableStateFlow("light")

                fun refresh() {
                    current.update { resolve() }
                    current.update { settings.getString("k") }
                }

                private fun resolve(): String = mode.value.uppercase()
            }
        """.trimIndent()

        assertEquals(0, rule.lintWithContext(env, code).size)
    }

    @Test
    fun `does not report pure update lambdas and effects outside`() {
        val code = prelude + "\n" + """
            class Holder(private val repository: ItemsRepository) {
                private val state = MutableStateFlow(State())
                private val effects = MutableSharedFlow<Int>(extraBufferCapacity = 1)

                suspend fun mutate() {
                    val loaded = repository.load()
                    state.update { it.copy(count = loaded, items = it.items.filter { item -> item > 0 }) }
                    val previous = state.getAndUpdate { it.copy(count = it.count + 1) }
                    effects.tryEmit(previous.count)
                    state.update { current ->
                        val handler = { repository.peek() }
                        current.copy(count = current.count + 1)
                    }
                }
            }

            class OtherHolder {
                fun update(block: () -> Unit) = block()
                suspend fun suspending() = Unit
            }
        """.trimIndent()

        assertEquals(0, rule.lintWithContext(env, code).size)
    }

    @Test
    fun `bypass via import alias of update`() {
        val code = """
            import kotlinx.coroutines.flow.MutableStateFlow
            import kotlinx.coroutines.flow.update as mutate

            interface ItemsRepository {
                fun peek(): Int
            }

            class Holder(private val repository: ItemsRepository) {
                private val state = MutableStateFlow(0)

                fun mutate() {
                    state.mutate { repository.peek() }
                }
            }
        """.trimIndent()

        assertEquals(1, rule.lintWithContext(env, code).size)
    }

    @Test
    fun `bypass via named lambda argument`() {
        val code = prelude + "\n" + """
            class Holder(private val repository: ItemsRepository) {
                private val state = MutableStateFlow(State())

                fun mutate() {
                    state.update(function = { it.copy(count = repository.peek()) })
                }
            }
        """.trimIndent()

        assertEquals(1, rule.lintWithContext(env, code).size)
    }

    @Test
    fun `bypass via also on the new value`() {
        val code = prelude + "\n" + """
            class Holder {
                private val state = MutableStateFlow(State())
                private val effects = MutableSharedFlow<Int>(extraBufferCapacity = 1)

                fun mutate() {
                    state.update { it.copy(count = 1).also { effects.tryEmit(1) } }
                }
            }
        """.trimIndent()

        assertEquals(1, rule.lintWithContext(env, code).size)
    }

    @Test
    fun `bypass via with on the repository`() {
        val code = prelude + "\n" + """
            class Holder(private val repository: ItemsRepository) {
                private val state = MutableStateFlow(State())

                fun mutate() {
                    state.update { current -> with(repository) { current.copy(count = peek()) } }
                }
            }
        """.trimIndent()

        assertEquals(1, rule.lintWithContext(env, code).size)
    }

    @Test
    fun `bypass via let on the storage`() {
        val code = prelude + "\n" + """
            class Holder(private val settings: Settings) {
                private val state = MutableStateFlow(State())

                fun mutate() {
                    state.getAndUpdate { current -> settings.let { it.putString("k", "v") }; current }
                }
            }
        """.trimIndent()

        assertEquals(1, rule.lintWithContext(env, code).size)
    }
}

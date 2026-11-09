package ru.alarmtrade.specmp.detekt

import dev.detekt.api.Config
import dev.detekt.test.lintWithContext
import dev.detekt.test.utils.createEnvironment
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class RawFlowFromRepositoryTest {
    private val env = createEnvironment()
    private val rule = RawFlowFromRepository(Config.empty)

    private val prelude = """
        package ru.alarmtrade.specmp.core.common

        import kotlinx.coroutines.flow.Flow
        import kotlinx.coroutines.flow.MutableStateFlow
        import kotlinx.coroutines.flow.SharedFlow
        import kotlinx.coroutines.flow.StateFlow
        import kotlinx.coroutines.flow.flowOf

        sealed class Response<out T> {
            data class Success<out T>(val value: T) : Response<T>()
            class Error(val exception: Throwable) : Response<Nothing>()
        }

        interface NotesRepository {
            fun observeNotes(): Flow<List<String>>
            val state: StateFlow<Int>
        }
        interface BaseRepository
        interface AuthRepository : BaseRepository
    """.trimIndent()

    @Test
    fun `reports public raw flows of classes implementing a Repository`() {
        val code = prelude + "\n" + """
            internal class NotesRepositoryImpl(val source: Flow<Int>, private val hidden: Flow<Int>) : NotesRepository {
                override fun observeNotes(): Flow<List<String>> = flowOf(emptyList())
                override val state: StateFlow<Int> = MutableStateFlow(0)
                fun extra(): SharedFlow<String> = MutableStateFlow("")
                val inferred get() = flowOf(1)
            }

            // Имя класса не важно, репозиторий может быть унаследован транзитивно.
            object NoOpAuthRepository : AuthRepository {
                val events: Flow<Unit> = flowOf(Unit)
            }
        """.trimIndent()

        assertEquals(6, rule.lintWithContext(env, code).size)
    }

    @Test
    fun `does not report Response flows, private members, non-flows and non-repositories`() {
        val code = prelude + "\n" + """
            interface GoodRepository {
                fun observe(): Flow<Response<String>>
            }

            class GoodRepositoryImpl(private val raw: Flow<Int>, source: Flow<Int>) : GoodRepository {
                override fun observe(): Flow<Response<String>> = flowOf(Response.Success(""))
                val state: StateFlow<Response.Success<Int>> = MutableStateFlow(Response.Success(1))
                private val cache: Flow<Int> = flowOf(1)
                private fun raw(): Flow<Int> = flowOf(1)
                fun count(): Int = 0
                suspend fun load(): Response<Unit> = Response.Success(Unit)
            }

            class NotesViewModel {
                val state: StateFlow<Int> = MutableStateFlow(0)
            }

            abstract class AbstractRepository {
                val raw: Flow<Int> = flowOf(1)
            }
            class NotARepository : AbstractRepository()
        """.trimIndent()

        assertEquals(0, rule.lintWithContext(env, code).size)
    }
}

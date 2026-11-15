package ru.alarmtrade.specmp.detekt

import dev.detekt.api.Config
import dev.detekt.test.lintWithContext
import dev.detekt.test.utils.createEnvironment
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class RawFlowFromRepositoryBypassTest {
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
            val state: StateFlow<Response<Int>>
        }
    """.trimIndent()

    @Test
    fun `bypass via typealias of Flow`() {
        val code = prelude + "\n" + """
            typealias Stream<T> = Flow<T>

            class NotesRepositoryImpl : NotesRepository {
                override fun observeNotes(): Stream<List<String>> = flowOf(emptyList())
                override val state: StateFlow<Response<Int>> = MutableStateFlow(Response.Success(0))
            }
        """.trimIndent()

        assertEquals(1, rule.lintWithContext(env, code).size)
    }

    @Test
    fun `bypass via anonymous object`() {
        val code = prelude + "\n" + """
            fun notes(): NotesRepository = object : NotesRepository {
                override fun observeNotes(): Flow<List<String>> = flowOf(emptyList())
                override val state: StateFlow<Response<Int>> = MutableStateFlow(Response.Success(0))
            }
        """.trimIndent()

        assertEquals(1, rule.lintWithContext(env, code).size)
    }

    @Test
    fun `bypass via intermediate interface`() {
        val code = prelude + "\n" + """
            interface CachedNotes : NotesRepository

            class Notes : CachedNotes {
                override fun observeNotes(): Flow<List<String>> = flowOf(emptyList())
                override val state: StateFlow<Response<Int>> = MutableStateFlow(Response.Success(0))
                fun extra(): SharedFlow<Int> = MutableStateFlow(0)
            }
        """.trimIndent()

        assertEquals(2, rule.lintWithContext(env, code).size)
    }

    @Test
    fun `bypass via inferred return type and star projection`() {
        val code = prelude + "\n" + """
            class Notes : NotesRepository {
                override fun observeNotes() = flowOf(emptyList<String>())
                override val state: StateFlow<Response<Int>> = MutableStateFlow(Response.Success(0))
                fun any(): Flow<*> = flowOf(1)
            }
        """.trimIndent()

        assertEquals(2, rule.lintWithContext(env, code).size)
    }

    @Test
    fun `bypass via nullable Response element`() {
        val code = prelude + "\n" + """
            class Notes : NotesRepository {
                override fun observeNotes(): Flow<List<String>> = flowOf(emptyList())
                override val state: StateFlow<Response<Int>> = MutableStateFlow(Response.Success(0))
                fun maybe(): Flow<Response<Int>?> = flowOf(null)
            }
        """.trimIndent()

        assertEquals(2, rule.lintWithContext(env, code).size)
    }

    @Test
    fun `does not report Response flows behind aliases and private raw flows`() {
        val code = prelude + "\n" + """
            typealias Stream<T> = Flow<Response<T>>

            interface CleanRepository

            class Notes : CleanRepository {
                val state: StateFlow<Response<Int>> = MutableStateFlow(Response.Success(0))
                fun all(): Stream<Int> = flowOf(Response.Success(1))
                private fun raw(): Flow<Int> = flowOf(1)
            }
        """.trimIndent()

        assertEquals(0, rule.lintWithContext(env, code).size)
    }
}

package ru.alarmtrade.specmp.detekt

import dev.detekt.api.Config
import dev.detekt.test.lint
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class RepositoryWithContextBypassTest {
    private val rule = RepositoryWithContext(Config.empty)

    @Test
    fun `bypass via import alias`() {
        val code = """
            import kotlinx.coroutines.withContext as onIo

            internal class FooRepositoryImpl(private val dispatchers: AppDispatchers) : FooRepository {
                override suspend fun load() = onIo(dispatchers.io) { read() }
            }
        """.trimIndent()

        assertEquals(1, rule.lint(code).size)
    }

    @Test
    fun `bypass via fully qualified name`() {
        val code = """
            internal class FooRepositoryImpl(private val dispatchers: AppDispatchers) : FooRepository {
                override suspend fun load() = kotlinx.coroutines.withContext(dispatchers.io) { read() }
            }
        """.trimIndent()

        assertEquals(1, rule.lint(code).size)
    }

    @Test
    fun `bypass via top-level extension function on the repository`() {
        val code = """
            internal class FooRepositoryImpl(val dispatchers: AppDispatchers) : FooRepository

            private suspend fun FooRepositoryImpl.readOnIo() = withContext(dispatchers.io) { read() }
        """.trimIndent()

        assertEquals(1, rule.lint(code).size)
    }

    @Test
    fun `bypass via companion object of the repository`() {
        val code = """
            internal class FooRepositoryImpl : FooRepository {
                companion object {
                    suspend fun parse(io: CoroutineDispatcher, raw: String) = withContext(io) { raw.toInt() }
                }
            }
        """.trimIndent()

        assertEquals(1, rule.lint(code).size)
    }

    @Test
    fun `bypass via local function and scope function lambda`() {
        val code = """
            internal class FooRemoteDataSource(private val dispatchers: AppDispatchers) {
                suspend fun load(id: String): Int {
                    suspend fun fetch() = withContext(context = dispatchers.io) { id.toInt() }
                    return id.let { withContext(dispatchers.default) { fetch() } }
                }
            }
        """.trimIndent()

        assertEquals(2, rule.lint(code).size)
    }

    @Test
    fun `bypass via interface default method`() {
        val code = """
            interface FooRepository {
                suspend fun load(io: CoroutineDispatcher) = withContext(io) { 1 }
            }
        """.trimIndent()

        assertEquals(1, rule.lint(code).size)
    }
}

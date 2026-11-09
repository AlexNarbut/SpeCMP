package ru.alarmtrade.specmp.detekt

import dev.detekt.api.Config
import dev.detekt.test.lint
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ThrowInRepositoryTest {
    private val rule = ThrowInRepository(Config.empty)

    @Test
    fun `reports throwing outside the Response boundary in repositories and use cases`() {
        val code = """
            internal class AccountRepositoryImpl : AccountRepository {
                override suspend fun load(): Response<Parent> = safeRun { activate() }
                private fun activate(): Parent = checkNotNull(read()) { "missing" }
                fun pack(): Pack = cached ?: error("empty")
                fun parse(raw: String?): Int = raw!!.length
                fun save(id: String) {
                    require(id.isNotBlank())
                    scope.launch { throw IllegalStateException("boom") }
                }
                fun rethrow() = try { write() } catch (e: IOException) { throw e }
                fun observe(): Flow<Int> = source.map { requireNotNull(it) }
            }
            private fun Row.toModel(): Model = Model(kind = Kind.entries.find { it.name == kind } ?: error("unknown"))
            class GetSleepUseCase {
                operator fun invoke(id: String): Int { check(id.isNotEmpty()); return 1 }
            }
        """.trimIndent()

        assertEquals(9, rule.lint(code).size)
    }

    @Test
    fun `reports Specialist repository implementations`() {
        val code = """
            internal class SpecialistLoginRepository(private val sdk: Sdk) : LoginRepository {
                override suspend fun login(): Response<Unit> = safeRun { sdk.login()!! }
                fun token(): String = sdk.token() ?: error("no token")
            }
        """.trimIndent()

        assertEquals(1, rule.lint(code).size)
    }

    @Test
    fun `allows throwing inside safeRun, safeResponseRun, asResponseFlow chains and qualified check calls`() {
        val code = """
            internal class AuthRepositoryImpl : AuthRepository {
                override suspend fun send(): Response<String> = safeRun {
                    val token = client.token() ?: error("No token")
                    token.let { requireNotNull(it) }
                }
                override suspend fun pack(): Response<Pack> = safeResponseRun(log = false) {
                    Response.Success(cached ?: error("empty"))
                }
                override fun observe(): Flow<Response<List<Event>>> = queries.all()
                    .asFlow()
                    .map { rows -> rows.map { row -> row.toEvent(action = row.action ?: error("unknown")) } }
                    .asResponseFlow()
                fun validate(input: Input): Boolean = validator.check(input)
            }
        """.trimIndent()

        assertEquals(0, rule.lint(code).size)
    }

    @Test
    fun `allows rethrowing CancellationException`() {
        val code = """
            internal class ContentSyncRepositoryImpl : ContentSyncRepository {
                fun parse(dto: Dto): Recipe? = try {
                    dto.toDomain()
                } catch (ce: CancellationException) {
                    throw ce
                } catch (e: IllegalArgumentException) {
                    null
                }
                fun recognize(): Flow<Response<Int>> = source.catch { e ->
                    if (e is CancellationException) throw e
                    emit(Response.Error(e))
                }
                fun wrong(): Flow<Int> = source.catch { e ->
                    if (e !is CancellationException) throw e
                }
            }
        """.trimIndent()

        assertEquals(1, rule.lint(code).size)
    }

    @Test
    fun `does not report files without repository implementations and use cases`() {
        val code = """
            class SleepViewModel : BaseViewModel<SleepState, Nothing>() {
                fun save(id: String) { require(id.isNotBlank()); val x = cached!!; error("boom") }
            }
            class WeekAggregator { fun aggregate(days: Int) = require(days > 0) }
            interface SleepRepository { suspend fun load(): Response<Int> }
        """.trimIndent()

        assertEquals(0, rule.lint(code).size)
    }
}

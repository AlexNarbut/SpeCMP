package ru.alarmtrade.specmp.detekt

import dev.detekt.api.Config
import dev.detekt.test.lintWithContext
import dev.detekt.test.utils.createEnvironment
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class RedundantSafeRunBypassTest {
    private val env = createEnvironment()
    private val rule = RedundantSafeRun(Config.empty)

    private fun file(imports: String = "", body: String) = """
        package ru.alarmtrade.specmp.core.common

        $imports

        sealed class Response<out T> {
            data class Success<out T>(val value: T) : Response<T>()
            class Error(val exception: Throwable) : Response<Nothing>()
        }

        inline fun <R> safeRun(log: Boolean = true, block: () -> R): Response<R> = Response.Success(block())
        inline fun <R> safeResponseRun(log: Boolean = true, block: () -> Response<R>): Response<R> = block()

        interface UserRepository {
            fun fetchUser(id: String): Response<String>
        }
    """.trimIndent() + "\n" + body.trimIndent()

    @Test
    fun `bypass via lambda passed as named argument`() {
        val code = file(
            body = """
                class Sample(private val repository: UserRepository, private val id: String) {
                    fun a() = safeRun(block = { repository.fetchUser(id) })
                    fun b() = safeResponseRun(log = false, block = { repository.fetchUser(id) })
                }
            """,
        )

        assertEquals(2, rule.lintWithContext(env, code).size)
    }

    @Test
    fun `bypass via labeled return`() {
        val code = file(
            body = """
                class Sample(private val repository: UserRepository, private val id: String) {
                    fun a() = safeRun { return@safeRun repository.fetchUser(id) }
                    fun b() = safeResponseRun { return@safeResponseRun repository.fetchUser(id) }
                }
            """,
        )

        assertEquals(2, rule.lintWithContext(env, code).size)
    }

    @Test
    fun `bypass via import alias`() {
        val code = file(
            imports = "import ru.alarmtrade.specmp.core.common.safeRun as guarded",
            body = """
                class Sample(private val repository: UserRepository, private val id: String) {
                    fun a() = guarded { repository.fetchUser(id) }
                }
            """,
        )

        assertEquals(1, rule.lintWithContext(env, code).size)
    }

    @Test
    fun `bypass via parentheses around the single call`() {
        val code = file(
            body = """
                class Sample(private val repository: UserRepository, private val id: String) {
                    fun a() = safeResponseRun { (repository.fetchUser(id)) }
                    fun b() = safeRun { ((repository).fetchUser(id)) }
                }
            """,
        )

        assertEquals(2, rule.lintWithContext(env, code).size)
    }

    @Test
    fun `bypass via scope function returning Response`() {
        val code = file(
            body = """
                class Sample(private val repository: UserRepository, private val id: String) {
                    fun a() = safeRun { repository.run { fetchUser(id) } }
                    fun b() = safeRun { id.let(repository::fetchUser) }
                }
            """,
        )

        assertEquals(2, rule.lintWithContext(env, code).size)
    }
}

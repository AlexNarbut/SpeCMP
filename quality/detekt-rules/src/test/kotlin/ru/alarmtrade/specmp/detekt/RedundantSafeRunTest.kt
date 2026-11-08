package ru.alarmtrade.specmp.detekt

import dev.detekt.api.Config
import dev.detekt.test.lintWithContext
import dev.detekt.test.utils.createEnvironment
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class RedundantSafeRunTest {
    private val env = createEnvironment()
    private val rule = RedundantSafeRun(Config.empty)

    private val prelude = """
        package ru.alarmtrade.specmp.core.common

        sealed class Response<out T> {
            data class Success<out T>(val value: T) : Response<T>()
            class Error(val exception: Throwable) : Response<Nothing>()
        }

        inline fun <R> safeRun(log: Boolean = true, block: () -> R): Response<R> = Response.Success(block())
        inline fun <R> safeResponseRun(log: Boolean = true, block: () -> Response<R>): Response<R> = block()

        interface UserRepository {
            fun fetchUser(id: String): Response<String>
        }
        class Api { fun load(id: String): String = id }
        fun String.toResponse(): Response<String> = Response.Success(this)
    """.trimIndent()

    @Test
    fun `reports safeRun around Response and safeResponseRun around a single Response call`() {
        val code = prelude + "\n" + """
            class Sample(private val repository: UserRepository, private val id: String) {
                fun a() = safeRun { repository.fetchUser(id) }
                fun b() = safeRun(log = false) { repository.fetchUser("x") }
                fun c() = safeResponseRun { repository.fetchUser(id) }
                fun d() = safeResponseRun { this.repository.fetchUser(id) }
            }
        """.trimIndent()

        assertEquals(4, rule.lintWithContext(env, code).size)
    }

    @Test
    fun `does not report throwing code inside safeResponseRun and plain values in safeRun`() {
        val code = prelude + "\n" + """
            class Sample(private val repository: UserRepository, private val api: Api, private val id: String) {
                fun a() = safeRun { api.load(id) }
                fun b() = safeResponseRun { api.load(id).toResponse() }
                fun c() = safeResponseRun { repository.fetchUser(api.load(id)) }
                fun d() = safeResponseRun {
                    val user = api.load(id)
                    repository.fetchUser(user)
                }
                fun e() = safeResponseRun { repository.fetchUser(id!!) }
                fun f(block: (String) -> Response<String>) = safeResponseRun { block(id) }
                fun g(block: (String) -> Response<String>) = safeResponseRun { block.invoke(id) }
                fun h() = listOf<(String) -> Response<String>>().map { safeResponseRun { it(id) } }
                fun i(): Response<String> {
                    val load: (String) -> Response<String> = { repository.fetchUser(it) }
                    return safeResponseRun { load(id) }
                }
            }
        """.trimIndent()

        assertEquals(0, rule.lintWithContext(env, code).size)
    }
}

package ru.alarmtrade.specmp.detekt

import dev.detekt.api.Config
import dev.detekt.test.lintWithContext
import dev.detekt.test.utils.createEnvironment
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ResponseIgnoredBypassTest {
    private val env = createEnvironment()
    private val rule = ResponseIgnored(Config.empty)

    private fun file(body: String) = """
        package ru.alarmtrade.specmp.core.common

        sealed class Response<out T> {
            data class Success<out T>(val value: T) : Response<T>()
            class Error(val exception: Throwable) : Response<Nothing>()
        }

        inline fun <T> Response<T>.onError(action: (Throwable) -> Unit): Response<T> = this
        inline fun <T> Response<T>.onSuccess(action: (T) -> Unit): Response<T> = this
        fun log(message: String) = Unit

        interface Repository {
            fun save(id: String): Response<Unit>
            fun peek(): Response<Int>
        }
    """.trimIndent() + "\n" + body.trimIndent()

    @Test
    fun `bypass via forEach lambda coerced to Unit`() {
        val code = file(
            """
                fun a(repository: Repository, ids: List<String>) {
                    ids.forEach { repository.save(it) }
                }
            """,
        )

        assertEquals(1, rule.lintWithContext(env, code).size)
    }

    @Test
    fun `bypass via callable reference coerced to Unit`() {
        val code = file(
            """
                fun a(repository: Repository, ids: List<String>) {
                    ids.forEach(repository::save)
                }
            """,
        )

        assertEquals(1, rule.lintWithContext(env, code).size)
    }

    @Test
    fun `bypass via scope functions as statements`() {
        val code = file(
            """
                fun a(repository: Repository) {
                    with(repository) { peek() }
                    repository.run { save("x") }
                    log("after")
                }
            """,
        )

        assertEquals(2, rule.lintWithContext(env, code).size)
    }

    @Test
    fun `bypass via if, when and try statements`() {
        val code = file(
            """
                fun a(repository: Repository, flag: Boolean, n: Int) {
                    if (flag) repository.peek() else repository.save("x")
                    when (n) {
                        1 -> repository.peek()
                        else -> log("other")
                    }
                    try {
                        repository.save("y")
                    } catch (e: IllegalStateException) {
                        log("failed")
                    }
                    log("after")
                }
            """,
        )

        assertEquals(4, rule.lintWithContext(env, code).size)
    }

    @Test
    fun `bypass via onSuccess chain without onError`() {
        val code = file(
            """
                fun a(repository: Repository) {
                    repository.peek().onSuccess { log(it.toString()) }
                    log("after")
                }
            """,
        )

        assertEquals(1, rule.lintWithContext(env, code).size)
    }

    @Test
    fun `does not report callable references that keep the Response`() {
        val code = file(
            """
                fun a(repository: Repository, ids: List<String>): List<Response<Unit>> = ids.map(repository::save)
            """,
        )

        assertEquals(0, rule.lintWithContext(env, code).size)
    }
}

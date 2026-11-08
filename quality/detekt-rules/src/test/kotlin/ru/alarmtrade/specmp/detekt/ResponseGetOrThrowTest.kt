package ru.alarmtrade.specmp.detekt

import dev.detekt.api.Config
import dev.detekt.test.lintWithContext
import dev.detekt.test.utils.createEnvironment
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ResponseGetOrThrowTest {
    private val env = createEnvironment()
    private val rule = ResponseGetOrThrow(Config.empty)

    private val prelude = """
        package ru.alarmtrade.specmp.core.common

        sealed class Response<out T> {
            data class Success<out T>(val value: T) : Response<T>()
            class Error(val exception: Throwable) : Response<Nothing>()
        }

        fun <T> Response<T>.getOrThrow(): T = when (this) {
            is Response.Success -> value
            is Response.Error -> throw exception
        }
        fun <T> Response<T>.getOrNull(): T? = (this as? Response.Success)?.value

        interface UserRepository {
            suspend fun user(id: String): Response<String>
        }
    """.trimIndent()

    @Test
    fun `reports getOrThrow on Response and its subtypes`() {
        val code = prelude + "\n" + """
            class Sample(private val repository: UserRepository) {
                suspend fun a(id: String): String = repository.user(id).getOrThrow()
                fun b(response: Response<Int>): Int = response.getOrThrow()
                fun c(success: Response.Success<Int>): Int = success.getOrThrow()
                fun d(response: Response<Int>?): Int? = response?.getOrThrow()
            }
        """.trimIndent()

        assertEquals(4, rule.lintWithContext(env, code).size)
    }

    @Test
    fun `does not report Result getOrThrow and other Response accessors`() {
        val code = prelude + "\n" + """
            class Sample(private val repository: UserRepository) {
                suspend fun a(id: String): String? = repository.user(id).getOrNull()
                fun b(result: Result<Int>): Int = result.getOrThrow()
                fun c(): Int = runCatching { 1 }.getOrThrow()
            }
        """.trimIndent()

        assertEquals(0, rule.lintWithContext(env, code).size)
    }
}

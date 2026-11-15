package ru.alarmtrade.specmp.detekt

import dev.detekt.api.Config
import dev.detekt.test.lintWithContext
import dev.detekt.test.utils.createEnvironment
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ResponseGetOrThrowBypassTest {
    private val env = createEnvironment()
    private val rule = ResponseGetOrThrow(Config.empty)

    private fun file(imports: String = "", body: String) = """
        package ru.alarmtrade.specmp.core.common

        $imports

        sealed class Response<out T> {
            data class Success<out T>(val value: T) : Response<T>()
            class Error(val exception: Throwable) : Response<Nothing>()
        }

        fun <T> Response<T>.getOrThrow(): T = when (this) {
            is Response.Success -> value
            is Response.Error -> throw exception
        }
    """.trimIndent() + "\n" + body.trimIndent()

    @Test
    fun `bypass via import alias`() {
        val code = file(
            imports = "import ru.alarmtrade.specmp.core.common.getOrThrow as unwrap",
            body = "fun a(response: Response<Int>): Int = response.unwrap()",
        )

        assertEquals(1, rule.lintWithContext(env, code).size)
    }

    @Test
    fun `bypass via implicit receiver in scope functions`() {
        val code = file(
            body = """
                fun a(response: Response<Int>): Int = with(response) { getOrThrow() }
                fun b(response: Response<Int>): Int = response.run { getOrThrow() }
                fun Response<Int>.c(): Int = getOrThrow()
            """,
        )

        assertEquals(3, rule.lintWithContext(env, code).size)
    }

    @Test
    fun `bypass via callable reference`() {
        val code = file(
            body = """
                fun a(responses: List<Response<Int>>): List<Int> = responses.map(Response<Int>::getOrThrow)
                fun b(response: Response<Int>): () -> Int = response::getOrThrow
            """,
        )

        assertEquals(2, rule.lintWithContext(env, code).size)
    }

    @Test
    fun `bypass via parenthesized and cast receiver`() {
        val code = file(
            body = """
                fun a(response: Response<Int>): Int = (response).getOrThrow()
                fun b(value: Any): Int = (value as Response<Int>).getOrThrow()
            """,
        )

        assertEquals(2, rule.lintWithContext(env, code).size)
    }

    @Test
    fun `does not report Result getOrThrow through the same shapes`() {
        val code = file(
            body = """
                fun a(result: Result<Int>): Int = with(result) { getOrThrow() }
                fun b(results: List<Result<Int>>): List<Int> = results.map(Result<Int>::getOrThrow)
            """,
        )

        assertEquals(0, rule.lintWithContext(env, code).size)
    }
}

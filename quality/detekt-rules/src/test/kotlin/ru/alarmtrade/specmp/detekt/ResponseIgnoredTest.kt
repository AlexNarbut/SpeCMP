package ru.alarmtrade.specmp.detekt

import dev.detekt.api.Config
import dev.detekt.test.lintWithContext
import dev.detekt.test.utils.createEnvironment
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ResponseIgnoredTest {
    private val env = createEnvironment()
    private val rule = ResponseIgnored(Config.empty)

    private val prelude = """
        package ru.alarmtrade.specmp.core.common

        sealed class Response<out T> {
            data class Success<out T>(val value: T) : Response<T>()
            class Error(val exception: Throwable) : Response<Nothing>()
        }

        inline fun <T> Response<T>.onError(action: (Throwable) -> Unit): Response<T> = this
        inline fun <T, R> Response<T>.map(transform: (T) -> R): Response<R> = this as Response<R>
        inline fun <R> safeRun(block: () -> R): Response<R> = Response.Success(block())
        inline fun <R> safeResponseRun(block: () -> Response<R>): Response<R> = block()
        fun <T> Response<T>.getValueIfSuccess(): T? = (this as? Response.Success)?.value

        suspend fun <T> withContext(context: Any, block: suspend () -> T): T = block()
        fun intent(context: Any, block: suspend () -> Unit) = Unit
        fun intent(block: suspend () -> Unit) = Unit
        fun log(message: String) = Unit

        interface Repository {
            suspend fun save(id: String): Response<Unit>
            fun peek(): Response<Int>
        }
    """.trimIndent()

    @Test
    fun `reports ignored Response`() {
        val code = prelude + "\n" + """
            class Sample(private val repository: Repository, private val nullable: Repository?, private val io: Any) {
                suspend fun statement() {
                    repository.save("a")
                    log("after")
                }
                fun lastInUnitLambda() = intent(io) { repository.save("b") }
                fun lastInIntent() = intent {
                    log("before")
                    repository.save("c")
                }
                fun ifWithoutElse(flag: Boolean) {
                    if (flag) repository.peek()
                }
                fun safeCall() {
                    nullable?.peek()
                }
                suspend fun withContextStatement() {
                    withContext(io) { repository.save("d") }
                    log("after")
                }
                fun also() {
                    repository.peek().also { log("peeked") }
                }
            }
        """.trimIndent()

        assertEquals(7, rule.lintWithContext(env, code).size)
    }

    @Test
    fun `does not report handled or used Response`() {
        val code = prelude + "\n" + """
            class Sample(private val repository: Repository, private val io: Any) {
                suspend fun onErrorChain() {
                    repository.save("a").onError { log("failed") }
                    repository.peek().map { it + 1 }.onError { log("failed") }
                }
                suspend fun assigned() {
                    val response = repository.save("c")
                    log(response.toString())
                }
                suspend fun returned(): Response<Unit> {
                    return repository.save("d")
                }
                suspend fun expressionBody() = repository.save("e")
                fun argument() = log(repository.peek().toString())
                fun condition() {
                    if (repository.peek() is Response.Success) log("ok")
                    when (val r = repository.peek()) {
                        is Response.Success -> log(r.value.toString())
                        is Response.Error -> log("failed")
                    }
                }
                suspend fun lastOfUsedLambda(): Response<Unit> = withContext(io) { repository.save("f") }
                suspend fun lastOfSafeResponseRun(): Response<Unit> = safeResponseRun { repository.peek(); repository.save("g") }
                suspend fun wrapperOfHandled() {
                    withContext(io) { repository.save("h").onError { log("failed") } }
                    log("after")
                }
                fun safeRunStatement() {
                    safeRun { log("may throw") }
                    safeResponseRun { repository.peek() }
                }
                fun notResponse() {
                    runCatching { log("x") }
                    buildList { add(repository.peek().getValueIfSuccess()) }
                    repository.peek().getValueIfSuccess()
                }
            }
        """.trimIndent()

        // `repository.peek();` внутри safeResponseRun — оператор, его результат отброшен: верное срабатывание.
        assertEquals(1, rule.lintWithContext(env, code).size)
    }
}

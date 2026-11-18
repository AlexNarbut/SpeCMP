package ru.alarmtrade.specmp.detekt

import dev.detekt.api.Config
import dev.detekt.test.lintWithContext
import dev.detekt.test.utils.createEnvironment
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class LoopWithoutCancellationTest {
    private val env = createEnvironment()
    private val rule = LoopWithoutCancellation(Config.empty)

    private val prelude = """
        import kotlinx.coroutines.CoroutineScope
        import kotlinx.coroutines.currentCoroutineContext
        import kotlinx.coroutines.delay
        import kotlinx.coroutines.flow.flow
        import kotlinx.coroutines.isActive
        import kotlinx.coroutines.launch
        import kotlin.coroutines.RestrictsSuspension

        fun busy(): Boolean = false

        fun LaunchedEffect(key: Any?, block: suspend CoroutineScope.() -> Unit) = Unit

        @RestrictsSuspension
        interface AwaitPointerEventScope {
            suspend fun awaitPointerEvent(): Int
        }

        interface PointerInputScope {
            suspend fun <R> awaitPointerEventScope(block: suspend AwaitPointerEventScope.() -> R): R
        }

        fun pointerInput(key: Any?, block: suspend PointerInputScope.() -> Unit) = Unit
    """.trimIndent()

    @Test
    fun `reports constant loops in suspend functions even with suspension points`() {
        val code = prelude + "\n" + """
            suspend fun spin() {
                while (true) {
                    if (busy()) break
                }
            }

            suspend fun tick() {
                while (true) {
                    delay(1000)
                }
            }
        """.trimIndent()

        assertEquals(2, rule.lintWithContext(env, code).size)
    }

    @Test
    fun `reports constant loops in coroutine builders`() {
        val code = prelude + "\n" + """
            fun ticks() = flow {
                while (true) {
                    emit(1)
                    delay(1)
                }
            }

            fun CoroutineScope.start() {
                launch {
                    while (true) {
                        delay(1)
                    }
                }
            }

            fun effect() {
                LaunchedEffect(Unit) {
                    while (true) { delay(1) }
                }
            }
        """.trimIndent()

        assertEquals(3, rule.lintWithContext(env, code).size)
    }

    @Test
    fun `reports constant loops in pointer input scopes`() {
        val code = prelude + "\n" + """
            fun gestures() {
                pointerInput(Unit) {
                    awaitPointerEventScope {
                        while (true) {
                            awaitPointerEvent()
                        }
                    }
                }
                pointerInput(Unit) {
                    while (true) {
                        awaitPointerEventScope { awaitPointerEvent() }
                    }
                }
            }
        """.trimIndent()

        assertEquals(2, rule.lintWithContext(env, code).size)
    }

    @Test
    fun `does not report loops on coroutine isActive`() {
        val code = prelude + "\n" + """
            fun CoroutineScope.start(job: kotlinx.coroutines.Job) {
                launch {
                    while (isActive) { delay(1) }
                    while (isActive && busy()) { delay(1) }
                    while (busy() && (coroutineContext.isActive)) { delay(1) }
                    while (job.isActive) { delay(1) }
                }
            }

            fun ticks() = flow {
                while (currentCoroutineContext().isActive) {
                    emit(1)
                    delay(1)
                }
            }

            fun gestures() {
                pointerInput(Unit) {
                    val context = currentCoroutineContext()
                    awaitPointerEventScope {
                        while (context.isActive) { awaitPointerEvent() }
                    }
                }
            }
        """.trimIndent()

        assertEquals(0, rule.lintWithContext(env, code).size)
    }

    @Test
    fun `does not report loops outside coroutines`() {
        val code = prelude + "\n" + """
            fun name(): Int {
                var i = 0
                while (true) {
                    if (busy()) return i
                    i++
                }
            }

            class Poller {
                val isActive = true

                fun poll() {
                    while (isActive) { busy() }
                }
            }
        """.trimIndent()

        assertEquals(0, rule.lintWithContext(env, code).size)
    }
}

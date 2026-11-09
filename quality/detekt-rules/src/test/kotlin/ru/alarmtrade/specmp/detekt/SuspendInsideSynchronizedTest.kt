package ru.alarmtrade.specmp.detekt

import dev.detekt.api.Config
import dev.detekt.test.lintWithContext
import dev.detekt.test.utils.createEnvironment
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class SuspendInsideSynchronizedTest {
    private val env = createEnvironment()
    private val rule = SuspendInsideSynchronized(Config.empty)

    private val prelude = """
        import java.util.concurrent.locks.ReentrantLock
        import kotlin.concurrent.withLock
        import kotlinx.coroutines.CoroutineScope
        import kotlinx.coroutines.delay
        import kotlinx.coroutines.launch

        suspend fun load(): Int = 1
        fun peek(): Int = 1
    """.trimIndent()

    @Test
    fun `reports suspend calls under thread locks`() {
        val code = prelude + "\n" + """
            class Store {
                private val monitor = Any()
                private val lock = ReentrantLock()

                suspend fun a() {
                    synchronized(monitor) { load() }
                }

                suspend fun b() {
                    lock.withLock { listOf(1).forEach { delay(1) } }
                }

                suspend fun c() {
                    lock.lock()
                    try {
                        load()
                    } finally {
                        lock.unlock()
                    }
                }

                @Synchronized
                suspend fun d() = Unit
            }
        """.trimIndent()

        assertEquals(4, rule.lintWithContext(env, code).size)
    }

    @Test
    fun `does not report blocking-only critical sections and other coroutines`() {
        val code = prelude + "\n" + """
            import kotlinx.coroutines.sync.Mutex
            import kotlinx.coroutines.sync.withLock as withMutex

            class Store(private val scope: CoroutineScope) {
                private val monitor = Any()
                private val lock = ReentrantLock()
                private val mutex = Mutex()

                @Synchronized
                fun a() = peek()

                suspend fun b() {
                    synchronized(monitor) {
                        peek()
                        scope.launch { load() }
                    }
                    lock.withLock { peek() }
                    lock.lock()
                    try { peek() } finally { lock.unlock() }
                    load()
                    mutex.withMutex { load() }
                }
            }
        """.trimIndent()

        assertEquals(0, rule.lintWithContext(env, code).size)
    }
}

package ru.alarmtrade.specmp.detekt

import dev.detekt.api.Config
import dev.detekt.test.lintWithContext
import dev.detekt.test.utils.createEnvironment
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class SuspendInsideSynchronizedBypassTest {
    private val env = createEnvironment()
    private val rule = SuspendInsideSynchronized(Config.empty)

    private fun file(imports: String, body: String) = imports.trimIndent() + "\n" + """
        import java.util.concurrent.locks.ReentrantLock
        import java.util.concurrent.locks.ReentrantReadWriteLock

        suspend fun load(): Int = 1
    """.trimIndent() + "\n" + body.trimIndent()

    @Test
    fun `bypass via import alias of withLock`() {
        val code = file(
            "import kotlin.concurrent.withLock as locked",
            """
                class Store {
                    private val lock = ReentrantLock()
                    suspend fun a() = lock.locked { load() }
                }
            """,
        )

        assertEquals(1, rule.lintWithContext(env, code).size)
    }

    @Test
    fun `bypass via named lambda argument`() {
        val code = file(
            "import kotlin.concurrent.withLock",
            """
                class Store {
                    private val lock = ReentrantLock()
                    private val monitor = Any()
                    suspend fun a() = lock.withLock(action = { load() })
                    suspend fun b() = synchronized(monitor, block = { load() })
                }
            """,
        )

        assertEquals(2, rule.lintWithContext(env, code).size)
    }

    @Test
    fun `bypass via read-write lock helpers`() {
        val code = file(
            """
                import kotlin.concurrent.read
                import kotlin.concurrent.write
            """,
            """
                class Store {
                    private val rw = ReentrantReadWriteLock()
                    suspend fun a() = rw.read { load() }
                    suspend fun b() = rw.write { load() }
                }
            """,
        )

        assertEquals(2, rule.lintWithContext(env, code).size)
    }

    @Test
    fun `bypass via tryLock and lockInterruptibly`() {
        val code = file(
            "",
            """
                class Store {
                    private val lock = ReentrantLock()
                    suspend fun a() {
                        if (lock.tryLock()) {
                            try { load() } finally { lock.unlock() }
                        }
                    }
                    suspend fun b() {
                        lock.lockInterruptibly()
                        try { load() } finally { lock.unlock() }
                    }
                }
            """,
        )

        assertEquals(2, rule.lintWithContext(env, code).size)
    }

    @Test
    fun `bypass via aliased Synchronized annotation`() {
        val code = file(
            "import kotlin.jvm.Synchronized as Locked",
            """
                class Store {
                    @Locked
                    suspend fun a() = load()
                }
            """,
        )

        assertEquals(1, rule.lintWithContext(env, code).size)
    }

    @Test
    fun `does not report Mutex withLock under an alias`() {
        val code = file(
            """
                import kotlinx.coroutines.sync.Mutex
                import kotlinx.coroutines.sync.withLock as locked
            """,
            """
                class Store {
                    private val mutex = Mutex()
                    suspend fun a() = mutex.locked { load() }
                }
            """,
        )

        assertEquals(0, rule.lintWithContext(env, code).size)
    }
}

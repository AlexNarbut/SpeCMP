package ru.alarmtrade.specmp.detekt

import dev.detekt.api.Config
import dev.detekt.test.lintWithContext
import dev.detekt.test.utils.createEnvironment
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class MutexIsLockedCheckBypassTest {
    private val env = createEnvironment()
    private val rule = MutexIsLockedCheck(Config.empty)

    private val prelude = """
        import kotlinx.coroutines.sync.Mutex
    """.trimIndent()

    @Test
    fun `bypass via with scope`() {
        val code = prelude + "\n" + """
            suspend fun a(mutex: Mutex) {
                if (with(mutex) { isLocked }) return
                mutex.run { if (isLocked) return }
            }
        """.trimIndent()

        assertEquals(2, rule.lintWithContext(env, code).size)
    }

    @Test
    fun `bypass via extension on Mutex`() {
        val code = prelude + "\n" + """
            val Mutex.busy: Boolean get() = isLocked

            fun Mutex.free(): Boolean = !this.isLocked
        """.trimIndent()

        assertEquals(2, rule.lintWithContext(env, code).size)
    }

    @Test
    fun `bypass via property reference`() {
        val code = prelude + "\n" + """
            fun a(mutex: Mutex): () -> Boolean = mutex::isLocked
        """.trimIndent()

        assertEquals(1, rule.lintWithContext(env, code).size)
    }

    @Test
    fun `bypass via delegating subtype`() {
        val code = prelude + "\n" + """
            class TracedMutex(private val inner: Mutex) : Mutex by inner

            suspend fun a(mutex: TracedMutex) {
                if (mutex.isLocked) return
            }
        """.trimIndent()

        assertEquals(1, rule.lintWithContext(env, code).size)
    }

    @Test
    fun `bypass via let and safe call chain`() {
        val code = prelude + "\n" + """
            class Holder(val mutex: Mutex?)

            fun a(holder: Holder?) = holder?.mutex?.let { it.isLocked } == true
        """.trimIndent()

        assertEquals(1, rule.lintWithContext(env, code).size)
    }

    @Test
    fun `does not report isLocked of other types inside scope functions`() {
        val code = prelude + "\n" + """
            class Door {
                val isLocked: Boolean = false
            }

            fun a(door: Door, mutex: Mutex) {
                if (with(door) { isLocked }) return
                val check: () -> Boolean = door::isLocked
                mutex.run { tryLock() }
            }
        """.trimIndent()

        assertEquals(0, rule.lintWithContext(env, code).size)
    }
}

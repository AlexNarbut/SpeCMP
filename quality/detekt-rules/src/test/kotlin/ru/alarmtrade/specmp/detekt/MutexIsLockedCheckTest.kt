package ru.alarmtrade.specmp.detekt

import dev.detekt.api.Config
import dev.detekt.test.lintWithContext
import dev.detekt.test.utils.createEnvironment
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class MutexIsLockedCheckTest {
    private val env = createEnvironment()
    private val rule = MutexIsLockedCheck(Config.empty)

    @Test
    fun `reports isLocked reads on Mutex`() {
        val code = """
            import kotlinx.coroutines.sync.Mutex
            import kotlinx.coroutines.sync.withLock

            class Loader {
                private val mutex = Mutex()
                private val nullable: Mutex? = null

                suspend fun load() {
                    if (mutex.isLocked) return
                    if (nullable?.isLocked == true) return
                    mutex.withLock { }
                }
            }
        """.trimIndent()

        assertEquals(2, rule.lintWithContext(env, code).size)
    }

    @Test
    fun `does not report tryLock, withLock and other isLocked`() {
        val code = """
            import kotlinx.coroutines.sync.Mutex
            import kotlinx.coroutines.sync.withLock

            class Door {
                val isLocked: Boolean = false
            }

            class Loader {
                private val mutex = Mutex()
                private val door = Door()

                suspend fun load() {
                    if (!mutex.tryLock()) return
                    try { } finally { mutex.unlock() }
                    mutex.withLock { }
                    if (door.isLocked) return
                }
            }
        """.trimIndent()

        assertEquals(0, rule.lintWithContext(env, code).size)
    }
}

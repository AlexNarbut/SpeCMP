package ru.alarmtrade.specmp.detekt

import dev.detekt.api.Config
import dev.detekt.test.lint
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class DirectClockUsageBypassTest {
    private val rule = DirectClockUsage(Config.empty)

    @Test
    fun `bypass via fully qualified names`() {
        val code = """
            fun a() = kotlin.time.Clock.System.now()
            fun b() = java.lang.System.currentTimeMillis()
        """.trimIndent()

        assertEquals(2, rule.lint(code).size)
    }

    @Test
    fun `bypass via member import of Clock System`() {
        val code = """
            import kotlin.time.Clock.System

            fun a() = System.now()
        """.trimIndent()

        assertEquals(1, rule.lint(code).size)
    }

    @Test
    fun `bypass via import alias of Clock`() {
        val code = """
            import kotlin.time.Clock as WallClock

            fun a() = WallClock.System.now()
        """.trimIndent()

        assertEquals(1, rule.lint(code).size)
    }

    @Test
    fun `bypass via explicit Companion of TimeZone`() {
        val code = """
            fun a() = TimeZone.Companion.currentSystemDefault()
        """.trimIndent()

        assertEquals(1, rule.lint(code).size)
    }

    @Test
    fun `bypass via callable reference and function default parameter`() {
        val code = """
            fun a(now: () -> Instant = Clock.System::now) = now()

            class Foo {
                constructor(clock: Clock = Clock.System)
            }
        """.trimIndent()

        assertEquals(2, rule.lint(code).size)
    }

    @Test
    fun `does not report plain type imports and injected clocks`() {
        val code = """
            import kotlin.time.Clock
            import kotlinx.datetime.TimeZone
            import kotlin.time.Clock as WallClock

            class Foo(private val clock: Clock = Clock.System, private val zone: WallClock) {
                fun now() = clock.now()
                fun zone(source: TimeZoneSource) = source.current()
            }
        """.trimIndent()

        assertEquals(0, rule.lint(code).size)
    }
}

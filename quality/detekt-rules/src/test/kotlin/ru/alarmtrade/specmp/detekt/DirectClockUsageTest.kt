package ru.alarmtrade.specmp.detekt

import dev.detekt.api.Config
import dev.detekt.test.lint
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class DirectClockUsageTest {
    private val rule = DirectClockUsage(Config.empty)

    @Test
    fun `reports system clocks and time zone in bodies and properties`() {
        val code = """
            package ru.alarmtrade.specmp.feature.sleep.data.repository

            class SleepRepositoryImpl(private val dao: SleepDao) {
                private val zone = TimeZone.currentSystemDefault()
                private val source = TimeZoneSource.System

                fun save() {
                    val now = Clock.System.now()
                    val qualified = kotlin.time.Clock.System.now()
                    val millis = System.currentTimeMillis()
                    val nanos = java.lang.System.nanoTime()
                    val date = NSDate()
                }
            }
        """.trimIndent()

        assertEquals(7, rule.lint(code).size)
    }

    @Test
    fun `reports defaults of ordinary function parameters and secondary constructors`() {
        val code = """
            package ru.alarmtrade.specmp.feature.recipes.data.content

            class ContentDbWriter(private val db: Db) {
                constructor(db: Db, zone: TimeZone = TimeZone.currentSystemDefault()) : this(db)

                fun write(syncedAtMs: Long = Clock.System.now().toEpochMilliseconds()) = Unit
            }

            fun suggest(timeZone: TimeZone = TimeZone.currentSystemDefault()) = Unit
        """.trimIndent()

        assertEquals(3, rule.lint(code).size)
    }

    @Test
    fun `allows defaults of primary constructor parameters`() {
        val code = """
            package ru.alarmtrade.specmp.feature.sleep.presentation

            class SleepEntryViewModel(
                private val clock: Clock = Clock.System,
                private val timeZones: TimeZoneSource = TimeZoneSource.System,
                private val nowMillis: () -> Long = { Clock.System.now().toEpochMilliseconds() },
                private val today: LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault()),
            )
        """.trimIndent()

        assertEquals(0, rule.lint(code).size)
    }

    @Test
    fun `reports composable bodies and composable parameter defaults`() {
        val code = """
            package ru.alarmtrade.specmp.feature.reminders.presentation.components

            @Composable
            fun TriageSheet(nowEpochMs: Long = Clock.System.now().toEpochMilliseconds()) {
                val today = remember { Clock.System.todayIn(TimeZone.currentSystemDefault()) }
            }

            val LocalNow = staticCompositionLocalOf<Clock> { Clock.System }
        """.trimIndent()

        assertEquals(4, rule.lint(code).size)
    }

    @Test
    fun `allows clock from composition locals and expect constructor defaults`() {
        val code = """
            package ru.alarmtrade.specmp.feature.sleep.presentation.widget

            expect class DiagnosticsLogStore(clock: Clock = Clock.System, timeZones: TimeZoneSource = TimeZoneSource.System)

            @Composable
            fun SleepTimer(
                startedAt: Instant,
                nowEpochMs: Long = LocalClock.current.now().toEpochMilliseconds(),
            ) {
                val clock = LocalClock.current
                val zone = currentTimeZone()
                val elapsed = clock.now() - startedAt
            }
        """.trimIndent()

        assertEquals(0, rule.lint(code).size)
    }

    @Test
    fun `ignores injected sources, monotonic time and unrelated System members`() {
        val code = """
            package ru.alarmtrade.specmp.feature.sleep.domain

            import kotlin.time.Clock

            class GetSleepDayDataUseCase(private val clock: Clock, private val timeZones: TimeZoneSource) {
                fun run() {
                    val now = clock.now()
                    val zone = timeZones.current()
                    val mark = TimeSource.Monotonic.markNow()
                    val parsed = NSDate(timeIntervalSince1970 = 0.0)
                    val other = Theme.System
                    val line = System.lineSeparator()
                    val utc = TimeZone.UTC
                }
            }
        """.trimIndent()

        assertEquals(0, rule.lint(code).size)
    }
}

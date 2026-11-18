package ru.alarmtrade.specmp.feature.device.signallog.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant

class SignalLogFileNameTest {

    @Test
    fun `file name uses local time of the given zone`() {
        val time = LocalDateTime(2026, 11, 18, 21, 5, 59).toInstant(TimeZone.UTC).toEpochMilliseconds()

        assertEquals("signal-log_1234567_20261119-0005.txt", signalLogFileName("1234567", time, MOSCOW))
    }

    @Test
    fun `single digit date parts are zero padded`() {
        val time = LocalDateTime(2026, 1, 2, 3, 4).toInstant(TimeZone.UTC).toEpochMilliseconds()

        assertEquals("signal-log_42_20260102-0304.txt", signalLogFileName("42", time, TimeZone.UTC))
    }

    private companion object {
        val MOSCOW = TimeZone.of("Europe/Moscow")
    }
}

package ru.alarmtrade.specmp.feature.device.signallog.domain.model

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format.char
import kotlinx.datetime.toLocalDateTime

fun signalLogFileName(serial: String, timeMs: Long, timeZone: TimeZone): String {
    val time = Instant.fromEpochMilliseconds(timeMs).toLocalDateTime(timeZone)
    return "signal-log_${serial}_${FILE_TIME_FORMAT.format(time)}.txt"
}

private val FILE_TIME_FORMAT = LocalDateTime.Format {
    year()
    monthNumber()
    dayOfMonth()
    char('-')
    hour()
    minute()
}

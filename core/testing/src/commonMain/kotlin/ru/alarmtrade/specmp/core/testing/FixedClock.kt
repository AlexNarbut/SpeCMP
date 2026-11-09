package ru.alarmtrade.specmp.core.testing

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

class FixedClock(var instant: Instant) : Clock {
    override fun now(): Instant = instant
}

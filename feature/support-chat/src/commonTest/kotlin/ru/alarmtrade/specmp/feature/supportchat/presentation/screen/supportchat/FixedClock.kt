package ru.alarmtrade.specmp.feature.supportchat.presentation.screen.supportchat

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

class FixedClock(var instant: Instant) : Clock {
    override fun now(): Instant = instant
}

package ru.alarmtrade.specmp.core.common.time

import kotlinx.datetime.TimeZone

// Пояс читается на каждый вызов: синглтон с внедрённым значением TimeZone
// «замёрз» бы при смене пояса на устройстве.
fun interface TimeZoneSource {
    fun current(): TimeZone

    companion object {
        val System: TimeZoneSource = TimeZoneSource { TimeZone.currentSystemDefault() }

        fun fixed(timeZone: TimeZone): TimeZoneSource = TimeZoneSource { timeZone }
    }
}

package ru.alarmtrade.specmp.feature.device.signallog.data.mapper

import ru.alarmtrade.specialist.domain.serviceLog.ServiceLogLine
import ru.alarmtrade.specialist.domain.serviceLog.ServiceLogSummary
import ru.alarmtrade.specmp.feature.device.signallog.domain.model.SignalLogLine
import ru.alarmtrade.specmp.feature.device.signallog.domain.model.SignalLogSummary

internal fun ServiceLogLine.toSignalLogLine(): SignalLogLine = SignalLogLine(id = id, timeMs = timeMs, text = text)

internal fun List<ServiceLogLine>.toSignalLogLines(): List<SignalLogLine> = map { it.toSignalLogLine() }

internal fun ServiceLogSummary.toSignalLogSummary(): SignalLogSummary =
    SignalLogSummary(count = count, firstTimeMs = firstTimeMs, lastTimeMs = lastTimeMs)

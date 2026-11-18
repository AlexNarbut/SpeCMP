package ru.alarmtrade.specmp.feature.device.signallog.domain.model

/** [timeMs] — момент приёма строки телефоном: блок время не присылает. */
data class SignalLogLine(val id: Long, val timeMs: Long, val text: String)

/** Сводка по всему логу блока; времена `null`, когда лог пуст. */
data class SignalLogSummary(val count: Int, val firstTimeMs: Long?, val lastTimeMs: Long?)

/** Неудача отправки — `Response.Error`. */
sealed interface SignalLogSendResult {
    data object Sent : SignalLogSendResult

    /** Нет связи с сервером: отчёт в очереди SDK и уйдёт позже. */
    data object Queued : SignalLogSendResult

    data object Empty : SignalLogSendResult
}

class SignalLogEmptyException : Exception("Signal log of the device is empty")

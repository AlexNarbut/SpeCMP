package ru.alarmtrade.specmp.feature.device.signallog.ui.presentation.screen

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.toPersistentList
import ru.alarmtrade.specmp.core.ui.lce.LCEState
import ru.alarmtrade.specmp.feature.device.signallog.domain.model.SignalLogLine
import ru.alarmtrade.specmp.feature.device.signallog.domain.model.SignalLogSummary

/** SDK хранит последние 10 000 строк блока — больше в ленте держать незачем. */
internal const val MAX_LINES = 10_000

/** Слияние без дублей в порядке «новые первыми»; частые случаи — дописать в начало или в конец без сортировки. */
internal fun ImmutableList<SignalLogLine>.mergeNewestFirst(
    incoming: List<SignalLogLine>,
): PersistentList<SignalLogLine> {
    val current = toPersistentList()
    if (incoming.isEmpty()) return current
    val sorted = incoming.distinctBy { it.id }.sortedByDescending { it.id }
    return when {
        current.isEmpty() -> sorted.toPersistentList()
        sorted.last().id > current.first().id -> current.addAll(0, sorted)
        sorted.first().id < current.last().id -> current.addAll(sorted)
        else -> (current + sorted).distinctBy { it.id }.sortedByDescending { it.id }.toPersistentList()
    }
}

/** Лишние старые строки уходят из ленты, но остаются в логе блока — их снова подгрузит прокрутка вверх. */
internal fun SignalLogState.withLines(merged: PersistentList<SignalLogLine>): SignalLogState =
    if (merged.size > MAX_LINES) {
        copy(lines = merged.subList(0, MAX_LINES).toPersistentList(), hasOlder = true)
    } else {
        copy(lines = merged)
    }

/** Живая строка: на паузе — только счётчик пропущенного; запись в лог блока идёт в любом случае. */
internal fun SignalLogState.withLiveLine(line: SignalLogLine): SignalLogState {
    val newest = lines.firstOrNull()?.id
    if (newest != null && line.id <= newest && lines.any { it.id == line.id }) return this
    if (load !is LCEState.Content) return withLines(lines.mergeNewestFirst(listOf(line)))
    val counted = copy(summary = summary.withLine(line))
    return if (paused) {
        counted.copy(pendingCount = pendingCount + 1)
    } else {
        counted.withLines(lines.mergeNewestFirst(listOf(line))).copy(search = search?.withLiveLine(line))
    }
}

internal fun SignalLogSummary.withLine(line: SignalLogLine): SignalLogSummary = SignalLogSummary(
    count = minOf(count + 1, MAX_LINES),
    firstTimeMs = firstTimeMs ?: line.timeMs,
    lastTimeMs = line.timeMs,
)

private fun SignalLogSearch.withLiveLine(line: SignalLogLine): SignalLogSearch {
    val query = query.trim()
    return if (query.isNotEmpty() && line.text.contains(query, ignoreCase = true)) {
        copy(matches = (matches + line).toPersistentList())
    } else {
        this
    }
}

/** Ответ поиска применяется, только если запрос с тех пор не сменился; текущее совпадение — самое новое. */
internal fun SignalLogState.withMatches(query: String, matches: List<SignalLogLine>): SignalLogState {
    val search = search ?: return this
    if (search.query.trim() != query) return this
    return copy(search = search.copy(matches = matches.toPersistentList(), current = matches.lastIndex))
}

/** Совпадение ещё не в ленте, а более ранние строки есть — нужна догрузка. */
internal fun SignalLogState.needsOlderFor(lineId: Long): Boolean =
    hasOlder && (lines.lastOrNull()?.id ?: Long.MAX_VALUE) > lineId

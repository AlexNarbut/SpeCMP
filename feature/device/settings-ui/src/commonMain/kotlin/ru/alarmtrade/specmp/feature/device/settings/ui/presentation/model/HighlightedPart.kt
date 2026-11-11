package ru.alarmtrade.specmp.feature.device.settings.ui.presentation.model

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import ru.alarmtrade.specmp.feature.device.settings.domain.model.Channel
import ru.alarmtrade.specmp.feature.device.settings.domain.model.SearchHighlights

internal class HighlightedPart(val text: String, val ranges: List<IntRange> = emptyList())

internal fun highlightedText(parts: List<HighlightedPart>, style: SpanStyle): AnnotatedString = buildAnnotatedString {
    parts.forEach { part ->
        val offset = length
        append(part.text)
        normalizedRanges(part.ranges, part.text.length).forEach { range ->
            addStyle(style, offset + range.first, offset + range.last + 1)
        }
    }
}

/** Диапазоны в пределах `0 until length`, по возрастанию, пересекающиеся и смежные — слиты. */
internal fun normalizedRanges(ranges: List<IntRange>, length: Int): List<IntRange> {
    val clamped = ranges
        .map { it.first.coerceAtLeast(0)..it.last.coerceAtMost(length - 1) }
        .filter { !it.isEmpty() }
        .sortedBy { it.first }
    val merged = mutableListOf<IntRange>()
    clamped.forEach { range ->
        val last = merged.lastOrNull()
        if (last != null && range.first <= last.last + 1) {
            merged[merged.lastIndex] = last.first..maxOf(last.last, range.last)
        } else {
            merged += range
        }
    }
    return merged
}

internal fun numberedTitleParts(number: String, name: String, highlights: SearchHighlights): List<HighlightedPart> =
    if (number.isEmpty()) {
        listOf(HighlightedPart(name, highlights.name))
    } else {
        listOf(
            HighlightedPart(number, highlights.number),
            HighlightedPart("  "),
            HighlightedPart(name, highlights.name),
        )
    }

internal fun settingTitleParts(
    name: String,
    channel: Channel?,
    highlights: SearchHighlights = SearchHighlights(),
): List<HighlightedPart> = when {
    channel == null || channel.name.isBlank() -> listOf(HighlightedPart(name, highlights.name))

    channel.name == name -> listOf(HighlightedPart(name, highlights.name + highlights.channel))

    name.isBlank() -> listOf(HighlightedPart(channel.name, highlights.channel))

    else -> listOf(
        HighlightedPart(channel.name, highlights.channel),
        HighlightedPart(" · "),
        HighlightedPart(name, highlights.name),
    )
}

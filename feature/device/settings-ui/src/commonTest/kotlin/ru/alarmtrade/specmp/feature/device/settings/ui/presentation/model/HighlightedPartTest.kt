package ru.alarmtrade.specmp.feature.device.settings.ui.presentation.model

import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontWeight
import kotlin.test.Test
import kotlin.test.assertEquals
import ru.alarmtrade.specmp.feature.device.settings.domain.model.Channel
import ru.alarmtrade.specmp.feature.device.settings.domain.model.SearchHighlights

class HighlightedPartTest {

    private val style = SpanStyle(fontWeight = FontWeight.SemiBold)

    @Test
    fun `ranges are clamped to the text, sorted and merged when overlapping or adjacent`() {
        assertEquals(
            listOf(0..4, 7..9),
            normalizedRanges(listOf(8..20, -3..1, 2..4, 3..3, 7..7, 15..18, IntRange.EMPTY), length = 10),
        )
        assertEquals(emptyList(), normalizedRanges(listOf(0..3), length = 0))
    }

    @Test
    fun `parts are joined and their ranges are shifted to the part offset`() {
        val text = highlightedText(
            numberedTitleParts("3.2", "Датчик удара", SearchHighlights(name = listOf(7..11), number = listOf(0..2))),
            style,
        )

        assertEquals("3.2  Датчик удара", text.text)
        assertEquals(listOf(0 to 3, 12 to 17), text.spanStyles.map { it.start to it.end })
        assertEquals(setOf(style), text.spanStyles.map { it.item }.toSet())
    }

    @Test
    fun `channel title highlights the channel name and the setting name separately`() {
        val channel = Channel(1, "IN1", emptyList())
        val highlights = SearchHighlights(name = listOf(0..3), channel = listOf(0..2))

        val text = highlightedText(settingTitleParts("Дверь", channel, highlights), style)

        assertEquals("IN1 · Дверь", text.text)
        assertEquals(listOf(0 to 3, 6 to 10), text.spanStyles.map { it.start to it.end })
        assertEquals("IN1", settingTitle("IN1", channel))
    }
}

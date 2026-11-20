package ru.alarmtrade.specmp.feature.mountpassport.presentation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import ru.alarmtrade.specmp.feature.mountpassport.presentation.model.PassportMatchField
import ru.alarmtrade.specmp.feature.mountpassport.presentation.model.extraMatch
import ru.alarmtrade.specmp.feature.mountpassport.presentation.model.highlightRange

class SearchHighlightTest {

    @Test
    fun caseInsensitiveSubstring() {
        assertEquals(6..8, highlightRange("Ирина Ковалёва", "ков"))
    }

    @Test
    fun spacesAndDashesInTextAreSkipped() {
        assertEquals(0..6, highlightRange("А 123 ВС 777", "а123в"))
    }

    @Test
    fun latinLookalikesMatchCyrillicPlate() {
        assertEquals(0..6, highlightRange("А 123 ВС 777", "A123B"))
    }

    @Test
    fun yoMatchesYe() {
        assertEquals(6..13, highlightRange("Ирина Ковалёва", "Ковалева"))
    }

    @Test
    fun noMatchOrBlankQueryIsNull() {
        assertNull(highlightRange("Kia Sportage", "Mazda"))
        assertNull(highlightRange("Kia Sportage", "  "))
    }

    @Test
    fun extraLineOnlyWhenMatchIsOutsideTitleLine() {
        val item = summary()

        assertNull(extraMatch(item, "Sport"))
        assertNull(extraMatch(item, "123"))
        assertEquals(PassportMatchField.CLIENT, extraMatch(item, "Ирина"))
        assertEquals(PassportMatchField.VIN, extraMatch(item, "XWEPH"))
        assertEquals(PassportMatchField.PHONE, extraMatch(item, "903 555"))
        assertNull(extraMatch(item, ""))
    }
}

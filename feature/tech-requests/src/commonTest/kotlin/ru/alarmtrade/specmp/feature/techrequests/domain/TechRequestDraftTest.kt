package ru.alarmtrade.specmp.feature.techrequests.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import ru.alarmtrade.specmp.feature.techrequests.domain.model.TechRequestDraft
import ru.alarmtrade.specmp.feature.techrequests.domain.model.TechRequestField

class TechRequestDraftTest {

    @Test
    fun validDraftHasNoErrors() {
        assertEquals(emptySet(), TechRequestDraft("Иван", "79001234567", "тест").fieldErrors())
    }

    @Test
    fun blankFieldsAreErrors() {
        assertEquals(
            setOf(TechRequestField.Name, TechRequestField.Phone, TechRequestField.Issue),
            TechRequestDraft("  ", "", " \n ").fieldErrors(),
        )
    }

    @Test
    fun phoneMustMatchCountryLength() {
        assertEquals(setOf(TechRequestField.Phone), TechRequestDraft("Иван", "7900123456", "тест").fieldErrors())
    }

    @Test
    fun issueIsLimited() {
        val max = "a".repeat(TechRequestDraft.ISSUE_MAX_LENGTH)

        assertEquals(emptySet(), TechRequestDraft("Иван", "79001234567", max).fieldErrors())
        assertEquals(setOf(TechRequestField.Issue), TechRequestDraft("Иван", "79001234567", max + "a").fieldErrors())
    }

    @Test
    fun valuesAreTrimmed() {
        val draft = TechRequestDraft("  Иван ", "79001234567", "\n тест \n")

        assertEquals("Иван", draft.normalizedUserName)
        assertEquals("тест", draft.normalizedIssue)
    }
}

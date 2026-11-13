package ru.alarmtrade.specmp.feature.techrequests.domain.model

import ru.alarmtrade.specmp.core.common.phone.PhoneNumbers

/** Новая заявка. [phoneDigits] — цифры с кодом страны без `+` ([PhoneNumbers]). */
data class TechRequestDraft(val userName: String, val phoneDigits: String, val issue: String) {
    val normalizedUserName: String = userName.trim()
    val normalizedIssue: String = issue.trim()

    fun fieldErrors(): Set<TechRequestField> = buildSet {
        if (normalizedUserName.isEmpty()) add(TechRequestField.Name)
        if (!PhoneNumbers.isValid(phoneDigits)) add(TechRequestField.Phone)
        if (normalizedIssue.isEmpty() || issue.length > ISSUE_MAX_LENGTH) add(TechRequestField.Issue)
    }

    companion object {
        const val ISSUE_MAX_LENGTH = 2000
    }
}

enum class TechRequestField { Name, Phone, Issue }

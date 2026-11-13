package ru.alarmtrade.specmp.feature.techrequests.domain.model

/** Этапы отправки; неудача — последний `Response.Error` ([TechRequestException] или общая `AppException`). */
sealed interface TechRequestSubmitStep {
    data object ReadingDevice : TechRequestSubmitStep

    data object Sending : TechRequestSubmitStep

    data class Created(val request: TechRequest) : TechRequestSubmitStep
}

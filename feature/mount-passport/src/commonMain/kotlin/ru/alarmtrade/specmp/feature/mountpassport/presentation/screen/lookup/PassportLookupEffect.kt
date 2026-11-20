package ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.lookup

import ru.alarmtrade.specmp.feature.mountpassport.presentation.model.PassportError

internal sealed interface PassportLookupEffect {
    /** Паспорт без блока создан — открыть предотчёт вместо экрана «Блок». */
    data class Created(val passportId: String) : PassportLookupEffect

    data class CreateFailed(val error: PassportError) : PassportLookupEffect

    data object FocusId : PassportLookupEffect
}

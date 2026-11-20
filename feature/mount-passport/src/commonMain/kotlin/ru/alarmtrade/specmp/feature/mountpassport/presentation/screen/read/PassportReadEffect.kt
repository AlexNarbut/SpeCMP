package ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.read

internal sealed interface PassportReadEffect {
    /** Паспорт сохранён — открыть предотчёт вместо считывания. */
    data class Done(val passportId: String) : PassportReadEffect

    /** Считывание отменено без записи. */
    data object Cancelled : PassportReadEffect
}

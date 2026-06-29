package ru.alarmtrade.specmp.feature.calculator.presentation

import androidx.compose.runtime.Immutable

/**
 * One-shot события из [CalculatorViewModel] в UI.
 * См. `docs/patterns.md` — раздел «One-shot события (Effects)».
 */
@Immutable
sealed interface CalculatorEffect {
    /**
     * Попытка деления на ноль. Без текста — строку резолвит UI через
     * `stringResource` (строки не передаём из ViewModel, см. docs/localization.md).
     */
    data object DivideByZero : CalculatorEffect
}

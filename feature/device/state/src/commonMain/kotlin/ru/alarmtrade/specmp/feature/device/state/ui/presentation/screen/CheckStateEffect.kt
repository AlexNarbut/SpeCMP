package ru.alarmtrade.specmp.feature.device.state.ui.presentation.screen

import ru.alarmtrade.specmp.feature.device.state.domain.model.StateCommand

sealed interface CheckStateEffect {
    /** Итог команды — снэкбар («Повторить» — при неуспехе). */
    data class CommandFinished(val command: StateCommand, val outcome: CommandOutcome) : CheckStateEffect

    /** Уйти с экрана (после выключения режимов или подтверждения). */
    data object Close : CheckStateEffect

    /** Быстрый монтаж: шаг завершён. */
    data object Next : CheckStateEffect
}

package ru.alarmtrade.specmp.feature.calculator.presentation

import androidx.compose.runtime.Immutable
import ru.alarmtrade.specmp.feature.calculator.domain.model.CalculatorOperation

@Immutable
data class CalculatorState(
    val display: String = "0",
    val firstOperand: Double? = null,
    val operation: CalculatorOperation? = null,
    val isNewInput: Boolean = false,
    val timerSeconds: Int = 0,
    val timerRunning: Boolean = false,
)

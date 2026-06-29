package ru.alarmtrade.specmp.feature.calculator.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.alarmtrade.specmp.core.common.AppDispatchers
import ru.alarmtrade.specmp.core.ui.effect.sideEffectFlow
import ru.alarmtrade.specmp.feature.calculator.domain.model.CalculatorOperation

class CalculatorViewModel(
    private val dispatchers: AppDispatchers
) : ViewModel() {

    private var timerJob: Job? = null

    private val _state = MutableStateFlow(CalculatorState())
    val state: StateFlow<CalculatorState> = _state.asStateFlow()

    private val _effect = sideEffectFlow<CalculatorEffect>()
    val effect: SharedFlow<CalculatorEffect> = _effect.asSharedFlow()

    private fun emitEffect(effect: CalculatorEffect) {
        _effect.tryEmit(effect)
    }

    fun onDigit(digit: String) {
        _state.update { current ->
            val newDisplay = if (current.isNewInput || current.display == "0") {
                digit
            } else {
                current.display + digit
            }
            current.copy(display = newDisplay, isNewInput = false)
        }
    }

    fun onDecimal() {
        _state.update { current ->
            val display = if (current.isNewInput) "0" else current.display
            if (display.contains('.')) return@update current
            current.copy(display = "$display.", isNewInput = false)
        }
    }

    fun onOperation(op: CalculatorOperation) {
        _state.update { current ->
            val operand = current.display.toDoubleOrNull() ?: return@update current
            current.copy(firstOperand = operand, operation = op, isNewInput = true)
        }
    }

    fun onEqual() {
        val current = _state.value
        val first = current.firstOperand ?: return
        val second = current.display.toDoubleOrNull() ?: return
        if (current.operation is CalculatorOperation.Divide && second == 0.0) {
            emitEffect(CalculatorEffect.DivideByZero)
            return
        }
        val result = when (current.operation) {
            is CalculatorOperation.Add -> first + second
            is CalculatorOperation.Subtract -> first - second
            is CalculatorOperation.Multiply -> first * second
            is CalculatorOperation.Divide -> first / second
            null -> return
        }
        val resultStr = if (result == result.toLong().toDouble()) result.toLong().toString()
        else result.toString()
        _state.update {
            it.copy(display = resultStr, firstOperand = null, operation = null, isNewInput = true)
        }
    }

    fun onClear() {
        _state.update { CalculatorState() }
    }

    fun onDelete() {
        _state.update { current ->
            if (current.isNewInput || current.display == "0") return@update current
            val newDisplay = if (current.display.length == 1) "0" else current.display.dropLast(1)
            current.copy(display = newDisplay)
        }
    }

    fun onTimerStart() {
        if (_state.value.timerRunning) return
        _state.update { it.copy(timerRunning = true) }
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1_000L)
                _state.update { it.copy(timerSeconds = it.timerSeconds + 1) }
            }
        }
    }

    fun onTimerStop() {
        timerJob?.cancel()
        timerJob = null
        _state.update { it.copy(timerRunning = false) }
    }

    fun onTimerReset() {
        timerJob?.cancel()
        timerJob = null
        _state.update { it.copy(timerSeconds = 0, timerRunning = false) }
    }
}

package ru.alarmtrade.specmp.core.ui.lce

import androidx.compose.runtime.Immutable

/** [E] — ошибка фичи (`sealed interface` в `presentation/model`); правила — docs/patterns.md, «Ошибки фичи» и «LCE». */
@Immutable
sealed interface LCEState<out T, out E> {
    /** Загрузка ещё не запускалась. */
    data object None : LCEState<Nothing, Nothing>

    /** Первая загрузка: показать нечего. */
    data object Loading : LCEState<Nothing, Nothing>

    /** [isUpdating]/[updateError] — фоновая операция поверх показанного контента и её ошибка. */
    data class Content<out T, out E>(val content: T, val isUpdating: Boolean = false, val updateError: E? = null) :
        LCEState<T, E>

    data class Error<out E>(val error: E) : LCEState<Nothing, E>
}

package ru.alarmtrade.specmp.core.ui.lce

import androidx.compose.runtime.Immutable

/**
 * Loading-Content-Error (LCE) — состояние загрузки для UI-слоя (VM state → Compose).
 *
 * [T] — контент, [E] — ошибка **фичи**: её `sealed interface` в `presentation/model` со своими подтипами и
 * подтипом-обёрткой над общей ошибкой (`data class Common(val error: AppError)`). Compose разбирает ошибку
 * исчерпывающим `when` и приводит к локализованной строке; общие случаи — `AppError.message()`.
 * Правила — docs/patterns.md, «Ошибки фичи» и «LCE».
 *
 * ```kotlin
 * val brands: LCEState<List<CarBrand>, CatalogError> = LCEState.None
 *
 * _state.update { it.copy(brands = LCEState.Loading) }
 * val response = repository.brands(query)
 * _state.update { it.copy(brands = response.asLCEState(CatalogError::from)) }
 *
 * // Compose
 * when (val brands = state.brands) {
 *     LCEState.None, LCEState.Loading -> Progress()
 *     is LCEState.Content -> BrandList(brands.content)
 *     is LCEState.Error -> ErrorBox(brands.error.title())
 * }
 * ```
 */
@Immutable
sealed interface LCEState<out T, out E> {
    /** Загрузка ещё не запускалась. */
    data object None : LCEState<Nothing, Nothing>

    /** Первая загрузка: показать нечего. */
    data object Loading : LCEState<Nothing, Nothing>

    /**
     * Контент показан. [isUpdating] — поверх него идёт фоновая операция (догрузка страницы, обновление);
     * [updateError] — она не удалась, а контент остаётся (баннер с повтором вместо экрана ошибки).
     */
    data class Content<out T, out E>(val content: T, val isUpdating: Boolean = false, val updateError: E? = null) :
        LCEState<T, E>

    /** Загрузка не удалась, контента нет. */
    data class Error<out E>(val error: E) : LCEState<Nothing, E>
}

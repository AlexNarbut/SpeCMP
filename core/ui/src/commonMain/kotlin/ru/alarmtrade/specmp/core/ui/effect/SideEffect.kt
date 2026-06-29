package ru.alarmtrade.specmp.core.ui.effect

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow

/**
 * One-shot события (навигация, snackbar и т.п.) из ViewModel в UI.
 * См. `docs/patterns.md` — раздел «One-shot события (Effects)».
 *
 * Этот файл изолирует обе стороны механизма SideEffect:
 *  - [sideEffectFlow] — создание потока на стороне ViewModel со стандартной конфигурацией;
 *  - [collectSideEffect] — lifecycle-aware сбор на стороне Compose UI.
 */

/**
 * Создаёт [MutableSharedFlow] для one-shot эффектов со стандартной конфигурацией проекта:
 * `extraBufferCapacity = 1` + `tryEmit` (см. `docs/patterns.md`). `replay = 0` —
 * повтор события после пересоздания экрана не нужен (иначе двойная навигация).
 *
 *
 * ```kotlin
 * private val _effect = sideEffectFlow<CalculatorEffect>()
 * val effect: SharedFlow<CalculatorEffect> = _effect.asSharedFlow()
 *
 * private fun emitEffect(effect: CalculatorEffect) { _effect.tryEmit(effect) }
 * ```
 */
fun <T> sideEffectFlow(): MutableSharedFlow<T> =
    MutableSharedFlow(replay = 0, extraBufferCapacity = 1)

/**
 * Lifecycle-aware сбор one-shot эффектов в Compose.
 *
 * Подписка активна только пока экран в состоянии не ниже [minActiveState] и
 * приостанавливается при уходе в фон — событие, эмитнутое «в пустоту», теряется
 * (поэтому эмитить эффекты только в ответ на действия пользователя, не из `init`).
 *
 * [onEffect] оборачивается в [rememberUpdatedState], поэтому пересоздание лямбды при
 * рекомпозиции НЕ перезапускает сбор, но вызывается всегда её актуальная версия.
 *
 * ```kotlin
 * viewModel.effect.collectSideEffect { effect ->
 *     when (effect) {
 *         is CalculatorEffect.NavigateBack -> onBack()
 *     }
 * }
 * ```
 */
@Composable
fun <T> Flow<T>.collectSideEffect(
    minActiveState: Lifecycle.State = Lifecycle.State.STARTED,
    onEffect: suspend (effect: T) -> Unit,
) {
    val effectFlow = this
    val lifecycleOwner = LocalLifecycleOwner.current
    val currentOnEffect by rememberUpdatedState(onEffect)

    LaunchedEffect(effectFlow, lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(minActiveState) {
            effectFlow.collect { currentOnEffect(it) }
        }
    }
}

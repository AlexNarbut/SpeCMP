package ru.alarmtrade.specmp.core.ui.effect

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow

/*
 * One-shot события (навигация, snackbar и т.п.) из ViewModel в UI — `docs/patterns.md`, «One-shot события (Effects)».
 * Emit-сторона — `postSideEffect` в `BaseViewModel`, сбор в Compose — [collectSideEffect].
 */

/**
 * Поток эффектов `BaseViewModel`: `replay = 0` — повтор события после пересоздания экрана не нужен (иначе двойная
 * навигация). Буфер больше одного: обработчик в [collectSideEffect] может надолго suspend'иться (показ снекбара), и
 * эффекты, эмитнутые в это время, не должны теряться; при переполнении отбрасываются самые старые.
 */
internal fun <T> sideEffectFlow(): MutableSharedFlow<T> = MutableSharedFlow(
    replay = 0,
    extraBufferCapacity = SIDE_EFFECT_BUFFER,
    onBufferOverflow = BufferOverflow.DROP_OLDEST,
)

private const val SIDE_EFFECT_BUFFER = 16

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
 * viewModel.sideEffect.collectSideEffect { effect ->
 *     when (effect) {
 *         is LoginEffect.NavigateBack -> onBack()
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

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

/**
 * replay = 0 — повтор после пересоздания экрана дал бы двойную навигацию; буфер > 1 — обработчик может надолго
 * suspend'иться (снекбар), при переполнении отбрасываются самые старые.
 */
internal fun <T> sideEffectFlow(): MutableSharedFlow<T> = MutableSharedFlow(
    replay = 0,
    extraBufferCapacity = SIDE_EFFECT_BUFFER,
    onBufferOverflow = BufferOverflow.DROP_OLDEST,
)

private const val SIDE_EFFECT_BUFFER = 16

/** Сбор только пока экран не ниже [minActiveState]: эффект, эмитнутый в фоне, теряется. */
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

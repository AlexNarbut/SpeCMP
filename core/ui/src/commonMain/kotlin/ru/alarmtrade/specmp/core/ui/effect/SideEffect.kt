package ru.alarmtrade.specmp.core.ui.effect

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.flow.Flow

/**
 * Сбор только пока экран не ниже [minActiveState]; обработчики идут по очереди, поэтому [onEffect] не ждёт UI —
 * снекбар и прочее ожидание пользователя запускаются в `rememberCoroutineScope()` (detekt `SideEffectAwaitsUi`).
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

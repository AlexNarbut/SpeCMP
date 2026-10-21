package ru.alarmtrade.specmp.core.lifecycle

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn

internal actual fun platformAppLifecycle(): AppLifecycle = ProcessAppLifecycle

/**
 * `ProcessLifecycleOwner`: `ON_START` — приложение снова видно, `ON_STOP` — ушло в фон (Android шлёт его с задержкой
 * ~700 мс, чтобы не срабатывать на смену конфигурации).
 */
private object ProcessAppLifecycle : AppLifecycle {

    override val events: Flow<AppLifecycleEvent> = callbackFlow {
        // addObserver синхронно «догоняет» текущее состояние (ON_CREATE/ON_START) — это не смена состояния, пропускаем.
        var attached = false
        val observer = object : DefaultLifecycleObserver {
            override fun onStart(owner: LifecycleOwner) {
                if (attached) trySend(AppLifecycleEvent.FOREGROUND)
            }

            override fun onStop(owner: LifecycleOwner) {
                if (attached) trySend(AppLifecycleEvent.BACKGROUND)
            }
        }
        val lifecycle = ProcessLifecycleOwner.get().lifecycle
        lifecycle.addObserver(observer)
        attached = true
        awaitClose { lifecycle.removeObserver(observer) }
    }.flowOn(Dispatchers.Main.immediate) // Lifecycle требует main thread
}

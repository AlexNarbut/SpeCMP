package ru.alarmtrade.specmp.core.lifecycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest

private class TestAppLifecycle : AppLifecycle {
    override val events = MutableSharedFlow<AppLifecycleEvent>(extraBufferCapacity = 8)
}

class AppForegroundProviderTest {

    @Test
    fun `returns emit only on foreground events`() = runTest {
        val lifecycle = TestAppLifecycle()
        val provider: AppForegroundProvider = AppLifecycleForegroundProvider(lifecycle)
        var returns = 0
        val job = launch(UnconfinedTestDispatcher(testScheduler)) { provider.foregroundReturns.collect { returns++ } }

        lifecycle.events.emit(AppLifecycleEvent.BACKGROUND)
        lifecycle.events.emit(AppLifecycleEvent.FOREGROUND)
        lifecycle.events.emit(AppLifecycleEvent.BACKGROUND)
        lifecycle.events.emit(AppLifecycleEvent.FOREGROUND)

        assertEquals(2, returns)
        job.cancel()
    }
}

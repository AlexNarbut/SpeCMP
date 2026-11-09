package ru.alarmtrade.specmp.core.testing

import kotlinx.coroutines.flow.MutableSharedFlow
import ru.alarmtrade.specmp.core.lifecycle.AppForegroundProvider

class FakeAppForegroundProvider : AppForegroundProvider {
    override val foregroundReturns = MutableSharedFlow<Unit>()
}

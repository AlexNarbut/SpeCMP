package ru.alarmtrade.specmp.core.testing

import kotlinx.coroutines.CoroutineDispatcher
import ru.alarmtrade.specmp.core.common.AppDispatchers

fun testDispatchers(dispatcher: CoroutineDispatcher): AppDispatchers =
    AppDispatchers(dispatcher, dispatcher, dispatcher)

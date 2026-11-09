package ru.alarmtrade.specmp.core.common

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

data class AppDispatchers(
    val io: CoroutineDispatcher = Dispatchers.IO,

    val default: CoroutineDispatcher = Dispatchers.Default,

    val main: CoroutineDispatcher = Dispatchers.Main,
)

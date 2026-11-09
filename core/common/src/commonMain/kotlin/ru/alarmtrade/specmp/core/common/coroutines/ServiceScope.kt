package ru.alarmtrade.specmp.core.common.coroutines

import co.touchlab.kermit.Logger
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob

/**
 * Scope синглтона Koin: живёт столько же, сколько объект; сбой одной корутины не отменяет соседние и не роняет
 * процесс. [parent] — только для тестов (привязка к `backgroundScope`, гасится в конце теста).
 */
fun serviceScope(
    dispatcher: CoroutineDispatcher,
    logTag: String = DEFAULT_LOG_TAG,
    parent: CoroutineContext = EmptyCoroutineContext,
): CoroutineScope = CoroutineScope(
    parent + SupervisorJob(parent[Job]) + dispatcher + CoroutineExceptionHandler { _, error ->
        Logger.withTag(logTag).e(error) { "Uncaught failure in service scope" }
    },
)

private const val DEFAULT_LOG_TAG = "ServiceScope"

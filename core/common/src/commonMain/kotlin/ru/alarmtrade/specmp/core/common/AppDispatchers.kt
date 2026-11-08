package ru.alarmtrade.specmp.core.common

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

/**
 * Centralized wrapper for coroutine dispatchers used across the application.
 *
 * This class provides access to common dispatchers with semantic names
 * that make their intended use clear.
 *
 * ## Usage
 * ```kotlin
 * class MyRepository(private val dispatchers: AppDispatchers) {
 *     suspend fun fetchData() = withContext(dispatchers.io) {
 *         // Network or database operation
 *     }
 *
 *     suspend fun processData(data: Data) = withContext(dispatchers.default) {
 *         // CPU-intensive processing
 *     }
 * }
 * ```
 *
 * ## Testing
 * In tests, you can inject custom dispatchers:
 * ```kotlin
 * val testDispatchers = AppDispatchers(
 *     io = UnconfinedTestDispatcher(),
 *     default = UnconfinedTestDispatcher(),
 *     main = UnconfinedTestDispatcher()
 * )
 * ```
 */
data class AppDispatchers(
    /**
     * IO dispatcher for network and database operations.
     * Maps to Dispatchers.IO.
     */
    val io: CoroutineDispatcher = Dispatchers.IO,

    /**
     * Default dispatcher for CPU-intensive work.
     * Maps to Dispatchers.Default.
     */
    val default: CoroutineDispatcher = Dispatchers.Default,

    /**
     * Main dispatcher for UI updates.
     * Maps to Dispatchers.Main.
     */
    val main: CoroutineDispatcher = Dispatchers.Main,
)

package ru.alarmtrade.specmp.core.common

/**
 * A generic class that represents the result of an operation.
 *
 * ## ARCHITECTURAL RULE
 * All suspend functions and asynchronous operations MUST return entities wrapped in Response<T>.
 *
 * ## Response vs LCEState
 * - **Response**: Used in business logic layer (repositories, use cases)
 *   - Only Success or Error states
 *   - No Loading state (loading is UI concern)
 * - **LCEState**: Used in UI layer (ViewModels, Compose)
 *   - Has Loading, Content, Error, None states
 *   - Convert Response to LCEState using `.asLCEState()`
 *
 * ## Usage
 * ```kotlin
 * // Repository layer
 * suspend fun getUser(id: String): Response<User> {
 *     return try {
 *         val user = apiClient.fetchUser(id)
 *         Response.Success(user)
 *     } catch (e: Exception) {
 *         Response.Error(e, "Failed to fetch user")
 *     }
 * }
 *
 * // Use case layer
 * suspend fun invoke(id: String): Response<User> {
 *     return repository.getUser(id)
 * }
 *
 * // UI layer - convert to LCEState
 * val uiState: StateFlow<LCEState<User>> = flow {
 *     emit(useCase().asLCEState { it.message ?: "Error" })
 * }.stateIn(viewModelScope, SharingStarted.Lazily, LCEState.Loading())
 * ```
 *
 * @see ResponseExt for extension functions
 * @see LCEState for UI state management
 */
sealed class Response<out T> {
    /**
     * Represents a successful result with value.
     */
    data class Success<out T>(val value: T) : Response<T>()

    /**
     * Represents an error result with exception and optional message.
     */
    class Error(val exception: Throwable, val message: String? = exception.message) : Response<Nothing>()
}

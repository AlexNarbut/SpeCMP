package ru.alarmtrade.specmp.core.ui.lce

import androidx.compose.runtime.Immutable

/**
 * Loading-Content-Error (LCE) state for UI layer.
 *
 * This class is used in ViewModels to represent UI states.
 * Unlike Response (which is for business logic), LCEState includes Loading state.
 *
 * ## States:
 * - **None**: Initial state, no data and no loading
 * - **Loading**: Data is being fetched
 * - **Content**: Data successfully loaded
 * - **Error**: An error occurred
 *
 * ## Usage in ViewModel:
 * ```kotlin
 * class UserViewModel : ViewModel() {
 *     private val _userState = MutableStateFlow<LCEState<User>>(LCEState.None())
 *     val userState: StateFlow<LCEState<User>> = _userState.asStateFlow()
 *
 *     fun loadUser(id: String) {
 *         viewModelScope.launch {
 *             _userState.update { LCEState.Loading() }
 *             val response = getUserUseCase(id)
 *             _userState.update { response.asLCEState { it.message ?: "Error" } }
 *         }
 *     }
 * }
 * ```
 *
 * ## Usage in Compose:
 * ```kotlin
 * val userState by viewModel.userState.collectAsState()
 * when (userState) {
 *     is LCEState.None -> { /* Show placeholder */ }
 *     is LCEState.Loading -> { CircularProgressIndicator() }
 *     is LCEState.Content -> { UserCard(userState.content) }
 *     is LCEState.Error -> { ErrorMessage(userState.error) }
 * }
 * ```
 *
 * @see ru.alarmtrade.specmp.core.common.Response for business logic results
 */
@Immutable
sealed class LCEState<out T> {
    /**
     * Initial state - no data, not loading.
     */
    class None : LCEState<Nothing>() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || this::class != other::class) return false
            return true
        }

        override fun hashCode(): Int = 0
    }

    /**
     * Loading state - data is being fetched.
     */
    class Loading : LCEState<Nothing>() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || this::class != other::class) return false
            return true
        }

        override fun hashCode(): Int = 1
    }

    /**
     * Content state - data successfully loaded.
     * @param content The loaded data
     */
    data class Content<T>(val content: T) : LCEState<T>()

    /**
     * Error state - an error occurred.
     * @param error Error message to display to the user
     */
    class Error(val error: String) : LCEState<Nothing>()
}

package ru.alarmtrade.specmp.core.ui.view.dialog

import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import ru.alarmtrade.specmp.core.ui.view.button.CmpTextButton
import ru.alarmtrade.specmp.core.ui.view.text.CmpText

data class CmpSnackbarData(val message: String, val actionLabel: String? = null, val withDismissAction: Boolean = false)

enum class CmpSnackbarDuration { Short, Long, Indefinite }

enum class CmpSnackbarResult { Dismissed, ActionPerformed }

/**
 * Wrapper around [SnackbarHostState] — feature modules interact only with this class,
 * never with the material3 type directly.
 */
class CmpSnackbarHostState {
    internal val delegate = SnackbarHostState()

    suspend fun showSnackbar(
        message: String,
        actionLabel: String? = null,
        withDismissAction: Boolean = false,
        duration: CmpSnackbarDuration = CmpSnackbarDuration.Short,
    ): CmpSnackbarResult = delegate.showSnackbar(
        message = message,
        actionLabel = actionLabel,
        withDismissAction = withDismissAction,
        duration = duration.toMaterial(),
    ).toCmp()

    private fun CmpSnackbarDuration.toMaterial(): SnackbarDuration = when (this) {
        CmpSnackbarDuration.Short -> SnackbarDuration.Short
        CmpSnackbarDuration.Long -> SnackbarDuration.Long
        CmpSnackbarDuration.Indefinite -> SnackbarDuration.Indefinite
    }

    private fun SnackbarResult.toCmp(): CmpSnackbarResult = when (this) {
        SnackbarResult.Dismissed -> CmpSnackbarResult.Dismissed
        SnackbarResult.ActionPerformed -> CmpSnackbarResult.ActionPerformed
    }
}

@Composable
fun rememberCmpSnackbarHostState(): CmpSnackbarHostState = remember { CmpSnackbarHostState() }

@Composable
fun CmpSnackbarHost(hostState: CmpSnackbarHostState, modifier: Modifier = Modifier) {
    SnackbarHost(
        hostState = hostState.delegate,
        modifier = modifier,
    )
}

@Composable
fun CmpSnackbar(
    data: CmpSnackbarData,
    modifier: Modifier = Modifier,
    onActionClick: (() -> Unit)? = null,
    onDismiss: (() -> Unit)? = null,
) {
    Snackbar(
        modifier = modifier,
        action = if (data.actionLabel != null && onActionClick != null) {
            { CmpTextButton(text = data.actionLabel, onClick = onActionClick) }
        } else {
            null
        },
        dismissAction = if (data.withDismissAction && onDismiss != null) {
            { CmpTextButton(text = "✕", onClick = onDismiss) }
        } else {
            null
        },
    ) {
        CmpText(text = data.message)
    }
}

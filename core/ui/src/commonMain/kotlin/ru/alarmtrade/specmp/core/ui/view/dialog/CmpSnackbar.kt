package ru.alarmtrade.specmp.core.ui.view.dialog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.SnackbarVisuals
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import ru.alarmtrade.specmp.core.ui.theme.CmpTheme
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpRadius
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSize
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSpacing
import ru.alarmtrade.specmp.core.ui.view.button.CmpIconButton
import ru.alarmtrade.specmp.core.ui.view.effect.cmpPressable
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcon
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.core.ui.view.text.CmpTextStyle

data class CmpSnackbarData(
    val message: String,
    val actionLabel: String? = null,
    val withDismissAction: Boolean = false,
    val tone: CmpSnackbarTone? = null,
)

enum class CmpSnackbarDuration { Short, Long, Indefinite }

enum class CmpSnackbarResult { Dismissed, ActionPerformed }

/** Иконка статуса слева в снекбаре. */
enum class CmpSnackbarTone { Success, Error, Info }

class CmpSnackbarHostState {
    internal val delegate = SnackbarHostState()

    suspend fun showSnackbar(
        message: String,
        actionLabel: String? = null,
        withDismissAction: Boolean = false,
        duration: CmpSnackbarDuration = CmpSnackbarDuration.Short,
        tone: CmpSnackbarTone? = null,
    ): CmpSnackbarResult = delegate.showSnackbar(
        ToneVisuals(message, actionLabel, withDismissAction, duration.toMaterial(), tone),
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

private class ToneVisuals(
    override val message: String,
    override val actionLabel: String?,
    override val withDismissAction: Boolean,
    override val duration: SnackbarDuration,
    val tone: CmpSnackbarTone?,
) : SnackbarVisuals

@Composable
fun rememberCmpSnackbarHostState(): CmpSnackbarHostState = remember { CmpSnackbarHostState() }

@Composable
fun CmpSnackbarHost(hostState: CmpSnackbarHostState, modifier: Modifier = Modifier) {
    SnackbarHost(hostState = hostState.delegate, modifier = modifier) { data ->
        val visuals = data.visuals
        CmpSnackbar(
            data = CmpSnackbarData(
                message = visuals.message,
                actionLabel = visuals.actionLabel,
                withDismissAction = visuals.withDismissAction,
                tone = (visuals as? ToneVisuals)?.tone,
            ),
            onActionClick = data::performAction,
            onDismiss = data::dismiss,
        )
    }
}

/** Снекбар: заливка чернилами, текст Paragraph2, иконка тона, действие — синим капсом; радиус `CmpRadius.snackbar`. */
@Composable
fun CmpSnackbar(
    data: CmpSnackbarData,
    modifier: Modifier = Modifier,
    onActionClick: (() -> Unit)? = null,
    onDismiss: (() -> Unit)? = null,
) {
    val colors = CmpTheme.colors
    val shape = RoundedCornerShape(CmpRadius.snackbar)
    Row(
        modifier = modifier
            .padding(CmpSpacing.space12)
            .fillMaxWidth()
            .heightIn(min = CmpSize.touchMin)
            .background(colors.backgroundSurfacePrimary, shape)
            .padding(start = CmpSpacing.space16, end = CmpSpacing.space4),
        horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space12),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        data.tone?.let { tone ->
            val (icon, tint) = when (tone) {
                CmpSnackbarTone.Success -> CmpIcons.CheckCircle to colors.successDefault
                CmpSnackbarTone.Error -> CmpIcons.ErrorOutline to colors.errorDefault
                CmpSnackbarTone.Info -> CmpIcons.Info to colors.infoDefault
            }
            CmpIcon(icon = icon, contentDescription = null, tint = tint, modifier = Modifier.size(CmpSize.iconM))
        }
        CmpText(
            text = data.message,
            style = CmpTextStyle.Paragraph2,
            color = colors.foregroundOnLightPrimaryDefault,
            modifier = Modifier.weight(1f).padding(vertical = CmpSpacing.space12),
        )
        if (data.actionLabel != null && onActionClick != null) {
            CmpText(
                text = data.actionLabel,
                style = CmpTextStyle.Headline3,
                uppercase = true,
                color = MaterialTheme.colorScheme.inversePrimary,
                modifier = Modifier
                    .cmpPressable(onClick = onActionClick, shape = shape)
                    .padding(horizontal = CmpSpacing.space12, vertical = CmpSpacing.space12),
            )
        }
        if (data.withDismissAction && onDismiss != null) {
            CmpIconButton(
                onClick = onDismiss,
                icon = CmpIcons.Close,
                contentDescription = null,
                tint = colors.foregroundOnLightSecondaryDefault,
            )
        }
    }
}

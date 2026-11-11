@file:Suppress("MatchingDeclarationName")

package ru.alarmtrade.specmp.core.ui.view.dialog

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcon
import ru.alarmtrade.specmp.core.ui.view.input.clearFocusOnTapOutside

/** `AlertDialog` M3 ограничен 560 dp; для длинного текста — доля окна в пределах [min]..[max]. */
class CmpDialogWidth(private val fraction: Float, private val min: Dp, private val max: Dp) {

    internal fun resolve(window: Dp): Dp =
        (window * fraction).coerceIn(min, max).coerceAtMost(window - WINDOW_MARGIN * 2)

    companion object {
        private val WINDOW_MARGIN = 16.dp

        val Wide = CmpDialogWidth(fraction = 0.6f, min = 480.dp, max = 840.dp)
    }
}

private val ContentPadding = 24.dp
private val DialogVerticalMargin = 48.dp
private val ButtonsGap = 8.dp
private val SectionGap = 16.dp

@Composable
fun CmpDialog(
    onDismissRequest: () -> Unit,
    confirmButton: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    dismissButton: (@Composable () -> Unit)? = null,
    title: (@Composable () -> Unit)? = null,
    text: (@Composable () -> Unit)? = null,
    icon: ImageVector? = null,
    shape: Shape? = null,
    width: CmpDialogWidth? = null,
) {
    if (width == null) {
        AlertDialog(
            onDismissRequest = onDismissRequest,
            confirmButton = confirmButton,
            modifier = modifier.clearFocusOnTapOutside(),
            dismissButton = dismissButton,
            title = title,
            text = text,
            icon = icon?.let { { CmpIcon(imageVector = it, contentDescription = null) } },
            shape = shape ?: AlertDialogDefaults.shape,
        )
    } else {
        WideDialog(onDismissRequest, confirmButton, modifier, dismissButton, title, text, icon, shape, width)
    }
}

// modifier относится к карточке диалога: корень — окно Dialog, оно вне иерархии layout
@Suppress("ktlint:compose:modifier-not-used-at-root")
@Composable
private fun WideDialog(
    onDismissRequest: () -> Unit,
    confirmButton: @Composable () -> Unit,
    modifier: Modifier,
    dismissButton: (@Composable () -> Unit)?,
    title: (@Composable () -> Unit)?,
    text: (@Composable () -> Unit)?,
    icon: ImageVector?,
    shape: Shape?,
    width: CmpDialogWidth,
) {
    Dialog(onDismissRequest = onDismissRequest, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        // Окно диалога на всю ширину: касание вне карточки закрывает диалог, касание карточки — нет.
        val outside = remember { MutableInteractionSource() }
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .clearFocusOnTapOutside()
                .clickable(
                    interactionSource = outside,
                    indication = null,
                    onClick = onDismissRequest,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Surface(
                modifier = modifier
                    .width(width.resolve(maxWidth))
                    .heightIn(max = maxHeight - DialogVerticalMargin * 2)
                    .pointerInput(Unit) { detectTapGestures { } },
                shape = shape ?: AlertDialogDefaults.shape,
                color = AlertDialogDefaults.containerColor,
                tonalElevation = AlertDialogDefaults.TonalElevation,
            ) {
                Column(modifier = Modifier.padding(ContentPadding)) {
                    DialogHeader(icon, title)
                    text?.let { DialogText(it) }
                    Row(
                        modifier = Modifier.align(Alignment.End).padding(top = SectionGap),
                        horizontalArrangement = Arrangement.spacedBy(ButtonsGap),
                    ) {
                        dismissButton?.invoke()
                        confirmButton()
                    }
                }
            }
        }
    }
}

@Composable
private fun ColumnScope.DialogHeader(icon: ImageVector?, title: (@Composable () -> Unit)?) {
    icon?.let {
        CompositionLocalProvider(LocalContentColor provides AlertDialogDefaults.iconContentColor) {
            CmpIcon(
                imageVector = it,
                contentDescription = null,
                modifier = Modifier.align(Alignment.CenterHorizontally).padding(bottom = SectionGap),
            )
        }
    }
    title?.let {
        CompositionLocalProvider(LocalContentColor provides AlertDialogDefaults.titleContentColor) {
            ProvideTextStyle(MaterialTheme.typography.headlineSmall) {
                Column(modifier = Modifier.padding(bottom = SectionGap)) { it() }
            }
        }
    }
}

@Composable
private fun ColumnScope.DialogText(text: @Composable () -> Unit) {
    CompositionLocalProvider(LocalContentColor provides AlertDialogDefaults.textContentColor) {
        ProvideTextStyle(MaterialTheme.typography.bodyMedium) {
            Column(modifier = Modifier.weight(1f, fill = false).verticalScroll(rememberScrollState())) { text() }
        }
    }
}

package ru.alarmtrade.specmp.feature.device.presentation.core.pin

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import kotlin.time.Duration
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.deviceinteraction_pin_a11y_input
import ru.alarmtrade.specmp.core.localization.resources.deviceinteraction_pin_btn_cancel
import ru.alarmtrade.specmp.core.localization.resources.deviceinteraction_pin_btn_submit
import ru.alarmtrade.specmp.core.localization.resources.deviceinteraction_pin_error_wrong
import ru.alarmtrade.specmp.core.localization.resources.deviceinteraction_pin_msg_locked
import ru.alarmtrade.specmp.core.localization.resources.deviceinteraction_pin_title
import ru.alarmtrade.specmp.core.ui.theme.cmpThemeColors
import ru.alarmtrade.specmp.core.ui.view.button.CmpButton
import ru.alarmtrade.specmp.core.ui.view.button.CmpTextButton
import ru.alarmtrade.specmp.core.ui.view.dialog.CmpDialog
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.icon.imageVector
import ru.alarmtrade.specmp.core.ui.view.input.CmpTextField
import ru.alarmtrade.specmp.core.ui.view.layout.CmpBox
import ru.alarmtrade.specmp.core.ui.view.layout.CmpColumn
import ru.alarmtrade.specmp.core.ui.view.layout.CmpRow
import ru.alarmtrade.specmp.core.ui.view.progress.CmpCircularProgressIndicator
import ru.alarmtrade.specmp.core.ui.view.surface.CmpSurface
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.core.ui.view.text.CmpTextStyle

private val ButtonHeight = 48.dp
private val DialogMaxWidth = 440.dp
private const val PIN_LENGTH = 4
private const val MILLIS_IN_SECOND = 1_000L

const val SERVICE_PIN_TAG_PREFIX: String = "device_pin"

/** @property wrongCount счётчик неверных попыток: каждое изменение очищает поле. */
@Immutable
data class ServicePinDialogState(
    val sending: Boolean = false,
    val wrong: Boolean = false,
    val wrongCount: Int = 0,
    val lockedFor: Duration? = null,
)

@Composable
fun ServicePinDialog(
    message: String,
    state: ServicePinDialogState,
    onDismiss: () -> Unit,
    onSubmit: (String) -> Unit,
    modifier: Modifier = Modifier,
    testTagPrefix: String = SERVICE_PIN_TAG_PREFIX,
) {
    var pin by remember { mutableStateOf("") }
    val locked = state.lockedFor != null
    val canType = !locked && !state.sending
    // Неверный ввод — поле очищается (значение PIN нигде, кроме этого поля, не хранится).
    LaunchedEffect(state.wrongCount) { pin = "" }
    CmpDialog(
        onDismissRequest = onDismiss,
        modifier = modifier.widthIn(max = DialogMaxWidth),
        icon = CmpIcons.Lock.imageVector(),
        title = {
            CmpText(text = stringResource(Strings.string.deviceinteraction_pin_title), style = CmpTextStyle.Paragraph2)
        },
        text = {
            CmpColumn(
                verticalArrangement = Arrangement.spacedBy(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                CmpText(text = message, style = CmpTextStyle.Paragraph2)
                PinCells(
                    pin = pin,
                    onPinChange = { pin = it },
                    isError = state.wrong,
                    enabled = canType,
                    inputTag = "${testTagPrefix}_input",
                    onDone = { if (pin.length == PIN_LENGTH) onSubmit(pin) },
                )
                PinStatus(state = state, lockedTag = "${testTagPrefix}_locked")
            }
        },
        dismissButton = {
            CmpTextButton(
                text = stringResource(Strings.string.deviceinteraction_pin_btn_cancel),
                onClick = onDismiss,
                enabled = !state.sending,
                modifier = Modifier.height(ButtonHeight).testTag("${testTagPrefix}_cancel"),
            )
        },
        confirmButton = {
            CmpButton(
                onClick = { onSubmit(pin) },
                enabled = canType && pin.length == PIN_LENGTH,
                modifier = Modifier.height(ButtonHeight).testTag("${testTagPrefix}_submit"),
            ) {
                if (state.sending) {
                    CmpCircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.5.dp)
                } else {
                    CmpText(
                        text = stringResource(Strings.string.deviceinteraction_pin_btn_submit),
                        style = CmpTextStyle.Paragraph2,
                    )
                }
            }
        },
    )
}

@Composable
private fun PinStatus(state: ServicePinDialogState, lockedTag: String) {
    val colors = cmpThemeColors()
    val seconds = state.lockedFor?.let { left -> (left.inWholeMilliseconds + MILLIS_IN_SECOND - 1) / MILLIS_IN_SECOND }
    CmpColumn(
        modifier = Modifier.heightIn(min = 44.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (state.wrong) {
            CmpText(
                text = stringResource(Strings.string.deviceinteraction_pin_error_wrong),
                style = CmpTextStyle.Headline3,
                color = colors.error,
            )
        }
        if (seconds != null) {
            CmpText(
                text = stringResource(Strings.string.deviceinteraction_pin_msg_locked, seconds.toInt()),
                color = colors.onSurfaceVariant,
                style = CmpTextStyle.Paragraph2,
                modifier = Modifier.testTag(lockedTag),
            )
        }
    }
}

@Composable
private fun PinCells(
    pin: String,
    onPinChange: (String) -> Unit,
    isError: Boolean,
    enabled: Boolean,
    inputTag: String,
    onDone: () -> Unit,
) {
    val colors = cmpThemeColors()
    val focus = remember { FocusRequester() }
    // Клавиатура открывается сразу и возвращается, когда блок снова принимает ввод.
    LaunchedEffect(enabled) { if (enabled) focus.requestFocus() }
    val a11y = stringResource(Strings.string.deviceinteraction_pin_a11y_input)
    CmpBox(contentAlignment = Alignment.Center) {
        CmpRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            repeat(PIN_LENGTH) { index ->
                val active = index == minOf(pin.length, PIN_LENGTH - 1)
                CmpSurface(
                    modifier = Modifier.width(56.dp).height(64.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = colors.surface,
                    border = when {
                        isError -> BorderStroke(2.dp, colors.error)
                        active && enabled -> BorderStroke(2.dp, colors.primary)
                        else -> BorderStroke(1.dp, colors.outline)
                    },
                ) {
                    CmpBox(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        if (index < pin.length) CmpText(text = "•", style = CmpTextStyle.Title1)
                    }
                }
            }
        }
        // Невидимое поле на всю ширину ячеек: касание ячеек фокусирует его.
        CmpTextField(
            value = pin,
            onValueChange = { onPinChange(it.filter(Char::isDigit).take(PIN_LENGTH)) },
            modifier = Modifier
                .matchParentSize()
                .alpha(0f)
                .focusRequester(focus)
                .testTag(inputTag)
                .semantics { contentDescription = a11y },
            enabled = enabled,
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { onDone() }),
        )
    }
}

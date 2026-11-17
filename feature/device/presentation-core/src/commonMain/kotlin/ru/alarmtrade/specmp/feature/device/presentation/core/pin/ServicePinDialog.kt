package ru.alarmtrade.specmp.feature.device.presentation.core.pin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlin.time.Duration
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.deviceinteraction_pin_a11y_clear
import ru.alarmtrade.specmp.core.localization.resources.deviceinteraction_pin_a11y_input
import ru.alarmtrade.specmp.core.localization.resources.deviceinteraction_pin_btn_cancel
import ru.alarmtrade.specmp.core.localization.resources.deviceinteraction_pin_btn_submit
import ru.alarmtrade.specmp.core.localization.resources.deviceinteraction_pin_error_wrong
import ru.alarmtrade.specmp.core.localization.resources.deviceinteraction_pin_msg_locked
import ru.alarmtrade.specmp.core.localization.resources.deviceinteraction_pin_title
import ru.alarmtrade.specmp.core.ui.theme.CmpTheme
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSpacing
import ru.alarmtrade.specmp.core.ui.view.button.CmpButton
import ru.alarmtrade.specmp.core.ui.view.button.CmpButtonSize
import ru.alarmtrade.specmp.core.ui.view.button.CmpButtonType
import ru.alarmtrade.specmp.core.ui.view.dialog.CmpDialog
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.icon.imageVector
import ru.alarmtrade.specmp.core.ui.view.input.CmpCodeField
import ru.alarmtrade.specmp.core.ui.view.layout.CmpColumn
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.core.ui.view.text.CmpTextStyle

private val DialogMaxWidth = 440.dp
private val StatusMinHeight = 44.dp
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
            CmpText(text = stringResource(Strings.string.deviceinteraction_pin_title), style = CmpTextStyle.Headline1)
        },
        text = {
            CmpColumn(
                verticalArrangement = Arrangement.spacedBy(CmpSpacing.space12),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                CmpText(text = message, style = CmpTextStyle.Paragraph2, textAlign = TextAlign.Center)
                PinInput(
                    pin = pin,
                    onPinChange = { pin = it },
                    isError = state.wrong,
                    enabled = canType,
                    inputTag = "${testTagPrefix}_input",
                    onDone = { if (canType && pin.length == PIN_LENGTH) onSubmit(pin) },
                )
                PinStatus(state = state, lockedTag = "${testTagPrefix}_locked")
            }
        },
        dismissButton = {
            CmpButton(
                text = stringResource(Strings.string.deviceinteraction_pin_btn_cancel),
                onClick = onDismiss,
                enabled = !state.sending,
                type = CmpButtonType.Tertiary,
                size = CmpButtonSize.S,
                modifier = Modifier.testTag("${testTagPrefix}_cancel"),
            )
        },
        confirmButton = {
            CmpButton(
                text = stringResource(Strings.string.deviceinteraction_pin_btn_submit),
                onClick = { onSubmit(pin) },
                enabled = canType && pin.length == PIN_LENGTH,
                loading = state.sending,
                size = CmpButtonSize.S,
                modifier = Modifier.testTag("${testTagPrefix}_submit"),
            )
        },
    )
}

@Composable
private fun PinStatus(state: ServicePinDialogState, lockedTag: String) {
    val colors = CmpTheme.colors
    val seconds = state.lockedFor?.let { left -> (left.inWholeMilliseconds + MILLIS_IN_SECOND - 1) / MILLIS_IN_SECOND }
    CmpColumn(
        modifier = Modifier.heightIn(min = StatusMinHeight),
        verticalArrangement = Arrangement.spacedBy(CmpSpacing.space4),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (state.wrong) {
            CmpText(
                text = stringResource(Strings.string.deviceinteraction_pin_error_wrong),
                style = CmpTextStyle.Headline3,
                color = colors.errorDefault,
            )
        }
        if (seconds != null) {
            CmpText(
                text = stringResource(Strings.string.deviceinteraction_pin_msg_locked, seconds.toInt()),
                color = colors.foregroundOnDarkSecondaryDefault,
                style = CmpTextStyle.Paragraph2,
                modifier = Modifier.testTag(lockedTag),
            )
        }
    }
}

@Composable
private fun PinInput(
    pin: String,
    onPinChange: (String) -> Unit,
    isError: Boolean,
    enabled: Boolean,
    inputTag: String,
    onDone: () -> Unit,
) {
    val focus = remember { FocusRequester() }
    // Клавиатура открывается сразу и возвращается, когда блок снова принимает ввод.
    LaunchedEffect(enabled) { if (enabled) focus.requestFocus() }
    CmpCodeField(
        value = pin,
        onValueChange = onPinChange,
        label = stringResource(Strings.string.deviceinteraction_pin_a11y_input),
        clearContentDescription = stringResource(Strings.string.deviceinteraction_pin_a11y_clear),
        length = PIN_LENGTH,
        isError = isError,
        secure = true,
        enabled = enabled,
        showClear = false,
        keyboardActions = KeyboardActions(onDone = { onDone() }),
        fieldModifier = Modifier.testTag(inputTag),
        focusRequester = focus,
    )
}

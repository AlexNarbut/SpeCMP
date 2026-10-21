package ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.check.subcomponents

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.ui.theme.cmpThemeColors
import ru.alarmtrade.specmp.core.ui.view.button.CmpButton
import ru.alarmtrade.specmp.core.ui.view.button.CmpTextButton
import ru.alarmtrade.specmp.core.ui.view.dialog.CmpDialog
import ru.alarmtrade.specmp.core.ui.view.dialog.CmpDialogWidth
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
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.components.VersionText
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.check.CheckDialog
import specmp.feature.device.firmware_ui.generated.resources.Res
import specmp.feature.device.firmware_ui.generated.resources.firmware_check_btn_update
import specmp.feature.device.firmware_ui.generated.resources.firmware_check_dialog_btn_confirm
import specmp.feature.device.firmware_ui.generated.resources.firmware_check_dialog_label_full_description
import specmp.feature.device.firmware_ui.generated.resources.firmware_check_dialog_msg_confirm
import specmp.feature.device.firmware_ui.generated.resources.firmware_check_dialog_msg_no_description
import specmp.feature.device.firmware_ui.generated.resources.firmware_check_dialog_title_confirm
import specmp.feature.device.firmware_ui.generated.resources.firmware_check_dialog_title_description
import specmp.feature.device.firmware_ui.generated.resources.firmware_check_pin_a11y_input
import specmp.feature.device.firmware_ui.generated.resources.firmware_check_pin_btn_submit
import specmp.feature.device.firmware_ui.generated.resources.firmware_check_pin_error_wrong
import specmp.feature.device.firmware_ui.generated.resources.firmware_check_pin_msg
import specmp.feature.device.firmware_ui.generated.resources.firmware_check_pin_msg_locked
import specmp.feature.device.firmware_ui.generated.resources.firmware_check_pin_title
import specmp.feature.device.firmware_ui.generated.resources.firmware_common_btn_cancel
import specmp.feature.device.firmware_ui.generated.resources.firmware_common_label_version

private val ButtonHeight = 48.dp
private val DialogMaxWidth = 440.dp
private const val PIN_LENGTH = 4

internal const val ENTER_CONFIRM_TAG = "firmware_enter_confirm"
internal const val PIN_INPUT_TAG = "firmware_pin_input"
internal const val PIN_SUBMIT_TAG = "firmware_pin_submit"
internal const val PIN_CANCEL_TAG = "firmware_pin_cancel"
internal const val PIN_LOCKED_TAG = "firmware_pin_locked"

/** Описание версии: номер и полное описание; «Обновить» ведёт к подтверждению перехода в режим обновления. */
@Composable
internal fun DescriptionDialog(dialog: CheckDialog.Description, onDismiss: () -> Unit, onUpdate: () -> Unit) {
    val colors = cmpThemeColors()
    CmpDialog(
        onDismissRequest = onDismiss,
        width = CmpDialogWidth.Wide,
        title = { CmpText(text = stringResource(Res.string.firmware_check_dialog_title_description)) },
        text = {
            CmpColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                CmpRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CmpText(
                        text = stringResource(Res.string.firmware_common_label_version),
                        color = colors.onSurfaceVariant
                    )
                    VersionText(dialog.version)
                }
                CmpText(
                    text = stringResource(Res.string.firmware_check_dialog_label_full_description),
                    fontWeight = FontWeight.Medium
                )
                CmpText(
                    text = dialog.description.ifBlank {
                        stringResource(Res.string.firmware_check_dialog_msg_no_description)
                    },
                    color = colors.onSurfaceVariant,
                    style = CmpTextStyle.Body
                )
            }
        },
        dismissButton = {
            CmpTextButton(
                text = stringResource(Res.string.firmware_common_btn_cancel),
                onClick = onDismiss,
                modifier = Modifier.height(ButtonHeight)
            )
        },
        confirmButton = {
            CmpTextButton(
                text = stringResource(Res.string.firmware_check_btn_update),
                onClick = onUpdate,
                modifier = Modifier.height(ButtonHeight)
            )
        }
    )
}

/** Подтверждение перехода в режим обновления: блок перезагрузится, обновление надо довести до конца. */
@Composable
internal fun ConfirmBootDialog(dialog: CheckDialog.ConfirmBoot, onDismiss: () -> Unit, onConfirm: () -> Unit) {
    CmpDialog(
        onDismissRequest = onDismiss,
        width = CmpDialogWidth.Wide,
        icon = CmpIcons.PowerSettingsNew.imageVector(),
        title = { CmpText(text = stringResource(Res.string.firmware_check_dialog_title_confirm)) },
        text = {
            CmpText(text = stringResource(Res.string.firmware_check_dialog_msg_confirm), style = CmpTextStyle.Body)
        },
        dismissButton = {
            CmpTextButton(
                text = stringResource(Res.string.firmware_common_btn_cancel),
                onClick = onDismiss,
                enabled = !dialog.sending,
                modifier = Modifier.height(ButtonHeight)
            )
        },
        confirmButton = {
            ProgressButton(
                text = stringResource(Res.string.firmware_check_dialog_btn_confirm),
                sending = dialog.sending,
                enabled = !dialog.sending,
                onClick = onConfirm,
                modifier = Modifier.testTag(ENTER_CONFIRM_TAG)
            )
        }
    )
}

/**
 * PIN программирования: четыре ячейки, невидимое поле ввода поверх них держит цифровую клавиатуру.
 * Пока блок не принимает ввод ([CheckDialog.Pin.lockedFor]), поле и «Продолжить» отключены, внизу — отсчёт.
 */
@Composable
internal fun PinDialog(dialog: CheckDialog.Pin, onDismiss: () -> Unit, onSubmit: (String) -> Unit) {
    var pin by remember { mutableStateOf("") }
    val locked = dialog.lockedFor != null
    val canType = !locked && !dialog.sending
    // Неверный ввод — поле очищается (значение PIN нигде, кроме этого поля, не хранится).
    LaunchedEffect(dialog.wrongCount) { pin = "" }
    CmpDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.widthIn(max = DialogMaxWidth),
        icon = CmpIcons.Lock.imageVector(),
        title = { CmpText(text = stringResource(Res.string.firmware_check_pin_title)) },
        text = {
            CmpColumn(
                verticalArrangement = Arrangement.spacedBy(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CmpText(text = stringResource(Res.string.firmware_check_pin_msg), style = CmpTextStyle.Body)
                PinCells(
                    pin = pin,
                    onPinChange = { pin = it },
                    isError = dialog.wrong,
                    enabled = canType,
                    onDone = { if (pin.length == PIN_LENGTH) onSubmit(pin) }
                )
                PinStatus(dialog)
            }
        },
        dismissButton = {
            CmpTextButton(
                text = stringResource(Res.string.firmware_common_btn_cancel),
                onClick = onDismiss,
                enabled = !dialog.sending,
                modifier = Modifier.height(ButtonHeight).testTag(PIN_CANCEL_TAG)
            )
        },
        confirmButton = {
            ProgressButton(
                text = stringResource(Res.string.firmware_check_pin_btn_submit),
                sending = dialog.sending,
                enabled = canType && pin.length == PIN_LENGTH,
                onClick = { onSubmit(pin) },
                modifier = Modifier.testTag(PIN_SUBMIT_TAG)
            )
        }
    )
}

@Composable
private fun PinStatus(dialog: CheckDialog.Pin) {
    val colors = cmpThemeColors()
    val seconds = dialog.lockedFor?.let { left -> (left.inWholeMilliseconds + MILLIS_IN_SECOND - 1) / MILLIS_IN_SECOND }
    CmpColumn(
        modifier = Modifier.heightIn(min = 44.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (dialog.wrong) {
            CmpText(
                text = stringResource(Res.string.firmware_check_pin_error_wrong),
                fontWeight = FontWeight.SemiBold,
                color = colors.error
            )
        }
        if (seconds != null) {
            CmpText(
                text = stringResource(Res.string.firmware_check_pin_msg_locked, seconds.toInt()),
                color = colors.onSurfaceVariant,
                style = CmpTextStyle.Body,
                modifier = Modifier.testTag(PIN_LOCKED_TAG)
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
    onDone: () -> Unit
) {
    val colors = cmpThemeColors()
    val focus = remember { FocusRequester() }
    // Клавиатура открывается сразу и возвращается, когда блок снова принимает ввод.
    LaunchedEffect(enabled) { if (enabled) focus.requestFocus() }
    val a11y = stringResource(Res.string.firmware_check_pin_a11y_input)
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
                    }
                ) {
                    CmpBox(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        if (index < pin.length) CmpText(text = "•", fontSize = 30.sp)
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
                .testTag(PIN_INPUT_TAG)
                .semantics { contentDescription = a11y },
            enabled = enabled,
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { onDone() })
        )
    }
}

/** Кнопка подтверждения: пока команда уходит — вместо текста индикатор. */
@Composable
private fun ProgressButton(text: String, sending: Boolean, enabled: Boolean, onClick: () -> Unit, modifier: Modifier) {
    CmpButton(onClick = onClick, enabled = enabled, modifier = modifier.height(ButtonHeight)) {
        if (sending) {
            CmpCircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.5.dp)
        } else {
            CmpText(text = text)
        }
    }
}

private const val MILLIS_IN_SECOND = 1_000L

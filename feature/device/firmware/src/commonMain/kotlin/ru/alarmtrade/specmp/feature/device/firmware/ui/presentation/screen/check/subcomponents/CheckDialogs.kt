package ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.check.subcomponents

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.firmware_check_btn_dialog_confirm
import ru.alarmtrade.specmp.core.localization.resources.firmware_check_btn_update
import ru.alarmtrade.specmp.core.localization.resources.firmware_check_label_dialog_full_description
import ru.alarmtrade.specmp.core.localization.resources.firmware_check_msg_dialog_confirm
import ru.alarmtrade.specmp.core.localization.resources.firmware_check_msg_dialog_no_description
import ru.alarmtrade.specmp.core.localization.resources.firmware_check_msg_pin
import ru.alarmtrade.specmp.core.localization.resources.firmware_check_title_dialog_confirm
import ru.alarmtrade.specmp.core.localization.resources.firmware_check_title_dialog_description
import ru.alarmtrade.specmp.core.localization.resources.firmware_common_btn_cancel
import ru.alarmtrade.specmp.core.localization.resources.firmware_common_label_version
import ru.alarmtrade.specmp.core.ui.theme.cmpThemeColors
import ru.alarmtrade.specmp.core.ui.view.button.CmpButton
import ru.alarmtrade.specmp.core.ui.view.button.CmpTextButton
import ru.alarmtrade.specmp.core.ui.view.dialog.CmpDialog
import ru.alarmtrade.specmp.core.ui.view.dialog.CmpDialogWidth
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.icon.imageVector
import ru.alarmtrade.specmp.core.ui.view.layout.CmpColumn
import ru.alarmtrade.specmp.core.ui.view.layout.CmpRow
import ru.alarmtrade.specmp.core.ui.view.progress.CmpCircularProgressIndicator
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.core.ui.view.text.CmpTextStyle
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.components.VersionText
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.check.CheckDialog
import ru.alarmtrade.specmp.feature.device.interaction.ui.presentation.pin.ServicePinDialog
import ru.alarmtrade.specmp.feature.device.interaction.ui.presentation.pin.ServicePinDialogState

private val ButtonHeight = 48.dp

internal const val ENTER_CONFIRM_TAG = "firmware_enter_confirm"
private const val PIN_TAG_PREFIX = "firmware_pin"

/** Описание версии: номер и полное описание; «Обновить» ведёт к подтверждению перехода в режим обновления. */
@Composable
internal fun DescriptionDialog(dialog: CheckDialog.Description, onDismiss: () -> Unit, onUpdate: () -> Unit) {
    val colors = cmpThemeColors()
    CmpDialog(
        onDismissRequest = onDismiss,
        width = CmpDialogWidth.Wide,
        title = { CmpText(text = stringResource(Strings.string.firmware_check_title_dialog_description)) },
        text = {
            CmpColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                CmpRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    CmpText(
                        text = stringResource(Strings.string.firmware_common_label_version),
                        color = colors.onSurfaceVariant,
                    )
                    VersionText(dialog.version)
                }
                CmpText(
                    text = stringResource(Strings.string.firmware_check_label_dialog_full_description),
                    fontWeight = FontWeight.Medium,
                )
                CmpText(
                    text = dialog.description.ifBlank {
                        stringResource(Strings.string.firmware_check_msg_dialog_no_description)
                    },
                    color = colors.onSurfaceVariant,
                    style = CmpTextStyle.Body,
                )
            }
        },
        dismissButton = {
            CmpTextButton(
                text = stringResource(Strings.string.firmware_common_btn_cancel),
                onClick = onDismiss,
                modifier = Modifier.height(ButtonHeight),
            )
        },
        confirmButton = {
            CmpTextButton(
                text = stringResource(Strings.string.firmware_check_btn_update),
                onClick = onUpdate,
                modifier = Modifier.height(ButtonHeight),
            )
        },
    )
}

/** Подтверждение перехода в режим обновления: блок перезагрузится, обновление надо довести до конца. */
@Composable
internal fun ConfirmBootDialog(dialog: CheckDialog.ConfirmBoot, onDismiss: () -> Unit, onConfirm: () -> Unit) {
    CmpDialog(
        onDismissRequest = onDismiss,
        width = CmpDialogWidth.Wide,
        icon = CmpIcons.PowerSettingsNew.imageVector(),
        title = { CmpText(text = stringResource(Strings.string.firmware_check_title_dialog_confirm)) },
        text = {
            CmpText(text = stringResource(Strings.string.firmware_check_msg_dialog_confirm), style = CmpTextStyle.Body)
        },
        dismissButton = {
            CmpTextButton(
                text = stringResource(Strings.string.firmware_common_btn_cancel),
                onClick = onDismiss,
                enabled = !dialog.sending,
                modifier = Modifier.height(ButtonHeight),
            )
        },
        confirmButton = {
            ProgressButton(
                text = stringResource(Strings.string.firmware_check_btn_dialog_confirm),
                sending = dialog.sending,
                enabled = !dialog.sending,
                onClick = onConfirm,
                modifier = Modifier.testTag(ENTER_CONFIRM_TAG),
            )
        },
    )
}

/**
 * PIN программирования для входа в загрузчик — общий [ServicePinDialog] с текстом прошивки; теги — `firmware_pin_*`.
 */
@Composable
internal fun PinDialog(dialog: CheckDialog.Pin, onDismiss: () -> Unit, onSubmit: (String) -> Unit) {
    ServicePinDialog(
        message = stringResource(Strings.string.firmware_check_msg_pin),
        state = ServicePinDialogState(
            sending = dialog.sending,
            wrong = dialog.wrong,
            wrongCount = dialog.wrongCount,
            lockedFor = dialog.lockedFor,
        ),
        onDismiss = onDismiss,
        onSubmit = onSubmit,
        testTagPrefix = PIN_TAG_PREFIX,
    )
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

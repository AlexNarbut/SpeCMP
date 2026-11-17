package ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.check.subcomponents

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
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
import ru.alarmtrade.specmp.core.ui.theme.CmpTheme
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSpacing
import ru.alarmtrade.specmp.core.ui.view.button.CmpButton
import ru.alarmtrade.specmp.core.ui.view.button.CmpButtonSize
import ru.alarmtrade.specmp.core.ui.view.button.CmpButtonType
import ru.alarmtrade.specmp.core.ui.view.dialog.CmpDialog
import ru.alarmtrade.specmp.core.ui.view.dialog.CmpDialogWidth
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.icon.imageVector
import ru.alarmtrade.specmp.core.ui.view.layout.CmpColumn
import ru.alarmtrade.specmp.core.ui.view.layout.CmpRow
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.core.ui.view.text.CmpTextStyle
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.components.VersionText
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.check.CheckDialog
import ru.alarmtrade.specmp.feature.device.presentation.core.pin.ServicePinDialog
import ru.alarmtrade.specmp.feature.device.presentation.core.pin.ServicePinDialogState

internal const val ENTER_CONFIRM_TAG = "firmware_enter_confirm"
private const val PIN_TAG_PREFIX = "firmware_pin"

@Composable
internal fun DescriptionDialog(dialog: CheckDialog.Description, onDismiss: () -> Unit, onUpdate: () -> Unit) {
    val colors = CmpTheme.colors
    CmpDialog(
        onDismissRequest = onDismiss,
        width = CmpDialogWidth.Wide,
        title = {
            CmpText(
                text = stringResource(Strings.string.firmware_check_title_dialog_description),
                style = CmpTextStyle.Headline1,
            )
        },
        text = {
            CmpColumn(verticalArrangement = Arrangement.spacedBy(CmpSpacing.space12)) {
                CmpRow(
                    horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space8),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    CmpText(
                        text = stringResource(Strings.string.firmware_common_label_version),
                        style = CmpTextStyle.Paragraph2,
                        color = colors.foregroundOnDarkSecondaryDefault,
                    )
                    VersionText(dialog.version)
                }
                CmpText(
                    text = stringResource(Strings.string.firmware_check_label_dialog_full_description),
                    style = CmpTextStyle.Headline3,
                )
                CmpText(
                    text = dialog.description.ifBlank {
                        stringResource(Strings.string.firmware_check_msg_dialog_no_description)
                    },
                    color = colors.foregroundOnDarkSecondaryDefault,
                    style = CmpTextStyle.Paragraph2,
                )
            }
        },
        dismissButton = {
            CmpButton(
                text = stringResource(Strings.string.firmware_common_btn_cancel),
                onClick = onDismiss,
                type = CmpButtonType.Tertiary,
                size = CmpButtonSize.S,
            )
        },
        confirmButton = {
            CmpButton(
                text = stringResource(Strings.string.firmware_check_btn_update),
                onClick = onUpdate,
                size = CmpButtonSize.S,
            )
        },
    )
}

@Composable
internal fun ConfirmBootDialog(dialog: CheckDialog.ConfirmBoot, onDismiss: () -> Unit, onConfirm: () -> Unit) {
    CmpDialog(
        onDismissRequest = onDismiss,
        width = CmpDialogWidth.Wide,
        icon = CmpIcons.PowerSettingsNew.imageVector(),
        title = {
            CmpText(
                text = stringResource(Strings.string.firmware_check_title_dialog_confirm),
                style = CmpTextStyle.Headline1,
            )
        },
        text = {
            CmpText(
                text = stringResource(Strings.string.firmware_check_msg_dialog_confirm),
                style = CmpTextStyle.Paragraph2,
            )
        },
        dismissButton = {
            CmpButton(
                text = stringResource(Strings.string.firmware_common_btn_cancel),
                onClick = onDismiss,
                enabled = !dialog.sending,
                type = CmpButtonType.Tertiary,
                size = CmpButtonSize.S,
            )
        },
        confirmButton = {
            CmpButton(
                text = stringResource(Strings.string.firmware_check_btn_dialog_confirm),
                onClick = onConfirm,
                enabled = !dialog.sending,
                loading = dialog.sending,
                size = CmpButtonSize.S,
                modifier = Modifier.testTag(ENTER_CONFIRM_TAG),
            )
        },
    )
}

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

package ru.alarmtrade.specmp.feature.techrequests.presentation.screen.requests.subcomponents

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.techrequests_common_a11y_close
import ru.alarmtrade.specmp.core.localization.resources.techrequests_form_btn_discard
import ru.alarmtrade.specmp.core.localization.resources.techrequests_form_btn_keep_editing
import ru.alarmtrade.specmp.core.localization.resources.techrequests_form_msg_discard
import ru.alarmtrade.specmp.core.localization.resources.techrequests_form_title_discard
import ru.alarmtrade.specmp.core.localization.resources.techrequests_list_btn_back
import ru.alarmtrade.specmp.core.localization.resources.techrequests_list_btn_cancel_request
import ru.alarmtrade.specmp.core.localization.resources.techrequests_list_btn_close
import ru.alarmtrade.specmp.core.localization.resources.techrequests_list_btn_connect
import ru.alarmtrade.specmp.core.localization.resources.techrequests_list_btn_keep
import ru.alarmtrade.specmp.core.localization.resources.techrequests_list_msg_cancel_confirm
import ru.alarmtrade.specmp.core.localization.resources.techrequests_list_msg_close_confirm
import ru.alarmtrade.specmp.core.localization.resources.techrequests_list_msg_no_device
import ru.alarmtrade.specmp.core.localization.resources.techrequests_list_title_cancel_confirm
import ru.alarmtrade.specmp.core.localization.resources.techrequests_list_title_close_confirm
import ru.alarmtrade.specmp.core.localization.resources.techrequests_list_title_no_device
import ru.alarmtrade.specmp.core.ui.view.button.CmpButton
import ru.alarmtrade.specmp.core.ui.view.button.CmpButtonSize
import ru.alarmtrade.specmp.core.ui.view.button.CmpButtonType
import ru.alarmtrade.specmp.core.ui.view.dialog.CmpDialog
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.icon.imageVector
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.core.ui.view.text.CmpTextStyle
import ru.alarmtrade.specmp.feature.techrequests.presentation.model.TechRequestsTags
import ru.alarmtrade.specmp.feature.techrequests.presentation.screen.requests.RequestAction
import ru.alarmtrade.specmp.feature.techrequests.presentation.screen.requests.TechRequestsDialog

@Composable
internal fun TechRequestsDialogHost(
    dialog: TechRequestsDialog?,
    onConfirmAction: () -> Unit,
    onDiscardDraft: () -> Unit,
    onConnectDevice: () -> Unit,
    onDismiss: () -> Unit,
) {
    when (dialog) {
        null -> Unit
        is TechRequestsDialog.Confirm -> ConfirmDialog(dialog, onConfirmAction, onDismiss)
        TechRequestsDialog.DiscardDraft -> DiscardDraftDialog(onDiscardDraft, onDismiss)
        TechRequestsDialog.NoDevice -> NoDeviceDialog(onConnectDevice, onDismiss)
    }
}

@Composable
private fun ConfirmDialog(dialog: TechRequestsDialog.Confirm, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    val number = dialog.id.toString()
    val cancel = dialog.action == RequestAction.Cancel
    CmpDialog(
        onDismissRequest = onDismiss,
        icon = if (cancel) CmpIcons.Block.imageVector() else CmpIcons.CheckCircle.imageVector(),
        title = {
            DialogTitle(
                stringResource(
                    if (cancel) {
                        Strings.string.techrequests_list_title_cancel_confirm
                    } else {
                        Strings.string.techrequests_list_title_close_confirm
                    },
                    number,
                ),
            )
        },
        text = {
            DialogText(
                stringResource(
                    if (cancel) {
                        Strings.string.techrequests_list_msg_cancel_confirm
                    } else {
                        Strings.string.techrequests_list_msg_close_confirm
                    },
                ),
            )
        },
        confirmButton = {
            CmpButton(
                text = stringResource(
                    if (cancel) {
                        Strings.string.techrequests_list_btn_cancel_request
                    } else {
                        Strings.string.techrequests_list_btn_close
                    },
                ),
                onClick = onConfirm,
                type = if (cancel) CmpButtonType.Danger else CmpButtonType.Primary,
                size = CmpButtonSize.S,
                modifier = Modifier.testTag(TechRequestsTags.CONFIRM_YES),
            )
        },
        dismissButton = {
            CmpButton(
                text = stringResource(
                    if (cancel) {
                        Strings.string.techrequests_list_btn_keep
                    } else {
                        Strings.string.techrequests_list_btn_back
                    },
                ),
                onClick = onDismiss,
                type = CmpButtonType.Tertiary,
                size = CmpButtonSize.S,
                modifier = Modifier.testTag(TechRequestsTags.CONFIRM_NO),
            )
        },
    )
}

@Composable
private fun DiscardDraftDialog(onDiscard: () -> Unit, onDismiss: () -> Unit) {
    CmpDialog(
        onDismissRequest = onDismiss,
        title = { DialogTitle(stringResource(Strings.string.techrequests_form_title_discard)) },
        text = { DialogText(stringResource(Strings.string.techrequests_form_msg_discard)) },
        confirmButton = {
            CmpButton(
                text = stringResource(Strings.string.techrequests_form_btn_discard),
                onClick = onDiscard,
                type = CmpButtonType.Danger,
                size = CmpButtonSize.S,
            )
        },
        dismissButton = {
            CmpButton(
                text = stringResource(Strings.string.techrequests_form_btn_keep_editing),
                onClick = onDismiss,
                type = CmpButtonType.Tertiary,
                size = CmpButtonSize.S,
            )
        },
    )
}

@Composable
private fun NoDeviceDialog(onConnectDevice: () -> Unit, onDismiss: () -> Unit) {
    CmpDialog(
        onDismissRequest = onDismiss,
        icon = CmpIcons.Bluetooth.imageVector(),
        title = { DialogTitle(stringResource(Strings.string.techrequests_list_title_no_device)) },
        text = { DialogText(stringResource(Strings.string.techrequests_list_msg_no_device)) },
        confirmButton = {
            CmpButton(
                text = stringResource(Strings.string.techrequests_list_btn_connect),
                onClick = {
                    onDismiss()
                    onConnectDevice()
                },
                size = CmpButtonSize.S,
                modifier = Modifier.testTag(TechRequestsTags.CONNECT),
            )
        },
        dismissButton = {
            CmpButton(
                text = stringResource(Strings.string.techrequests_common_a11y_close),
                onClick = onDismiss,
                type = CmpButtonType.Tertiary,
                size = CmpButtonSize.S,
            )
        },
    )
}

@Composable
private fun DialogTitle(text: String) {
    CmpText(text = text, style = CmpTextStyle.Headline1)
}

@Composable
private fun DialogText(text: String) {
    CmpText(text = text, style = CmpTextStyle.Paragraph2)
}

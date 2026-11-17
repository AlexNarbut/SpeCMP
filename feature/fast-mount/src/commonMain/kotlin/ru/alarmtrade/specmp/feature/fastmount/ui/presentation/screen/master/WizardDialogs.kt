package ru.alarmtrade.specmp.feature.fastmount.ui.presentation.screen.master

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.fastmount_dialog_btn_cancel
import ru.alarmtrade.specmp.core.localization.resources.fastmount_dialog_btn_continue_mount
import ru.alarmtrade.specmp.core.localization.resources.fastmount_dialog_btn_exit
import ru.alarmtrade.specmp.core.localization.resources.fastmount_dialog_btn_mount_done
import ru.alarmtrade.specmp.core.localization.resources.fastmount_dialog_btn_slave_done
import ru.alarmtrade.specmp.core.localization.resources.fastmount_dialog_btn_stay
import ru.alarmtrade.specmp.core.localization.resources.fastmount_dialog_btn_to_mount
import ru.alarmtrade.specmp.core.localization.resources.fastmount_dialog_msg_exit
import ru.alarmtrade.specmp.core.localization.resources.fastmount_dialog_msg_mount_done
import ru.alarmtrade.specmp.core.localization.resources.fastmount_dialog_msg_slave_done
import ru.alarmtrade.specmp.core.localization.resources.fastmount_dialog_msg_to_mount
import ru.alarmtrade.specmp.core.localization.resources.fastmount_dialog_msg_to_mount_clone
import ru.alarmtrade.specmp.core.localization.resources.fastmount_dialog_title_exit
import ru.alarmtrade.specmp.core.localization.resources.fastmount_dialog_title_mount_done
import ru.alarmtrade.specmp.core.localization.resources.fastmount_dialog_title_slave_done
import ru.alarmtrade.specmp.core.localization.resources.fastmount_dialog_title_to_mount
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSpacing
import ru.alarmtrade.specmp.core.ui.view.button.CmpButton
import ru.alarmtrade.specmp.core.ui.view.button.CmpButtonSize
import ru.alarmtrade.specmp.core.ui.view.button.CmpButtonType
import ru.alarmtrade.specmp.core.ui.view.dialog.CmpDialog
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.icon.imageVector
import ru.alarmtrade.specmp.core.ui.view.layout.CmpColumn
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.core.ui.view.text.CmpTextStyle

internal const val FAST_MOUNT_DIALOG_CONFIRM_TAG = "fastmount_dialog_confirm"
internal const val FAST_MOUNT_DIALOG_DISMISS_TAG = "fastmount_dialog_dismiss"

/** [primaryConfirm] — подтверждение безопасное (заливкой, первым); иначе первым идёт «остаться». */
private class DialogTexts(
    val icon: CmpIcons,
    val title: StringResource,
    val text: StringResource,
    val confirm: StringResource,
    val dismiss: StringResource,
    val primaryConfirm: Boolean,
)

private fun WizardDialog.texts(): DialogTexts = when (this) {
    WizardDialog.EXIT -> DialogTexts(
        CmpIcons.ExitToApp,
        Strings.string.fastmount_dialog_title_exit,
        Strings.string.fastmount_dialog_msg_exit,
        Strings.string.fastmount_dialog_btn_exit,
        Strings.string.fastmount_dialog_btn_continue_mount,
        primaryConfirm = false,
    )

    WizardDialog.TO_MOUNT -> DialogTexts(
        CmpIcons.Replay,
        Strings.string.fastmount_dialog_title_to_mount,
        Strings.string.fastmount_dialog_msg_to_mount,
        Strings.string.fastmount_dialog_btn_to_mount,
        Strings.string.fastmount_dialog_btn_stay,
        primaryConfirm = false,
    )

    WizardDialog.TO_MOUNT_CLONE -> DialogTexts(
        CmpIcons.Replay,
        Strings.string.fastmount_dialog_title_to_mount,
        Strings.string.fastmount_dialog_msg_to_mount_clone,
        Strings.string.fastmount_dialog_btn_to_mount,
        Strings.string.fastmount_dialog_btn_stay,
        primaryConfirm = false,
    )

    WizardDialog.MOUNT_DONE -> DialogTexts(
        CmpIcons.Build,
        Strings.string.fastmount_dialog_title_mount_done,
        Strings.string.fastmount_dialog_msg_mount_done,
        Strings.string.fastmount_dialog_btn_mount_done,
        Strings.string.fastmount_dialog_btn_cancel,
        primaryConfirm = true,
    )

    WizardDialog.SLAVE_DONE -> DialogTexts(
        CmpIcons.SettingsRemote,
        Strings.string.fastmount_dialog_title_slave_done,
        Strings.string.fastmount_dialog_msg_slave_done,
        Strings.string.fastmount_dialog_btn_slave_done,
        Strings.string.fastmount_dialog_btn_cancel,
        primaryConfirm = true,
    )
}

@Composable
internal fun WizardDialogView(dialog: WizardDialog, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    val texts = dialog.texts()
    val confirm: @Composable () -> Unit = {
        CmpButton(
            text = stringResource(texts.confirm),
            onClick = onConfirm,
            type = if (texts.primaryConfirm) CmpButtonType.Primary else CmpButtonType.Secondary,
            size = CmpButtonSize.S,
            fullWidth = true,
            maxLines = 2,
            modifier = Modifier.testTag(FAST_MOUNT_DIALOG_CONFIRM_TAG),
        )
    }
    val dismiss: @Composable () -> Unit = {
        CmpButton(
            text = stringResource(texts.dismiss),
            onClick = onDismiss,
            type = if (texts.primaryConfirm) CmpButtonType.Tertiary else CmpButtonType.Primary,
            size = CmpButtonSize.S,
            fullWidth = true,
            maxLines = 2,
            modifier = Modifier.testTag(FAST_MOUNT_DIALOG_DISMISS_TAG),
        )
    }
    CmpDialog(
        onDismissRequest = onDismiss,
        icon = texts.icon.imageVector(),
        title = {
            CmpText(
                text = stringResource(texts.title),
                style = CmpTextStyle.Headline2,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        },
        text = {
            CmpText(
                text = stringResource(texts.text),
                style = CmpTextStyle.Paragraph2,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        },
        confirmButton = {
            CmpColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(CmpSpacing.space8),
            ) {
                if (texts.primaryConfirm) {
                    confirm()
                    dismiss()
                } else {
                    dismiss()
                    confirm()
                }
            }
        },
    )
}
